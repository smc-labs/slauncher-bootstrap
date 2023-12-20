package ru.smclabs.bootstrap;

import lombok.Getter;
import ru.smclabs.bootstrap.environment.Environment;
import ru.smclabs.bootstrap.service.GuiService;
import ru.smclabs.bootstrap.util.LoggingUtils;
import ru.smclabs.bootstrap.util.RuntimeUtils;
import ru.smclabs.bootstrap.util.logger.Logger;
import ru.smclabs.resources.provider.DirProvider;

@Getter
public class Bootstrap {

    private static @Getter Bootstrap instance;

    private final Logger logger;
    private final Environment environment;
    private final GuiService guiService;
    private final DirProvider dirProvider;

    public Bootstrap() {
        instance = this;
        this.environment = new Environment();
        this.dirProvider = new DirProvider(this.environment.getDir());
        this.logger = LoggingUtils.create(this.dirProvider.getLogsDir(), "bootstrap");
        this.guiService = new GuiService(this);
    }

    public void start() {
        this.logger.info("Starting Bootstrap " + this.environment.getVersion());
        RuntimeUtils.setupUTF8();
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("http.agent", this.environment.getHttp().getUserAgent());

        this.guiService.postInit();
    }

    public void stop() {
        this.logger.info("Bootstrap closed. Bye-bye!");
    }

}
