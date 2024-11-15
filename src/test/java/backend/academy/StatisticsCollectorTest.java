package backend.academy;

import java.time.ZonedDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StatisticsCollectorTest {

    @Test
    public void testCollect() {
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

        stats.collect(record1);
        stats.collect(record2);
        stats.collect(record3);

        assertEquals(3, stats.getTotalRequests());
        assertEquals((500 + 300 + 700) / 3.0, stats.getAverageResponseSize(), 0.001);
        assertEquals(700, stats.getPercentile95ResponseSize());

        Map<String, Integer> topResources = stats.getTopResources(10);
        assertEquals(2, topResources.size());
        assertEquals(2, topResources.get("/index.html"));
        assertEquals(1, topResources.get("/submit"));

        Map<Integer, Integer> statusCodes = stats.getStatusCodes();
        assertEquals(2, statusCodes.size());
        assertEquals(2, statusCodes.get(200));
        assertEquals(1, statusCodes.get(404));
    }
}
