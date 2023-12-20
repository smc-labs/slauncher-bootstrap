package ru.smclabs.bootstrap.service.gui.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FrameDragListener extends MouseAdapter {

    private final JFrame frame;
    private Point coords = null;

    public FrameDragListener(JFrame frame) {
        this.frame = frame;
    }

    public void mouseReleased(MouseEvent e) {
        this.coords = null;
    }

    public void mousePressed(MouseEvent e) {
        this.coords = e.getPoint();
    }

    public void mouseDragged(MouseEvent e) {
        Point currCoords = e.getLocationOnScreen();
        this.frame.setLocation(currCoords.x - coords.x, currCoords.y - coords.y);
    }
}
