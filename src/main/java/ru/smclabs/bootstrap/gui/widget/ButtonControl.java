package ru.smclabs.bootstrap.gui.widget;

import ru.smclabs.bootstrap.Bootstrap;
import ru.smclabs.bootstrap.gui.core.ThemeManager;
import ru.smclabs.bootstrap.util.resources.ResourcesHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.RoundRectangle2D;

public abstract class ButtonControl extends JComponent implements MouseListener {
    private final Image imageRegular;
    private final Image imageHover;

    private boolean hovered = false;

    public ButtonControl(ThemeManager themeManager, String type) {
        setPreferredSize(new Dimension(40, 40));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        imageRegular = themeManager.getImage("buttons", type, 40, 40);
        imageHover = ResourcesHelper.loadScaledImage("/assets/buttons/" + type + "-hover.png", 40, 40);
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
        Dimension dimension = getPreferredSize().getSize();

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(Bootstrap.getInstance().getGuiService().getThemeManager().getColor("bg"));
        g2d.fill(new RoundRectangle2D.Double(0, 0, dimension.width, dimension.height, 0, 0));

        g2d.drawImage(hovered ? imageHover : imageRegular,
                0,
                0,
                dimension.width,
                dimension.height,
                this);
    }
}