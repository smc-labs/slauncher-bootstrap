package ru.smclabs.bootstrap.gui.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jthemedetecor.OsThemeDetector;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.smclabs.bootstrap.util.resources.ResourcesHelper;
import ru.smclabs.jacksonpack.Jackson;
import ru.smclabs.slauncher.resources.provider.DirProvider;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ThemeManager {
    private static final Logger log = LoggerFactory.getLogger(ThemeManager.class);

    private final DirProvider dirProvider;
    private final boolean dark;
    private final Map<String, Color> colors;

    public ThemeManager(DirProvider dirProvider) {
        this.dirProvider = dirProvider;
        dark = isDarkTheme();
        colors = new HashMap<>();
        initColors();
    }

    public Color getColor(String type) {
        if (!colors.containsKey(type)) throw new IllegalArgumentException(type + " color not registered!");
        return colors.get(type);
    }

    public Image getImage(String type, String name, int width, int height) {
        return ResourcesHelper.loadScaledImage("/assets/" + type + "/" + name
                + "-" + (dark ? "dark" : "light") + ".png", width, height);
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
            log.error("Failed to detect OS theme!", e);
            return false;
        }
    }

    @Nullable
    private String getLauncherTheme() {
        Path configPath = dirProvider.getPersistenceDir("data")
                .resolve("config")
                .resolve("launcher.json");

        if (Files.exists(configPath)) {
            try {
                Map<String, Object> config = Jackson.getMapper().readValue(configPath.toFile(),
                        new TypeReference<HashMap<String, Object>>() {
                        });

                Object themeEntry = config.get("theme");

                if (themeEntry != null) {
                    return (String) themeEntry;
                }
            } catch (IOException e) {
                log.error("Failed to detect launcher theme!", e);
            }
        }

        return null;
    }

    private void initColors() {
        bindColor("bg", Color.decode("#F8E6D6"), Color.decode("#15151D"));
        bindColor("bg-border", new Color(0, 0, 0, (int) (255 * 0.04)), new Color(255, 255, 255, (int) (255 * 0.04)));
        bindColor("title", Color.BLACK, Color.WHITE);
        bindColor("sub-title", new Color(0, 0, 0, (int) (255 * 0.6)), new Color(255, 255, 255, (int) (255 * 0.6)));
        bindColor("progress-bar", new Color(0, 0, 0, (int) (255 * 0.06)), new Color(255, 255, 255, (int) (255 * 0.06)));
        bindColor("progress-bar-track", Color.decode("#A2BE06"), Color.decode("#A2BE06"));
    }

    private void bindColor(String type, Color lightDark, Color darkColor) {
        colors.put(type, dark ? darkColor : lightDark);
    }
}
