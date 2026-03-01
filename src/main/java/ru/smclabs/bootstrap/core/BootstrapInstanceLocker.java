package ru.smclabs.bootstrap.core;

import ru.smclabs.slauncher.resources.provider.DirProvider;
import ru.smclabs.system.instancelocker.InstanceLocker;

public class BootstrapInstanceLocker extends InstanceLocker {
    public BootstrapInstanceLocker(DirProvider dirProvider) {
        super(dirProvider
                .getPersistenceDir("bootstrap")
                .resolve("bootstrap.lock")
        );
    }
}
