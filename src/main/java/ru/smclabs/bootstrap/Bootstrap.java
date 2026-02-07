package ru.smclabs.bootstrap;

import lombok.Getter;
import ru.smclabs.bootstrap.environment.BootstrapEnvironment;
import ru.smclabs.bootstrap.gui.core.GuiService;
import ru.smclabs.bootstrap.http.BootstrapHttpService;
import ru.smclabs.bootstrap.service.LauncherService;
import ru.smclabs.bootstrap.service.ResourcesService;
import ru.smclabs.bootstrap.util.report.BootstrapReportProvider;
import ru.smclabs.slauncher.http.HttpService;
import ru.smclabs.slauncher.logger.Logger;

@Getter
public class Bootstrap {
    private static final @Getter BootstrapReportProvider reportProvider = new BootstrapReportProvider();

    private final BootstrapContext context;
    private final BootstrapEnvironment environment;
    private final GuiService guiService;
    private final HttpService httpService;
    private final ResourcesService resourcesService;
    private final LauncherService launcherService;

    public Bootstrap(BootstrapContext context) {
        this.context = context;
        environment = new BootstrapEnvironment();
        httpService = new BootstrapHttpService(this);
        resourcesService = new ResourcesService(this);
        launcherService = new LauncherService(this);
        guiService = new GuiService(this);
    }

    public void createShutdownHook() {
        Thread thread = new Thread(this::stop);
        thread.setName("ShutdownHook Thread");
        Runtime.getRuntime().addShutdownHook(thread);
    }

    public Logger getLogger() {
        return context.getLogger();
    }

    public void start() {
        getLogger().info("Starting Bootstrap " + environment.getVersion());
        initProperties();
        guiService.postInit();
        resourcesService.createTask();
    }

    private void initProperties() {
        System.setProperty("http.agent", environment.getHttp().getUserAgent());
        System.setProperty("jna.tmpdir", context.getDirProvider().getPersistenceDir("native").toString());
    }

    public void stop() {
        getLogger().info("Bootstrap closed. Bye-bye!");
        resourcesService.cancelTask();
    }
}
