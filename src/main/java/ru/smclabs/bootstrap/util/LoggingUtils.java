package ru.smclabs.bootstrap.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.*;

public class LoggingUtils {

    private static final Formatter FORMATTER = new Formatter() {

        private final Calendar calendar = new GregorianCalendar();
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

        @Override
        public String format(LogRecord record) {
            this.calendar.setTimeInMillis(record.getMillis());
            return this.dateFormat.format(this.calendar.getTime()) + " [" + record.getLevel() + "]: "
                    + record.getMessage() + "\n" + throwableAsString(record.getThrown());
        }
    };

    public static ru.smclabs.bootstrap.util.logger.Logger create(Path dir, String name) {
        Logger logger = LogManager.getLogManager().getLogger("");

        for (Handler handler : logger.getHandlers()) {
            handler.setFormatter(FORMATTER);
        }

        try {
            FileHandler fileHandler = createFileHandler(Paths.get(dir + "/" + name + ".log"));
            logger.addHandler(fileHandler);
        } catch (IOException | SecurityException e) {
            logger.log(Level.SEVERE, "Failed to create FileHandler!", e);
        }

        return new ru.smclabs.bootstrap.util.logger.Logger(logger);
    }

    private static FileHandler createFileHandler(Path path) throws IOException {
        if (!Files.exists(path.getParent())) {
            Files.createDirectory(path.getParent());
        }

        FileHandler fileHandler = new FileHandler(path.toString(), true);
        fileHandler.setFormatter(FORMATTER);
        return fileHandler;
    }

    public static String throwableAsString(Throwable e) {
        if (e == null) return "";
        StringWriter buffer = new StringWriter();
        PrintWriter pw = new PrintWriter(buffer);
        e.printStackTrace(pw);

        /*
        if (e instanceof GameProcessFailedException) {
            buffer.append("\n------------------------------\n");
            buffer.append(" ***** Process output *****");
            buffer.append("\n------------------------------\n\n");
            buffer.append(((GameProcessFailedException) e).getProcessOutput());
        }
         */

        return buffer.toString();
    }
}
