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
        this.environment = new Environment();
        this.dirProvider = new DirProvider(this.environment.getDir());
        this.logger = LoggerFactory.create(this.dirProvider.getLogsDir(), "bootstrap");
        this.httpService = new HttpService(this.environment.getHttp(), this.logger);
        this.resourcesService = new ResourcesService(this);
        this.launcherService = new LauncherService(this);
        this.guiService = new GuiService(this);
    }

    public void start() {
        this.logger.info("Starting Bootstrap " + this.environment.getVersion());
        System.setProperty("http.agent", this.environment.getHttp().getUserAgent());
        System.setProperty("jna.tmpdir", this.dirProvider.getPersistenceDir("native").toString());

        this.guiService.postInit();
        this.resourcesService.createTask();
    }

    public void stop() {
        this.logger.info("Bootstrap closed. Bye-bye!");
        this.resourcesService.cancelTask();
    }
}
