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

        this.panelDownloadInfo = new PanelDownloadInfo(guiService.getThemeManager());

        this.setLayout(null);
        this.setBackground(null);
        this.setBounds(0, 72, Bootstrap.getInstance().getEnvironment().getGui().getFrameWidth(),
                Bootstrap.getInstance().getEnvironment().getGui().getFrameHeight() - 54);

        this.progressBarPosY = (int) (this.getBounds().getHeight() - this.progressPrefHeight - 92);
        this.progressPrefWidth = 442;
        this.startRepaintTimer();
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
                this.getBounds().width - 36 * 2,
                this.getBounds().height - 36 * 2,
                0,
                0);

        g2d.setColor(this.guiService.getThemeManager().getColor("bg"));
        g2d.fill(new Area(roundRectangle));

        int titlePosY = 36;

        this.drawProgressBar(g2d);
        this.drawCenteredString(g2d, this.labelTitle, this.labelTitleFont, this.guiService.getThemeManager().getColor("title"), 0, titlePosY);
        this.drawCenteredString(g2d, this.labelSubTitle, this.labelSubTitleFont, this.guiService.getThemeManager().getColor("sub-title"), 0, titlePosY + 33);
        this.panelDownloadInfo.paintComponent(g);

        g2d.dispose();
    }

    private void drawProgressBar(Graphics2D g2d) {
        int posX = 115;

        RoundRectangle2D.Double roundRectangle = new RoundRectangle2D.Double(posX,
                this.progressBarPosY,
                this.progressPrefWidth,
                this.progressPrefHeight,
                23,
                23);

        g2d.setColor(this.guiService.getThemeManager().getColor("progress-bar"));
        g2d.fill(new Area(roundRectangle));

        int width = this.progress <= 0D
                ? this.progressBouncerPrefWidth
                : (int) (this.progressPrefWidth * this.progress);

        RoundRectangle2D.Double roundRectangle3 = new RoundRectangle2D.Double(posX + this.platformPosX,
                this.progressBarPosY,
                width,
                this.progressPrefHeight,
                23,
                23);

        g2d.setPaint(this.guiService.getThemeManager().getColor("progress-bar-track"));
        g2d.fill(new Area(roundRectangle3));
    }

    public void drawCenteredString(Graphics2D g2d, String text, Font font, Paint paint, int x, int y) {
        g2d.setPaint(paint);
        g2d.setFont(font);
        int stringWidth = g2d.getFontMetrics().stringWidth(text);
        g2d.drawString(text, this.getBounds().x + ((this.getBounds().width / 2) - (stringWidth / 2)), y + this.getBounds().y + 5);
    }

    public void setProgress(double progress) {
        this.progress = progress <= 0D ? progress : Math.max(progress, 0.05);
        this.platformPosX = 0;
    }

    public void setLabelTimeRemain(String time) {
        this.panelDownloadInfo.setLabelTimeRemain(time);
    }

    public void setLabelSpeed(String speed) {
        this.panelDownloadInfo.setLabelSpeed(speed);
    }

    public void setLabelFileName(String fileName) {
        this.panelDownloadInfo.setLabelFileName(fileName);
    }
}
