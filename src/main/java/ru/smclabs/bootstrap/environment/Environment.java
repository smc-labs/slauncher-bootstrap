package ru.smclabs.bootstrap.environment;

import lombok.Getter;
import lombok.ToString;
import ru.smclabs.resources.provider.DirEnvironment;

@Getter
@ToString
public class Environment {

    private final String version = "1.0.3";
    private final GuiEnvironment gui = new GuiEnvironment();
    private final HttpEnvironment http = new HttpEnvironment(this.version);
    private final DirEnvironment dir = DirEnvironment.builder().persistenceDir("SIMPLEMINECRAFT").build();

}
