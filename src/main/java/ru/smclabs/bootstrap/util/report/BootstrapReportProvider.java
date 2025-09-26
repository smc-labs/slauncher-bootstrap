package ru.smclabs.bootstrap.util.report;

import com.fasterxml.jackson.core.JsonProcessingException;
import ru.smclabs.bootstrap.Bootstrap;
import ru.smclabs.jacksonpack.Jackson;
import ru.smclabs.report.api.Report;
import ru.smclabs.report.api.provider.ReportException;
import ru.smclabs.report.impl.HttpReportProvider;
import ru.smclabs.slauncher.util.logger.ILogger;
import ru.smclabs.slauncher.util.logger.LoggerFactory;
import ru.smclabs.system.info.SystemInfo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class BootstrapReportProvider extends HttpReportProvider {

    private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
        private int counter = 0;

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "BootstrapReportProvider #" + counter++);
        }
    });

    @Override
    protected String reportToJson(Report report) throws ReportException {
        try {
            return Jackson.getMapper().writeValueAsString(report);
        } catch (JsonProcessingException e) {
            throw new ReportException(e);
        }
    }

    public void send(String context, Throwable error) {
        send(context, createReport(error));
    }

    public void send(String context, String reportPayload) {
        executor.execute(() -> {
            printToLogger(reportPayload);

            try {
                send(Report.of(Report.Type.LAUNCHER, context, reportPayload));
            } catch (ReportException re) {
                printToLogger(createReport(re));
            }
        });
    }

    public String createReport(Throwable error) {
        Bootstrap bootstrap = Bootstrap.getInstance();
        String os = SystemInfo.get().getName() + " (" + (SystemInfo.get().isX64() ? "x64" : "i586");
        String runtime = System.getProperty("java.home") + " (version: " + System.getProperty("java.vm.version") + ")";
        String storage = bootstrap == null ? "null" : String.valueOf(bootstrap.getDirProvider());
        String httpEnvironment = bootstrap == null ? "null" : String.valueOf(bootstrap.getEnvironment().getHttp());

        return "------------------------------------------------------------------------\n\n" +
                "    OS: " + os + ")\n\n" +
                "    Runtime: " + runtime + "\n\n" +
                "    Storage: " + storage + "\n\n" +
                "    HttpEnvironment: " + httpEnvironment + "\n" +
                "\n------------------------------------------------------------------------\n" +
                "\n" + LoggerFactory.throwableAsString(error);
    }

    private void printToLogger(String report) {
        Bootstrap bootstrap = Bootstrap.getInstance();
        if (bootstrap == null) {
            System.err.println(report);
            return;
        }

        ILogger logger = bootstrap.getLogger();
        if (logger == null) {
            System.err.println(report);
            return;
        }

        logger.warn(report);
    }
}
