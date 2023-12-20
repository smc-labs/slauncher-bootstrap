package ru.smclabs.bootstrap.service.gui.component.button;

import ru.smclabs.bootstrap.Bootstrap;
import ru.smclabs.bootstrap.service.gui.ThemeManager;
import ru.smclabs.bootstrap.util.LocalResourceHelper;

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
        this.setPreferredSize(new Dimension(40, 40));
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.imageRegular = themeManager.getImage("buttons", type, 40, 40);
        this.imageHover = LocalResourceHelper.loadScaledImage("/assets/buttons/" + type + "-hover.png", 40, 40);
        this.addMouseListener(this);
    }

    public abstract void mouseClicked(MouseEvent e);

    @Override
    protected void paintComponent(Graphics g) {
        Dimension dimension = this.getPreferredSize().getSize();

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(Bootstrap.getInstance().getGuiService().getThemeManager().getColor("bg"));
        g2d.fill(new RoundRectangle2D.Double(0, 0, dimension.width, dimension.height, 0, 0));

        g2d.setComposite(AlphaComposite.SrcAtop);
        g2d.drawImage(this.hovered ? this.imageHover : this.imageRegular,
                0,
                0,
                dimension.width,
                dimension.height,
                this);

        g2d.dispose();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        this.hovered = true;
        this.repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        this.hovered = false;
        this.repaint();
    }
}