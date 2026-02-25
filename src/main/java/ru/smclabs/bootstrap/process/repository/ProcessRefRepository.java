package ru.smclabs.bootstrap.process.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.smclabs.bootstrap.process.ProcessRef;
import ru.smclabs.jacksonpack.Jackson;
import ru.smclabs.slauncher.resources.provider.DirProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ProcessRefRepository {
    private static final Logger log = LoggerFactory.getLogger(ProcessRefRepository.class);

    private final Path storageDir;

    public ProcessRefRepository(DirProvider dirProvider) {
        storageDir = dirProvider.getPersistenceDir("data/process");
    }

    public void save(ProcessRef ref) throws IOException {
        byte[] bytes = Jackson.getMapper().writeValueAsBytes(ref);

        Path refPath = resolveRefPath(ref);

        Files.createDirectories(refPath.getParent());

        Files.write(
                refPath,
                bytes,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    public void delete(ProcessRef ref) throws IOException {
        Files.deleteIfExists(resolveRefPath(ref));
    }

    public void deleteQuietly(ProcessRef ref) {
        try {
            delete(ref);
        } catch (IOException ignored) {
        }
    }

    public void deleteWithDestroy() {
        List<ProcessRef> refs = loadRefs();

        for (ProcessRef ref : refs) {
            ref.destroy();
            deleteQuietly(ref);
        }
    }

    private Path resolveRefPath(ProcessRef ref) {
        String filename = ref.getPid() + ".json";
        return storageDir.resolve("launcher").resolve(filename);
    }

    private List<ProcessRef> loadRefs() {
        List<ProcessRef> refs = new ArrayList<>();

        try (Stream<Path> stream = Files.find(storageDir, 2, this::processRefFilter)) {
            stream.forEach(path -> {
                try {
                    ProcessRef ref = Jackson.getMapper().readValue(
                            Files.readAllBytes(path),
                            ProcessRef.class
                    );

                    refs.add(ref);
                } catch (IOException e) {
                    log.error("Error reading ProcessRef from file {}", path.toAbsolutePath(), e);
                }
            });
        } catch (IOException e) {
            log.error("Error while reading ProcessRef files", e);
        }

        return refs;
    }

    private boolean processRefFilter(Path path, BasicFileAttributes attributes) {
        return attributes.isRegularFile() && path.getFileName().toString().endsWith(".json");
    }
}
