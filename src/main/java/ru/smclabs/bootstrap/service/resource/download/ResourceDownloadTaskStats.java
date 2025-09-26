package ru.smclabs.bootstrap.service.resource.download;

import ru.smclabs.bootstrap.service.gui.panel.PanelUpdate;
import ru.smclabs.bootstrap.util.TimeUtils;
import ru.smclabs.slauncher.resources.util.FileUtils;

import java.util.concurrent.TimeUnit;

public class ResourceDownloadTaskStats implements AutoCloseable {

    private final PanelUpdate panelUpdate;
    private final Thread thread;

    private long bytesMustBeRead;
    private long bytesRead;

    public ResourceDownloadTaskStats(PanelUpdate panelUpdate) {
        this.panelUpdate = panelUpdate;
        thread = new Thread(() -> {
            try {
                update();
            } catch (InterruptedException e) {
                close();
            }
        });
        thread.setName("ResourceDownloadTaskStats Thread");
        thread.setDaemon(true);
    }

    public void start(long bytesMustBeRead) {
        this.bytesMustBeRead = bytesMustBeRead;
        thread.start();
    }

    private void update() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        long bytesReadedLast = 0L;
        long bytesDifference;
        long remainingTime = 0;

        while (canTick()) {
            long currentTimeMillis = System.currentTimeMillis();

            if (currentTimeMillis % 100 == 0) {
                long bytesReadCopy = bytesRead;
                double progress = (double) (bytesReadCopy >> 10) / (bytesMustBeRead >> 10);

                if (bytesReadCopy > 0 && bytesMustBeRead > 0) {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    remainingTime = (elapsedTime * bytesMustBeRead / bytesReadCopy) - elapsedTime;
                }

                panelUpdate.setProgress(progress);
            }

            if (currentTimeMillis % 1000 == 0) {
                bytesDifference = bytesRead - bytesReadedLast;
                bytesReadedLast = bytesRead;

                panelUpdate.setLabelSpeed(FileUtils.humanSize(bytesDifference) + "/сек ~");
                panelUpdate.setLabelTimeRemain(TimeUtils.toHumanTime(Math.max(1000, remainingTime)) + " ~");
            }
        }
    }

    private boolean canTick() {
        try {
            TimeUnit.MILLISECONDS.sleep(1);
            return true;
        } catch (InterruptedException e) {
            close();
            return false;
        }
    }

    public void addReadBytes(int bytesRead) {
        this.bytesRead += bytesRead;
    }

    @Override
    public void close() {
        thread.interrupt();
    }
}
