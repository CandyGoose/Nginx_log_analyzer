package backend.academy;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogAnalyzerRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogAnalyzerRunner.class);

    // Константы для аргументов командной строки
    private static final String ARG_PATH = "path";
    private static final String ARG_FROM = "from";
    private static final String ARG_TO = "to";
    private static final String ARG_FORMAT = "format";
    private static final String ARG_FILTER_FIELD = "filter-field";
    private static final String ARG_FILTER_VALUE = "filter-value";
    private static final String DEFAULT_FORMAT = "markdown";

    public void run(String[] args) {
        Options options = buildOptions();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);

            String path = cmd.getOptionValue(ARG_PATH);
            String from = cmd.getOptionValue(ARG_FROM);
            String to = cmd.getOptionValue(ARG_TO);
            String format = cmd.getOptionValue(ARG_FORMAT, DEFAULT_FORMAT);
            String filterField = cmd.getOptionValue(ARG_FILTER_FIELD);
            String filterValue = cmd.getOptionValue(ARG_FILTER_VALUE);

            LogFileReader reader = new LogFileReader();
            LogParser logParser = new LogParser();
            StatisticsCollector statsCollector = new StatisticsCollector();

            reader.readLogs(path, logParser, statsCollector, from, to, filterField, filterValue);

            ReportFormatter formatterReport = new ReportFormatter(format);
            String report = formatterReport.formatReport(statsCollector, path, from, to);

            LOGGER.info("Отчет успешно создан:\n{}", report);

        } catch (ParseException e) {
            LOGGER.error("Ошибка парсинга аргументов: {}", e.getMessage());
            formatter.printHelp("analyzer", options);
            System.exit(1);
        } catch (Exception e) {
            LOGGER.error("Ошибка выполнения программы: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    private Options buildOptions() {
        Options options = new Options();

        options.addOption(Option.builder("p")
            .longOpt(ARG_PATH)
            .hasArg()
            .required()
            .desc("Путь к лог-файлам")
            .build());

        options.addOption(Option.builder()
            .longOpt(ARG_FROM)
            .hasArg()
            .desc("Начальная дата в формате ISO8601")
            .build());

        options.addOption(Option.builder()
            .longOpt(ARG_TO)
            .hasArg()
            .desc("Конечная дата в формате ISO8601")
            .build());

        options.addOption(Option.builder()
            .longOpt(ARG_FORMAT)
            .hasArg()
            .desc("Формат вывода (markdown или adoc), по умолчанию markdown")
            .build());

        options.addOption(Option.builder()
            .longOpt(ARG_FILTER_FIELD)
            .hasArg()
            .desc("Поле для фильтрации (например, agent, method)")
            .build());

        options.addOption(Option.builder()
            .longOpt(ARG_FILTER_VALUE)
            .hasArg()
            .desc("Значение для фильтрации (может содержать шаблоны)")
            .build());

        return options;
    }
}
