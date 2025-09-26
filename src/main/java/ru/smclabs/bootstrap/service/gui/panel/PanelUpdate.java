package ru.smclabs.bootstrap.service.gui.panel;

import lombok.Getter;
import lombok.Setter;
import ru.smclabs.bootstrap.Bootstrap;
import ru.smclabs.bootstrap.service.GuiService;
import ru.smclabs.bootstrap.util.LocalResourceHelper;
import ru.smclabs.system.info.SystemInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

public class PanelUpdate extends AbstractPanel {

    private final @Getter PanelDownloadInfo panelDownloadInfo;
    private final Font labelTitleFont = LocalResourceHelper.loadFont("Inter-Bold", 54);
    private final Font labelSubTitleFont = LocalResourceHelper.loadFont("Inter-Regular", 18);
    private final int progressPrefWidth;
    private final int progressPrefHeight = 23;
    private final int progressBouncerPrefWidth = 70;
    private final int progressBarPosY;

    @Setter
    private volatile String labelTitle = "Поиск обновлений";
    @Setter
    private volatile String labelSubTitle = "это займет пару мгновений...";
    private volatile double progress = 0.0D;
    private volatile boolean bouncingReverse = false;
    private int platformPosX = 0;

    public PanelUpdate(GuiService guiService) {
        super(guiService);

        panelDownloadInfo = new PanelDownloadInfo(guiService.getThemeManager());

        setLayout(null);
        setBackground(null);
        setBounds(0, 72, Bootstrap.getInstance().getEnvironment().getGui().getFrameWidth(),
                Bootstrap.getInstance().getEnvironment().getGui().getFrameHeight() - 54);

        progressBarPosY = (int) (getBounds().getHeight() - progressPrefHeight - 92);
        progressPrefWidth = 442;
        startRepaintTimer();
    }

    private void startRepaintTimer() {
        new Timer(10, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (progress <= 0D) {
                    if (!bouncingReverse) {
                        platformPosX = Math.min(platformPosX + 5, progressPrefWidth - progressBouncerPrefWidth);
                        if (platformPosX == progressPrefWidth - progressBouncerPrefWidth) bouncingReverse = true;
                    } else {
                        platformPosX = Math.max(platformPosX - 5, 0);
                        if (platformPosX == 0) bouncingReverse = false;
                    }
                }

                if (SystemInfo.get().isMacOS()) {
                    guiService.getPanelBackground().repaint();
                } else {
                    repaint();
                }
            }
        }).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        RoundRectangle2D.Double roundRectangle = new RoundRectangle2D.Double(36, 36,
                getBounds().width - 36 * 2,
                getBounds().height - 36 * 2,
                0,
                0);

        g2d.setColor(guiService.getThemeManager().getColor("bg"));
        g2d.fill(new Area(roundRectangle));

        int titlePosY = 36;

        drawProgressBar(g2d);
        drawCenteredString(g2d, labelTitle, labelTitleFont, guiService.getThemeManager().getColor("title"), 0, titlePosY);
        drawCenteredString(g2d, labelSubTitle, labelSubTitleFont, guiService.getThemeManager().getColor("sub-title"), 0, titlePosY + 33);
        panelDownloadInfo.paintComponent(g);

        g2d.dispose();
    }

    private void drawProgressBar(Graphics2D g2d) {
        int posX = 115;

        RoundRectangle2D.Double roundRectangle = new RoundRectangle2D.Double(posX,
                progressBarPosY,
                progressPrefWidth,
                progressPrefHeight,
                23,
                23);

        g2d.setColor(guiService.getThemeManager().getColor("progress-bar"));
        g2d.fill(new Area(roundRectangle));

        int width = progress <= 0D
                ? progressBouncerPrefWidth
                : (int) (progressPrefWidth * progress);

        RoundRectangle2D.Double roundRectangle3 = new RoundRectangle2D.Double(posX + platformPosX,
                progressBarPosY,
                width,
                progressPrefHeight,
                23,
                23);

        g2d.setPaint(guiService.getThemeManager().getColor("progress-bar-track"));
        g2d.fill(new Area(roundRectangle3));
    }

    public void drawCenteredString(Graphics2D g2d, String text, Font font, Paint paint, int x, int y) {
        g2d.setPaint(paint);
        g2d.setFont(font);
        int stringWidth = g2d.getFontMetrics().stringWidth(text);
        g2d.drawString(text, getBounds().x + ((getBounds().width / 2) - (stringWidth / 2)), y + getBounds().y + 5);
    }

    public void setProgress(double progress) {
        this.progress = progress <= 0D ? progress : Math.max(progress, 0.05);
        platformPosX = 0;
    }

    public void setLabelTimeRemain(String time) {
        panelDownloadInfo.setLabelTimeRemain(time);
    }

    public void setLabelSpeed(String speed) {
        panelDownloadInfo.setLabelSpeed(speed);
    }

    public void setLabelFileName(String fileName) {
        panelDownloadInfo.setLabelFileName(fileName);
    }
}
