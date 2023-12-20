package ru.smclabs.bootstrap.service.resource.download;

import lombok.Getter;
import lombok.Setter;
import ru.smclabs.bootstrap.Bootstrap;
import ru.smclabs.bootstrap.environment.HttpEnvironment;
import ru.smclabs.bootstrap.service.HttpService;
import ru.smclabs.bootstrap.service.http.exception.HttpClientException;
import ru.smclabs.bootstrap.service.resource.exception.ResourceNotCompleteException;
import ru.smclabs.bootstrap.service.resource.exception.ResourceServerException;
import ru.smclabs.bootstrap.service.resource.exception.ResourceWriteException;
import ru.smclabs.bootstrap.util.logger.Logger;
import ru.smclabs.resources.exception.ResourceException;
import ru.smclabs.resources.type.Resource;

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
        this.tempPath = Paths.get(resource.getPath() + ".download");
    }

    public void run() throws ResourceWriteException, HttpClientException, ResourceServerException, InterruptedException {
        this.prepareDir();

        try {
            this.downloadWithRetry(false);
        } catch (ResourceNotCompleteException e) {
            int errors = 0;

            while (true) {
                Bootstrap.getInstance()
                        .getLogger()
                        .info("Retry #" + errors + " file downloading: " + this.resource.getUrl());

                try {
                    this.downloadWithRetry(true);
                    break;
                } catch (ResourceNotCompleteException exception) {
                    if (errors++ == 10) {
                        throw new ResourceNotCompleteException(this.resource,
                                "Не удалось загрузить поврежденный файл: " + exception.getResource().getName());
                    }
                }
            }
        }

        if (this.resource instanceof IHasAfterDownloadAction) {
            ((IHasAfterDownloadAction) this.resource).runAction();
        }
    }

    private void prepareDir() {
        if (!Files.exists(this.tempPath.getParent())) {
            try {
                Files.createDirectories(this.tempPath.getParent());
            } catch (IOException e) {
                throw new ResourceException(e);
            }
        }
    }

    private void downloadWithRetry(boolean append) throws ResourceWriteException, HttpClientException, ResourceServerException, InterruptedException {
        HttpService httpService = Bootstrap.getInstance().getHttpService();
        HttpEnvironment environment = httpService.getEnvironment();
        Logger logger = httpService.getLogger();

        try {
            this.download(append);
        } catch (ResourceServerException | HttpClientException e) {
            logger.error("Failed to send request to " + this.resource.getUrl(), e);

            if (environment.isProtocolChanged() && environment.isZoneChanged()) {
                logger.info("Reset domain and protocol to default...");
                environment.changeProtocol("https");
                environment.changeZone("ru");
                throw e;
            } else if (!environment.isProtocolChanged()) {
                logger.info("Change working protocol to: HTTP");
                environment.changeProtocol("http");
                this.download(append);
            }

            logger.info("Change working protocol to: HTTPS");
            logger.info("Change working domain to: NET");
            environment.changeProtocol("https");
            environment.changeZone("net");

            this.download(append);
        }
    }

    private void download(boolean append) throws HttpClientException, ResourceServerException, ResourceWriteException, InterruptedException {
        HttpURLConnection connection = this.openConnection(append);

        try (BufferedInputStream connectionInputStream = new BufferedInputStream(connection.getInputStream())) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(this.tempPath.toString(), append)) {
                byte[] buffer = new byte[1024];
                int bytesRead;

                while (true) {
                    try {
                        if ((bytesRead = connectionInputStream.read(buffer, 0, 1024)) == -1) {
                            break;
                        }
                    } catch (IOException e) {
                        throw new ResourceServerException(e);
                    }

                    try {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    } catch (IOException e) {
                        throw new ResourceWriteException(this.resource, e);
                    }

                    if (this.stats != null) {
                        this.stats.addReadBytes(bytesRead);
                    }

                    if (Thread.interrupted()) {
                        throw new InterruptedException("Resource download " + this.resource.getName() + " cancelled.");
                    }
                }
            } catch (IOException e) {
                throw new ResourceWriteException(this.resource, e);
            }
        } catch (IOException e) {
            throw new ResourceServerException(e);
        } finally {
            connection.disconnect();
        }

        if (!Files.exists(this.tempPath)) {
            throw new ResourceNotCompleteException(this.resource, "Resource not exists!");
        }

        if (!Files.exists(this.tempPath)) {
            try {
                if (Files.size(this.tempPath) < this.resource.getSize() && !this.resource.getName().endsWith(".js")) {
                    throw new ResourceNotCompleteException(this.resource, "Files size not equals! (" +
                            Files.size(this.tempPath) + " vs " + this.resource.getSize() + ")");
                }
            } catch (IOException e) {
                throw new ResourceNotCompleteException(this.resource, e);
            }
        }

        try {
            Files.deleteIfExists(this.resource.getPath());
            Files.move(this.tempPath, this.resource.getPath());
        } catch (IOException e) {
            throw new ResourceException("Failed to move resource file!", e);
        }
    }

    private HttpURLConnection openConnection(boolean append) throws HttpClientException, ResourceServerException {
        HttpService httpService = Bootstrap.getInstance().getHttpService();
        URL url = httpService.createUrl(this.resource.getUrl());
        HttpURLConnection connection = httpService.openConnection(url, "GET");

        if (append) {
            try {
                connection.setRequestProperty("Range", "bytes=" + Files.size(this.tempPath) + "-" + this.resource.getSize());
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
                Files.deleteIfExists(this.tempPath);
            } catch (IOException e) {
                throw new ResourceException("Failed to remove temp file!", e);
            }

            return httpService.openConnection(url, "GET");
        }

        return connection;
    }

}
