package ru.smclabs.bootstrap.util;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
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

    public static void exit(int status) {
        System.exit(status);
    }
}
