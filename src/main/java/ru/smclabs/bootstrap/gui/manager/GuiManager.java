package ru.smclabs.bootstrap.gui.manager;

import ru.smclabs.bootstrap.gui.controller.UpdateViewController;
import ru.smclabs.bootstrap.gui.listener.FrameDragListener;
import ru.smclabs.bootstrap.gui.panel.background.BackgroundPanel;
import ru.smclabs.bootstrap.util.resources.ResourcesHelper;
import ru.smclabs.slauncher.resources.provider.DirProvider;

import javax.swing.*;
import java.awt.*;

public class GuiManager {
    public static final String FRAME_TITLE = "SimpleMinecraft";
    public static final int FRAME_WIDTH = 460;
    public static final int FRAME_HEIGHT = 340;

    private final JFrame frame;
    private final BackgroundPanel panelBackground;
    private final UpdateViewController updateViewController;

    public GuiManager(DirProvider dirProvider) {
        frame = createFrame();
        panelBackground = new BackgroundPanel(createThemeManager(dirProvider), frame);
        updateViewController = new UpdateViewController(panelBackground.getPanelUpdate());
    }

    public void start() {
        frame.setContentPane(panelBackground);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public UpdateViewController getUpdateViewController() {
        return updateViewController;
    }

    private ThemeManager createThemeManager(DirProvider dirProvider) {
        return new ThemeManager(dirProvider);
    }

    private JFrame createFrame() {
        setupSystemLookAndFeel();

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

    private void setupSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
            );
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException ignored) {
        }
    }
}
