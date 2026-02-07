package ru.smclabs.bootstrap.gui.panel;

import ru.smclabs.bootstrap.gui.core.GuiService;
import ru.smclabs.bootstrap.gui.widget.ButtonControl;

import java.awt.*;
import java.awt.event.MouseEvent;

public class PanelHeader extends Panel {
    public PanelHeader(GuiService guiService) {
        super(guiService);
        setLayout(createLayoutManager());
        setBackground(null);
        setBounds();
        addButtons();
    }

    private LayoutManager createLayoutManager() {
        FlowLayout layout = new FlowLayout(FlowLayout.RIGHT);
        layout.setVgap(PanelBackground.PADDING_Y);
        layout.setHgap(6);
        layout.setAlignOnBaseline(false);
        return layout;
    }

    private void setBounds() {
        int width = guiService.getBootstrap().getEnvironment().getGui().getFrameWidth();
        setBounds(0, 0, width - PanelBackground.PADDING_X + 2, 40 + (PanelBackground.PADDING_Y));
    }

    private void addButtons() {
        add(new ButtonControl(guiService.getThemeManager(), "turn") {
            @Override
            public void mouseClicked(MouseEvent e) {
                guiService.getFrame().setState(Frame.ICONIFIED);
            }
        });

        add(new ButtonControl(guiService.getThemeManager(), "close") {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.exit(0);
            }
        });
    }
}
