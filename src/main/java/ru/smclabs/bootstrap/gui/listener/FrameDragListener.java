package ru.smclabs.bootstrap.gui.listener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FrameDragListener extends MouseAdapter {
    private final JFrame frame;
    private Point coords;

    public FrameDragListener(JFrame frame) {
        this.frame = frame;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        coords = null;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        coords = e.getPoint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Point currCoords = e.getLocationOnScreen();
        frame.setLocation(currCoords.x - coords.x, currCoords.y - coords.y);
    }
}
