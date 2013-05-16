package at.ainf.owlapi3.module.iterative.diag;

import java.util.*;

import at.ainf.diagnosis.Speed4JMeasurement;
import org.slf4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 06.05.13
 * Time: 09:32
 * To change this template use File | Settings | File Templates.
 */
public class IterativeStatistics {

    public static List<Long> numberCS = new LinkedList<Long>();

    public static Average avgCardCS = new Average();

    public static List<Long> cardHS = new LinkedList<Long>();

    public static List<Long> moduleSize = new LinkedList<Long>();

    public static List<Long> diagnosisTime = new LinkedList<Long>();

    public static List<Long> moduleTime = new LinkedList<Long>();

    public static Average avgConsistencyTime = new Average();

    public static Average avgCoherencyTime = new Average();

    public static Average avgConsistencyCheck = new Average(Average.Mode.SUM);

    public static Average avgCoherencyCheck = new Average(Average.Mode.SUM);

    public static List<Long> numOfQueries = new LinkedList<Long>();

    public static Average avgTimeQueryGen = new Average();

    public static Average avgQueryCard = new Average();

    public static Average avgReactTime = new Average();

    public static void logAndClear (Logger log, Average set, String message) {
        logAndClear(log,set.toAverageCollection(),message);
        set.reset();
    }

    public static void logAndClear (Logger log, Collection<Long> set, String message) {
        Long min = 0L;
        Double mean = 0.0;
        Long max = 0L;
        if (!set.isEmpty()) {
            min = Collections.min(set);
            mean = Average.mean(set);
            max = Collections.max(set);
        }

        log.info(Speed4JMeasurement.getLabelOfLastStopWatch() + " " + message + ", (min,mean,max,numofentries), " + min + ", " + mean + ", " + max + ", " + set.size());
        set.clear();

    }

}
