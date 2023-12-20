package ru.smclabs.bootstrap.service;

import lombok.Getter;
import ru.smclabs.bootstrap.Bootstrap;
import ru.smclabs.bootstrap.environment.GuiEnvironment;
import ru.smclabs.bootstrap.service.gui.ThemeManager;
import ru.smclabs.bootstrap.service.gui.component.FrameDragListener;
import ru.smclabs.bootstrap.service.gui.pane.PanelBackground;
import ru.smclabs.bootstrap.util.LocalResourceHelper;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;

@Getter
public class GuiService extends AbstractService {

    private final ThemeManager themeManager;

    private final JFrame frame;
    private final PanelBackground panelBackground;

    public GuiService(Bootstrap bootstrap) {
        super(bootstrap);
        this.themeManager = new ThemeManager();
        this.frame = this.createFrame();
        this.frame.setContentPane(this.panelBackground = new PanelBackground(this));
    }

    private JFrame createFrame() {
        GuiEnvironment guiEnvironment = this.getBootstrap().getEnvironment().getGui();

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
        frame.setIconImage(LocalResourceHelper.loadScaledImage("/assets/icons/512.png", 128, 128));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.setResizable(false);
        frame.setBackground(new Color(0, 0, 0, 0));

        FrameDragListener dragListener = new FrameDragListener(frame);
        frame.addMouseListener(dragListener);
        frame.addMouseMotionListener(dragListener);

        return frame;
    }

    public void postInit() {
        this.frame.pack();
        this.frame.setLocationRelativeTo(null);
        this.frame.setVisible(true);
    }
}
