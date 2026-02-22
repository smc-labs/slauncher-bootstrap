package ru.smclabs.bootstrap.core.app;

import org.jetbrains.annotations.NotNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.smclabs.bootstrap.core.context.BootstrapContext;
import ru.smclabs.bootstrap.core.environment.BootstrapEnvironment;
import ru.smclabs.bootstrap.gui.core.GuiService;
import ru.smclabs.bootstrap.http.BootstrapHttpService;
import ru.smclabs.bootstrap.report.ReportProvider;
import ru.smclabs.bootstrap.resources.service.ResourcesService;
import ru.smclabs.bootstrap.resources.task.UpdateTask;

@NotNullByDefault
public class Bootstrap {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    private final BootstrapContext context;
    private final BootstrapEnvironment environment;
    private final ResourcesService resourcesService;
    private final GuiService guiService;

    public Bootstrap(BootstrapContext context) {
        this.context = context;
        environment = new BootstrapEnvironment();
        guiService = new GuiService(context.getDirProvider());
        resourcesService = new ResourcesService(context.getDirProvider(), new BootstrapHttpService(this), guiService);
        ReportProvider.INSTANCE.setBootstrap(this);
    }

    public void registerShutdownHook() {
        Thread thread = new Thread(this::stop);
        thread.setName("ShutdownHook Thread");
        Runtime.getRuntime().addShutdownHook(thread);
    }

    public void start() {
        LOGGER.info("Starting Bootstrap {}", environment.getVersion());
        initProperties();

        guiService.start();

        try {
            UpdateTask task = resourcesService.runUpdate();
            task.join();
        } catch (InterruptedException e) {
            LOGGER.error("Resources update cancelled", e);
        }

        LOGGER.info("Bootstrap closed. Bye-bye!");
        System.exit(0);
    }

    public void stop() {
        resourcesService.cancelTask();
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
