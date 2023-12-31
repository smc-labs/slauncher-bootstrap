package ru.smclabs.bootstrap.service.gui.panel;

import lombok.Getter;
import lombok.Setter;
import ru.smclabs.bootstrap.Bootstrap;
import ru.smclabs.bootstrap.service.gui.ThemeManager;
import ru.smclabs.bootstrap.util.LocalResourceHelper;

import javax.swing.*;
import java.awt.*;

@Setter
public class PanelDownloadInfo extends JPanel {

    private final ThemeManager themeManager;
    private final Font labelFont;

    private @Getter boolean visible;
    private volatile String labelFileName = "Launcher.jar";
    private volatile String labelTimeRemain = "2 минуты";
    private volatile String labelSpeed = "10 МБ/сек";

    public PanelDownloadInfo(ThemeManager themeManager) {
        this.themeManager = themeManager;
        this.labelFont = LocalResourceHelper.loadFont("Inter-Regular", 14);
        this.setLayout(null);
        this.setBackground(null);
        this.setBounds(0, 158, Bootstrap.getInstance().getEnvironment().getGui().getFrameWidth(), 74);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!this.visible) return;
        Graphics2D g2d = (Graphics2D) g;

        this.drawLabel(g2d,
                this.themeManager.getImage("icons", "file", 18, 18),
                this.themeManager.getColor("sub-title"),
                this.labelFileName,
                30);

        this.drawLabel(g2d,
                this.themeManager.getImage("icons", "time", 18, 18),
                this.themeManager.getColor("sub-title"),
                this.labelTimeRemain,
                30 + 28);

        this.drawLabel(g2d,
                this.themeManager.getImage("icons", "speed", 18, 18),
                this.themeManager.getColor("sub-title"),
                this.labelSpeed,
                30 + 28 * 2);
    }

    private void drawLabel(Graphics2D g2d, Image icon, Color color, String text, int posY) {
        g2d.setPaint(color);
        g2d.setFont(this.labelFont);

        int stringWidth = g2d.getFontMetrics().stringWidth(text) - icon.getWidth(this);
        int labelPosX = this.getBounds().x + ((this.getBounds().width / 2) - (stringWidth / 2)) - 18;
        int labelPosY = posY + this.getBounds().y + 6;

        g2d.drawString(text, labelPosX + 24, labelPosY + 18);
        g2d.drawImage(icon, labelPosX, labelPosY + 4, icon.getWidth(this), icon.getHeight(this), this);
    }
}
