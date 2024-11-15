package backend.academy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogFileReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogFileReader.class);

    /**
     * Читает лог-файлы и передает их записи в StatisticsCollector.
     *
     * @param pathPattern    Шаблон пути к файлам или URL
     * @param parser         Экземпляр LogParser для разбора строк
     * @param statsCollector Экземпляр StatisticsCollector для сбора статистики
     * @param fromStr        Начальная дата фильтрации в формате ISO8601 (может быть null)
     * @param toStr          Конечная дата фильтрации в формате ISO8601 (может быть null)
     * @throws Exception Если возникает ошибка при чтении файлов
     */
    public void readLogs(String pathPattern, LogParser parser, StatisticsCollector statsCollector,
        String fromStr, String toStr, String filterField, String filterValue) throws Exception {
        ZonedDateTime fromTime = fromStr != null ? ZonedDateTime.parse(fromStr) : null;
        ZonedDateTime toTime = toStr != null ? ZonedDateTime.parse(toStr) : null;

        Consumer<LogRecord> filterConsumer = logEntry -> {
            boolean matches = true;
            if (filterField != null && filterValue != null) {
                String fieldValue = getFieldValue(logEntry, filterField);
                if (fieldValue == null) {
                    matches = false;
                } else {
                    String regex = filterValue.replace("*", ".*");
                    matches = fieldValue.matches(regex);
                }
            }
            if (matches) {
                statsCollector.collect(logEntry);
            }
        };

        if (pathPattern.startsWith("http://") || pathPattern.startsWith("https://")) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(pathPattern).openStream()))) {
                processReader(reader, parser, fromTime, toTime, filterConsumer);
            }
        } else {
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pathPattern);
            try (Stream<Path> paths = Files.walk(Paths.get("."))) {
                paths.filter(Files::isRegularFile)
                    .filter(matcher::matches)
                    .forEach(path -> {
                        try (BufferedReader reader = Files.newBufferedReader(path)) {
                            processReader(reader, parser, fromTime, toTime, filterConsumer);
                        } catch (Exception e) {
                            LOGGER.error("Ошибка при чтении файла {}: {}", path, e.getMessage(), e);
                        }
                    });
            }
        }
    }

    private String getFieldValue(LogRecord logEntry, String field) {
        return switch (field.toLowerCase()) {
            case "agent" -> logEntry.getAgent();
            case "method" -> logEntry.getRequestMethod();
            case "resource" -> logEntry.getRequestResource();
            case "status" -> String.valueOf(logEntry.getStatus());
            case "ip" -> logEntry.getIp();
            case "user" -> logEntry.getUser();
            default -> {
                LOGGER.warn("Неизвестное поле для фильтрации: {}", field);
                yield null;
            }
        };
    }

    /**
     * Обрабатывает поток логов, парсит строки и передает данные в StatisticsCollector
     * или применяет пользовательский фильтр.
     *
     * @param reader         Поток для чтения строк (например, файл или URL)
     * @param parser         Экземпляр LogParser для разбора строк лога
     * @param fromTime       Начальная дата фильтрации (может быть null для отсутствия фильтрации)
     * @param toTime         Конечная дата фильтрации (может быть null для отсутствия фильтрации)
     * @param filterConsumer Дополнительный обработчик, который принимает записи LogRecord, прошедшие фильтрацию
     */
    private void processReader(Reader reader, LogParser parser, ZonedDateTime fromTime, ZonedDateTime toTime,
        Consumer<LogRecord> filterConsumer) {
        new BufferedReader(reader).lines().forEach(line -> {
            try {
                LogRecord logEntry = parser.parse(line);
                // Фильтрация по времени
                if ((fromTime == null || !logEntry.getTime().isBefore(fromTime))
                    && (toTime == null || !logEntry.getTime().isAfter(toTime))) {
                    filterConsumer.accept(logEntry);
                }
            } catch (Exception e) {
                LOGGER.error("Ошибка при разборе строки '{}': {}", line, e.getMessage(), e);
            }
        });
    }
}
