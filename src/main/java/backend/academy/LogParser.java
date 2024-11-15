package backend.academy;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogParser {
    private static final String LOG_PATTERN =
        "^(?<ip>[\\w:.]+) - (?<user>\\S+) \\[(?<time>[^\\]]+)] " +
            "\"(?<request>[^\"]+)\" (?<status>\\d{3}) (?<size>\\d+) " +
            "\"(?<referer>[^\"]*)\" \"(?<agent>[^\"]*)\"";

    private static final Pattern PATTERN = Pattern.compile(LOG_PATTERN);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);

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

        String ip = matcher.group("ip");
        String user = matcher.group("user");
        String timeStr = matcher.group("time");
        String request = matcher.group("request");
        int status = Integer.parseInt(matcher.group("status"));
        int size = Integer.parseInt(matcher.group("size"));
        String referer = matcher.group("referer");
        String agent = matcher.group("agent");

        ZonedDateTime time = ZonedDateTime.parse(timeStr, TIME_FORMATTER);

        return new LogRecord(ip, user, time, request, status, size, referer, agent);
    }
}
