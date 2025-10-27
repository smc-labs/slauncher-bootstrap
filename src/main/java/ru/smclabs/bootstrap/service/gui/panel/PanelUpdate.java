package ru.smclabs.bootstrap.service.gui.panel;

import ru.smclabs.bootstrap.Bootstrap;
import ru.smclabs.bootstrap.service.GuiService;
import ru.smclabs.bootstrap.service.gui.component.widget.FileDownloading;
import ru.smclabs.bootstrap.service.gui.component.widget.ProgressBar;
import ru.smclabs.bootstrap.util.LocalResourceHelper;

import java.awt.*;

public class PanelUpdate extends AbstractPanel {

    public static final int VERTICAL_GAP = 34;

    private final Font titleFont;
    private final Font subTitleFont;
    private final FileDownloading fileDownloading;
    private final ProgressBar progressBar;

    private String labelTitle = "Поиск обновлений";
    private String labelSubTitle = "это займет пару мгновений...";

    public PanelUpdate(GuiService guiService) {
        super(guiService);

        int posY = PanelBackground.PADDING_Y + 40 + VERTICAL_GAP;
        setBounds(PanelBackground.PADDING_X, posY, calcWidth(), calcHeight(posY));
        setBackground(null);
        setLayout(null);

        titleFont = LocalResourceHelper.loadFont("GolosText-Bold", 34);
        subTitleFont = LocalResourceHelper.loadFont("GolosText-Regular", 16);

        add(fileDownloading = new FileDownloading(guiService.getThemeManager(), this));
        add(progressBar = new ProgressBar(guiService.getThemeManager(), this));
    }

    private int calcWidth() {
        return guiService.getBootstrap().getEnvironment().getGui().getFrameWidth() - PanelBackground.PADDING_X * 2;
    }

    private int calcHeight(int posY) {
        return Bootstrap.getInstance().getEnvironment().getGui().getFrameHeight() - posY - PanelBackground.PADDING_Y;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g2d);

        drawCenteredString(g2d, labelTitle, titleFont, guiService.getThemeManager().getColor("title"), 25);
        drawCenteredString(g2d, labelSubTitle, subTitleFont, guiService.getThemeManager().getColor("sub-title"), 25 + 24);
    }

    private void drawBackground(Graphics2D g2d) {
        g2d.setColor(guiService.getThemeManager().getColor("bg"));
        g2d.fill(new Rectangle(0, 0, getWidth(), getHeight()));
    }

    public void drawCenteredString(Graphics2D g2d, String text, Font font, Paint paint, int y) {
        g2d.setPaint(paint);
        g2d.setFont(font);
        g2d.drawString(text, getWidth() / 2 - g2d.getFontMetrics().stringWidth(text) / 2, y);
    }

    public void setLabelTitle(String text) {
        labelTitle = text;
        repaint();
    }

    public void setLabelSubTitle(String text) {
        labelSubTitle = text;
        repaint();
    }

    public void setProgress(double progress) {
        progressBar.setProgress(progress);
    }

    public void setLabelTimeRemain(String time) {
        fileDownloading.setTimeRemain(time);
    }

    public void setLabelSpeed(String speed) {
        fileDownloading.setSpeed(speed);
    }

    public void setLabelFileName(String fileName) {
        fileDownloading.setFileName(fileName);
    }

    public void setFileDownloadingVisible(boolean visible) {
        fileDownloading.setVisible(visible);
        repaint();
    }

    /*
    private void startRepaintTimer() {
        new Timer(10, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (progress <= 0D) {
                    if (!bouncingReverse) {
                        platformPosX = Math.min(platformPosX + 5, progressPrefWidth - progressBouncerPrefWidth);
                        if (platformPosX == progressPrefWidth - progressBouncerPrefWidth) bouncingReverse = true;
                    } else {
                        platformPosX = Math.max(platformPosX - 5, 0);
                        if (platformPosX == 0) bouncingReverse = false;
                    }
                }

                if (SystemInfo.get().isMacOS()) {
                    guiService.getPanelBackground().repaint();
                } else {
                    repaint();
                }
            }
        }).start();
    }
     */
}
