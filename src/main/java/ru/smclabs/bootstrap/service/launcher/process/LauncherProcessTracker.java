package ru.smclabs.bootstrap.service.launcher.process;

import ru.smclabs.bootstrap.Bootstrap;
import ru.smclabs.bootstrap.service.launcher.exception.LauncherProcessFailedException;
import ru.smclabs.bootstrap.service.resource.ResourcesUpdateTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LauncherProcessTracker {

    private final StringBuilder processOutput;
    private final ResourcesUpdateTask updateTask;
    private final LauncherProcess launcherProcess;

    private boolean processFailed = true;

    public LauncherProcessTracker(ResourcesUpdateTask updateTask, LauncherProcess launcherProcess) {
        this.updateTask = updateTask;
        this.launcherProcess = launcherProcess;
        this.processOutput = new StringBuilder();
    }

    public void track() throws LauncherProcessFailedException, InterruptedException {
        try (InputStreamReader isr = new InputStreamReader(this.launcherProcess.getProcess().getInputStream());
             BufferedReader reader = new BufferedReader(isr)) {

            String line = reader.readLine();
            while (line != null) {
                this.updateTask.checkIfCancelled();
                if (this.checkIfStarted(line)) break;
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new LauncherProcessFailedException(e, this.processOutput());
        } finally {
            if (this.processFailed) {
                this.launcherProcess.destroy();
            }
        }

        if (this.processFailed) throw new LauncherProcessFailedException(this.processOutput());
    }

    private boolean checkIfStarted(String line) {
        this.processOutput.append(line).append("\n");

        if (line.contains("Starting SLauncher")) {
            Bootstrap.getInstance().getLogger().info("Launcher process started.");
            this.processFailed = false;
            return true;
        }

        return false;
    }

    private String processOutput() {
        return this.processOutput.length() == 0 ? null : this.processOutput.toString();
    }
}
