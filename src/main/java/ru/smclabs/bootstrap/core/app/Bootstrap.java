package ru.smclabs.bootstrap.core.app;

import org.jetbrains.annotations.NotNullByDefault;
import ru.smclabs.bootstrap.core.context.BootstrapContext;
import ru.smclabs.bootstrap.core.environment.BootstrapEnvironment;
import ru.smclabs.bootstrap.gui.core.GuiService;
import ru.smclabs.bootstrap.http.BootstrapHttpService;
import ru.smclabs.slauncher.http.HttpService;
import ru.smclabs.slauncher.logger.Logger;

@NotNullByDefault
public class Bootstrap {
    private final BootstrapContext context;
    private final BootstrapEnvironment environment;
    private final GuiService guiService;
    private final HttpService httpService;
    //private final ResourcesService resourcesService;
    //private final LauncherService launcherService;

    public Bootstrap(BootstrapContext context) {
        this.context = context;
        environment = new BootstrapEnvironment();
        httpService = new BootstrapHttpService(this);
        guiService = new GuiService(this);
        //resourcesService = new ResourcesService(this);
        //launcherService = new LauncherService(this);
    }

    public void createShutdownHook() {
        Thread thread = new Thread(this::stop);
        thread.setName("ShutdownHook Thread");
        Runtime.getRuntime().addShutdownHook(thread);
    }

    public void start() {
        getLogger().info("Starting Bootstrap " + environment.getVersion());
        initProperties();
        guiService.postInit();
        //resourcesService.createTask();
    }

    public void stop() {
        getLogger().info("Bootstrap closed. Bye-bye!");

        //resourcesService.cancelTask();
    }

    public Logger getLogger() {
        return context.getLogger();
    }

    public BootstrapEnvironment getEnvironment() {
        return environment;
    }

    public BootstrapContext getContext() {
        return context;
    }

    public GuiService getGuiService() {
        return guiService;
    }

    public HttpService getHttpService() {
        return httpService;
    }

    private void initProperties() {
        System.setProperty("http.agent", environment.getHttp().getUserAgent());
        System.setProperty("jna.tmpdir", context.getDirProvider().getPersistenceDir("native").toString());
    }
}
