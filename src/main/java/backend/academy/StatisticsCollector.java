package backend.academy;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class StatisticsCollector {
    private int totalRequests = 0;
    private long totalResponseSize = 0;
    private final List<Integer> responseSizes = new ArrayList<>();

    private final Map<String, AtomicInteger> resourceCount = new HashMap<>();
    private final Map<Integer, AtomicInteger> statusCount = new HashMap<>();

    private ZonedDateTime minDate = null;
    private ZonedDateTime maxDate = null;

    /**
     * Сбор данных из записи лога.
     *
     * @param record Запись лога
     */
    public void collect(LogRecord record) {
        totalRequests++;
        totalResponseSize += record.getSize();
        responseSizes.add(record.getSize());

        int status = record.getStatus();
        statusCount.computeIfAbsent(status, k -> new AtomicInteger(0)).incrementAndGet();

        ZonedDateTime recordTime = record.getTime();
        if (minDate == null || recordTime.isBefore(minDate)) {
            minDate = recordTime;
        }
        if (maxDate == null || recordTime.isAfter(maxDate)) {
            maxDate = recordTime;
        }
    }

    public int getTotalRequests() {
        return totalRequests;
    }

    public double getAverageResponseSize() {
        return totalRequests > 0 ? (double) totalResponseSize / totalRequests : 0;
    }

    public int getPercentile95ResponseSize() {
        if (responseSizes.isEmpty()) {
            return 0;
        }
        Collections.sort(responseSizes);
        int index = (int) Math.ceil(0.95 * responseSizes.size()) - 1;
        return responseSizes.get(index);
    }

    public Map<String, Integer> getTopResources(int limit) {
        return getTopEntries(resourceCount, limit);
    }

    public Map<Integer, Integer> getStatusCodes() {
        Map<Integer, Integer> result = new HashMap<>();
        statusCount.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }

    public ZonedDateTime getMinDate() {
        return minDate;
    }

    public ZonedDateTime getMaxDate() {
        return maxDate;
    }

    // Метод для сортировки и выборки топ-N записей
    private <K> Map<K, Integer> getTopEntries(Map<K, AtomicInteger> map, int limit) {
        Map<K, Integer> result = new LinkedHashMap<>();
        map.entrySet().stream()
            .sorted(Map.Entry.<K, AtomicInteger>comparingByValue(Comparator.comparingInt(AtomicInteger::get)).reversed())
            .limit(limit)
            .forEach(e -> result.put(e.getKey(), e.getValue().get()));
        return result;
    }
}
