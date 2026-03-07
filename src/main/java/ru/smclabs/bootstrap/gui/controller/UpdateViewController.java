package ru.smclabs.bootstrap.gui.controller;

import ru.smclabs.bootstrap.gui.panel.UpdatePanel;
import ru.smclabs.bootstrap.util.TimeUtils;
import ru.smclabs.slauncher.resources.downloader.stats.listener.ProgressListener;
import ru.smclabs.slauncher.resources.downloader.stats.listener.SpeedListener;
import ru.smclabs.slauncher.resources.downloader.stats.listener.TimeListener;

import javax.swing.*;
import java.nio.file.FileSystemException;

public class UpdateViewController implements SpeedListener, TimeListener, ProgressListener {
    private static final String DEFAULT_ERROR_TITLE = "Ошибка обновления";
    private static final String DEFAULT_ERROR_DIALOG = """
            Упс... Что-то пошло не так при обновлении:
            
            %s
            
            Попробуйте нажать «Повторить»
            
            Если ошибка повторяется - попробуйте следующие шаги:
            • Добавить лаунчер в исключения антивируса
            • Проверьте интернет соединение
            • Перезагрузить компьютер
            
            Если ничего не помогло — пишите в поддержку!
            
            """;

    private static final String ACCESS_DENIED_ERROR_DIALOG = """
            Не получилось обновить файл:
            
            %s
            
            Файл:
            %s
            
            Попробуйте:
            • Закрыть все окна лаунчера
            • Проверить диспетчер задач — нет ли там java?
            • Добавить лаунчер в исключения антивируса
            • Перезагрузить компьютер
            
            Если ничего не помогло — пишите в поддержку!
            
            """;

    private final UpdatePanel panel;

    public UpdateViewController(UpdatePanel panel) {
        this.panel = panel;
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
        if (e instanceof FileSystemException fse) {
            return showFileSystemError(fse);
        }

        return showRetryDialog(
                DEFAULT_ERROR_TITLE,
                DEFAULT_ERROR_DIALOG.formatted(e.getMessage() == null ? e.getCause().getMessage() : e.getMessage())
        );
    }

    public boolean showFileSystemError(FileSystemException e) {
        String reason = e.getReason() != null ? e.getReason() : e.getMessage();
        String message = ACCESS_DENIED_ERROR_DIALOG.formatted(reason, e.getFile());
        return showRetryDialog("Ошибка доступа к файлу", message);
    }

    private void toggleDownloadingStats(boolean visible) {
        panel.setFileDownloadingVisible(visible);
        panel.setLabelTimeRemain("...");
        panel.setLabelFileName("...");
        panel.setLabelSpeed("...");
        panel.setProgress(-1.0);
    }

    private boolean showRetryDialog(String title, String message) {
        int result = JOptionPane.showOptionDialog(
                panel,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                new Object[]{"Повторить", "Закрыть лаунчер"},
                "Повторить"
        );

        return result != 0;
    }
}
