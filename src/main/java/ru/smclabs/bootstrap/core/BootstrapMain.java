package ru.smclabs.bootstrap.core;

import ru.smclabs.bootstrap.report.ReportProvider;
import ru.smclabs.system.instancelocker.InstanceLocker;

public class BootstrapMain {
    void main() {
        BootstrapContext context = new BootstrapContext();
        InstanceLocker instanceLocker = new BootstrapInstanceLocker(context.getDirProvider());

        if (!instanceLocker.lock()) {
            System.exit(0);
        }

        System.exit(runBootstrap(context, instanceLocker));
    }

    private int runBootstrap(BootstrapContext context, InstanceLocker instanceLocker) {
        try {
            Bootstrap bootstrap = new Bootstrap(context);
            registerShutdownHook(bootstrap, instanceLocker);
            bootstrap.start();
            return 0;
        } catch (Exception e) {
            ReportProvider.INSTANCE.send("Bootstrap starting", e);
            return 1;
        }
    }

    private void registerShutdownHook(Bootstrap bootstrap, InstanceLocker instanceLocker) {
        Runtime.getRuntime().addShutdownHook(
                Thread.ofPlatform()
                        .name("bootstrap-shutdown-hook")
                        .unstarted(() -> {
                            try {
                                bootstrap.stop();
                            } finally {
                                instanceLocker.unlock();
                            }
                        })
        );
    }
}
