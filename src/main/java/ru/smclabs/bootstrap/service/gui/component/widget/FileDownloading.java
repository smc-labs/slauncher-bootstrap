package ru.smclabs.bootstrap.service.gui.component.widget;

import ru.smclabs.bootstrap.service.gui.ThemeManager;
import ru.smclabs.bootstrap.service.gui.panel.PanelUpdate;
import ru.smclabs.bootstrap.util.LocalResourceHelper;

import javax.swing.*;
import java.awt.*;

public class FileDownloading extends JComponent {

    private final ThemeManager themeManager;
    private final Font labelFont;
    private final Image[] images;

    private boolean visible = false;
    private String fileName = "...";
    private String timeRemain = "...";
    private String speed = "...";

    public FileDownloading(ThemeManager themeManager, PanelUpdate parent) {
        this.themeManager = themeManager;
        labelFont = LocalResourceHelper.loadFont("GolosText-Regular", 14);
        images = loadImages();
        setBounds(0, parent.getHeight() - 74 - 24 - PanelUpdate.VERTICAL_GAP, parent.getWidth(), 74);
    }

    private Image[] loadImages() {
        return new Image[]{
                themeManager.getImage("icons", "file", 18, 18),
                themeManager.getImage("icons", "time", 18, 18),
                themeManager.getImage("icons", "speed", 18, 18)
        };
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!visible) return;

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(new Color(0, 0, 0, 255 / 2));
        g2d.fill(new Rectangle(0, 0, getWidth(), getHeight()));

        drawLabel(g2d,
                images[0],
                themeManager.getColor("sub-title"),
                fileName,
                0);

        drawLabel(g2d,
                images[1],
                themeManager.getColor("sub-title"),
                timeRemain,
                28);

        drawLabel(g2d,
                images[2],
                themeManager.getColor("sub-title"),
                speed,
                28 * 2);
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

    public void setVisible(boolean value) {
        visible = value;
        repaint();
    }
}
