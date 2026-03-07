package ru.smclabs.bootstrap.gui.panel.background;

import ru.smclabs.bootstrap.gui.manager.ThemeManager;

import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class BackgroundBorder extends AbstractBorder {
    private static final double BORDER_RADIUS = BackgroundPanel.BORDER_RADIUS - 2.0;

    private final Insets insets;
    private final BasicStroke stroke;
    private final RenderingHints hints;
    private final Color borderColor;
    private final RoundRectangle2D.Double borderRect;

    public BackgroundBorder(ThemeManager themeManager) {
        stroke = new BasicStroke(1);
        hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        insets = new Insets(0, 0, 0, 0);
        borderColor = themeManager.getColor("bg-border");
        borderRect = new RoundRectangle2D.Double();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return insets;
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        return getBorderInsets(c);
    }

    @Override
    public void paintBorder(Component component, Graphics graphics, int x, int y, int width, int height) {
        borderRect.setRoundRect(
                x,
                y,
                width - stroke.getLineWidth(),
                height - stroke.getLineWidth(),
                BORDER_RADIUS,
                BORDER_RADIUS
        );

        Graphics2D g2 = (Graphics2D) graphics;
        g2.setPaint(borderColor);
        g2.setRenderingHints(hints);
        g2.setStroke(stroke);
        g2.draw(borderRect);
    }
}
