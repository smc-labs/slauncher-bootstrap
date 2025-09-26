package ru.smclabs.bootstrap;

import lombok.Getter;
import ru.smclabs.bootstrap.environment.Environment;
import ru.smclabs.bootstrap.service.GuiService;
import ru.smclabs.bootstrap.service.LauncherService;
import ru.smclabs.bootstrap.service.ResourcesService;
import ru.smclabs.bootstrap.util.report.BootstrapReportProvider;
import ru.smclabs.slauncher.http.HttpService;
import ru.smclabs.slauncher.resources.provider.DirProvider;
import ru.smclabs.slauncher.util.logger.ILogger;
import ru.smclabs.slauncher.util.logger.LoggerFactory;

@Getter
public class Bootstrap {

    private static @Getter Bootstrap instance;
    private static final @Getter BootstrapReportProvider reportProvider = new BootstrapReportProvider();

    private final ILogger logger;
    private final Environment environment;
    private final GuiService guiService;
    private final DirProvider dirProvider;
    private final HttpService httpService;
    private final ResourcesService resourcesService;
    private final LauncherService launcherService;

    public Bootstrap() {
        instance = this;
        environment = new Environment();
        dirProvider = new DirProvider(environment.getDir());
        logger = LoggerFactory.create(dirProvider.getLogsDir(), "bootstrap");
        httpService = new HttpService(environment.getHttp(), logger);
        resourcesService = new ResourcesService(this);
        launcherService = new LauncherService(this);
        guiService = new GuiService(this);
    }

    public void start() {
        logger.info("Starting Bootstrap " + environment.getVersion());
        System.setProperty("http.agent", environment.getHttp().getUserAgent());
        System.setProperty("jna.tmpdir", dirProvider.getPersistenceDir("native").toString());

        guiService.postInit();
        resourcesService.createTask();
    }

    public void stop() {
        logger.info("Bootstrap closed. Bye-bye!");
        resourcesService.cancelTask();
    }
}
