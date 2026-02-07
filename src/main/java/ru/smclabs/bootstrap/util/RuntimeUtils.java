package ru.smclabs.bootstrap.util;

import ru.smclabs.bootstrap.BootstrapMain;
import ru.smclabs.system.info.SystemInfo;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RuntimeUtils {
    public static Path getWorkingDir() {
        return Paths.get(System.getProperty("user.dir"));
    }

    public static boolean isStartedByWrongPackagedJre() {
        return isStartedByPackagedJre() && SystemInfo.get().isX64() && !System.getProperty("os.arch").contains("64");
    }

    public static boolean isStartedByPackagedJre() {
        return SystemInfo.get().isWindows() && Files.exists(Paths.get(getWorkingDir() + "/runtime/"));
    }

    public static Path getExecutableFilePath() throws URISyntaxException {
        return Paths.get(BootstrapMain.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    }

    public static String getExecutableFileName() {
        try {
            return getExecutableFilePath().getFileName().toString();
        } catch (Throwable e) {
            return "";
        }
    }

    public static boolean isExecutableFileExtension(String extension) {
        return getExecutableFileName().endsWith("." + extension);
    }
}
