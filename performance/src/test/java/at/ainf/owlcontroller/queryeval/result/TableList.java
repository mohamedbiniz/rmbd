package at.ainf.owlcontroller.queryeval.result;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 20.05.11
 * Time: 14:14
 * To change this template use File | Settings | File Templates.
 */
public class TableList {

    private LinkedList<Integer> queriesList = new LinkedList<Integer>();
     private LinkedList<Integer> queriesCardList = new LinkedList<Integer>();

    private LinkedList<Boolean> diagInWin = new LinkedList<Boolean>();

    private LinkedList<Boolean> mostProbableD = new LinkedList<Boolean>();

    private LinkedList<Integer> sizeOfWindow = new LinkedList<Integer>();

    private LinkedList<Boolean> userBreak = new LinkedList<Boolean>();

    private LinkedList<Boolean> systemBreak = new LinkedList<Boolean>();

    private LinkedList<Long> time = new LinkedList<Long>();

    private LinkedList<Time> queryTime = new LinkedList<Time>();
    private LinkedList<Time> diagTime = new LinkedList<Time>();
    private int consistencyChecks;
    private long reactionTime;
    private int calls = 0;


    public void addEntr(Integer query, int queryCardinality, Boolean isDiagInWindow,
                        Boolean mostProbable, Integer sizeOfWindow, Boolean userB,
                        Boolean systemB, long time, Time queryTime, Time diagTime, long reactionTime, int consistencyChecks) {
        queriesList.add(query);
        queriesCardList.add(queryCardinality);
        diagInWin.add(isDiagInWindow);
        mostProbableD.add(mostProbable);
        this.sizeOfWindow.add(sizeOfWindow);
        userBreak.add(userB);
        systemBreak.add(systemB);
        this.time.add(time);
        this.queryTime.add(queryTime);
        this.diagTime.add(diagTime);
        if(query!=0) {
            queryTime.setCalls(query);
            diagTime.setCalls(query);
        }
        this.consistencyChecks += consistencyChecks;
        this.reactionTime += reactionTime;
        this.calls++;
    }
    
    public double getReactionTime(){
        return this.reactionTime/calls;
    }
    
    public double getConsistencyChecks(){
        return this.consistencyChecks/calls;
    }

    public int getMaxQuery() {
        if (queriesList.isEmpty())
            return 0;
        return Collections.max(queriesList);
    }

    public double getAvgQueryCardinality() {
       double result = 0;

        for (Integer a : queriesCardList)
            result += a;

        return result / queriesCardList.size();
    }

    public int getMinQuery() {
        if (queriesList.isEmpty())
            return 0;
        return Collections.min(queriesList);
    }

    public double getMeanQuery() {
        double result = 0;

        for (Integer a : queriesList)
            result += a;

        return result / queriesList.size();
    }

    public long getMaxTime() {
        if (this.time.isEmpty())
            return 0;
        return Collections.max(this.time);
    }

    public long getMinTime() {
        if (this.time.isEmpty())
            return 0;
        return Collections.min(this.time);
    }

    public double getMeanTime() {
        double result = 0;

        for (Long a : this.time)
            result += a;

        return result / this.time.size();
    }

    public int getMaxWin() {
        if (sizeOfWindow.isEmpty())
            return 0;
        return Collections.max(sizeOfWindow);
    }

    public int getMinWin() {
        if (sizeOfWindow.isEmpty())
            return 0;
        return Collections.min(sizeOfWindow);
    }

    public double getMeanWin() {
        double result = 0;

        for (Integer a : sizeOfWindow)
            result += a;

        return result / sizeOfWindow.size();
    }

    public int getTargetDiagInWindowCount() {
        int cnt = 0;
        for (Boolean isDiagInWin : diagInWin)
            if (isDiagInWin) cnt++;
        return cnt;
    }

    public int getUserBreakCount() {
        int cnt = 0;
        for (Boolean isDiagInWin : userBreak)
            if (isDiagInWin) cnt++;
        return cnt;
    }

    public int getSystemBreakCount() {
        int cnt = 0;
        for (Boolean isDiagInWin : systemBreak)
            if (isDiagInWin) cnt++;
        return cnt;
    }

    public double getMeanTargetDiagInWin() {
        int cnt = 0;
        double result = 0;

        for (int j = 0; j < queriesList.size(); j++) {
            if (diagInWin.get(j)) {
                result += queriesList.get(j);
                cnt++;
            }
        }
        result = result / cnt;

        return result;
    }

    public int getCouTargetDiagIsMostProbable() {
        int cnt = 0;
        for (Boolean is : mostProbableD)
            if (is) cnt++;
        return cnt;
    }

    public double getMeanTargetDiagIsMostProbable() {
        int cnt = 0;
        double result = 0;

        for (int j = 0; j < queriesList.size(); j++) {
            if (mostProbableD.get(j)) {
                result += queriesList.get(j);
                cnt++;
            }
        }
        result = result / cnt;

        return result;
    }

    public LinkedList<Time> getQueryTime() {
        return queryTime;
    }

    public LinkedList<Time> getDiagTime() {
        return diagTime;
    }

    public long getMinTime(List<Time> queryTime) {
        return Collections.min(queryTime, new Comparator<Time>() {
            public int compare(Time o1, Time o2) {
                return ((Long) o1.min).compareTo(o2.min);
            }
        }).min;
    }

    public long getMaxTime(List<Time> queryTime) {
        return Collections.max(queryTime, new Comparator<Time>() {
            public int compare(Time o1, Time o2) {
                return ((Long) o1.max).compareTo(o2.max);
            }
        }).max;
    }

    public long getAvgTime(List<Time> queryTime) {
        long sum = 0;
        for (Time time : queryTime)
            sum += time.avg;
        return sum / queryTime.size();
    }


}
