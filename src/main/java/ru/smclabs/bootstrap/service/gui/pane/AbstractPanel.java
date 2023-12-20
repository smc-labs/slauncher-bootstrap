package ru.smclabs.bootstrap.service.gui.pane;

import lombok.Getter;
import ru.smclabs.bootstrap.service.GuiService;

import javax.swing.*;

@Getter
public class AbstractPanel extends JPanel {

    protected final GuiService guiService;

    public AbstractPanel(GuiService guiService) {
        this.guiService = guiService;
    }

}
