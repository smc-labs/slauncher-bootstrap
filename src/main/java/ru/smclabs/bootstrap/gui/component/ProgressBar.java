package ru.smclabs.bootstrap.gui.component;

import ru.smclabs.bootstrap.gui.panel.update.UpdatePanel;
import ru.smclabs.bootstrap.gui.manager.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class ProgressBar extends JComponent {
    private final ThemeManager themeManager;

    private double progress;
    private double bouncePos;
    private double bounceSpeed = 0.02;

    private Timer bounceTimer;
    private volatile boolean bounceMode;

    public ProgressBar(ThemeManager themeManager, UpdatePanel parent) {
        this.themeManager = themeManager;
        setBounds(0, parent.getHeight() - 24, parent.getWidth(), 24);
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
        Rectangle bounds = getBounds();
        drawBackground(g2d, bounds);

        if (bounceMode) {
            drawBounce(g2d, bounds);
        } else {
            drawProgress(g2d, bounds);
        }

        g2d.dispose();
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

    private void drawBackground(Graphics2D g2d, Rectangle bounds) {
        g2d.setColor(themeManager.getColor("progress-bar"));
        g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
    }

    private void drawProgress(Graphics2D g2d, Rectangle bounds) {
        g2d.setColor(themeManager.getColor("progress-bar-track"));
        g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth() * progress, getHeight(), getHeight(), getHeight()));
    }

    private void drawBounce(Graphics2D g2d, Rectangle bounds) {
        double barWidth = getWidth() * 0.2;
        double barPosX = (getWidth() - barWidth) * bouncePos;

        g2d.setColor(themeManager.getColor("progress-bar-track"));
        g2d.fill(new RoundRectangle2D.Double(barPosX, 0, barWidth, getHeight(), getHeight(), getHeight()));
    }
}
