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

    public void run(String[] args) {
        Options options = buildOptions();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);

            String path = cmd.getOptionValue("path");
            String from = cmd.getOptionValue("from");
            String to = cmd.getOptionValue("to");
            String format = cmd.getOptionValue("format", "markdown");
            String filterField = cmd.getOptionValue("filter-field");
            String filterValue = cmd.getOptionValue("filter-value");

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
            .longOpt("path")
            .hasArg()
            .required()
            .desc("Путь к лог-файлам")
            .build());

        options.addOption(Option.builder()
            .longOpt("from")
            .hasArg()
            .desc("Начальная дата в формате ISO8601")
            .build());

        options.addOption(Option.builder()
            .longOpt("to")
            .hasArg()
            .desc("Конечная дата в формате ISO8601")
            .build());

        options.addOption(Option.builder()
            .longOpt("format")
            .hasArg()
            .desc("Формат вывода (markdown или adoc), по умолчанию markdown")
            .build());

        options.addOption(Option.builder()
            .longOpt("filter-field")
            .hasArg()
            .desc("Поле для фильтрации (например, agent, method)")
            .build());

        options.addOption(Option.builder()
            .longOpt("filter-value")
            .hasArg()
            .desc("Значение для фильтрации (может содержать шаблоны)")
            .build());

        return options;
    }
}
