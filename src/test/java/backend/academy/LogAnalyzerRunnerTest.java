package backend.academy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class LogAnalyzerRunnerTest {

    @Test
    void testArguments() {
        LogAnalyzerRunner runner = new LogAnalyzerRunner();
        String[] args = {
            "--path", "logs/*.log",
            "--from", "2024-08-31T00:00:00Z",
            "--to", "2024-08-31T23:59:59Z",
            "--format", "markdown"
        };

        assertDoesNotThrow(() -> runner.run(args));
    }

    @Test
    void testMarkdownReportGeneration() {
        LogAnalyzerRunner runner = new LogAnalyzerRunner();
        String[] args = {
            "--path", "logs/*.log",
            "--format", "markdown"
        };

        assertDoesNotThrow(() -> runner.run(args));
    }

    @Test
    void testMissingDateArgument() {
        LogAnalyzerRunner runner = new LogAnalyzerRunner();
        String[] args = {
            "--path", "logs/*.log",
            "--format", "markdown"
        };

        assertDoesNotThrow(() -> runner.run(args));
    }
}
