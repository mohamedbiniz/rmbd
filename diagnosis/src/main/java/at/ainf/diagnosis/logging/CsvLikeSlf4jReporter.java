package at.ainf.diagnosis.logging;

import com.codahale.metrics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 28.05.13
 * Time: 11:42
 * To change this template use File | Settings | File Templates.
 */
public class CsvLikeSlf4jReporter extends ScheduledReporter {

    private final Logger logger;
    private final Marker marker;

    public CsvLikeSlf4jReporter(MetricRegistry registry,
                          Logger logger,
                          Marker marker,
                          TimeUnit rateUnit,
                          TimeUnit durationUnit,
                          MetricFilter filter) {
        super(registry, "logger-reporter", filter, rateUnit, durationUnit);
        this.logger = logger;
        this.marker = marker;
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
            logGauge(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Counter> entry : counters.entrySet()) {
            logCounter(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
            logHistogram(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Meter> entry : meters.entrySet()) {
            logMeter(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Timer> entry : timers.entrySet()) {
            logTimer(entry.getKey(), entry.getValue());
        }
    }

    private void logTimer(String name, Timer timer) {
        final Snapshot snapshot = timer.getSnapshot();
        logger.info(marker,
                "type / name / count / min / max / mean / stddev / median / " +
                        "p75 / p95 / p98 / p999 / mean_rate / m1 / m5 / m15 / rate_unit / duration_unit: " +
                        "TIMER, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}",
                name,
                timer.getCount(),
                convertDuration(snapshot.getMin()),
                convertDuration(snapshot.getMax()),
                convertDuration(snapshot.getMean()),
                convertDuration(snapshot.getStdDev()),
                convertDuration(snapshot.getMedian()),
                convertDuration(snapshot.get75thPercentile()),
                convertDuration(snapshot.get95thPercentile()),
                convertDuration(snapshot.get98thPercentile()),
                convertDuration(snapshot.get99thPercentile()),
                convertDuration(snapshot.get999thPercentile()),
                convertRate(timer.getMeanRate()),
                convertRate(timer.getOneMinuteRate()),
                convertRate(timer.getFiveMinuteRate()),
                convertRate(timer.getFifteenMinuteRate()),
                getRateUnit(),
                getDurationUnit());
    }

    private void logMeter(String name, Meter meter) {
        logger.info(marker,
                "type / name / count / mean_rate / " +
                        "m1 / m5 / m15 / rate_unit: " +
                        "METER, {}, {}, {}, {}, {}, {}, {}",
                name,
                meter.getCount(),
                convertRate(meter.getMeanRate()),
                convertRate(meter.getOneMinuteRate()),
                convertRate(meter.getFiveMinuteRate()),
                convertRate(meter.getFifteenMinuteRate()),
                getRateUnit());
    }

    private void logHistogram(String name, Histogram histogram) {
        final Snapshot snapshot = histogram.getSnapshot();
        logger.info(marker,
                "type / name / count / min / max / mean / " +
                        "stddev / median / p75 / p95 / p98 / p999: " +
                        "HISTOGRAM, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}",
                name,
                histogram.getCount(),
                snapshot.getMin(),
                snapshot.getMax(),
                snapshot.getMean(),
                snapshot.getStdDev(),
                snapshot.getMedian(),
                snapshot.get75thPercentile(),
                snapshot.get95thPercentile(),
                snapshot.get98thPercentile(),
                snapshot.get99thPercentile(),
                snapshot.get999thPercentile());
    }

    private void logCounter(String name, Counter counter) {
        logger.info(marker, "type / name / count: COUNTER, {}, {}", name, counter.getCount());
    }

    private void logGauge(String name, Gauge gauge) {
        logger.info(marker, "type / name / value: GAUGE, {}, {}", name, gauge.getValue());
    }

    @Override
    protected String getRateUnit() {
        return "events/" + super.getRateUnit();
    }
}
