package ru.smclabs.bootstrap.update;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.smclabs.bootstrap.gui.controller.UpdateViewController;
import ru.smclabs.bootstrap.http.request.FetchResourcesRequest;
import ru.smclabs.bootstrap.process.repository.ProcessRefRepository;
import ru.smclabs.bootstrap.process.starter.LauncherStarter;
import ru.smclabs.bootstrap.report.ReportProvider;
import ru.smclabs.bootstrap.update.resource.BootstrapResourcesFactory;
import ru.smclabs.bootstrap.update.resource.model.BootstrapResources;
import ru.smclabs.bootstrap.update.resource.model.ResourcesPack;
import ru.smclabs.slauncher.http.HttpService;
import ru.smclabs.slauncher.http.exception.HttpServiceException;
import ru.smclabs.slauncher.resources.compressed.resource.ResourceCompressed;
import ru.smclabs.slauncher.resources.downloader.Downloader;
import ru.smclabs.slauncher.resources.downloader.http.HttpDownloader;
import ru.smclabs.slauncher.resources.downloader.stats.StatsCollector;
import ru.smclabs.slauncher.resources.provider.DirProvider;
import ru.smclabs.slauncher.resources.type.Resource;
import ru.smclabs.slauncher.resources.type.ResourceStruct;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Задача, отвечающая за проверку и обновление ресурсов бутстрапа.
 * Выполняет получение информации о ресурсах с сервера, валидацию локальных файлов,
 * загрузку отсутствующих или повреждённых файлов и распаковку архивов.
 */
public class UpdateTask {
    private static final Logger log = LoggerFactory.getLogger(UpdateTask.class);

    private final HttpService httpService;
    private final ProcessRefRepository processRefStorage;
    private final LauncherStarter launcherStarter;
    private final UpdateViewController viewController;
    private final BootstrapResourcesFactory factory;
    private final Thread worker;

    /**
     * Создаёт новый экземпляр задачи обновления с необходимыми зависимостями.
     *
     * @param httpService HTTP-сервис для выполнения сетевых запросов
     * @param dirProvider Провайдер директорий для работы с файловой системой
     * @param processRefStorage Репозиторий для управления ссылками на процессы лаунчера
     * @param viewController Контроллер для обновления GUI в процессе загрузки
     * @param launcherStarter Стартер для запуска процесса лаунчера после обновления
     */
    public UpdateTask(
            HttpService httpService,
            DirProvider dirProvider,
            ProcessRefRepository processRefStorage,
            UpdateViewController viewController,
            LauncherStarter launcherStarter
    ) {
        this.httpService = httpService;
        this.processRefStorage = processRefStorage;
        this.viewController = viewController;
        this.launcherStarter = launcherStarter;
        this.factory = new BootstrapResourcesFactory(dirProvider);
        this.worker = createThread();
    }

    /**
     * Запускает задачу обновления в фоновом потоке.
     * Метод возвращается сразу после запуска рабочего потока.
     */
    public void start() {
        worker.start();
    }

    /**
     * Отменяет задачу обновления путём прерывания рабочего потока.
     * Все активные операции загрузки будут прерваны.
     */
    public void cancel() {
        worker.interrupt();
    }

    /**
     * Ожидает завершения задачи обновления.
     *
     * @throws InterruptedException если текущий поток был прерван во время ожидания
     */
    public void join() throws InterruptedException {
        worker.join();
    }

    /**
     * Проверяет, не была ли отменена задача обновления.
     *
     * @return true, если задача всё ещё выполняется; false, если была отменена
     */
    public boolean isNotCancelled() {
        return !worker.isInterrupted();
    }

    /**
     * Создаёт рабочий поток для выполнения процесса обновления.
     *
     * @return незапущенный платформенный поток, настроенный для задачи обновления
     */
    private Thread createThread() {
        return Thread.ofPlatform()
                .name("update-task-worker")
                .unstarted(this::runUpdate);
    }

