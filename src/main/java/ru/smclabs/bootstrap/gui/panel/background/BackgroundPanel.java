package ru.smclabs.bootstrap.gui.panel.background;

import ru.smclabs.bootstrap.gui.manager.ThemeManager;
import ru.smclabs.bootstrap.gui.panel.header.HeaderPanel;
import ru.smclabs.bootstrap.gui.panel.Panel;
import ru.smclabs.bootstrap.gui.panel.update.UpdatePanel;
import ru.smclabs.bootstrap.util.resources.ResourcesHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class BackgroundPanel extends Panel {
    public static final int PADDING_X = 34;
    public static final int PADDING_Y = 28;

    private static final int LOGO_WIDTH = 40;
    private static final int LOGO_HEIGHT = 40;

    private final Image imageLogo;
    private final UpdatePanel panelUpdate;
    private final ThemeManager themeManager;

    public BackgroundPanel(ThemeManager themeManager, JFrame frame) {
        this.themeManager = themeManager;
        imageLogo = ResourcesHelper.loadScaledImage("/assets/icons/favicon.png", LOGO_WIDTH, LOGO_HEIGHT);
        setLayout(null);
        setBackground(null);
        setBorder(new BackgroundBorder(themeManager));
        add(new HeaderPanel(themeManager, frame));
        add(panelUpdate = new UpdatePanel(themeManager));
    }

    public UpdatePanel getPanelUpdate() {
        return panelUpdate;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g2d);
        drawLogo(g2d);

        super.paintComponent(g);
    }

    private void drawBackground(Graphics2D g2d) {
        g2d.setColor(themeManager.getColor("bg"));
        g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), (18D / 2D) * Math.PI, (18D / 2D) * Math.PI));
    }

    private void drawLogo(Graphics2D g2d) {
        g2d.drawImage(imageLogo, PADDING_X, PADDING_Y, LOGO_WIDTH, LOGO_HEIGHT, this);
    }
}
