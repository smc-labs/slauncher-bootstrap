package ru.smclabs.bootstrap.report;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.smclabs.bootstrap.core.app.Bootstrap;
import ru.smclabs.jacksonpack.Jackson;
import ru.smclabs.report.api.Report;
import ru.smclabs.report.api.provider.ReportException;
import ru.smclabs.report.impl.HttpReportProvider;
import ru.smclabs.slauncher.logger.LoggerFactory;
import ru.smclabs.system.info.SystemInfo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BootstrapReportProvider extends HttpReportProvider {
    public static final BootstrapReportProvider INSTANCE = new BootstrapReportProvider();

    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("BootstrapReportProvider");
        return thread;
    });

    private @Nullable Bootstrap bootstrap;

    public void setBootstrap(@Nullable Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public void send(String context, Throwable error) {
        send(context, createReport(error));
    }

    public void send(String context, String reportPayload) {
        executor.execute(() -> {
            print(reportPayload);
            try {
                send(Report.of(Report.Type.LAUNCHER, context, reportPayload));
            } catch (ReportException re) {
                print(createReport(re));
            }
        });
    }

    public String createReport(Throwable error) {
        return "------------------------------------------------------------------------\n\n" +
                "    OS: " + getOperatingSystemInfo() + ")\n\n" +
                "    JRE: " + getJavaInfo() + "\n\n" +
                "    Storage: " + getStorageInfo() + "\n\n" +
                "    HttpEnvironment: " + getHttpInfo() + "\n" +
                "\n------------------------------------------------------------------------\n" +
                "\n" + LoggerFactory.throwableAsString(error);
    }

    @Override
    protected String reportToJson(Report report) throws ReportException {
        try {
            return Jackson.getMapper().writeValueAsString(report);
        } catch (JsonProcessingException e) {
            throw new ReportException(e);
        }
    }

    private String getOperatingSystemInfo() {
        return SystemInfo.get().getName() + " (" + (SystemInfo.get().isX64() ? "x64" : "i586");
    }

    private String getJavaInfo() {
        return System.getProperty("java.home") + " (version: " + System.getProperty("java.vm.version") + ")";
    }

    private String getStorageInfo() {
        if (bootstrap == null) return "null";
        return String.valueOf(bootstrap.getContext().getWorkingDir());
    }

    private String getHttpInfo() {
        if (bootstrap == null) return "null";
        return String.valueOf(bootstrap.getEnvironment().getHttp());
    }

    private void print(@NotNull String report) {
        if (bootstrap == null) {
            System.err.println(report);
            return;
        }

        bootstrap.getLogger().warn(report);
    }
}
