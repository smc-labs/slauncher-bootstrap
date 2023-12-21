package ru.smclabs.bootstrap;

import ru.smclabs.bootstrap.util.RuntimeUtils;

import java.io.IOException;
import java.nio.file.Paths;

public class BootstrapMain {

    public static void main(String[] args) {
        if (switchToSystemRuntime()) {
            System.exit(0);
            return;
        }

        Bootstrap bootstrap = new Bootstrap();
        createShutdownHook(bootstrap);
        bootstrap.start();
    }

    private static void createShutdownHook(Bootstrap bootstrap) {
        Thread thread = new Thread(bootstrap::stop);
        thread.setName("ShutdownHook Thread");
        Runtime.getRuntime().addShutdownHook(thread);
    }

    private static boolean switchToSystemRuntime() {
        if (RuntimeUtils.isStartedByWrongPackagedJre()) {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.redirectErrorStream(true);
            processBuilder.command(Paths.get(System.getProperty("user.dir") + "/runtime/x64/bin/java").toString(),
                    "-jar", "Bootstrap.exe");

            try {
                processBuilder.start();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}
