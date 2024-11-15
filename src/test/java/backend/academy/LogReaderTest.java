package backend.academy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogFileReaderTest {
    private LogFileReader logFileReader;
    private LogParser logParser;
    private StatisticsCollector statsCollector;

    @BeforeEach
    void setUp() {
        logFileReader = new LogFileReader();
        logParser = new LogParser();
        statsCollector = new StatisticsCollector();
    }

    @Test
    void testReadLogsFromFile() {
        String filePath = "src/test/java/backend/academy/resources/test_logs.txt";

        assertDoesNotThrow(() -> {
            logFileReader.readLogs(filePath, logParser, statsCollector, null, null,
                null, null);
        });

        assertEquals(9, statsCollector.getTotalRequests());
    }

    @Test
    void testReadLogsWithFilter() {
        String filePath = "src/test/java/backend/academy/resources/test_logs.txt";

        assertDoesNotThrow(() -> {
            logFileReader.readLogs(filePath, logParser, statsCollector, null, null,
                "method", "POST");
        });

        assertEquals(1, statsCollector.getTotalRequests());
    }

    @Test
    void testAbsolutePath() {
        Path absolutePath = Paths.get("src/test/java/backend/academy/resources/test_logs.txt").toAbsolutePath();

        assertTrue(Files.exists(absolutePath), "Файл должен существовать по абсолютному пути");
        assertDoesNotThrow(() -> logFileReader.readLogs(absolutePath.toString(), logParser, statsCollector,
            null, null, null, null));
    }

    @Test
    void testRelativePath() {
        String relativePath = "src/test/java/backend/academy/resources/test_logs.txt";

        assertTrue(Files.exists(Paths.get(relativePath)), "Файл должен существовать по относительному пути");
        assertDoesNotThrow(() -> logFileReader.readLogs(relativePath, logParser, statsCollector,
            null, null, null, null));
    }

    @Test
    void testGlobExpressions() {
        String globPattern = "**/test_logs.txt";

        assertDoesNotThrow(() -> {
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
            Path basePath = Paths.get("src/test/java/backend").toAbsolutePath().normalize();

            try (Stream<Path> paths = Files.walk(basePath)) {
                boolean anyMatch = paths.filter(Files::isRegularFile)
                    .anyMatch(matcher::matches);

                assertTrue(anyMatch, "Должен быть найден хотя бы один файл, соответствующий шаблону");
            }
        });
    }
}
