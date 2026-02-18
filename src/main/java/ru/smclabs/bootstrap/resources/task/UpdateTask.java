package ru.smclabs.bootstrap.resources.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.smclabs.bootstrap.gui.panel.UpdatePanel;
import ru.smclabs.bootstrap.launcher.ProcessManager;
import ru.smclabs.bootstrap.launcher.wrapper.ProcessWrapper;
import ru.smclabs.bootstrap.report.ReportProvider;
import ru.smclabs.bootstrap.resources.dto.BootstrapResources;
import ru.smclabs.bootstrap.resources.factory.BootstrapResourcesFactory;
import ru.smclabs.bootstrap.resources.factory.ResourcesPack;
import ru.smclabs.bootstrap.resources.reqeust.FetchResourcesRequest;
import ru.smclabs.bootstrap.util.TimeUtils;
import ru.smclabs.slauncher.http.HttpService;
import ru.smclabs.slauncher.http.exception.HttpServiceException;
import ru.smclabs.slauncher.resources.compressed.resource.ResourceCompressed;
import ru.smclabs.slauncher.resources.downloader.Downloader;
import ru.smclabs.slauncher.resources.downloader.http.HttpDownloader;
import ru.smclabs.slauncher.resources.downloader.stats.StatsCollector;
import ru.smclabs.slauncher.resources.downloader.stats.listener.ProgressListener;
import ru.smclabs.slauncher.resources.downloader.stats.listener.SpeedListener;
import ru.smclabs.slauncher.resources.downloader.stats.listener.TimeListener;
import ru.smclabs.slauncher.resources.provider.DirProvider;
import ru.smclabs.slauncher.resources.type.Resource;
import ru.smclabs.slauncher.resources.type.ResourceStruct;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class UpdateTask implements SpeedListener, TimeListener, ProgressListener {
    private static final Logger log = LoggerFactory.getLogger(UpdateTask.class);

    private static final int MAX_RETRIES = 5;
    private static final long MIN_DELAY_MS = TimeUnit.SECONDS.toMillis(5);
    private static final long MAX_DELAY_MS = TimeUnit.SECONDS.toMillis(30);

    private final HttpService httpService;
    private final DirProvider dirProvider;
    private final ProcessManager processManager;
    private final UpdatePanel panel;
    private final BootstrapResourcesFactory factory;
    private final Thread worker;

    public UpdateTask(
            HttpService httpService,
            DirProvider dirProvider,
            ProcessManager processManager,
            UpdatePanel panel
    ) {
        this.httpService = httpService;
        this.dirProvider = dirProvider;
        this.processManager = processManager;
        this.panel = panel;
        this.factory = new BootstrapResourcesFactory(dirProvider);
        this.worker = createThread();
    }

    @Override
    public void handleProgress(double progress) {
        panel.setProgress(progress);
    }

    @Override
    public void handleSpeed(String speed) {
        panel.setLabelSpeed(speed);
    }

    @Override
    public void handleTime(long time) {
        panel.setLabelTimeRemain(TimeUtils.toHumanTime(time));
    }

    public void start() {
        worker.start();
    }

    public void cancel() {
        worker.interrupt();
    }

    public void join() throws InterruptedException {
        worker.join();
    }

    public boolean isNotCancelled() {
        return !worker.isInterrupted();
    }

    private Thread createThread() {
        Thread thread = new Thread(this::runUpdate);
        thread.setName("update-task-worker");
        return thread;
    }

    private void runUpdate() {
        int attempt = 1;

        while (isNotCancelled()) {
            try {
                log.info("Update attempt {}/{}", attempt, MAX_RETRIES);

                ResourcesPack pack = fetchResources();
                updateResources(findInvalidResources(pack));
                startLauncher(pack);

                worker.interrupt();
                return;
            } catch (InterruptedIOException | InterruptedException e) {
                log.info("Update cancelled: {}", e.getMessage());
                worker.interrupt();
                return;
            } catch (Exception e) {
                ReportProvider.INSTANCE.send("Update attempt " + attempt + " failed", e);

                if (attempt >= MAX_RETRIES) {
                    handleError(e);
                    return;
                }

                long delay = calculateBackoff(attempt);
                log.info("Retrying in {} ms", delay);

                try {
                    waitWithCountdown(delay);
                } catch (InterruptedException ie) {
                    worker.interrupt();
                    return;
                }

                attempt++;
            }
        }
    }

    private void waitWithCountdown(long delayMs) throws InterruptedException {
        panel.setFileDownloadingVisible(false);
        panel.setLabelTitle("Что-то сломалось");

        long seconds = delayMs / 1000;
        long remainder = delayMs % 1000;

        for (long i = seconds; i > 0; i--) {
            panel.setLabelSubTitle("Повтор через " + i + " сек...");
            Thread.sleep(1000);

            if (worker.isInterrupted()) {
                throw new InterruptedException();
            }
        }

        if (remainder > 0) {
            Thread.sleep(remainder);
        }
    }

    private long calculateBackoff(int attempt) {
        long delay = MIN_DELAY_MS * (1L << (attempt - 1));
        return Math.min(delay, MAX_DELAY_MS);
    }

    private void handleError(Exception e) {
        log.error("Update failed", e);
        panel.setLabelTitle("Произошла ошибка");
        panel.setLabelSubTitle("Мы уже в курсе и исправляем!");

        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(10));
        } catch (InterruptedException e1) {
            worker.interrupt();
        }
    }

    private ResourcesPack fetchResources() throws HttpServiceException, JsonProcessingException {
        FetchResourcesRequest request = new FetchResourcesRequest(httpService);
        BootstrapResources dto = request.execute();
        return factory.build(dto);
    }

    private List<Resource> findInvalidResources(ResourcesPack pack) {
        return pack.getResources()
                .stream()
                .filter(Resource::isInvalid)
                .collect(Collectors.toList());
    }

    private void updateResources(List<Resource> resources) throws IOException {
        if (resources.isEmpty()) {
            return;
        }

        panel.setLabelTitle("Обновление");
        panel.setLabelSubTitle("Остановка процессов лаунчера...");

        processManager.destroyAll();

        List<ResourceCompressed> archives = new ArrayList<>();

        try (StatsCollector stats = new StatsCollector(resources.size())) {
            stats.onStart(resources.stream().mapToLong(ResourceStruct::getSize).sum());
            stats.addProgressListener(this);
            stats.addSpeedListener(this);
            stats.addTimeListener(this);

            panel.setLabelTitle("Обновление");
            panel.setLabelSubTitle("Скачивание файлов...");
            panel.setLabelTimeRemain("...");
            panel.setLabelFileName("...");
            panel.setLabelSpeed("...");
            panel.setFileDownloadingVisible(true);

            Downloader downloader = new HttpDownloader(
                    httpService.getEnvironment(),
                    stats
            );

            for (Resource resource : resources) {
                stats.onNextFile();
                panel.setLabelFileName(resource.getName());
                downloader.download(resource);

                if (resource instanceof ResourceCompressed) {
                    archives.add((ResourceCompressed) resource);
                }
            }
        } finally {
            panel.setFileDownloadingVisible(false);
            panel.setProgress(-1.0);
        }

        extractArchives(archives);
    }

    private void extractArchives(List<ResourceCompressed> archives) throws IOException {
        if (!archives.isEmpty()) {
            panel.setLabelSubTitle("Установка обновления...");

            for (ResourceCompressed archive : archives) {
                archive.extract();
            }
        }
    }

    private void startLauncher(ResourcesPack pack) throws IOException, InterruptedException {
        panel.setLabelTitle("Запуск лаунчера");
        panel.setLabelSubTitle("Пожалуйста подождите...");

        ProcessWrapper processWrapper = new ProcessWrapper(
                dirProvider,
                processManager,
                pack.getRuntime().getExecutableBinary(),
                pack.getLauncher().getPath()
        );
        processWrapper.start();
    }
}
