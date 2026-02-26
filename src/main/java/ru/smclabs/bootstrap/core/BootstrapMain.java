package ru.smclabs.bootstrap.core;

import ru.smclabs.bootstrap.report.ReportProvider;
import ru.smclabs.system.instancelocker.InstanceLocker;

public class BootstrapMain {
    public static void main(String[] args) {
        BootstrapContext context = new BootstrapContext();

        InstanceLocker instanceLocker = new InstanceLocker(
                context.getDirProvider()
                        .getPersistenceDir("bootstrap")
                        .resolve("bootstrap.lock")
        );

        if (!instanceLocker.lock()) {
            System.exit(0);
            return;
        }

        System.exit(start(context, instanceLocker));
    }

    private static int start(BootstrapContext context, InstanceLocker instanceLocker) {
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

    private static void registerShutdownHook(Bootstrap bootstrap, InstanceLocker instanceLocker) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                bootstrap.stop();
            } finally {
                instanceLocker.unlock();
            }
        }, "bootstrap-shutdown-hook"));
    }
}
