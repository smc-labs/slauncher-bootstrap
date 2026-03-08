package ru.smclabs.bootstrap.gui.panel.background;

import ru.smclabs.bootstrap.gui.manager.ThemeManager;
import ru.smclabs.bootstrap.gui.panel.HeaderPanel;
import ru.smclabs.bootstrap.gui.panel.UpdatePanel;
import ru.smclabs.bootstrap.util.resources.LocalResourceHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class BackgroundPanel extends JPanel {
    public static final int PADDING_X = 36;
    public static final int PADDING_Y = 28;
    public static final double BORDER_RADIUS = 30;

    private static final int LOGO_WIDTH = 40;
    private static final int LOGO_HEIGHT = 40;

    private final Image imageLogo;
    private final UpdatePanel panelUpdate;
    private final ThemeManager themeManager;
    private final RoundRectangle2D.Double roundRect;

    public BackgroundPanel(ThemeManager themeManager, JFrame frame) {
        this.themeManager = themeManager;
        imageLogo = LocalResourceHelper.loadScaledImage("/assets/icons/favicon.png", LOGO_WIDTH, LOGO_HEIGHT);
        roundRect = new RoundRectangle2D.Double();
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
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            drawBackground(g2d);
            drawLogo(g2d);
        } finally {
            g2d.dispose();
        }

        super.paintComponent(g);
    }

    private void drawBackground(Graphics2D g2d) {
        roundRect.setRoundRect(
                0,
                0,
                getWidth(),
                getHeight(),
                BORDER_RADIUS,
                BORDER_RADIUS
        );

        g2d.setColor(themeManager.getColor("bg"));
        g2d.fill(roundRect);
    }

    private void drawLogo(Graphics2D g2d) {
        g2d.drawImage(imageLogo, PADDING_X, PADDING_Y, LOGO_WIDTH, LOGO_HEIGHT, this);
    }
}
