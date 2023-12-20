package ru.smclabs.bootstrap.service.resource;

import ru.smclabs.resources.exception.ResourceException;
import ru.smclabs.resources.provider.DirProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResourcesFinder {

    private final DirProvider dirProvider;

    public ResourcesFinder(DirProvider dirProvider) {
        this.dirProvider = dirProvider;
    }

    public static List<Path> findFilesByExtension(Path dir, String... extensions) {
        return findFilesByPredicate(dir, ((filePath, attributes) -> {
            if (attributes.isRegularFile()) {
                for (String extension : extensions) {
                    if (extension.equals("all")) return true;
                    if (filePath.getFileName().toString().endsWith(extension)) return true;
                }
            }

            return false;
        }));
    }

    public static List<Path> findFilesByPredicate(Path dir, BiPredicate<Path, BasicFileAttributes> predicate) {
        if (!Files.exists(dir)) return Collections.emptyList();

        try (Stream<Path> stream = Files.find(dir, Integer.MAX_VALUE, predicate)) {
            return stream.collect(Collectors.toList());
        } catch (IOException e) {
            throw new ResourceException(e);
        }
    }

}
