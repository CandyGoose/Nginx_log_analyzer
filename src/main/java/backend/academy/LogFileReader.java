package backend.academy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.file.*;
import java.time.ZonedDateTime;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.regex.Pattern;

public class LogFileReader {

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

        Consumer<LogRecord> filterConsumer = record -> {
            boolean matches = true;
            if (filterField != null && filterValue != null) {
                String fieldValue = getFieldValue(record, filterField);
                if (fieldValue == null) {
                    matches = false;
                } else {
                    String regex = filterValue.replace("*", ".*");
                    matches = fieldValue.matches(regex);
                }
            }
            if (matches) {
                statsCollector.collect(record);
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
                            System.err.println("Ошибка при чтении файла " + path + ": " + e.getMessage());
                        }
                    });
            }
        }
    }

    private String getFieldValue(LogRecord record, String field) {
        switch (field.toLowerCase()) {
            case "agent":
                return record.getAgent();
            case "method":
                return record.getRequestMethod();
            case "resource":
                return record.getRequestResource();
            case "status":
                return String.valueOf(record.getStatus());
            case "ip":
                return record.getIp();
            case "user":
                return record.getUser();
            default:
                return null;
        }
    }

    /**
     * Обрабатывает поток логов, парсит строки и передает данные в StatisticsCollector или применяет пользовательский фильтр.
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
                LogRecord record = parser.parse(line);
                // Фильтрация по времени
                if ((fromTime == null || !record.getTime().isBefore(fromTime)) &&
                    (toTime == null || !record.getTime().isAfter(toTime))) {
                    filterConsumer.accept(record);
                }
            } catch (Exception e) {
                System.err.println("Ошибка при разборе строки: " + e.getMessage());
            }
        });
    }
}