    /**
     * Основной цикл выполнения обновления с автоматическим повтором при ошибках.
     * Получает ресурсы с сервера, валидирует их, загружает недостающие
     * и подготавливает стартер процесса лаунчера. При ошибках показывает
     * диалог пользователю и повторяет попытку по запросу.
     */
    private void runUpdate() {
        int attempt = 1;

        while (isNotCancelled()) {
            try {
                log.info("Update attempt #{} started", attempt);

                ResourcesPack pack = fetchResources();
                updateResources(findInvalidResources(pack));
                launcherStarter.setPack(pack);

                cancel();
                return;
            } catch (InterruptedIOException e) {
                log.info("Update attempt {} cancelled: {}", attempt, e.getMessage());

                cancel();
                return;
            } catch (Exception e) {
                ReportProvider.INSTANCE.send("Bootstrap update failed", e);

                if (viewController.showError(e)) {
                    cancel();
                    return;
                }

                attempt++;
            }
        }
    }

    /**
     * Получает информацию о ресурсах бутстрапа с удалённого сервера.
     *
     * @return пакет ресурсов, содержащий все необходимые для бутстрапа файлы
     * @throws HttpServiceException если сетевой запрос завершился ошибкой
     * @throws JsonProcessingException если не удалось разобрать ответ сервера
     */
    private ResourcesPack fetchResources() throws HttpServiceException, JsonProcessingException {
        FetchResourcesRequest request = new FetchResourcesRequest(httpService);
        BootstrapResources dto = request.execute();
        return factory.build(dto);
    }

    /**
     * Фильтрует ресурсы, чтобы найти те, которые отсутствуют или повреждены локально.
     *
     * @param pack пакет ресурсов для валидации
     * @return список ресурсов, которые необходимо скачать или обновить
     */
    private List<Resource> findInvalidResources(ResourcesPack pack) {
        return pack.getResources()
                .stream()
                .filter(Resource::isInvalid)
                .collect(Collectors.toList());
    }

    /**
     * Скачивает и устанавливает указанные ресурсы.
     * Перед обновлением завершает все запущенные процессы лаунчера,
     * отслеживает прогресс загрузки через view controller,
     * а после загрузки распаковывает сжатые архивы.
     *
     * @param resources список ресурсов для скачивания и установки
     * @throws IOException если произошла ошибка при загрузке или файловой операции
     */
    private void updateResources(List<Resource> resources) throws IOException {
        if (resources.isEmpty()) {
            return;
        }

        viewController.setTitles("Обновление", "Остановка процессов лаунчера...");
        processRefStorage.deleteWithDestroy();

        List<ResourceCompressed> archives = new ArrayList<>();

        try (StatsCollector stats = new StatsCollector(resources.size())) {
            stats.onStart(resources.stream().mapToLong(ResourceStruct::getSize).sum());
            stats.addProgressListener(viewController);
            stats.addSpeedListener(viewController);
            stats.addTimeListener(viewController);

            viewController.setSubTitle("Скачивание файлов...");
            viewController.showDownloadingStats();

            Downloader downloader = new HttpDownloader(
                    httpService.getEnvironment(),
                    stats
            );

            for (Resource resource : resources) {
                stats.onNextFile();
                viewController.handleFileName(resource.getName());
                downloader.download(resource);

                if (resource instanceof ResourceCompressed) {
                    archives.add((ResourceCompressed) resource);
                }
            }
        } finally {
            viewController.hideDownloadingStats();
        }

        extractArchives(archives);
    }

    /**
     * Распаковывает все загруженные сжатые архивы.
     *
     * @param archives список сжатых ресурсов для распаковки
     * @throws IOException если произошла ошибка при распаковке
     */
    private void extractArchives(List<ResourceCompressed> archives) throws IOException {
        if (!archives.isEmpty()) {
            viewController.setSubTitle("Установка обновления...");

            for (ResourceCompressed archive : archives) {
                archive.extract();
            }
        }
    }
}
