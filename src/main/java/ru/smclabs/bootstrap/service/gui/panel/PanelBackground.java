package ru.smclabs.bootstrap.service.gui.panel;

import lombok.Getter;
import ru.smclabs.bootstrap.service.GuiService;
import ru.smclabs.bootstrap.service.gui.component.BorderBackground;
import ru.smclabs.bootstrap.util.LocalResourceHelper;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class PanelBackground extends AbstractPanel {

    public static final int PADDING_X = 34;
    public static final int PADDING_Y = 28;

    private static final int LOGO_WIDTH = 40;
    private static final int LOGO_HEIGHT = 40;

    private final Image imageLogo;
    private final @Getter PanelUpdate panelUpdate;

    public PanelBackground(GuiService guiService) {
        super(guiService);
        imageLogo = LocalResourceHelper.loadScaledImage("/assets/icons/512.png", LOGO_WIDTH, LOGO_HEIGHT);

        setLayout(null);
        setBackground(null);
        setBorder(new BorderBackground(guiService.getThemeManager()));
        add(new PanelHeader(guiService));
        add(panelUpdate = new PanelUpdate(guiService));
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
        g2d.setColor(guiService.getThemeManager().getColor("bg"));
        g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), (18D / 2D) * Math.PI, (18D / 2D) * Math.PI));
    }

    private void drawLogo(Graphics2D g2d) {
        g2d.drawImage(imageLogo, PADDING_X, PADDING_Y, LOGO_WIDTH, LOGO_HEIGHT, this);
    }
}
