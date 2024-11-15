package backend.academy;

import org.apache.commons.cli.*;

public class LogAnalyzerRunner {

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

            System.out.println(report);

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("analyzer", options);
            System.exit(1);
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
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
