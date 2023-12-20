package ru.smclabs.bootstrap.service.gui.panel;

import ru.smclabs.bootstrap.service.GuiService;
import ru.smclabs.bootstrap.service.gui.component.button.ButtonControl;

import java.awt.*;
import java.awt.event.MouseEvent;

public class PanelHeader extends AbstractPanel {

    public PanelHeader(GuiService guiService) {
        super(guiService);
        this.setLayout(this.createLayoutManager());
        this.setBackground(null);
        this.setBounds(0, 7, guiService.getBootstrap().getEnvironment().getGui().getFrameWidth() + 6 - 32, 90);

        this.add(new ButtonControl(guiService.getThemeManager(), "turn") {
            @Override
            public void mouseClicked(MouseEvent e) {
                guiService.getFrame().setState(Frame.ICONIFIED);
            }
        });

        this.add(new ButtonControl(guiService.getThemeManager(), "close") {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.exit(0);
            }
        });
    }

    private LayoutManager createLayoutManager() {
        FlowLayout flowLayout = new FlowLayout(FlowLayout.RIGHT);
        flowLayout.setVgap(18);
        flowLayout.setHgap(6);
        flowLayout.setAlignOnBaseline(false);
        return flowLayout;
    }
}
