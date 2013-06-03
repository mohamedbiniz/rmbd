package at.ainf.diagnosis.logging;

import com.codahale.metrics.*;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 29.05.13
 * Time: 10:53
 * To change this template use File | Settings | File Templates.
 */
public class MetricsLogger {
    private static MetricsLogger instance;

    private Map<String,MetricRegistry> metrics = new HashMap<String, MetricRegistry>();

    private String standardMetric = "std";

    private String actualMetric;

    private List<String> labels = new LinkedList<String>();

    public static MetricsLogger getInstance() {
        if (instance != null)
            return instance;

        instance = new MetricsLogger();
        return instance;
    }

    public MetricsLogger() {
        metrics.put(standardMetric, new MetricRegistry());
        labels.add(standardMetric);
        actualMetric = standardMetric;
    }

    private Map<String,Integer> countOfLabels = new HashMap<String, Integer>();

    private int increaseCounter(String identifier) {
        if (!countOfLabels.containsKey(identifier))
            countOfLabels.put(identifier,0);

        Integer counter = countOfLabels.get(identifier);
        countOfLabels.put(identifier,counter+1);
        return counter;
    }

    private int getActualCounter(String identifier) {
        return countOfLabels.get(identifier) - 1;
    }

    Map<String,Timer.Context> contexts = new HashMap<String, Timer.Context>();

    public void startTimer (String timer) {
        contexts.put(timer, getTimer(timer).time());
    }

    public long stopTimer (String timer) {
        long time = contexts.get(timer).stop();
        logTime(timer, time);
        return time;
    }

    public MetricRegistry addLabel (String label) {

        String labelFull = label + "-" + increaseCounter(label);
        labels.add(labelFull);
        MetricRegistry metricRegistry = new MetricRegistry();
        metrics.put(labelFull, metricRegistry);
        actualMetric = labelFull;
        return metricRegistry;
    }

    public MetricRegistry removeLabel (String label) {

        String labelFull = label + "-" + getActualCounter(label);
        labels.remove(labelFull);
        MetricRegistry removed = metrics.remove(labelFull);
        actualMetric = labels.get(labels.size() - 1);
        return removed;
    }

    public String getLabelsConcat() {
        StringBuilder full = new StringBuilder();
        for (String label : labels) {
            full.append(label);
            full.append("_");
        }
        return full.toString();
    }

    public void logTime(String timerIdent, long time) {
        LoggerFactory.getLogger("at.ainf.metrics").info(getLabelsConcat() + "_" + timerIdent + ": " + time);
    }

    public MetricRegistry getActualMetric() {
        return metrics.get(actualMetric);
    }

    public Histogram getHistogram (String name) {
        Histogram histogram = getActualMetric().getHistograms().get(name);
        if (histogram == null)
            histogram = getActualMetric().histogram(name);
        return histogram;
    }

    public void updateHistogram (String name, long[] values) {
        Histogram histogram = getHistogram(name);
        for (long value : values)
            histogram.update(value);
    }

    public Timer getTimer (String name) {
        Timer timer = getActualMetric().getTimers().get(name);
        if (timer == null)
            timer = getActualMetric().timer(name);
        return timer;
    }

    public Gauge createGauge (String identifier, final int value) {
        Gauge gauge = getActualMetric().getGauges().get(identifier);
        if (gauge == null)
            gauge = getActualMetric().register(name(MetricsLogger.class, identifier), new Gauge<Integer>() {
                @Override
                public Integer getValue() {
                    return value;
                }
            });
        return gauge;
    }

    public Counter getCounter (String name) {
        Counter counter = getActualMetric().getCounters().get(name);
        if (counter == null)
            counter = getActualMetric().counter(name);
        return counter;
    }

    public void logStandardMetrics() {
        MetricRegistry metric = metrics.get(standardMetric);
        CsvLikeSlf4jReporter reporter = new CsvLikeSlf4jReporter(metric, LoggerFactory.getLogger("at.ainf.metrics"), null,
                TimeUnit.SECONDS, TimeUnit.MILLISECONDS, MetricFilter.ALL);
        reporter.report(metric.getGauges(),metric.getCounters(),metric.getHistograms(),
                metric.getMeters(),metric.getTimers());
    }


}
