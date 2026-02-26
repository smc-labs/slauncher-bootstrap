package ru.smclabs.bootstrap.gui.controller;

import ru.smclabs.bootstrap.gui.panel.update.UpdatePanel;
import ru.smclabs.bootstrap.util.TimeUtils;
import ru.smclabs.slauncher.resources.downloader.stats.listener.ProgressListener;
import ru.smclabs.slauncher.resources.downloader.stats.listener.SpeedListener;
import ru.smclabs.slauncher.resources.downloader.stats.listener.TimeListener;

import javax.swing.*;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;

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

        if (e instanceof FileSystemException fse) {
            return showAccessDeniedDialog(fse);
        }

        return false;
    }

    public boolean showAccessDeniedDialog(FileSystemException e) {
        String message = """
            Не удалось получить доступ к файлу:

            %s

            Путь к файлу:
            %s

            Возможные причины:
            • Запущен другой экземпляр лаунчера.
            • Файл используется другим процессом.
            • Антивирус или система безопасности блокирует доступ.

            Что можно сделать:
            • Закрыть все копии лаунчера.
            • Завершить процессы java через диспетчер задач.
            • Добавить лаунчер в исключения антивируса.
            • Перезагрузить компьютер и попробовать снова.
            """.formatted(
                e.getReason() != null ? e.getReason() : e.getMessage(),
                e.getFile()
        );

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

    private boolean showDestroyProcessesDialog(String message) {
        int result = JOptionPane.showOptionDialog(
                panel,
                message,
                "Ошибка доступа к файлу",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                new Object[]{"Повторить", "Завершить процессы и повторить", "Закрыть лаунчер"},
                "Повторить"
        );

        return result != 0;
    }
}
