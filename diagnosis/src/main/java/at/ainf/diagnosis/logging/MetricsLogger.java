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

    private Map<String,Timer.Context> contexts = new HashMap<String, Timer.Context>();

    private LabelManager labelManager;

    public static MetricsLogger getInstance() {
        if (instance != null)
            return instance;

        instance = new MetricsLogger();
        return instance;
    }

    public MetricsLogger() {
        metrics.put(standardMetric, new MetricRegistry());
        labelManager = new LabelManager(standardMetric);
        actualMetric = standardMetric;
    }

    public LabelManager getLabelManager() {
        return labelManager;
    }

    public MetricRegistry addLabel (String label) {

        String labelFull = labelManager.addLabel(label);
        MetricRegistry metricRegistry = new MetricRegistry();
        metrics.put(labelFull, metricRegistry);
        actualMetric = labelFull;
        return metricRegistry;
    }

    public MetricRegistry removeLabel (String label) {

        String labelFull = labelManager.removeLabel(label);
        MetricRegistry removed = metrics.remove(labelFull);
        actualMetric = labelManager.getActualLabel();
        return removed;
    }


    public void startTimer (String timer) {
        contexts.put(timer, getTimer(timer).time());
    }

    public long stopTimer (String timer) {
        long time = contexts.get(timer).stop();
        logTime(timer, time);
        return time;
    }

    public void logTime(String timer, long time) {
        LoggerFactory.getLogger("at.ainf.metrics").info(labelManager.getLabelsConc() + "_" + timer + ": " + time);
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

    public Counter getCounter (String name) {
        Counter counter = getActualMetric().getCounters().get(name);
        if (counter == null)
            counter = getActualMetric().counter(name);
        return counter;
    }

    public Gauge createGauge (String identifier, final int value) {
        Gauge gauge = getActualMetric().getGauges().get(name(MetricsLogger.class, identifier));
        if (gauge == null)
            gauge = getActualMetric().register(name(MetricsLogger.class, identifier), new Gauge<Integer>() {
                @Override
                public Integer getValue() {
                    return value;
                }
            });
        return gauge;
    }

    public void logStandardMetrics() {
        MetricRegistry metric = metrics.get(standardMetric);
        CsvLikeSlf4jReporter reporter = new CsvLikeSlf4jReporter(metric, LoggerFactory.getLogger("at.ainf.metrics"), null,
                TimeUnit.SECONDS, TimeUnit.MILLISECONDS, MetricFilter.ALL);
        reporter.report(metric.getGauges(),metric.getCounters(),metric.getHistograms(),
                metric.getMeters(),metric.getTimers());
    }


}
