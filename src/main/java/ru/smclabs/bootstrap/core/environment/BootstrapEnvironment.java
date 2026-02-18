package ru.smclabs.bootstrap.core.environment;

import ru.smclabs.slauncher.http.environment.impl.SLauncherHttpEnvironment;

public class BootstrapEnvironment {
    private final String version = "26.1.1";
    private final SLauncherHttpEnvironment http = new SLauncherHttpEnvironment(
            "BOOTSTRAP-" + version, System.getProperty("os.name", "Unknown")
    );

    public String getVersion() {
        return version;
    }

    public SLauncherHttpEnvironment getHttp() {
        return http;
    }

    @Override
    public String toString() {
        return "BootstrapEnvironment{" +
                "version='" + version + '\'' +
                ", http=" + http +
                '}';
    }
}
