package ru.smclabs.bootstrap.core;

import org.jetbrains.annotations.NotNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.smclabs.bootstrap.gui.manager.GuiManager;
import ru.smclabs.bootstrap.http.BootstrapHttpService;
import ru.smclabs.bootstrap.process.repository.ProcessRefRepository;
import ru.smclabs.bootstrap.process.starter.LauncherProcessStarter;
import ru.smclabs.bootstrap.report.ReportProvider;
import ru.smclabs.bootstrap.update.UpdateTask;
import ru.smclabs.bootstrap.update.manager.UpdateManager;
import ru.smclabs.system.instancelocker.InstanceLocker;

import java.io.IOException;

@NotNullByDefault
public class Bootstrap {
    private static final Logger log = LoggerFactory.getLogger(Bootstrap.class);

    private final BootstrapContext context;
    private final BootstrapEnvironment environment;
    private final GuiManager guiManager;
    private final LauncherProcessStarter launcherProcessStarter;
    private final UpdateManager updateManager;

    public Bootstrap(BootstrapContext context) {
        this.context = context;
        environment = new BootstrapEnvironment();
        guiManager = new GuiManager(context.getDirProvider());

        ProcessRefRepository processRefStorage = new ProcessRefRepository(context.getDirProvider());

        launcherProcessStarter = new LauncherProcessStarter(
                guiManager.getUpdateViewController(),
                processRefStorage,
                context.getDirProvider()
        );

        updateManager = new UpdateManager(
                context.getDirProvider(),
                new BootstrapHttpService(this),
                processRefStorage,
                launcherProcessStarter,
                guiManager.getUpdateViewController()
        );

        ReportProvider.INSTANCE.setBootstrap(this);
    }

    public void start() {
        log.info("Starting Bootstrap {}", environment.getVersion());
        initProperties();

        guiManager.start();

        try {
            UpdateTask task = updateManager.runUpdate();
            task.join();

            launcherProcessStarter.start();
        } catch (InterruptedException e) {
            log.error("Resources update cancelled", e);
        } catch (IOException e) {
            ReportProvider.INSTANCE.send("Failed to start launcher", e);
        }
    }

    public void stop() {
        updateManager.cancelTask();
        log.info("Bootstrap closed. Bye-bye!");
    }

    public BootstrapEnvironment getEnvironment() {
        return environment;
    }

    public BootstrapContext getContext() {
        return context;
    }

    private void initProperties() {
        System.setProperty("http.agent", environment.getHttp().getUserAgent());
        System.setProperty("jna.tmpdir", context.getDirProvider().getPersistenceDir("native").toString());
        System.setProperty("java.io.tmpdir", context.getDirProvider().getPersistenceDir("temp").toString());
    }
}
