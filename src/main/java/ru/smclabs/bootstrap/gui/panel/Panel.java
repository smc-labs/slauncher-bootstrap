package ru.smclabs.bootstrap.gui.panel;

import ru.smclabs.bootstrap.gui.core.GuiService;

import javax.swing.*;

public abstract class Panel extends JPanel {
    protected final GuiService guiService;

    public Panel(GuiService guiService) {
        this.guiService = guiService;
    }

    public GuiService getGuiService() {
        return guiService;
    }
}
