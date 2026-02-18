package ru.smclabs.bootstrap.util.resources;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;

public class ResourcesHelper {
    public static URL toUrl(String path) throws LocalResourceException {
        URL url = ResourcesHelper.class.getResource(path);
        if (url == null) throw new LocalResourceException("Resource not found: " + path);
        return url;
    }

    private static BufferedImage loadBufferedImage(String path) throws LocalResourceException {
        try {
            return ImageIO.read(toUrl(path));
        } catch (Exception e) {
            throw new LocalResourceException("Failed to load local image!", e);
        }
    }

    public static Image loadScaledImage(String path, int width, int height) {
        return loadBufferedImage(path).getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }

    public static Font loadFont(String name, float size) {
        Font font;
        try {
            font = readFont(name, size);
        } catch (Exception e) {
            font = findFont(name, size);
        }

        if (font == null) {
            font = new Font(null, Font.PLAIN, (int) size);
        }

        return font;
    }

    private static Font readFont(String name, float size) throws Exception {
        try (InputStream inputStream = toUrl("/assets/fonts/" + name + ".ttf").openStream()) {
            return Font.createFont(Font.TRUETYPE_FONT, inputStream).deriveFont(size);
        }
    }

    private static Font findFont(String name, float size) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] fonts = ge.getAllFonts();

        for (Font font : fonts) {
            if (font.getName().equals(name) || font.getName().startsWith(name)) {
                return font.deriveFont(size);
            }
        }

        return null;
    }
}
