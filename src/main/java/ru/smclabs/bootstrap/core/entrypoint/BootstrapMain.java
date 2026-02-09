package ru.smclabs.bootstrap.core.entrypoint;

import ru.smclabs.bootstrap.core.app.Bootstrap;
import ru.smclabs.bootstrap.core.context.BootstrapContext;
import ru.smclabs.bootstrap.report.BootstrapReportProvider;
import ru.smclabs.bootstrap.util.RuntimeUtils;

import java.io.IOException;
import java.nio.file.Paths;

public class BootstrapMain {

    public static void main(String[] args) {
        BootstrapContext context = new BootstrapContext();

        if (switchToSystemRuntime()) {
            System.exit(0);
            return;
        }

        try {
            Bootstrap bootstrap = new Bootstrap(context);
            bootstrap.createShutdownHook();
            bootstrap.start();
        } catch (Exception e) {
            BootstrapReportProvider.INSTANCE.send("Bootstrap starting", e);
        }
    }

    private static boolean switchToSystemRuntime() {
        if (RuntimeUtils.isExecutableFileExtension("jar")) {
            return false;
        }

        if (RuntimeUtils.isStartedByWrongPackagedJre()) {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.redirectErrorStream(true);
            processBuilder.command(Paths.get(System.getProperty("user.dir") + "/runtime/x64/bin/java").toString(),
                    "-jar", "Bootstrap.exe");

            try {
                processBuilder.start();
                return true;
            } catch (IOException e) {
                BootstrapReportProvider.INSTANCE.send("Switch to bundle runtime", e);
            }
        }

        return false;
    }
}
