package ru.smclabs.bootstrap.service.launcher.process;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.smclabs.bootstrap.service.launcher.exception.LauncherServiceException;
import ru.smclabs.jacksonpack.Jackson;
import ru.smclabs.slauncher.resources.provider.DirProvider;
import ru.smclabs.system.exception.SystemException;
import ru.smclabs.system.process.ProcessActions;
import ru.smclabs.system.process.ProcessId;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LauncherProcess {

    @Getter
    @JsonProperty
    private UUID uuid;

    @Getter
    @JsonProperty
    private Long pid;

    private @JsonIgnore List<String> params = new ArrayList<>();
    private @JsonIgnore Process delegate;
    private @JsonIgnore Path file;

    public LauncherProcess(DirProvider dirProvider, Path executableBinary) {
        uuid = UUID.randomUUID();
        file = Paths.get(dirProvider.getPersistenceDir("data/process/launcher") + "/" + uuid + ".json");
        addParam(executableBinary.toString()).addSystemParam("puid", uuid);
    }

    public LauncherProcess(Path file) throws LauncherServiceException {
        readFromDisk(file);
    }

    protected Process getProcess() {
        return process;
    }

    public LauncherProcess addParam(String param) {
        if (process != null) throw new IllegalStateException("Game process is already created!");
        params.add(param);
        return this;
    }

    public LauncherProcess addSystemParam(String key, Object value) {
        return addParam("-D" + key + "=" + value);
    }

    public void start(DirProvider dirProvider) throws LauncherServiceException {
        ProcessBuilder builder = new ProcessBuilder(params);
        builder.directory(dirProvider.getPersistenceDir().toFile());
        builder.redirectErrorStream(true);
        builder.environment().put("_JAVA_OPTIONS", "");

        try {
            process = builder.start();
            pid = ProcessId.from(process);
            writeToDisk();
        } catch (Throwable e) {
            destroy();
            throw new LauncherServiceException("Failed to start process!", e);
        }
    }

    public void destroy() throws LauncherServiceException {
        try {
            if (process != null) process.destroy();
            else if (pid != null) ProcessActions.kill(pid);
        } catch (SystemException e) {
            throw new LauncherServiceException("Failed to destroy process!", e);
        } finally {
            removeFromDisk();
        }
    }

    public void removeFromDisk() throws LauncherServiceException {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new LauncherServiceException("Failed to remove launcher process data!", e);
        }
    }

    public void writeToDisk() throws LauncherServiceException {
        try {
            Files.write(file, Jackson.getMapper().writeValueAsString(this).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new LauncherServiceException("Failed to write launcher process data!", e);
        }
    }

    public void readFromDisk(Path path) throws LauncherServiceException {
        file = path;
        try {
            LauncherProcess process = Jackson.getMapper().readValue(path.toFile(), LauncherProcess.class);
            pid = process.getPid();
            uuid = process.getUuid();
        } catch (IOException e) {
            throw new LauncherServiceException("Failed to read launcher process data!", e);
        }
    }

}
