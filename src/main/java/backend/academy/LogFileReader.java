package backend.academy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.file.*;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

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

        if (pathPattern.startsWith("http://") || pathPattern.startsWith("https://")) {
            // Чтение из URL
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(pathPattern).openStream()))) {
                processReader(reader, parser, statsCollector, fromTime, toTime);
            }
        } else {
            // Чтение локальных файлов
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pathPattern);
            try (Stream<Path> paths = Files.walk(Paths.get("."))) {
                paths.filter(Files::isRegularFile)
                    .filter(matcher::matches)
                    .forEach(path -> {
                        try (BufferedReader reader = Files.newBufferedReader(path)) {
                            processReader(reader, parser, statsCollector, fromTime, toTime);
                        } catch (Exception e) {
                            System.err.println("Ошибка при чтении файла " + path + ": " + e.getMessage());
                        }
                    });
            }
        }
    }

    /**
     * Обрабатывает поток логов, парсит строки и передает данные в StatisticsCollector.
     *
     * @param reader         Поток для чтения строк
     * @param parser         Экземпляр LogParser
     * @param statsCollector Экземпляр StatisticsCollector
     * @param fromTime       Начальная дата фильтрации
     * @param toTime         Конечная дата фильтрации
     */
    private void processReader(Reader reader, LogParser parser, StatisticsCollector statsCollector,
        ZonedDateTime fromTime, ZonedDateTime toTime) {
        new BufferedReader(reader).lines().forEach(line -> {
            try {
                LogRecord record = parser.parse(line);
                // Фильтрация по времени
                if ((fromTime == null || !record.getTime().isBefore(fromTime)) &&
                    (toTime == null || !record.getTime().isAfter(toTime))) {
                    statsCollector.collect(record);
                }
            } catch (Exception e) {
                System.err.println("Ошибка при разборе строки: " + e.getMessage());
            }
        });
    }
}
