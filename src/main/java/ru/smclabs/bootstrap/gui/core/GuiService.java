package ru.smclabs.bootstrap.gui.core;

import ru.smclabs.bootstrap.core.app.Bootstrap;
import ru.smclabs.bootstrap.gui.listener.FrameDragListener;
import ru.smclabs.bootstrap.gui.panel.BackgroundPanel;
import ru.smclabs.bootstrap.util.resources.ResourcesHelper;

import javax.swing.*;
import java.awt.*;

public class GuiService {
    public static final String FRAME_TITLE = "SimpleMinecraft";
    public static final int FRAME_WIDTH = 460;
    public static final int FRAME_HEIGHT = 340;

    private final JFrame frame;
    private final BackgroundPanel panelBackground;

    public GuiService(Bootstrap bootstrap) {
        frame = createFrame();
        panelBackground = new BackgroundPanel(createThemeManager(bootstrap), frame);
    }

    public void start() {
        frame.setContentPane(panelBackground);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public BackgroundPanel getPanelBackground() {
        return panelBackground;
    }

    private ThemeManager createThemeManager(Bootstrap bootstrap) {
        return new ThemeManager(bootstrap.getContext().getDirProvider());
    }

    private JFrame createFrame() {
        JFrame frame = new JFrame();
        frame.setTitle(FRAME_TITLE);
        frame.setName(FRAME_TITLE);
        frame.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        frame.setIconImage(ResourcesHelper.loadScaledImage("/assets/icons/favicon.png", 128, 128));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.setResizable(false);
        frame.setBackground(new Color(0, 0, 0, 0));

        FrameDragListener dragListener = new FrameDragListener(frame);
        frame.addMouseListener(dragListener);
        frame.addMouseMotionListener(dragListener);

        return frame;
    }
}
