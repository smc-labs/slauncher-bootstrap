package ru.smclabs.bootstrap.core.environment;

import ru.smclabs.bootstrap.gui.environment.GuiEnvironment;
import ru.smclabs.slauncher.http.environment.impl.SLauncherHttpEnvironment;
import ru.smclabs.system.info.SystemInfo;

public class BootstrapEnvironment {
    private final String version = "2026.1";
    private final GuiEnvironment gui = new GuiEnvironment();
    private final SLauncherHttpEnvironment http = new SLauncherHttpEnvironment(
            "BOOTSTRAP-" + version, SystemInfo.get().getName()
    );

    public String getVersion() {
        return version;
    }

    public GuiEnvironment getGui() {
        return gui;
    }

    public SLauncherHttpEnvironment getHttp() {
        return http;
    }

    @Override
    public String toString() {
        return "BootstrapEnvironment{" +
                "version='" + version + '\'' +
                ", gui=" + gui +
                ", http=" + http +
                '}';
    }
}
