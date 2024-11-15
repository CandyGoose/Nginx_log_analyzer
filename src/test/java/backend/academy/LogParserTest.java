package backend.academy;

import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LogParserTest {

    @Test
    public void testParseValidLog() throws Exception {
        String logLine = "192.168.1.1 - - [17/May/2015:08:05:52 +0000] \"GET /downloads/product_1 HTTP/1.1\" 200 85619205 \"-\" \"Mozilla/5.0\"";
        LogParser parser = new LogParser();
        LogRecord record = parser.parse(logLine);

        assertEquals("192.168.1.1", record.getIp());
        assertEquals("-", record.getUser());
        assertEquals(ZonedDateTime.parse("2015-05-17T08:05:52Z"), record.getTime());
        assertEquals("GET /downloads/product_1 HTTP/1.1", record.getRequest());
        assertEquals(200, record.getStatus());
        assertEquals(85619205, record.getSize());
        assertEquals("-", record.getReferer());
        assertEquals("Mozilla/5.0", record.getAgent());
    }

    @Test
    public void testParseInvalidLog() {
        String invalidLog = "Invalid log line";
        LogParser parser = new LogParser();
        Exception exception = assertThrows(Exception.class, () -> {
            parser.parse(invalidLog);
        });

        String expectedMessage = "Неверный формат лога";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}
