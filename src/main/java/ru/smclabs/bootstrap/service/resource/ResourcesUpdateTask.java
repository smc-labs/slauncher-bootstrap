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

    private final ResourcesService service;
    private final BootstrapResourcesFactory factory;
    private final @Getter PanelUpdate panelUpdate;
    private final Thread thread;

    public ResourcesUpdateTask(ResourcesService service, PanelUpdate panelUpdate) {
        this.service = service;
        this.panelUpdate = panelUpdate;
        this.factory = new BootstrapResourcesFactory();
        this.thread = this.createThread();
    }

    private Thread createThread() {
        Thread thread = new Thread(() -> {
            try {
                this.update();
            } catch (InterruptedException e) {
                Bootstrap.getInstance().getLogger().info("Update cancelled.");
            } finally {
                this.service.cancelTask();
            }
        });
        thread.setName("ResourceUpdateTask Thread");
        return thread;
    }

    private void retryUpdate() throws InterruptedException {
        TimeUnit.SECONDS.sleep(5);

        for (int i = 0; i < 5; i++) {
            this.panelUpdate.setLabelSubTitle("следующая попытка через " + (5 - i) + " сек");
            TimeUnit.SECONDS.sleep(1);
        }

        this.update();
    }

    private void update() throws InterruptedException {
        try {
            this.run();
        } catch (LauncherProcessFailedException e) {
            BootstrapReportProvider reportProvider = Bootstrap.getReportProvider();
            String reportPayload = reportProvider.createReport(e)
                    + "\n\nLauncher process output:\n"
                    + e.getProcessOutput();

            reportProvider.send("Launcher process startup", reportPayload);

            this.panelUpdate.setLabelTitle("Что-то пошло не так");
            this.panelUpdate.setLabelSubTitle("не удалось запустить лаунчер");
            this.retryUpdate();
        } catch (HttpServiceException | JsonProcessingException | ResourceWriteException |
                 ResourceServerException | LauncherServiceException | ResourceException e) {

            Bootstrap.getReportProvider().send("Bootstrap update process", e);
            this.panelUpdate.setLabelTitle("Что-то пошло не так");
            this.panelUpdate.setLabelSubTitle("не удалось обновить лаунчер");
            this.retryUpdate();
        }
    }

    private void run() throws HttpServiceException, JsonProcessingException,
            ResourceWriteException, ResourceServerException, InterruptedException, LauncherServiceException {

        ResourcesBuild resourcesBuild = this.factory.buildModels(this.fetchResourceList());

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

        this.downloadInvalidFiles(resourcesBuild, runtime, launcher);
        this.removeOldLauncherVersions(runtime, launcher);

        this.panelUpdate.setLabelTitle("Все готово");
        this.panelUpdate.setLabelSubTitle("запуск лаунчера...");
        this.checkIfCancelled();

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
            List<ResourceCompressed> compressedResources = new ArrayList<>();
            ProcessManager processManager = Bootstrap.getInstance().getLauncherService().getProcessManager();
            processManager.readProcessesFromDisk();

            processManager.destroyLauncherProcesses(runtime, launcher);
            TimeUnit.SECONDS.sleep(1);

            try (ResourceDownloadTaskStats stats = new ResourceDownloadTaskStats(this.panelUpdate)) {
                this.panelUpdate.setLabelTitle("Обновление");
                this.panelUpdate.setLabelSubTitle("скачивание обновления...");
                this.panelUpdate.setLabelTimeRemain("...");
                this.panelUpdate.setLabelFileName("...");
                this.panelUpdate.setLabelSpeed("...");
                this.panelUpdate.setProgress(0.01D);
                this.panelUpdate.getPanelDownloadInfo().setVisible(true);

                stats.start(downloads.stream().mapToLong(task -> task.getResource().getSize()).sum());

                for (ResourceDownloadTask download : downloads) {
                    this.panelUpdate.setLabelFileName(download.getResource().getName());
                    download.setStats(stats);
                    download.run();

                    if (download.getResource() instanceof ResourceCompressed) {
                        compressedResources.add((ResourceCompressed) download.getResource());
                    }
                }
            }

            this.panelUpdate.setLabelSubTitle("установка обновления...");
            this.panelUpdate.setProgress(0D);
            this.panelUpdate.getPanelDownloadInfo().setVisible(false);

            for (ResourceCompressed resource : compressedResources) {
                this.checkIfCancelled();
                resource.extract();
            }
        }
    }

    public void start() {
        this.thread.start();
    }

    public void interrupt() {
        this.thread.interrupt();
    }

    public boolean isCancelled() {
        return this.thread.isInterrupted();
    }

    public void checkIfCancelled() throws InterruptedException {
        if (this.isCancelled()) throw new InterruptedException("Update cancelled by user");
    }
}
