package ru.smclabs.bootstrap.environment;

import java.util.Arrays;

public class HttpEnvironment extends ru.smclabs.http.environment.HttpEnvironment {

    public HttpEnvironment(String version) {
        super(Arrays.asList("net", "ru"),
                "S-LAUNCHER@" + version,
                "slauncher.simpleminecraft.%zone%",
                "%slauncher-backend%");
    }
}
