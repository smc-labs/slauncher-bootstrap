package ru.smclabs.bootstrap.util;

import ru.smclabs.resources.type.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FileUtils {

    public static String sizeForHuman(long bytes) {
        long kilobytes = bytes / 1024;
        if (kilobytes > 1024 * 1024) return String.format("%.2f", (float) kilobytes / 1024 * 1024) + " ГБ";
        if (kilobytes > 1024) return String.format("%.1f", (float) kilobytes / 1024) + " МБ";
        return kilobytes + " КБ";
    }

    public static long safeSize(Path file) {
        try {
            return Files.size(file);
        } catch (IOException e) {
            return 0;
        }
    }

    public static boolean compareSize(Path file, Resource resource) {
        return safeSize(file) != resource.getSize();
    }

    public static void deleteDir(Path path) {
        deleteDir(path, (predicatePath) -> true);
    }

    public static void deleteDir(Path path, Predicate<Path> filter) {
        if (!Files.exists(path)) {
            return;
        }

        try (Stream<Path> files = Files.walk(path).filter(filter).sorted(Comparator.reverseOrder())) {
            files.forEach(file -> {
                try {
                    Files.delete(file);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }
}
