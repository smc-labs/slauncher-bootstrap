package ru.smclabs.bootstrap.service.launcher.process;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.smclabs.bootstrap.service.launcher.exception.LauncherServiceException;
import ru.smclabs.jacksonpack.Jackson;
import ru.smclabs.processutils.ProcessUtils;
import ru.smclabs.processutils.exception.ProcessException;
import ru.smclabs.resources.provider.DirProvider;

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
    private @JsonIgnore Process process;
    private @JsonIgnore Path file;

    public LauncherProcess(DirProvider dirProvider, Path executableBinary) {
        this.uuid = UUID.randomUUID();
        this.file = Paths.get(dirProvider.getPersistenceDir("data/process/launcher") + "/" + this.uuid + ".json");
        this.param(executableBinary.toString()).systemParam("puid", this.uuid);
    }

    public LauncherProcess(Path file) throws LauncherServiceException {
        this.readFromDisk(file);
    }

    protected Process getProcess() {
        return this.process;
    }

    public LauncherProcess param(String param) {
        if (this.process != null) throw new IllegalStateException("Game process is already created!");
        this.params.add(param);
        return this;
    }

    public LauncherProcess systemParam(String key, Object value) {
        return this.param("-D" + key + "=" + value);
    }

    public void start(DirProvider dirProvider) throws LauncherServiceException {
        ProcessBuilder builder = new ProcessBuilder(this.params);
        builder.directory(dirProvider.getPersistenceDir().toFile());
        builder.redirectErrorStream(true);
        builder.environment().put("_JAVA_OPTIONS", "");

        try {
            this.process = builder.start();
            this.pid = ProcessUtils.getProcessId(this.process);
            this.writeToDisk();
        } catch (Throwable e) {
            this.destroy();
            throw new LauncherServiceException("Failed to start process!", e);
        }
    }

    public void destroy() throws LauncherServiceException {
        try {
            if (this.process != null) this.process.destroy();
            else if (this.pid != null) ProcessUtils.destroyProcessById(this.pid);
        } catch (ProcessException e) {
            throw new LauncherServiceException("Failed to destroy process!", e);
        } finally {
            this.removeFromDisk();
        }
    }

    public void removeFromDisk() throws LauncherServiceException {
        try {
            Files.deleteIfExists(this.file);
        } catch (IOException e) {
            throw new LauncherServiceException("Failed to remove launcher process data!", e);
        }
    }

    public void writeToDisk() throws LauncherServiceException {
        try {
            Files.write(this.file, Jackson.getMapper().writeValueAsString(this).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new LauncherServiceException("Failed to write launcher process data!", e);
        }
    }

    public void readFromDisk(Path path) throws LauncherServiceException {
        this.file = path;
        try {
            LauncherProcess process = Jackson.getMapper().readValue(path.toFile(), LauncherProcess.class);
            this.pid = process.getPid();
            this.uuid = process.getUuid();
        } catch (IOException e) {
            throw new LauncherServiceException("Failed to read launcher process data!", e);
        }
    }

}
