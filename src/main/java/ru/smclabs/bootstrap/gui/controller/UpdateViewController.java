package ru.smclabs.bootstrap.gui.controller;

import ru.smclabs.bootstrap.gui.panel.update.UpdatePanel;
import ru.smclabs.bootstrap.util.TimeUtils;
import ru.smclabs.slauncher.resources.downloader.stats.listener.ProgressListener;
import ru.smclabs.slauncher.resources.downloader.stats.listener.SpeedListener;
import ru.smclabs.slauncher.resources.downloader.stats.listener.TimeListener;

import javax.swing.*;
import java.nio.file.AccessDeniedException;

public class UpdateViewController implements SpeedListener, TimeListener, ProgressListener {
    private final UpdatePanel panel;

    public UpdateViewController(UpdatePanel panel) {
        this.panel = panel;
    }

    public void setTitles(String title, String subTitle) {
        setTitle(title);
        setSubTitle(subTitle);
    }

    public void setTitle(String title) {
        panel.setLabelSubTitle(title);
    }

    public void setSubTitle(String subTitle) {
        panel.setLabelSubTitle(subTitle);
    }

    public void showDownloadingStats() {
        toggleDownloadingStats(true);
    }

    public void hideDownloadingStats() {
        toggleDownloadingStats(false);
    }

    public boolean showError(Exception e) {
        if (e instanceof AccessDeniedException ade) {
            return showAccessDeniedDialog(ade);
        }

        return false;
    }

    public boolean showAccessDeniedDialog(AccessDeniedException e) {
        String message = """
                Не удалось получить доступ к файлу:
                
                %s
                
                Возможные причины:
                • Антивирус или система блокирует лаунчер.
                • Запущена другая копия лаунчера.
                • Файл используется другим процессом.
                
                Рекомендуется:
                • Добавить лаунчер в исключения антивируса.
                • Закрыть все запущенные копии лаунчера (или завершить java-процессы через диспетчер задач).
                • Перезагрузить компьютер и попробовать снова.
                
                """.formatted(e.getMessage());

        return showRetryDialog(message);
    }

    public void showRetryCounter(long seconds) {
        setSubTitle("Повтор через " + seconds + " сек...");
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

    public void handleFileName(String fileName) {
        panel.setLabelFileName(fileName);
    }

    private void toggleDownloadingStats(boolean visible) {
        panel.setFileDownloadingVisible(visible);
        panel.setLabelTimeRemain("...");
        panel.setLabelFileName("...");
        panel.setLabelSpeed("...");
        panel.setProgress(-1.0);
    }

    private boolean showRetryDialog(String message) {
        int result = JOptionPane.showOptionDialog(
                panel,
                message,
                "Ошибка доступа к файлу",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                new Object[]{"Повторить", "Закрыть лаунчер"},
                "Повторить"
        );

        return result != 0;
    }
}
