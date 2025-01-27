package backend.academy;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class StatisticsCollector {
    private static final double PERCENTILE_95 = 0.95;

    private int totalRequests = 0;
    private long totalResponseSize = 0;
    private List<Integer> responseSizes = new ArrayList<>();

    private Map<String, AtomicInteger> resourceCount = new HashMap<>();
    private Map<Integer, AtomicInteger> statusCount = new HashMap<>();
    private Map<String, AtomicInteger> methodCount = new HashMap<>();

    private ZonedDateTime minDate = null;
    private ZonedDateTime maxDate = null;

    /**
     * Сбор данных из записи лога.
     *
     * @param logEntry Запись лога
     */
    public void collect(LogRecord logEntry) {
        totalRequests++;
        totalResponseSize += logEntry.getSize();
        responseSizes.add(logEntry.getSize());

        String resource = logEntry.getRequestResource();
        resourceCount.computeIfAbsent(resource, k -> new AtomicInteger(0)).incrementAndGet();

        int status = logEntry.getStatus();
        statusCount.computeIfAbsent(status, k -> new AtomicInteger(0)).incrementAndGet();

        String method = logEntry.getRequestMethod();
        methodCount.computeIfAbsent(method, k -> new AtomicInteger(0)).incrementAndGet();

        ZonedDateTime recordTime = logEntry.getTime();
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
        int index = (int) Math.ceil(PERCENTILE_95 * responseSizes.size()) - 1;
        return responseSizes.get(index);
    }

    public Map<String, Integer> getTopResources(int limit) {
        return getTopEntries(resourceCount, limit);
    }

    public Map<Integer, Integer> getStatusCodes() {
        return convertAtomicMapToIntegerMap(statusCount);
    }

    public Map<String, Integer> getHttpMethods() {
        return convertAtomicMapToIntegerMap(methodCount);
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
            .sorted(Map.Entry.<K, AtomicInteger>comparingByValue(Comparator.comparingInt(AtomicInteger::get))
                .reversed())
            .limit(limit)
            .forEach(e -> result.put(e.getKey(), e.getValue().get()));
        return result;
    }

    // Метод для конвертации карты AtomicInteger в Integer
    private <K> Map<K, Integer> convertAtomicMapToIntegerMap(Map<K, AtomicInteger> map) {
        Map<K, Integer> result = new HashMap<>();
        map.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }
}
