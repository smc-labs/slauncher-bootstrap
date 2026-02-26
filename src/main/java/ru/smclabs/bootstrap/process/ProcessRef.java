package ru.smclabs.bootstrap.process;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessRef {
    public static ProcessRef from(Process process) {
        return new ProcessRef(process.pid());
    }

    @JsonProperty
    private long pid;

    @JsonCreator
    public ProcessRef(@JsonProperty("pid") long pid) {
        this.pid = pid;
    }

    public long getPid() {
        return pid;
    }

    @JsonIgnore
    public Optional<ProcessHandle> getHandle() {
        return ProcessHandle.of(pid);
    }

    public void destroy() {
        getHandle().ifPresent(handle -> {
            if (canDestroy(handle)) {
                handle.destroy();

                try {
                    handle.onExit().get(5, TimeUnit.SECONDS);
                } catch (Exception ignored) {
                    handle.destroyForcibly();
                }
            }
        });
    }

    @Override
    public String toString() {
        return "ProcessRef{pid=" + pid + "}";
    }

    private boolean canDestroy(ProcessHandle handle) {
        return handle.isAlive()
                && handle.info().command().stream().anyMatch(command -> command.contains("java"));
    }
}
