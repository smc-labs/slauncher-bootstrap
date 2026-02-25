package ru.smclabs.bootstrap.report;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.smclabs.bootstrap.core.Bootstrap;
import ru.smclabs.jacksonpack.Jackson;
import ru.smclabs.report.dto.Report;
import ru.smclabs.report.dto.ReportType;
import ru.smclabs.report.http.HttpReportProvider;
import ru.smclabs.report.provider.ReportException;
import ru.smclabs.system.info.arch.ArchType;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportProvider extends HttpReportProvider {
    private static final Logger log = LoggerFactory.getLogger(ReportProvider.class);

    public static final ReportProvider INSTANCE = new ReportProvider();

    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("BootstrapReportProvider");
        return thread;
    });

    private @Nullable Bootstrap bootstrap;

    private ReportProvider() {
    }

    public void setBootstrap(@Nullable Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public void send(String context, Throwable error) {
        send(context, createReport(error));
    }

    public void send(String context, String reportPayload) {
        executor.execute(() -> {
            log.error(reportPayload);
            try {
                send(Report.of(ReportType.LAUNCHER, context, reportPayload));
            } catch (ReportException re) {
                log.error("Failed to send report: {}", createReport(re));
            }
        });
    }

    public String createReport(Throwable error) {
        return """
        ------------------------------------------------------------------------

            OS: %s

            JRE: %s

            Storage: %s

            HttpEnvironment: %s

        ------------------------------------------------------------------------
        %s
        """.formatted(
                getOperatingSystemInfo(),
                getJavaInfo(),
                getStorageInfo(),
                getHttpInfo(),
                stacktraceToString(error)
        );
    }

    @Override
    protected String reportToJson(Report report) throws ru.smclabs.report.provider.ReportException {
        try {
            return Jackson.getMapper().writeValueAsString(report);
        } catch (JsonProcessingException e) {
            throw new ReportException(e);
        }
    }

    private String getOperatingSystemInfo() {
        return System.getProperty("os.name", "Unknown") + " (" + ArchType.current().bitness() + ")";
    }

    private String getJavaInfo() {
        return String.format("%s %s (%s, build %s)",
                System.getProperty("java.runtime.name"),
                System.getProperty("java.runtime.version"),
                System.getProperty("java.vm.vendor"),
                System.getProperty("java.vm.version")
        );
    }

    private String getStorageInfo() {
        if (bootstrap == null) return "null";
        return String.valueOf(bootstrap.getContext().getWorkingDir());
    }

    private String getHttpInfo() {
        if (bootstrap == null) return "null";
        return String.valueOf(bootstrap.getEnvironment().getHttp());
    }

    private String stacktraceToString(Throwable error) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        error.printStackTrace(pw);
        return sw.toString();
    }
}
