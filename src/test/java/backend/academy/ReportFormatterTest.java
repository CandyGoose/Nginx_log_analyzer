package backend.academy;

import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReportFormatterTest {
    StatisticsCollector stats = new StatisticsCollector();
    LogRecord record1 = new LogRecord.Builder()
        .setIp("192.168.1.1")
        .setUser("-")
        .setTime(ZonedDateTime.parse("2024-08-31T10:00:00Z"))
        .setRequest("GET /index.html HTTP/1.1")
        .setStatus(200)
        .setSize(500)
        .setReferer("-")
        .setAgent("Mozilla/5.0")
        .build();

    LogRecord record2 = new LogRecord.Builder()
        .setIp("192.168.1.2")
        .setUser("-")
        .setTime(ZonedDateTime.parse("2024-08-31T10:05:00Z"))
        .setRequest("POST /submit HTTP/1.1")
        .setStatus(404)
        .setSize(300)
        .setReferer("-")
        .setAgent("curl/7.68.0")
        .build();

    LogRecord record3 = new LogRecord.Builder()
        .setIp("192.168.1.3")
        .setUser("-")
        .setTime(ZonedDateTime.parse("2024-08-31T10:10:00Z"))
        .setRequest("GET /index.html HTTP/1.1")
        .setStatus(200)
        .setSize(700)
        .setReferer("-")
        .setAgent("Mozilla/5.0")
        .build();

    @Test
    public void testFormatMarkdownReport() {
        stats.collect(record1);
        stats.collect(record2);
        stats.collect(record3);

        ReportFormatter formatter = new ReportFormatter("markdown");
        String report = formatter.formatReport(stats, "access.log", "2024-08-31T00:00:00Z", "2024-08-31T23:59:59Z");
        assertTrue(report.contains("### Общая информация"));
        assertTrue(report.contains("| Количество запросов | 3 |"));
        assertTrue(report.contains("| Средний размер ответа | 500 B |"));
        assertTrue(report.contains("| 95-й перцентиль размера ответа | 700 B |"));
        assertTrue(report.contains("### Запрашиваемые ресурсы"));
        assertTrue(report.contains("| `/index.html` | 2 |"));
        assertTrue(report.contains("| `/submit` | 1 |"));
        assertTrue(report.contains("### Коды ответа"));
        assertTrue(report.contains("| 200 | 2 |"));
        assertTrue(report.contains("| 404 | 1 |"));
    }

    @Test
    public void testFormatAsciiDocReport() {
        StatisticsCollector stats = new StatisticsCollector();
        stats.collect(record1);
        stats.collect(record2);
        stats.collect(record3);

        ReportFormatter formatter = new ReportFormatter("adoc");
        String report = formatter.formatReport(stats, "access.log", "2024-08-31T00:00:00Z", "2024-08-31T23:59:59Z");
        assertTrue(report.contains("==== Общая информация ===="));
        assertTrue(report.contains("| Количество запросов | 3 "));
        assertTrue(report.contains("| Средний размер ответа | 500 B "));
        assertTrue(report.contains("| 95-й перцентиль размера ответа | 700 B "));
        assertTrue(report.contains("==== Запрашиваемые ресурсы ===="));
        assertTrue(report.contains("| `/index.html` | 2"));
        assertTrue(report.contains("| `/submit` | 1 "));
        assertTrue(report.contains("==== Коды ответа ===="));
        assertTrue(report.contains("| 200 | 2 "));
        assertTrue(report.contains("| 404 | 1 "));
    }
}
