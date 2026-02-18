package ru.smclabs.bootstrap.gui.border;

import ru.smclabs.bootstrap.gui.core.GuiService;
import ru.smclabs.bootstrap.gui.core.ThemeManager;

import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

public class BorderBackground extends AbstractBorder {
    private final Insets insets;
    private final BasicStroke stroke;
    private final RenderingHints hints;
    private final Color borderColor;

    public BorderBackground(ThemeManager themeManager) {
        stroke = new BasicStroke(1);
        hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        insets = new Insets(0, 0, 0, 0);
        borderColor = themeManager.getColor("bg-border");
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
        RoundRectangle2D.Double roundRectangle = new RoundRectangle2D.Double(0, 0,
                width - stroke.getLineWidth(),
                height - stroke.getLineWidth(),
                (17D / 2D) * Math.PI, (17D / 2D) * Math.PI);

        Graphics2D g2 = (Graphics2D) graphics;
        g2.setPaint(borderColor);
        g2.setRenderingHints(hints);
        g2.setStroke(stroke);
        g2.draw(new Area(roundRectangle));
    }
}
