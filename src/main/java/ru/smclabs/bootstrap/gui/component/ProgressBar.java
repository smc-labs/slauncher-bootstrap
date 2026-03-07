package ru.smclabs.bootstrap.gui.component;

import ru.smclabs.bootstrap.gui.manager.ThemeManager;
import ru.smclabs.bootstrap.gui.panel.UpdatePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class ProgressBar extends JComponent {
    private static final int PROGRESS_BAR_HEIGHT = 24;

    private final ThemeManager themeManager;
    private final RoundRectangle2D.Double roundRect;

    private double progress;
    private double bouncePos;
    private double bounceSpeed = 0.02;

    private Timer bounceTimer;
    private volatile boolean bounceMode;

    public ProgressBar(ThemeManager themeManager, UpdatePanel parent) {
        this.themeManager = themeManager;
        roundRect = new RoundRectangle2D.Double();
        setBounds(0, parent.getHeight() - PROGRESS_BAR_HEIGHT, parent.getWidth(), PROGRESS_BAR_HEIGHT);
        setProgress(-1.0);
    }

    public void setProgress(double value) {
        if (value < 0.0) {
            startBounce();
        } else {
            stopBounce();
            progress = Math.min(1.0, Math.max(0.035, value));
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            drawBackground(g2d, width, height);

            if (bounceMode) {
                drawBounce(g2d, width, height);
            } else {
                drawProgress(g2d, width, height);
            }
        } finally {
            g2d.dispose();
        }
    }

    private void startBounce() {
        if (bounceMode) return;
        bounceMode = true;
        bouncePos = 0.0;

        bounceTimer = new Timer(8, e -> {
            bouncePos += bounceSpeed;

            if (bouncePos < 0.0) {
                bouncePos = 0.0;
                bounceSpeed = -bounceSpeed;
            } else if (bouncePos > 1.0) {
                bouncePos = 1.0;
                bounceSpeed = -bounceSpeed;
            }

            repaint();
        });

        bounceTimer.start();
    }

    private void stopBounce() {
        if (bounceTimer != null) {
            bounceMode = false;
            bounceTimer.stop();
            bounceTimer = null;
        }
    }

    private void drawBackground(Graphics2D g2d, int width, int height) {
        g2d.setColor(themeManager.getColor("progress-bar"));
        roundRect.setRoundRect(0, 0, width, height, height, height);
        g2d.fill(roundRect);
    }

    private void drawProgress(Graphics2D g2d, int width, int height) {
        g2d.setColor(themeManager.getColor("progress-bar-track"));
        roundRect.setRoundRect(0, 0, width * progress, height, height, height);
        g2d.fill(roundRect);
    }

    private void drawBounce(Graphics2D g2d, int width, int height) {
        double barWidth = width * 0.2;
        double barPosX = (width - barWidth) * bouncePos;

        g2d.setColor(themeManager.getColor("progress-bar-track"));
        roundRect.setRoundRect(barPosX, 0, barWidth, height, height, height);
        g2d.fill(roundRect);
    }
}
