package ru.smclabs.bootstrap.service.gui.panel;

import lombok.Getter;
import ru.smclabs.bootstrap.service.GuiService;
import ru.smclabs.bootstrap.service.gui.component.BorderBackground;
import ru.smclabs.bootstrap.util.LocalResourceHelper;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class PanelBackground extends AbstractPanel {

    private final Image imageLogo;
    private final Font titleFont = LocalResourceHelper.loadFont("Inter-Bold", 28);
    private final @Getter PanelUpdate panelUpdate;

    public PanelBackground(GuiService guiService) {
        super(guiService);
        this.setBackground(null);
        this.setBorder(new BorderBackground(guiService.getThemeManager()));
        this.setLayout(null);
        this.imageLogo = LocalResourceHelper.loadScaledImage("/assets/icons/512.png", 44, 44);
        this.add(new PanelHeader(guiService));
        this.add(this.panelUpdate = new PanelUpdate(guiService));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(this.guiService.getThemeManager().getColor("bg"));
        g2d.fill(new RoundRectangle2D.Double(0, 0, this.getWidth(), this.getHeight(), (18D / 2D) * Math.PI, (18D / 2D) * Math.PI));
        g2d.setComposite(AlphaComposite.SrcAtop);

        int logoPosX = 32;
        int logoPosY = 23;
        int logoWidth = 44;
        int logoHeight = 44;

        g2d.drawImage(this.imageLogo, logoPosX, logoPosY, logoWidth, logoHeight, this);

        g2d.setPaint(this.guiService.getThemeManager().getColor("title"));
        g2d.setFont(this.titleFont);
        g2d.drawString("SIMPLEMINECRAFT", logoPosX + logoWidth + 12, logoPosY + 22 + 11);

        g2d.setColor(this.guiService.getThemeManager().getColor("bg-border"));
        g2d.fill(new RoundRectangle2D.Double(0, 90, this.getWidth(), 1, 0, 0));

        super.paintComponent(g);
    }

}
