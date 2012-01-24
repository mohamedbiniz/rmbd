package _dev;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 27.09.11
 * Time: 18:39
 * To change this template use File | Settings | File Templates.
 */
public class TimeLog {
    private static Map<String, Event> timeMap = new HashMap<String, Event>();
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(TimeLog.class.getName());
    private static final boolean LOG = true;
    private static final boolean TRACE = false;
    private static List<List<String>> messages = new LinkedList<List<String>>();
    private static Set<HashMap<String, Event>> timeList = new HashSet<HashMap<String, Event>>();

    private static class Event {
        long time = 0;
        double overall = 0;
        double count = 0;
        String message;
        long max = 0;
        long min = 0;
    }

    public static void start(String message) {
        if (LOG) {
            String name = getMethodName(Thread.currentThread().getStackTrace(), "start");
            start(message, name);
        }
    }

    public static void start(String message, String name) {
        Long time = System.currentTimeMillis();
        Event ev = timeMap.get(name);
        if (ev == null)
            ev = new Event();
        ev.time = time;
        ev.message = message;
        timeMap.put(name, ev);
        if (logger.isTraceEnabled() && TRACE)
            logger.trace("Starting " + name + " " + message);
    }

    private static String getMethodName(StackTraceElement[] e, String caller) {
        boolean doNext = false;
        for (StackTraceElement s : e) {
            if (doNext) {
                return s.getMethodName();
            }
            doNext = s.getMethodName().equals(caller);
        }
        throw new RuntimeException("Method name not found!");
    }

    public static void stop() {
        if (LOG) {
            String stop = getMethodName(Thread.currentThread().getStackTrace(), "stop");
            stop(stop);
        }
    }

    public static void stop(String stop) {
        Event ev = timeMap.get(stop);
        long time = System.currentTimeMillis();
        if (ev==null)
             return ;
        ev.count++;
        long in = time - ev.time;
        ev.overall += in;
        ev.time = 0;
        if (in > ev.max || ev.max == 0)
            ev.max = in;
        if (in < ev.min || ev.min == 0)
            ev.min = in;

        if (logger.isTraceEnabled() && TRACE)
            logger.trace(ev.message + ": " + stop + " finished in " + in + " ms.");
    }

    public static void printStatsAndClear(String message) {
        List<String> msgs = new LinkedList<String>();
        if (TRACE)
        for (String key : timeMap.keySet()) {
            Event ev = timeMap.get(key);
            //  " min/max/avg/total/calls: "
            String msg = message + ": " + key + " " + ev.message + " : " + ev.min + ", "
                    + ev.max + ", " + (ev.overall / ev.count)
                    + ", " + ev.overall + ", " + ev.count;
            logger.trace(msg);
            msgs.add(msg);
        }
        messages.add(msgs);
        timeList.add(new HashMap<String, Event>(timeMap));
        timeMap.clear();
    }

    public static void printOverallStats(String message) {

        for (String key : ((Map<String, Event>) timeList.iterator().next()).keySet()) {
            long min = 0;
            long max = 0;
            long overall = 0;
            double avg = 0;
            double calls = 0;

            for (Map<String, Event> tm : timeList) {
                Event ev = tm.get(key);
                min += ev.min;
                max += ev.max;
                overall += ev.overall;
                avg += (ev.overall / ev.count);
                calls += ev.count;
            }
            String msg = message + ": " + key + ": " + min/timeList.size() + ", " + max/timeList.size() + ", "
                    + avg/timeList.size() + ", " + overall/timeList.size()
                    + ", " +calls/timeList.size();
            logger.info(msg);

        }
            timeList.clear();

    }

}
