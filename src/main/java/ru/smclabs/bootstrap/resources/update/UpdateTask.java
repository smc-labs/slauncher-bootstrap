package ru.smclabs.bootstrap.resources.update;

import com.fasterxml.jackson.core.JsonProcessingException;
import ru.smclabs.bootstrap.gui.panel.PanelUpdate;
import ru.smclabs.bootstrap.resources.dto.BootstrapResources;
import ru.smclabs.bootstrap.resources.factory.BootstrapResourcesFactory;
import ru.smclabs.bootstrap.resources.reqeust.FetchResourcesRequest;
import ru.smclabs.bootstrap.util.TimeUtils;
import ru.smclabs.slauncher.http.HttpService;
import ru.smclabs.slauncher.http.exception.HttpServiceException;
import ru.smclabs.slauncher.logger.Logger;
import ru.smclabs.slauncher.resources.downloader.Downloader;
import ru.smclabs.slauncher.resources.downloader.http.HttpDownloader;
import ru.smclabs.slauncher.resources.downloader.stats.StatsCollector;
import ru.smclabs.slauncher.resources.downloader.stats.listener.ProgressListener;
import ru.smclabs.slauncher.resources.downloader.stats.listener.SpeedListener;
import ru.smclabs.slauncher.resources.downloader.stats.listener.TimeListener;
import ru.smclabs.slauncher.resources.provider.DirProvider;
import ru.smclabs.slauncher.resources.type.Resource;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;

public class UpdateTask implements SpeedListener, TimeListener, ProgressListener {
    private final BootstrapResourcesFactory factory;
    private final HttpService httpService;
    private final PanelUpdate panel;
    private final Logger logger;
    private final Thread worker;

    public UpdateTask(
            Logger logger,
            HttpService httpService,
            DirProvider dirProvider,
            PanelUpdate panel
    ) {
        factory = new BootstrapResourcesFactory(dirProvider);
        this.httpService = httpService;
        this.panel = panel;
        this.logger = logger;
        worker = createThread();
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

    public boolean isCancelled() {
        return worker.isInterrupted();
    }

    private Thread createThread() {
        Thread thread = new Thread(this::runUpdate);
        thread.setName("UpdateTask Thread");
        return thread;
    }

    private void runUpdate() {
        try {
            List<Resource> invalidResources = findInvalidResources();
            updateResources(invalidResources);
        } catch (InterruptedIOException e) {
            logger.info("Update cancelled: " + e.getMessage());
            worker.interrupt();
        } catch (HttpServiceException | IOException e) {
            handleError(e);
        }
    }

    private void handleError(Exception e) {
        logger.error("Update failed", e);
    }

    private List<Resource> findInvalidResources() throws HttpServiceException, JsonProcessingException {
        FetchResourcesRequest request = new FetchResourcesRequest(httpService);
        BootstrapResources dto = request.execute();
        List<Resource> resources = factory.build(dto);
        List<Resource> invalidResources = new ArrayList<>();

        for (Resource resource : resources) {
            if (resource.isInvalid()) {
                invalidResources.add(resource);
            }
        }

        return invalidResources;
    }

    private void updateResources(List<Resource> resources) throws IOException {
        try (StatsCollector stats = new StatsCollector(resources.size())) {
            stats.addProgressListener(this);
            stats.addSpeedListener(this);
            stats.addTimeListener(this);

            panel.setFileDownloadingVisible(true);
            panel.setLabelSubTitle("Скачивание обновления...");
            panel.setLabelTimeRemain("...");
            panel.setLabelFileName("...");
            panel.setLabelSpeed("...");

            Downloader downloader = new HttpDownloader(
                    httpService.getEnvironment(),
                    logger,
                    stats
            );

            for (Resource resource : resources) {
                stats.onNextFile();
                panel.setLabelFileName(resource.getName());
                downloader.download(resource);
            }
        } finally {
            panel.setFileDownloadingVisible(false);
        }
    }
}
