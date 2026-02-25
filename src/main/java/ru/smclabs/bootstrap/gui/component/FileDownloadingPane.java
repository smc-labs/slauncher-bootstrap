package ru.smclabs.bootstrap.gui.component;

import ru.smclabs.bootstrap.gui.panel.update.UpdatePanel;
import ru.smclabs.bootstrap.gui.manager.ThemeManager;
import ru.smclabs.bootstrap.util.resources.ResourcesHelper;

import javax.swing.*;
import java.awt.*;

public class FileDownloadingPane extends JComponent {
    private final ThemeManager themeManager;
    private final Font labelFont;
    private final Image[] images;

    private String fileName = "...";
    private String timeRemain = "...";
    private String speed = "...";

    public FileDownloadingPane(ThemeManager themeManager, UpdatePanel parent) {
        this.themeManager = themeManager;
        labelFont = ResourcesHelper.loadFont("GolosText-Regular", 14);
        images = bakeImages();
        setBounds(0, parent.getHeight() - 74 - 20 - UpdatePanel.VERTICAL_GAP, parent.getWidth(), 74);
        setVisible(false);
    }

    public void setFileName(String value) {
        fileName = value;
        repaint();
    }

    public void setTimeRemain(String value) {
        timeRemain = value;
        repaint();
    }

    public void setSpeed(String value) {
        speed = value;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        drawLabel(g2d, images[0], themeManager.getColor("sub-title"), fileName, 0);
        drawLabel(g2d, images[1], themeManager.getColor("sub-title"), timeRemain, 28);
        drawLabel(g2d, images[2], themeManager.getColor("sub-title"), speed, 28 * 2);
    }

    private void drawLabel(Graphics2D g2d, Image icon, Color color, String text, int posY) {
        g2d.setPaint(color);
        g2d.setFont(labelFont);

        int imageWidth = icon.getWidth(this);
        int imageHeight = icon.getHeight(this);
        int stringWidth = g2d.getFontMetrics().stringWidth(text) - imageWidth;

        int labelPosX = ((getWidth() / 2) - (stringWidth / 2)) - imageWidth;

        g2d.drawString(text, labelPosX + imageWidth + 6, posY + 14);
        g2d.drawImage(icon, labelPosX, posY, imageWidth, imageHeight, this);
    }

    private Image[] bakeImages() {
        return new Image[]{
                themeManager.getImage("icons", "file", 18, 18),
                themeManager.getImage("icons", "time", 18, 18),
                themeManager.getImage("icons", "speed", 18, 18)
        };
    }
}
