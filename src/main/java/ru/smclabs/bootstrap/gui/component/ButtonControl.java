package ru.smclabs.bootstrap.gui.component;

import ru.smclabs.bootstrap.gui.manager.ThemeManager;
import ru.smclabs.bootstrap.util.resources.LocalResourceHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public abstract class ButtonControl extends JComponent implements MouseListener {
    private final ThemeManager themeManager;
    private final Image imageRegular;
    private final Image imageHover;

    private boolean hovered = false;

    public ButtonControl(ThemeManager themeManager, String type) {
        this.themeManager = themeManager;
        imageRegular = themeManager.getImage("buttons", type, 40, 40);
        imageHover = LocalResourceHelper.loadScaledImage(
                "/assets/buttons/" + type + "-hover.png",
                40,
                40
        );

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(40, 40));
        addMouseListener(this);
    }

    public abstract void mouseClicked(MouseEvent e);

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        hovered = true;
        repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        hovered = false;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            int width = getWidth();
            int height = getHeight();

            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(themeManager.getColor("bg"));

            g2d.fillRect(0, 0, width, height);

            g2d.drawImage(
                    hovered ? imageHover : imageRegular,
                    0,
                    0,
                    width,
                    height,
                    this
            );
        } finally {
            g2d.dispose();
        }
    }
}