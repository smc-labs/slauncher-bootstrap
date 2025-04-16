package ru.smclabs.bootstrap.util;

import ru.smclabs.bootstrap.BootstrapMain;
import ru.smclabs.system.info.SystemInfo;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class RuntimeUtils {

    @SuppressWarnings("CallToSystemGC")
    public static void callGC() {
        Runtime.getRuntime().gc();
        Runtime.getRuntime().runFinalization();
    }

    public static void setupUTF8() {
        System.setProperty("file.encoding", "UTF-8");
        try {
            Field charset = Charset.class.getDeclaredField("defaultCharset");
            charset.setAccessible(true);
            charset.set(null, null);
        } catch (Throwable ignored) {
        }
    }

    public static List<String> getInputArguments() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments();
    }

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
