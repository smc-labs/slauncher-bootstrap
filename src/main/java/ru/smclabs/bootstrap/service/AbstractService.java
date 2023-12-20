package ru.smclabs.bootstrap.service;

import lombok.Getter;
import ru.smclabs.bootstrap.Bootstrap;

@Getter
public class AbstractService {

    private final Bootstrap bootstrap;

    public AbstractService(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }
}
