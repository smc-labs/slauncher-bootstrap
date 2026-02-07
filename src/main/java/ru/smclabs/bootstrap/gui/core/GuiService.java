package ru.smclabs.bootstrap.gui.core;

import ru.smclabs.bootstrap.Bootstrap;
import ru.smclabs.bootstrap.gui.environment.GuiEnvironment;
import ru.smclabs.bootstrap.gui.listener.FrameDragListener;
import ru.smclabs.bootstrap.gui.panel.PanelBackground;
import ru.smclabs.bootstrap.util.resources.ResourcesHelper;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;

public class GuiService extends AbstractService {
    private final JFrame frame;
    private final ThemeManager themeManager;
    private final PanelBackground panelBackground;

    public GuiService(Bootstrap bootstrap) {
        super(bootstrap);
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
        GuiEnvironment guiEnvironment = getBootstrap().getEnvironment().getGui();
        try {
            Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
            Field awtAppClassNameField = defaultToolkit.getClass().getDeclaredField("awtAppClassName");
            awtAppClassNameField.setAccessible(true);
            awtAppClassNameField.set(defaultToolkit, guiEnvironment.getFrameTitle());
        } catch (Throwable ignored) {
        }

        JFrame frame = new JFrame();
        frame.setTitle(guiEnvironment.getFrameTitle());
        frame.setName(guiEnvironment.getFrameTitle());
        frame.setPreferredSize(new Dimension(guiEnvironment.getFrameWidth(), guiEnvironment.getFrameHeight()));
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
