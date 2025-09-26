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
        processOutput = new StringBuilder();
    }

    public void track() throws LauncherProcessFailedException, InterruptedException {
        try (InputStreamReader isr = new InputStreamReader(launcherProcess.getProcess().getInputStream());
             BufferedReader reader = new BufferedReader(isr)) {

            String line = reader.readLine();
            while (line != null) {
                updateTask.checkIfCancelled();
                if (checkIfStarted(line)) break;
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new LauncherProcessFailedException(e, processOutput());
        } finally {
            if (processFailed) {
                launcherProcess.destroy();
            }
        }

        if (processFailed) throw new LauncherProcessFailedException(processOutput());
    }

    private boolean checkIfStarted(String line) {
        processOutput.append(line).append("\n");

        if (line.contains("Starting SLauncher")) {
            Bootstrap.getInstance().getLogger().info("Launcher process started.");
            processFailed = false;
            return true;
        }

        return false;
    }

    private String processOutput() {
        return processOutput.length() == 0 ? null : processOutput.toString();
    }
}
