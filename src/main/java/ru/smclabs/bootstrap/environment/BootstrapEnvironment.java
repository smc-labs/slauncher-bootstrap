package ru.smclabs.bootstrap.environment;

import lombok.Getter;
import lombok.ToString;
import ru.smclabs.bootstrap.gui.environment.GuiEnvironment;
import ru.smclabs.slauncher.http.environment.impl.SLauncherHttpEnvironment;
import ru.smclabs.system.info.SystemInfo;

@Getter
@ToString
public class BootstrapEnvironment {
    private final String version = "2026.1";
    private final GuiEnvironment gui = new GuiEnvironment();
    private final SLauncherHttpEnvironment http = new SLauncherHttpEnvironment(
            "BOOTSTRAP-" + version, SystemInfo.get().getName()
    );
}
