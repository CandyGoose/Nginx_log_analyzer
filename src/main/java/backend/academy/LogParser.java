package backend.academy;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogParser {
    private static final String LOG_PATTERN =
        "^(?<ip>[\\w:.]+) - (?<user>\\S+) \\[(?<time>[^\\]]+)] "
            + "\"(?<request>[^\"]+)\" (?<status>\\d{3}) (?<size>\\d+) "
            + "\"(?<referer>[^\"]*)\" \"(?<agent>[^\"]*)\"";

    private static final Pattern PATTERN = Pattern.compile(LOG_PATTERN);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z",
        Locale.ENGLISH);

    /**
     * Парсит строку лога в объект LogRecord.
     *
     * @param logLine Строка лога
     * @return Объект LogRecord
     * @throws Exception Если строка не соответствует ожидаемому формату
     */
    public LogRecord parse(String logLine) throws Exception {
        Matcher matcher = PATTERN.matcher(logLine);
        if (!matcher.find()) {
            throw new Exception("Неверный формат лога: " + logLine);
        }

        return new LogRecord.Builder()
            .setIp(matcher.group("ip"))
            .setUser(matcher.group("user"))
            .setTime(ZonedDateTime.parse(matcher.group("time"), TIME_FORMATTER))
            .setRequest(matcher.group("request"))
            .setStatus(Integer.parseInt(matcher.group("status")))
            .setSize(Integer.parseInt(matcher.group("size")))
            .setReferer(matcher.group("referer"))
            .setAgent(matcher.group("agent"))
            .build();
    }
}
