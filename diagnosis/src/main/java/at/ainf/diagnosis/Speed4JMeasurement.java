package at.ainf.diagnosis;

import com.ecyrd.speed4j.StopWatch;
import com.ecyrd.speed4j.StopWatchFactory;
import com.ecyrd.speed4j.log.Slf4jLog;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 01.05.13
 * Time: 11:35
 * To change this template use File | Settings | File Templates.
 */
public class Speed4JMeasurement {

    private static Map<String,Integer> countOfLabels = new HashMap<String, Integer>();

    private static List<StopWatch> runningStopWatches = new LinkedList<StopWatch>();

    private static StopWatchFactory swf;

    private static boolean init = false;

    private static void init() {
        Slf4jLog myLog = new Slf4jLog();
        myLog.setName("normalFactory");
        myLog.setSlf4jLogname("at.ainf.speed4j");
        swf = StopWatchFactory.getInstance(myLog);
    }

    private static int getCounter (String identifier) {
        if (!countOfLabels.containsKey(identifier))
            countOfLabels.put(identifier,0);

        Integer counter = countOfLabels.get(identifier);
        countOfLabels.put(identifier,counter+1);
        return counter;
    }

    public static void start(String identifier) {
        if(!init)
            init();

        String label = identifier + "-" + getCounter(identifier);
        if (!runningStopWatches.isEmpty())
            label = runningStopWatches.get(runningStopWatches.size()-1).getTag() + ", " + label;

        runningStopWatches.add(swf.getStopWatch(label));
    }

    public static long stop() {
        StopWatch sw = runningStopWatches.remove(runningStopWatches.size()-1);
        sw.stop();
        return sw.getTimeMicros();
    }

    public static String getLabelOfLastStopWatch() {
        if (runningStopWatches.isEmpty())
            return "";
        return runningStopWatches.get(runningStopWatches.size()-1).getTag();
    }

}
