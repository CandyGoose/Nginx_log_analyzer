package backend.academy;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.BiConsumer;

public class ReportFormatter {
    private final String format;

    // Константы для форматирования
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,###");
    private static final DateTimeFormatter OUTPUT_DATE_FORMATTER =
        DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss", Locale.ENGLISH);

    private static final String COLUMN_SEPARATOR = " | ";
    private static final String ROW_START = "| ";
    // Константы для таблиц Markdown
    private static final String MARKDOWN_TABLE_HEADER = "|:------------------------|--------------------------:|\n";
    private static final String MARKDOWN_ROW_END = " |\n";
    private static final String MARKDOWN_FORMAT = "markdown";

    // Константы для таблиц AsciiDoc
    private static final String ASCIIDOC_TABLE_START = "|===\n";
    private static final String ASCIIDOC_TABLE_END = "|===\n\n";
    private static final String ASCIIDOC_ROW_END = " \n";
    private static final String ASCIIDOC_FORMAT = "adoc";

    // Magic numbers
    private static final int TOP_RESOURCES_LIMIT = 10;
    private static final int BYTES_IN_KILOBYTE = 1024;
    private static final int BYTES_IN_MEGABYTE = BYTES_IN_KILOBYTE * BYTES_IN_KILOBYTE;
    private static final int BYTES_IN_GIGABYTE = BYTES_IN_KILOBYTE * BYTES_IN_MEGABYTE;

    public ReportFormatter(String format) {
        this.format = format.toLowerCase();
    }

    public String formatReport(StatisticsCollector stats, String path, String from, String to) {
        StringBuilder report = new StringBuilder();

        if (MARKDOWN_FORMAT.equals(format)) {
            formatReport(report, stats, path, from, to, this::formatMarkdownTable);
        } else if (ASCIIDOC_FORMAT.equals(format)) {
            formatReport(report, stats, path, from, to, this::formatAsciiDocTable);
        } else {
            report.append("Неизвестный формат вывода: ").append(format);
        }

        return report.toString();
    }

    private void formatReport(
        StringBuilder report,
        StatisticsCollector stats,
        String path,
        String from,
        String to,
        BiConsumer<StringBuilder, String> tableFormatter) {
        report.append(formatHeader("Общая информация"));
        tableFormatter.accept(report, "Метрика | Значение");
        appendRow(report, tableFormatter, "Файл(-ы)", "`" + path + "`");
        appendRow(report, tableFormatter, "Начальная дата", (from != null) ? from : "-");
        appendRow(report, tableFormatter, "Конечная дата", (to != null) ? to : "-");
        appendRow(report, tableFormatter, "Количество запросов", NUMBER_FORMAT.format(stats.getTotalRequests()));
        appendRow(report, tableFormatter, "Средний размер ответа", formatSize(stats.getAverageResponseSize()));
        appendRow(report, tableFormatter, "95-й перцентиль размера ответа",
            formatSize(stats.getPercentile95ResponseSize()));
        tableFormatter.accept(report, null);

        report.append(formatHeader("Запрашиваемые ресурсы"));
        tableFormatter.accept(report, "Ресурс | Количество");
        stats.getTopResources(TOP_RESOURCES_LIMIT)
            .forEach((resource, count) -> appendRow(report, tableFormatter, "`" + resource + "`",
                NUMBER_FORMAT.format(count)));
        tableFormatter.accept(report, null);

        report.append(formatHeader("Коды ответа"));
        tableFormatter.accept(report, "Код | Количество");
        stats.getStatusCodes()
            .forEach((status, count) -> appendRow(report, tableFormatter, String.valueOf(status),
                NUMBER_FORMAT.format(count)));
        tableFormatter.accept(report, null);
    }

    private String formatHeader(String title) {
        return MARKDOWN_FORMAT.equals(format) ? "### " + title + "\n\n" : "==== " + title + " ====\n\n";
    }

    private void formatMarkdownTable(StringBuilder report, String header) {
        if (header != null) {
            report.append("| ").append(header).append(MARKDOWN_ROW_END);
            report.append(MARKDOWN_TABLE_HEADER);
        }
    }

    private void formatAsciiDocTable(StringBuilder report, String header) {
        if (header != null) {
            report.append(ASCIIDOC_TABLE_START);
            report.append("| ").append(header).append(ASCIIDOC_ROW_END);
        } else {
            report.append(ASCIIDOC_TABLE_END);
        }
    }

    private void appendRow(StringBuilder report, BiConsumer<StringBuilder, String> tableFormatter,
        String col1, String col2) {
        if (ASCIIDOC_FORMAT.equals(format)) {
            report.append("| ").append(col1).append(COLUMN_SEPARATOR).append(col2).append(ASCIIDOC_ROW_END);
        } else { // Markdown
            report.append("| ").append(col1).append(COLUMN_SEPARATOR).append(col2).append(MARKDOWN_ROW_END);
        }
    }

    private String formatSize(double sizeInBytes) {
        if (sizeInBytes < BYTES_IN_KILOBYTE) {
            return String.format("%.0f B", sizeInBytes);
        } else if (sizeInBytes < BYTES_IN_MEGABYTE) {
            return String.format("%.2f KB", sizeInBytes / BYTES_IN_KILOBYTE);
        } else if (sizeInBytes < BYTES_IN_GIGABYTE) {
            return String.format("%.2f MB", sizeInBytes / BYTES_IN_MEGABYTE);
        } else {
            return String.format("%.2f GB", sizeInBytes / BYTES_IN_GIGABYTE);
        }
    }
}
