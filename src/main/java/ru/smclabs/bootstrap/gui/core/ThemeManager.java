package ru.smclabs.bootstrap.gui.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jthemedetecor.OsThemeDetector;
import ru.smclabs.bootstrap.core.app.Bootstrap;
import ru.smclabs.bootstrap.util.resources.ResourcesHelper;
import ru.smclabs.jacksonpack.Jackson;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ThemeManager {
    private final Bootstrap bootstrap;
    private final boolean dark;
    private final Map<String, Color> colors = new HashMap<>();

    public ThemeManager() {
        dark = isDarkTheme();
        registerColor("bg", Color.decode("#F8E6D6"), Color.decode("#15151D"));
        registerColor("bg-border", new Color(0, 0, 0, (int) (255 * 0.04)), new Color(255, 255, 255, (int) (255 * 0.04)));
        registerColor("title", Color.BLACK, Color.WHITE);
        registerColor("sub-title", new Color(0, 0, 0, (int) (255 * 0.6)), new Color(255, 255, 255, (int) (255 * 0.6)));
        registerColor("progress-bar", new Color(0, 0, 0, (int) (255 * 0.06)), new Color(255, 255, 255, (int) (255 * 0.06)));
        registerColor("progress-bar-track", Color.decode("#A2BE06"), Color.decode("#A2BE06"));
    }

    public boolean isDark() {
        return dark;
    }

    public Color getColor(String type) {
        if (!colors.containsKey(type)) throw new IllegalArgumentException(type + " color not registered!");
        return colors.get(type);
    }

    public Image getImage(String type, String name, int width, int height) {
        return ResourcesHelper.loadScaledImage("/assets/" + type + "/" + name
                + "-" + (dark ? "dark" : "light") + ".png", width, height);
    }

    private void registerColor(String type, Color lightDark, Color darkColor) {
        colors.put(type, dark ? darkColor : lightDark);
    }

    private boolean isDarkTheme() {
        String theme = getLauncherTheme();
        if (Objects.equals(theme, "light")) {
            return false;
        } else if (Objects.equals(theme, "dark")) {
            return true;
        }

        try {
            return OsThemeDetector.isSupported() && OsThemeDetector.getDetector().isDark();
        } catch (Throwable e) {
            Bootstrap.getInstance().getLogger().error("Failed to detect OS theme!", e);
            return false;
        }
    }

    private String getLauncherTheme() {
        Path configPath = Paths.get(Bootstrap.getInstance().getDirProvider()
                .getPersistenceDir("data/config") + "/launcher.json");

        if (Files.notExists(configPath)) {
            return null;
        }

        try {
            Map<String, Object> config = Jackson.getMapper().readValue(configPath.toFile(),
                    new TypeReference<HashMap<String, Object>>() {
                    });

            return (String) config.get("theme");
        } catch (IOException e) {
            return null;
        }
    }
}
