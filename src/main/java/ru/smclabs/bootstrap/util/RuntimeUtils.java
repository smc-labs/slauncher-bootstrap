package ru.smclabs.bootstrap.util;

import ru.smclabs.bootstrap.core.BootstrapMain;
import ru.smclabs.system.info.arch.ArchType;
import ru.smclabs.system.info.os.OsType;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RuntimeUtils {
    public static Path getWorkingDir() {
        return Paths.get(System.getProperty("user.dir"));
    }

    public static boolean isStartedByWrongPackagedJre() {
        return isStartedByPackagedJre()
                && ArchType.current().is64Bit()
                && !System.getProperty("os.arch").contains("64");
    }

    public static boolean isStartedByPackagedJre() {
        return OsType.current() == OsType.WINDOWS && Files.exists(Paths.get(getWorkingDir() + "/runtime/"));
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
