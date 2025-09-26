package ru.smclabs.bootstrap;

import ru.smclabs.bootstrap.util.RuntimeUtils;

import java.io.IOException;
import java.nio.file.Paths;

public class BootstrapMain {

    public static void main(String[] args) {
        System.out.println("Launcher is started");

        if (switchToSystemRuntime()) {
            System.exit(0);
            return;
        }

        try {
            Bootstrap bootstrap = new Bootstrap();
            createShutdownHook(bootstrap);
            bootstrap.start();
        } catch (Throwable e) {
            Bootstrap.getReportProvider().send("Bootstrap starting", e);
        }
    }

    private static void createShutdownHook(Bootstrap bootstrap) {
        Thread thread = new Thread(bootstrap::stop);
        thread.setName("ShutdownHook Thread");
        Runtime.getRuntime().addShutdownHook(thread);
    }

    private static boolean switchToSystemRuntime() {
        if (RuntimeUtils.isExecutableFileExtension("jar")) return false;

        if (RuntimeUtils.isStartedByWrongPackagedJre()) {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.redirectErrorStream(true);
            processBuilder.command(Paths.get(System.getProperty("user.dir") + "/runtime/x64/bin/java").toString(),
                    "-jar", "Bootstrap.exe");

            try {
                processBuilder.start();
                return true;
            } catch (IOException e) {
                Bootstrap.getReportProvider().send("Switch to bundle runtime", e);
            }
        }

        return false;
    }
}
