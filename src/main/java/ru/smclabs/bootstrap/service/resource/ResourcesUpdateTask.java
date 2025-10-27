package ru.smclabs.bootstrap.service.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import ru.smclabs.bootstrap.Bootstrap;
import ru.smclabs.bootstrap.service.ResourcesService;
import ru.smclabs.bootstrap.service.gui.panel.PanelUpdate;
import ru.smclabs.bootstrap.service.launcher.ProcessManager;
import ru.smclabs.bootstrap.service.launcher.exception.LauncherProcessFailedException;
import ru.smclabs.bootstrap.service.launcher.exception.LauncherServiceException;
import ru.smclabs.bootstrap.service.launcher.process.LauncherProcess;
import ru.smclabs.bootstrap.service.launcher.process.LauncherProcessTracker;
import ru.smclabs.bootstrap.service.resource.download.ResourceDownloadTask;
import ru.smclabs.bootstrap.service.resource.download.ResourceDownloadTaskStats;
import ru.smclabs.bootstrap.service.resource.dto.BootstrapResourceList;
import ru.smclabs.bootstrap.service.resource.exception.ResourceServerException;
import ru.smclabs.bootstrap.service.resource.exception.ResourceWriteException;
import ru.smclabs.bootstrap.service.resource.type.ResourceLauncher;
import ru.smclabs.bootstrap.util.report.BootstrapReportProvider;
import ru.smclabs.slauncher.http.HttpService;
import ru.smclabs.slauncher.http.exception.HttpServiceException;
import ru.smclabs.slauncher.http.request.HttpRequest;
import ru.smclabs.slauncher.resources.exception.ResourceException;
import ru.smclabs.slauncher.resources.type.ResourceCompressed;
import ru.smclabs.slauncher.resources.type.ResourceCompressedRuntime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ResourcesUpdateTask {

    private final BootstrapResourcesFactory factory = new BootstrapResourcesFactory();
    private final @Getter PanelUpdate panelUpdate;
    private final ResourcesService service;
    private final Thread thread;

    public ResourcesUpdateTask(ResourcesService service, PanelUpdate panelUpdate) {
        this.service = service;
        this.panelUpdate = panelUpdate;
        thread = createThread();
    }

    private Thread createThread() {
        Thread thread = new Thread(() -> {
            try {
                update();
            } catch (InterruptedException e) {
                Bootstrap.getInstance().getLogger().info("Update cancelled.");
            } finally {
                service.cancelTask();
            }
        });
        thread.setName("ResourceUpdateTask Thread");
        return thread;
    }

    private void retryUpdate() throws InterruptedException {
        TimeUnit.SECONDS.sleep(5);

        for (int i = 0; i < 5; i++) {
            panelUpdate.setLabelSubTitle("следующая попытка через " + (5 - i) + " сек");
            TimeUnit.SECONDS.sleep(1);
        }

        update();
    }

    private void update() throws InterruptedException {
        try {
            run();
        } catch (LauncherProcessFailedException e) {
            BootstrapReportProvider reportProvider = Bootstrap.getReportProvider();
            String reportPayload = reportProvider.createReport(e)
                    + "\n\nLauncher process output:\n"
                    + e.getProcessOutput();

            reportProvider.send("Launcher process startup", reportPayload);

            panelUpdate.setLabelTitle("Что-то пошло не так");
            panelUpdate.setLabelSubTitle("не удалось запустить лаунчер");
            retryUpdate();
        } catch (HttpServiceException | JsonProcessingException | ResourceWriteException |
                 ResourceServerException | LauncherServiceException | ResourceException e) {

            Bootstrap.getReportProvider().send("Bootstrap update process", e);
            panelUpdate.setLabelTitle("Что-то пошло не так");
            panelUpdate.setLabelSubTitle("не удалось обновить лаунчер");
            retryUpdate();
        }
    }

    private void run() throws HttpServiceException, JsonProcessingException,
            ResourceWriteException, ResourceServerException, InterruptedException, LauncherServiceException {

        ResourcesBuild resourcesBuild = factory.buildModels(fetchResourceList());

        ResourceCompressedRuntime runtime = resourcesBuild.getResources().stream()
                .filter(resource -> resource instanceof ResourceCompressedRuntime)
                .findFirst()
                .map(ResourceCompressedRuntime.class::cast)
                .orElseThrow(() -> new ResourceException("Runtime not found!"));

        ResourceLauncher launcher = resourcesBuild.getResources().stream()
                .filter(resource -> resource instanceof ResourceLauncher)
                .findFirst()
                .map(ResourceLauncher.class::cast)
                .orElseThrow(() -> new ResourceException("Launcher not found!"));

        downloadInvalidFiles(resourcesBuild, runtime, launcher);
        removeOldLauncherVersions(runtime, launcher);

        panelUpdate.setLabelTitle("Все готово");
        panelUpdate.setLabelSubTitle("запуск лаунчера...");
        checkIfCancelled();

        LauncherProcess launcherProcess = Bootstrap.getInstance().getLauncherService().getProcessManager()
                .create(launcher.getPath(), runtime.getExecutableBinary());

        LauncherProcessTracker launcherProcessTracker = new LauncherProcessTracker(this, launcherProcess);
        launcherProcessTracker.track();

        System.exit(0);
    }

    private BootstrapResourceList fetchResourceList() throws HttpServiceException, JsonProcessingException {
        HttpRequest<HttpService, BootstrapResourceList> request = new HttpRequest<>(Bootstrap.getInstance().getHttpService(),
                "GET",
                "application/json",
                "%slauncher-backend%/bootstrap");

        return request.execute(BootstrapResourceList.class);
    }

    private void removeOldLauncherVersions(ResourceCompressedRuntime runtime, ResourceLauncher launcher) throws InterruptedException {
        ProcessManager processManager = Bootstrap.getInstance().getLauncherService().getProcessManager();
        processManager.readProcessesFromDisk();
        processManager.destroyLauncherProcesses(runtime, launcher);
        TimeUnit.SECONDS.sleep(1);
        launcher.removeOlderVersions();
    }

    private void downloadInvalidFiles(ResourcesBuild resourcesBuild,
                                      ResourceCompressedRuntime runtime,
                                      ResourceLauncher launcher) throws InterruptedException, ResourceWriteException, ResourceServerException, HttpServiceException {

        List<ResourceDownloadTask> downloads = resourcesBuild.findInvalidResources();

        if (!downloads.isEmpty()) {
            ProcessManager processManager = Bootstrap.getInstance().getLauncherService().getProcessManager();
            processManager.readProcessesFromDisk();
            processManager.destroyLauncherProcesses(runtime, launcher);

            TimeUnit.SECONDS.sleep(1);

            List<ResourceCompressed> compressedResources = new ArrayList<>();

            try (ResourceDownloadTaskStats stats = new ResourceDownloadTaskStats(panelUpdate)) {
                panelUpdate.setLabelTitle("Обновление");
                panelUpdate.setLabelSubTitle("скачивание обновления...");

                panelUpdate.setLabelTimeRemain("...");
                panelUpdate.setLabelFileName("...");
                panelUpdate.setLabelSpeed("...");
                panelUpdate.setProgress(0.0);
                panelUpdate.setFileDownloadingVisible(true);

                stats.start(downloads.stream().mapToLong(task -> task.getResource().getSize()).sum());

                for (ResourceDownloadTask download : downloads) {
                    panelUpdate.setLabelFileName(download.getResource().getName());
                    download.setStats(stats);
                    download.run();

                    if (download.getResource() instanceof ResourceCompressed) {
                        compressedResources.add((ResourceCompressed) download.getResource());
                    }
                }
            }

            panelUpdate.setLabelSubTitle("установка обновления...");
            panelUpdate.setProgress(-1.0);
            panelUpdate.setFileDownloadingVisible(false);

            for (ResourceCompressed resource : compressedResources) {
                checkIfCancelled();
                resource.extract();
            }
        }
    }

    public void start() {
        thread.start();
    }

    public void interrupt() {
        thread.interrupt();
    }

    public boolean isCancelled() {
        return thread.isInterrupted();
    }

    public void checkIfCancelled() throws InterruptedException {
        if (isCancelled()) throw new InterruptedException("Update cancelled by user");
    }
}
