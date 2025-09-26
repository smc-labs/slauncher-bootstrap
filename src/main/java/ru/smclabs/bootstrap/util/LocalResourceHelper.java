package ru.smclabs.bootstrap.util;

import ru.smclabs.bootstrap.util.resource.LocalResourceException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;

public class LocalResourceHelper {

    public static URL toUrl(String path) throws LocalResourceException {
        URL url = LocalResourceHelper.class.getResource(path);
        if (url == null) throw new LocalResourceException("Resource not found: " + path);
        return url;
    }

    public static String toUrlExternal(String path) throws LocalResourceException {
        return toUrl(path).toExternalForm();
    }

    private static BufferedImage loadBufferedImage(String path) throws LocalResourceException {
        try {
            return ImageIO.read(toUrl(path));
        } catch (Throwable e) {
            throw new LocalResourceException("Failed to load local image!", e);
        }
    }

    public static Image loadScaledImage(String path, int width, int height) {
        return loadBufferedImage(path).getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }

    public static Font loadFont(String name, float size) {
        try (InputStream inputStream = toUrl("/assets/fonts/" + name + ".ttf").openStream()) {
            return Font.createFont(0, inputStream).deriveFont(size);
        } catch (Throwable e) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Font[] fonts = ge.getAllFonts();

            for (Font font : fonts) {
                if (font.getName().equals(name)) {
                    return font.deriveFont(size);
                }
            }

            for (Font font : fonts) {
                if (font.getName().startsWith(name)) return font.deriveFont(size);
            }
        }

        return new Font(null, Font.PLAIN, (int) size);
    }

}
