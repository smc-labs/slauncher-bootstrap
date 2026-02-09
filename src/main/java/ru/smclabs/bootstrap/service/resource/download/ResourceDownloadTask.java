package ru.smclabs.bootstrap.service.resource.download;

import lombok.Getter;
import lombok.Setter;
import ru.smclabs.bootstrap.core.app.Bootstrap;
import ru.smclabs.bootstrap.service.resource.exception.ResourceNotCompleteException;
import ru.smclabs.bootstrap.service.resource.exception.ResourceServerException;
import ru.smclabs.bootstrap.service.resource.exception.ResourceWriteException;
import ru.smclabs.slauncher.http.HttpService;
import ru.smclabs.slauncher.http.environment.HttpEnvironment;
import ru.smclabs.slauncher.http.exception.HttpClientException;
import ru.smclabs.slauncher.http.exception.HttpServiceException;
import ru.smclabs.slauncher.logger.Logger;
import ru.smclabs.slauncher.resources.exception.ResourceException;
import ru.smclabs.slauncher.resources.type.Resource;
import ru.smclabs.slauncher.resources.util.FileUtils;

import javax.net.ssl.SSLException;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceDownloadTask {
    private final @Getter Resource resource;
    private final Path tempPath;

    private @Setter ResourceDownloadTaskStats stats;

    public ResourceDownloadTask(Resource resource) {
        this.resource = resource;
        tempPath = Paths.get(resource.getPath() + ".download");
    }

    public void run() throws ResourceWriteException, InterruptedException {
        prepareDir();

        try {
            downloadWithRetry(false);
        } catch (ResourceNotCompleteException e) {
            int errors = 0;

            while (true) {
                Bootstrap.getInstance()
                        .getLogger()
                        .info("Retry #" + errors + " file downloading: " + resource.getUrl());

                try {
                    downloadWithRetry(true);
                    break;
                } catch (ResourceNotCompleteException exception) {
                    if (errors++ == 10) {
                        throw new ResourceNotCompleteException(resource,
                                "Не удалось загрузить поврежденный файл: " + exception.getResource().getName());
                    }
                }
            }
        }

        if (resource instanceof AfterDownloadAction) {
            ((AfterDownloadAction) resource).afterDownload();
        }
    }

    private void prepareDir() {
        try {
            Files.createDirectories(tempPath.getParent());
        } catch (IOException e) {
            throw new ResourceException(e);
        }
    }

    private void downloadWithRetry(boolean append) throws ResourceWriteException, InterruptedException {
        HttpService httpService = Bootstrap.getInstance().getHttpService();
        try {
            download(append);
        } catch (ResourceServerException | HttpClientException e) {
            HttpEnvironment environment = httpService.getEnvironment();

            Logger logger = httpService.getLogger();
            logger.warn("Failed to send request to " + resource.getUrl() + "! " +
                    "(zone: ." + environment.getZone() + ", protocol: " + environment.getProtocol() + ")");

            logger.info("Search working zone...");

            for (int i = 0; i < environment.getZones().size(); i++) {
                environment.setZoneIndex(i);

                for (int j = 0; j < environment.getProtocols().size(); j++) {
                    environment.setProtocolIndex(j);
                    logger.info("Try zone: ." + environment.getZone() + ", protocol: " + environment.getProtocol());

                    try {
                        download(append);
                        logger.info("Found working zone: ." + environment.getZone()
                                + ", protocol: " + environment.getProtocol());
                        return;
                    } catch (HttpServiceException | ResourceServerException e1) {
                        e.addSuppressed(e1);
                    }
                }
            }
        }
    }

    private void download(boolean append) throws HttpClientException, ResourceServerException, ResourceWriteException, InterruptedException {
        HttpURLConnection connection = openConnection(append);

        try (BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream())) {
            try (FileOutputStream outputStream = new FileOutputStream(tempPath.toString(), append)) {
                byte[] buffer = new byte[1024];
                int bytesRead;

                while (true) {
                    try {
                        if ((bytesRead = inputStream.read(buffer, 0, buffer.length)) == -1) {
                            break;
                        }
                    } catch (SSLException e) {
                        throw new HttpClientException(e);
                    } catch (IOException e) {
                        throw new ResourceServerException(e);
                    }

                    try {
                        outputStream.write(buffer, 0, bytesRead);
                    } catch (IOException e) {
                        throw new ResourceWriteException(resource, e);
                    }

                    if (stats != null) {
                        stats.addReadBytes(bytesRead);
                    }

                    if (Thread.interrupted()) {
                        throw new InterruptedException("Resource download " + resource.getName() + " cancelled.");
                    }
                }
            } catch (IOException e) {
                throw new ResourceWriteException(resource, e);
            }
        } catch (IOException e) {
            throw new ResourceServerException(e);
        } finally {
            connection.disconnect();
        }

        if (Files.notExists(tempPath)) {
            throw new ResourceNotCompleteException(resource, "Resource not exists!");
        }

        try {
            if (Files.size(tempPath) < resource.getSize() && !resource.getName().endsWith(".js")) {
                throw new ResourceNotCompleteException(resource, "Files size not equals! (" +
                        Files.size(tempPath) + " vs " + resource.getSize() + ")");
            }
        } catch (IOException e) {
            throw new ResourceNotCompleteException(resource, e);
        }

        try {
            FileUtils.atomicMove(tempPath, resource.getPath(), 10);
        } catch (IOException e) {
            throw new ResourceException("Failed to move resource file!", e);
        }
    }

    private HttpURLConnection openConnection(boolean append) throws HttpClientException, ResourceServerException {
        HttpService httpService = Bootstrap.getInstance().getHttpService();
        URL url = httpService.createUrl(resource.getUrl());
        HttpURLConnection connection = httpService.openConnection(url, "GET");

        if (append) {
            try {
                connection.setRequestProperty("Range", "bytes=" + Files.size(tempPath) + "-" + resource.getSize());
            } catch (IOException e) {
                throw new ResourceException("Failed to check temp file size!", e);
            }
        }

        int responseCode;
        try {
            responseCode = connection.getResponseCode();
        } catch (IOException e) {
            throw new ResourceServerException("Failed to get response code from server!", e);
        }

        if (responseCode == 416) {
            try {
                Files.deleteIfExists(tempPath);
            } catch (IOException e) {
                throw new ResourceException("Failed to remove temp file!", e);
            }

            return httpService.openConnection(url, "GET");
        }

        return connection;
    }

}
