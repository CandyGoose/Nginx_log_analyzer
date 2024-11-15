package backend.academy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZonedDateTime;

public class ReportFormatterTest {

    @Test
    public void testFormatMarkdownReport() {
        StatisticsCollector stats = new StatisticsCollector();
        stats.collect(new LogRecord("192.168.1.1", "-", ZonedDateTime.parse("2024-08-31T10:00:00Z"), "GET /index.html HTTP/1.1", 200, 500, "-", "Mozilla/5.0"));
        stats.collect(new LogRecord("192.168.1.2", "-", ZonedDateTime.parse("2024-08-31T10:05:00Z"), "POST /submit HTTP/1.1", 404, 300, "-", "curl/7.68.0"));
        stats.collect(new LogRecord("192.168.1.3", "-", ZonedDateTime.parse("2024-08-31T10:10:00Z"), "GET /index.html HTTP/1.1", 200, 700, "-", "Mozilla/5.0"));

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
        stats.collect(new LogRecord("192.168.1.1", "-", ZonedDateTime.parse("2024-08-31T10:00:00Z"), "GET /index.html HTTP/1.1", 200, 500, "-", "Mozilla/5.0"));
        stats.collect(new LogRecord("192.168.1.2", "-", ZonedDateTime.parse("2024-08-31T10:05:00Z"), "POST /submit HTTP/1.1", 404, 300, "-", "curl/7.68.0"));
        stats.collect(new LogRecord("192.168.1.3", "-", ZonedDateTime.parse("2024-08-31T10:10:00Z"), "GET /index.html HTTP/1.1", 200, 700, "-", "Mozilla/5.0"));

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
