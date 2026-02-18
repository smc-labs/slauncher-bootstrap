package ru.smclabs.bootstrap.gui.panel;

import ru.smclabs.bootstrap.gui.core.GuiService;
import ru.smclabs.bootstrap.gui.core.ThemeManager;
import ru.smclabs.bootstrap.gui.widget.ButtonControl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class HeaderPanel extends Panel {
    private final ThemeManager themeManager;
    private final JFrame frame;

    public HeaderPanel(ThemeManager themeManager, JFrame frame) {
        this.themeManager = themeManager;
        this.frame = frame;
        setLayout(createLayoutManager());
        setBackground(null);
        setBounds();
        addButtons();
    }

    private LayoutManager createLayoutManager() {
        FlowLayout layout = new FlowLayout(FlowLayout.RIGHT);
        layout.setVgap(BackgroundPanel.PADDING_Y);
        layout.setHgap(6);
        layout.setAlignOnBaseline(false);
        return layout;
    }

    private void setBounds() {
        int width = GuiService.FRAME_WIDTH - BackgroundPanel.PADDING_X + 2;
        int height = 40 + BackgroundPanel.PADDING_Y;
        setBounds(0, 0, width, height);
    }

    private void addButtons() {
        add(new ButtonControl(themeManager, "turn") {
            @Override
            public void mouseClicked(MouseEvent e) {
                frame.setState(Frame.ICONIFIED);
            }
        });

        add(new ButtonControl(themeManager, "close") {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.exit(0);
            }
        });
    }
}
