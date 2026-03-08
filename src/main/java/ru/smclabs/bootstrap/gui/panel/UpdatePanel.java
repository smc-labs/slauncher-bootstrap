package ru.smclabs.bootstrap.gui.panel;

import ru.smclabs.bootstrap.gui.component.FileDownloadingPane;
import ru.smclabs.bootstrap.gui.component.ProgressBar;
import ru.smclabs.bootstrap.gui.manager.GuiManager;
import ru.smclabs.bootstrap.gui.manager.ThemeManager;
import ru.smclabs.bootstrap.gui.panel.background.BackgroundPanel;
import ru.smclabs.bootstrap.util.resources.LocalResourceHelper;

import javax.swing.*;
import java.awt.*;

public class UpdatePanel extends JPanel {
    public static final int VERTICAL_GAP = 24;

    private final ThemeManager themeManager;
    private final Font titleFont;
    private final Font subTitleFont;
    private final ProgressBar progressBar;
    private final FileDownloadingPane fileDownloading;

    private String labelTitle = "Поиск обновлений";
    private String labelSubTitle = "Это займет пару мгновений...";

    public UpdatePanel(ThemeManager themeManager) {
        this.themeManager = themeManager;

        int posY = BackgroundPanel.PADDING_Y + 38 + VERTICAL_GAP;
        setBounds(BackgroundPanel.PADDING_X, posY, calcWidth(), calcHeight(posY));
        setBackground(null);
        setLayout(null);

        titleFont = LocalResourceHelper.loadFont("GolosText-Bold", 34);
        subTitleFont = LocalResourceHelper.loadFont("GolosText-Regular", 16);

        add(progressBar = new ProgressBar(themeManager, this));
        add(fileDownloading = new FileDownloadingPane(themeManager, this));
    }

    public void drawCenteredString(Graphics2D g2d, String text, Font font, Paint paint, int y) {
        g2d.setPaint(paint);
        g2d.setFont(font);
        g2d.drawString(text, getWidth() / 2 - g2d.getFontMetrics().stringWidth(text) / 2, y);
    }

    public void setLabelTitle(String text) {
        labelTitle = text;
        repaint();
    }

    public void setLabelSubTitle(String text) {
        labelSubTitle = text;
        repaint();
    }

    public void setProgress(double progress) {
        progressBar.setProgress(progress);
    }

    public void setLabelTimeRemain(String time) {
        fileDownloading.setTimeRemain(time);
    }

    public void setLabelSpeed(String speed) {
        fileDownloading.setSpeed(speed);
    }

    public void setLabelFileName(String fileName) {
        fileDownloading.setFileName(fileName);
    }

    public void setFileDownloadingVisible(boolean visible) {
        fileDownloading.setVisible(visible);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            drawBackground(g2d);

            boolean downloadingVisible = fileDownloading.isVisible();

            drawCenteredString(
                    g2d,
                    labelTitle,
                    titleFont,
                    themeManager.getColor("title"),
                    downloadingVisible ? 35 : 70
            );

            drawCenteredString(
                    g2d,
                    labelSubTitle,
                    subTitleFont,
                    themeManager.getColor("sub-title"),
                    downloadingVisible ? 60 : 95
            );
        } finally {
            g2d.dispose();
        }
    }

    private int calcWidth() {
        return GuiManager.FRAME_WIDTH - BackgroundPanel.PADDING_X * 2;
    }

    private int calcHeight(int posY) {
        return GuiManager.FRAME_HEIGHT - posY - BackgroundPanel.PADDING_Y;
    }

    private void drawBackground(Graphics2D g2d) {
        g2d.setColor(themeManager.getColor("bg"));
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}
