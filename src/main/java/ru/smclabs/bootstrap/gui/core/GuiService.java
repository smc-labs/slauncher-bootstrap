package ru.smclabs.bootstrap.gui.core;

import ru.smclabs.bootstrap.core.app.Bootstrap;
import ru.smclabs.bootstrap.gui.listener.FrameDragListener;
import ru.smclabs.bootstrap.gui.panel.PanelBackground;
import ru.smclabs.bootstrap.util.resources.ResourcesHelper;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;

public class GuiService {
    private static final String FRAME_TITLE = "SimpleMinecraft";
    private static final int FRAME_WIDTH = 460;
    private static final int FRAME_HEIGHT = 340;

    private final Bootstrap bootstrap;
    private final JFrame frame;
    private final ThemeManager themeManager;
    private final PanelBackground panelBackground;

    public GuiService(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
        frame = createFrame();
        themeManager = new ThemeManager();
        panelBackground = new PanelBackground(this);
    }

    public void postInit() {
        frame.setContentPane(panelBackground);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public JFrame getFrame() {
        return frame;
    }

    public ThemeManager getThemeManager() {
        return themeManager;
    }

    public PanelBackground getPanelBackground() {
        return panelBackground;
    }

    private JFrame createFrame() {
        try {
            Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
            Field awtAppClassNameField = defaultToolkit.getClass().getDeclaredField("awtAppClassName");
            awtAppClassNameField.setAccessible(true);
            awtAppClassNameField.set(defaultToolkit, bootstrap.getEnvironment().getGui());
        } catch (Throwable ignored) {
        }

        JFrame frame = new JFrame();
        frame.setTitle(FRAME_TITLE);
        frame.setName(FRAME_TITLE);
        frame.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        frame.setIconImage(ResourcesHelper.loadScaledImage("/assets/icons/512.png", 128, 128));
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
