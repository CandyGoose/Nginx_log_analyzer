package backend.academy;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.BiConsumer;

public class ReportFormatter {
    private String format;
    private static final DecimalFormat numberFormat = new DecimalFormat("#,###");
    private static final DecimalFormat sizeFormat = new DecimalFormat("#,###.##");
    private static final DateTimeFormatter outputDateFormatter =
        DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss", Locale.ENGLISH);

    private static final String MARKDOWN_TABLE_HEADER = "|:------------------------|--------------------------:|\n";

    private static final String ASCIIDOC_TABLE_START = "|===\n";
    private static final String ASCIIDOC_TABLE_END = "|===\n\n";

    public ReportFormatter(String format) {
        this.format = format.toLowerCase();
    }

    public String formatReport(StatisticsCollector stats, String path, String from, String to) {
        StringBuilder report = new StringBuilder();

        if (format.equals("markdown")) {
            formatReport(report, stats, path, from, to, this::formatMarkdownTable);
        } else if (format.equals("adoc")) {
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
        appendRow(report, tableFormatter, "Начальная дата",
            (from != null) ? from : "-");
        appendRow(report, tableFormatter, "Конечная дата",
            (to != null) ? to : "-");
        appendRow(report, tableFormatter, "Количество запросов", numberFormat.format(stats.getTotalRequests()));
        appendRow(report, tableFormatter, "Средний размер ответа", formatSize(stats.getAverageResponseSize()));
        appendRow(report, tableFormatter, "95-й перцентиль размера ответа", formatSize(stats.getPercentile95ResponseSize()));
        tableFormatter.accept(report, null);

        report.append(formatHeader("Запрашиваемые ресурсы"));
        tableFormatter.accept(report, "Ресурс | Количество");
        stats.getTopResources(10)
            .forEach((resource, count) -> appendRow(report, tableFormatter, "`" + resource + "`", numberFormat.format(count)));
        tableFormatter.accept(report, null);

        report.append(formatHeader("Коды ответа"));
        tableFormatter.accept(report, "Код | Количество");
        stats.getStatusCodes()
            .forEach((status, count) -> appendRow(report, tableFormatter, String.valueOf(status), numberFormat.format(count)));
        tableFormatter.accept(report, null);
    }

    private String formatHeader(String title) {
        return format.equals("markdown") ? "### " + title + "\n\n" : "==== " + title + " ====\n\n";
    }

    private void formatMarkdownTable(StringBuilder report, String header) {
        if (header != null) {
            report.append("| ").append(header).append(" |\n");
            report.append(MARKDOWN_TABLE_HEADER);
        }
    }

    private void formatAsciiDocTable(StringBuilder report, String header) {
        if (header != null) {
            report.append(ASCIIDOC_TABLE_START);
            report.append("| ").append(header).append(" \n");
        } else {
            report.append(ASCIIDOC_TABLE_END);
        }
    }

    private void appendRow(StringBuilder report, BiConsumer<StringBuilder, String> tableFormatter, String col1, String col2) {
        if (format.equals("adoc")) {
            report.append("| ").append(col1).append(" | ").append(col2).append(" \n");
        } else { // Markdown
            report.append("| ").append(col1).append(" | ").append(col2).append(" |\n");
        }
    }

    private String formatSize(double sizeInBytes) {
        if (sizeInBytes < 1024) {
            return String.format("%.0f B", sizeInBytes);
        } else if (sizeInBytes < 1024 * 1024) {
            return String.format("%.2f KB", sizeInBytes / 1024);
        } else if (sizeInBytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", sizeInBytes / (1024 * 1024));
        } else {
            return String.format("%.2f GB", sizeInBytes / (1024 * 1024 * 1024));
        }
    }
}
