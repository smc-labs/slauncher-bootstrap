package ru.smclabs.bootstrap.environment;

import lombok.Getter;
import lombok.ToString;
import ru.smclabs.slauncher.http.environment.impl.SLauncherHttpEnvironment;
import ru.smclabs.slauncher.resources.provider.DirEnvironment;
import ru.smclabs.system.info.SystemInfo;

@Getter
@ToString
public class Environment {

    private final String version = "1.2.1";
    private final GuiEnvironment gui = new GuiEnvironment();
    private final SLauncherHttpEnvironment http = new SLauncherHttpEnvironment(this.version, SystemInfo.get().getName());
    private final DirEnvironment dir = DirEnvironment.builder().persistenceDir("SIMPLEMINECRAFT").build();

}
