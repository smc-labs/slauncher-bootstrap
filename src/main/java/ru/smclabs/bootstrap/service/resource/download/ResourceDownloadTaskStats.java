package ru.smclabs.bootstrap.service.resource.download;

import ru.smclabs.bootstrap.service.gui.panel.PanelUpdate;
import ru.smclabs.bootstrap.util.FileUtils;
import ru.smclabs.bootstrap.util.TimeUtils;

import java.util.concurrent.TimeUnit;

public class ResourceDownloadTaskStats implements AutoCloseable {

    private final PanelUpdate panelUpdate;
    private final Thread thread;

    private long bytesMustBeRead;
    private long bytesRead;

    public ResourceDownloadTaskStats(PanelUpdate panelUpdate) {
        this.panelUpdate = panelUpdate;
        this.thread = new Thread(() -> {
            try {
                this.update();
            } catch (InterruptedException e) {
                this.close();
            }
        });
        this.thread.setName("ResourceDownloadTaskStats Thread");
        this.thread.setDaemon(true);
    }

    public void start(long bytesMustBeRead) {
        this.bytesMustBeRead = bytesMustBeRead;
        this.thread.start();
    }

    private void update() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        long bytesReadedLast = 0L;
        long bytesDifference;
        long remainingTime = 0;

        while (canTick()) {
            long currentTimeMillis = System.currentTimeMillis();

            if (currentTimeMillis % 100 == 0) {
                long bytesReadCopy = this.bytesRead;
                double progress = (double) (bytesReadCopy >> 10) / (this.bytesMustBeRead >> 10);

                if (bytesReadCopy > 0 && this.bytesMustBeRead > 0) {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    remainingTime = (elapsedTime * this.bytesMustBeRead / bytesReadCopy) - elapsedTime;
                }

                this.panelUpdate.setProgress(progress);
            }

            if (currentTimeMillis % 1000 == 0) {
                bytesDifference = this.bytesRead - bytesReadedLast;
                bytesReadedLast = this.bytesRead;

                this.panelUpdate.setLabelSpeed(FileUtils.sizeForHuman(bytesDifference) + "/сек ~");
                this.panelUpdate.setLabelTimeRemain(TimeUtils.toHumanTime(Math.max(1000, remainingTime)) + " ~");
            }
        }
    }

    private boolean canTick() {
        try {
            TimeUnit.MILLISECONDS.sleep(1);
            return true;
        } catch (InterruptedException e) {
            this.close();
            return false;
        }
    }

    public void addReadBytes(int bytesRead) {
        this.bytesRead += bytesRead;
    }

    @Override
    public void close() {
        this.thread.interrupt();
    }
}
