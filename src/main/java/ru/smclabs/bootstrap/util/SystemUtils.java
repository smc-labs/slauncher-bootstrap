package ru.smclabs.bootstrap.util;

import ru.smclabs.bootstrap.Bootstrap;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

public class SystemUtils {

    private static final SystemInfo SYSTEM_INFO = new SystemInfo(System.getProperty("os.name", "Unknown OS"));

    public static boolean isI586() {
        return !SYSTEM_INFO.x64;
    }

    public static boolean isX64() {
        return SYSTEM_INFO.x64;
    }

    public static boolean isWindows() {
        return getId().contains("windows");
    }

    public static boolean isLinux() {
        return getId().contains("linux");
    }

    public static boolean isMacOS() {
        return getId().contains("macos");
    }

    public static String getId() {
        return SYSTEM_INFO.toString();
    }

    public static String getName() {
        return SYSTEM_INFO.name;
    }

    public static void openFile(Path path) {
        openFile(path.toFile());
    }

    public static void openFile(File file) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                Bootstrap.getInstance().getLogger().error("Failed to open file in system.", e);
            }
        }
    }

    public static void openURL(String url) {
        try {
            openURL(new URL(url));
        } catch (MalformedURLException e) {
            Bootstrap.getInstance().getLogger().error("Failed to open url in system.", e);
        }
    }

    public static void openURL(URL url) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(url.toURI());
            } catch (IOException | URISyntaxException e) {
                Bootstrap.getInstance().getLogger().error("Failed to open url in system.", e);
            }
        }
    }

    private static boolean detectArch64(String name) {
        return name.equals("windows")
                ? System.getenv("ProgramFiles(x86)") != null
                : System.getProperty("os.arch").contains("64");
    }

    private static String detectId(String name) {
        if (name.contains("win")) {
            name = "windows";
        } else if (name.contains("linux") || name.contains("unix")) {
            name = "linux";
        } else if (name.contains("mac")) {
            name = "macos";
        } else {
            name = "unknown";
        }

        return name;
    }

    private static class SystemInfo {

        private final String id;
        private final String name;
        private final boolean x64;

        private SystemInfo(String name) {
            this.name = name;
            this.id = detectId(name.toLowerCase());
            this.x64 = detectArch64(id);
        }

        @Override
        public String toString() {
            return this.id + (this.x64 ? "-x64" : "i586");
        }
    }

}
