package ru.smclabs.bootstrap;

public class BootstrapMain {

    public static void main(String[] args) {
        Bootstrap bootstrap = new Bootstrap();
        createShutdownHook(bootstrap);
        bootstrap.start();
    }

    private static void createShutdownHook(Bootstrap bootstrap) {
        Thread thread = new Thread(bootstrap::stop);
        thread.setName("ShutdownHook Thread");
        Runtime.getRuntime().addShutdownHook(thread);
    }
}
