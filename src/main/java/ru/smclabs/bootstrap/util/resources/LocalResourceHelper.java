package ru.smclabs.bootstrap.util.resources;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;

/**
 * Вспомогательный класс для работы с локальными ресурсами приложения.
 * <p>
 * Предоставляет методы для удобной загрузки изображений и шрифтов,
 * находящихся в classpath (например, внутри JAR-файла).
 * </p>
 */
public class LocalResourceHelper {
    /**
     * Преобразует путь к ресурсу в объект {@link URL}.
     *
     * @param path путь к ресурсу (например, "/assets/icon.png")
     * @return {@link URL} указанного ресурса
     * @throws LocalResourceException если ресурс по указанному пути не найден
     */
    public static URL toUrl(String path) {
        URL url = LocalResourceHelper.class.getResource(path);
        if (url == null) throw new LocalResourceException("Resource not found: " + path);
        return url;
    }

    /**
     * Загружает изображение из ресурсов и масштабирует его до заданных размеров.
     * <p>
     * Для масштабирования используется алгоритм {@link Image#SCALE_SMOOTH}.
     * </p>
     *
     * @param path   путь к изображению в ресурсах
     * @param width  целевая ширина
     * @param height целевая высота
     * @return масштабированное изображение {@link Image}
     * @throws LocalResourceException если не удалось загрузить изображение
     */
    public static Image loadScaledImage(String path, int width, int height) {
        return readImage(path).getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }

    /**
     * Загружает шрифт с указанным именем и размером.
     * <p>
     * Метод пытается загрузить шрифт в следующем порядке:
     * <ol>
     *   <li>Из локальных ресурсов (директория {@code /assets/fonts/}).</li>
     *   <li>Из списка системных шрифтов (по точному или частичному совпадению имени).</li>
     *   <li>Возвращает логический шрифт по умолчанию, если предыдущие шаги не удались.</li>
     * </ol>
     * </p>
     *
     * @param name имя шрифта (без расширения .ttf для локальных ресурсов)
     * @param size размер шрифта
     * @return объект {@link Font}
     */
    public static Font loadFont(String name, float size) {
        Font font;
        try {
            font = readFont(name, size);
        } catch (Exception e) {
            font = findFont(name, size);
        }

        if (font == null) {
            font = new Font(null, Font.PLAIN, (int) size);
        }

        return font;
    }

    /**
     * Читает изображение из ресурсов по указанному пути.
     *
     * @param path путь к изображению
     * @return объект {@link BufferedImage}
     * @throws LocalResourceException при ошибке чтения изображения
     */
    private static BufferedImage readImage(String path) {
        try {
            return ImageIO.read(toUrl(path));
        } catch (Exception e) {
            throw new LocalResourceException("Failed to load local image!", e);
        }
    }

    /**
     * Создает шрифт из файла TrueType, расположенного в ресурсах.
     *
     * @param name имя файла шрифта (без расширения)
     * @param size размер шрифта
     * @return объект {@link Font}
     * @throws Exception при ошибке чтения или создания шрифта
     */
    private static Font readFont(String name, float size) throws Exception {
        try (InputStream is = toUrl("/assets/fonts/" + name + ".ttf").openStream()) {
            return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(size);
        }
    }

    /**
     * Ищет шрифт среди доступных системных шрифтов.
     *
     * @param name имя шрифта для поиска
     * @param size размер шрифта
     * @return найденный шрифт или {@code null}, если шрифт не найден
     */
    private static Font findFont(String name, float size) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] fonts = ge.getAllFonts();

        for (Font font : fonts) {
            if (font.getName().equals(name) || font.getName().startsWith(name)) {
                return font.deriveFont(size);
            }
        }

        return null;
    }
}
