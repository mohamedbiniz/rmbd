package at.ainf.diagnosis.logging.old;

import at.ainf.diagnosis.logging.CsvLikeSlf4jReporter;
import com.codahale.metrics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 28.05.13
 * Time: 11:09
 * To change this template use File | Settings | File Templates.
 */
public class MetricsManager {

    private static MetricsManager instance;

    private MetricRegistry timeMetrics;

    private MetricRegistry metrics;

    private CsvLikeSlf4jReporter reporter;

    public static MetricsManager getInstance() {
        if (instance != null)
            return instance;

        instance = new MetricsManager();
        return instance;
    }

    public MetricsManager() {
        metrics = new MetricRegistry();
        timeMetrics = new MetricRegistry();
        reporter = new CsvLikeSlf4jReporter(metrics,LoggerFactory.getLogger("at.ainf.metrics"),null,
                TimeUnit.SECONDS,TimeUnit.MILLISECONDS, MetricFilter.ALL);
    }

    public MetricRegistry getMetrics() {
        return metrics;
    }

    public void logAllMetrics() {
        reporter.report(metrics.getGauges(),metrics.getCounters(),metrics.getHistograms(),
                metrics.getMeters(),metrics.getTimers());
    }


    private class RunningTimer {
        public Timer timer;
        public Timer.Context context;
        public String label;

        public RunningTimer(String label) {
            timer = timeMetrics.timer(label);
            context = timer.time();
            this.label = label;

        }
    }

    private Map<String,Integer> countOfLabels = new HashMap<String, Integer>();

    private List<RunningTimer> runningTimers = new LinkedList<RunningTimer>();


    private int getCounter (String identifier) {
        if (!countOfLabels.containsKey(identifier))
            countOfLabels.put(identifier,0);

        Integer counter = countOfLabels.get(identifier);
        countOfLabels.put(identifier,counter+1);
        return counter;
    }

    public void startNewTimer(String identifier) {
        runningTimers.add(new RunningTimer(identifier + "-" + getCounter(identifier)));
    }

    public long stopTimer() {
        RunningTimer timer = runningTimers.remove(runningTimers.size() - 1);
        return timer.context.stop();
    }

    public long stopAndLogTimer() {
        Logger logger = LoggerFactory.getLogger("at.ainf.metrics");
        RunningTimer timer = runningTimers.remove(runningTimers.size() - 1);
        long time = timer.context.stop();
        logger.info(getLabels() + " " + timer.label + ": " + time + " ns");
        return time;
    }

    public String getLabels() {
        StringBuilder full = new StringBuilder();
        for (RunningTimer timer : runningTimers) {
            full.append(timer.label);
            full.append("/");
        }
        return full.toString();

    }

}
