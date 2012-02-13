package at.ainf.diagnosis.partitioning.postprocessor;

import at.ainf.theory.storage.Partition;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 13.02.12
 * Time: 17:52
 * To change this template use File | Settings | File Templates.
 */
public class PenaltyQSS<T> extends MinScoreQSS<T> {

    double maxPenalty;

    double penalty;
    
    private Partition<T> lastQuery;
    
    private boolean answerToLastQuery;

    public PenaltyQSS(double maxPenalty) {
        this.maxPenalty = maxPenalty;
        penalty = 0d;
    }

    protected boolean isGreaterMaxPenalty (Partition<T> partition) {
        return penalty + getMaxPenaltyOfQuery(partition) > maxPenalty;
    }

    protected int getNumOfAllDiags(Partition<T> partition) {
        return partition.dx.size() + partition.dnx.size() + partition.dz.size();
    }

    protected int getMaxPenaltyOfQuery(Partition<T> partition){
        return (int)Math.floor((double)getNumOfAllDiags(partition)/(double)2) - getMinNumOfElimDiags(partition);
    }

    public class MaxPenaltyComparator implements Comparator<Partition<T>> {
        public int compare(Partition<T> o1, Partition<T> o2) {
            if (getMaxPenaltyOfQuery(o1) < getMaxPenaltyOfQuery(o2))
                return -1;
            else if (getMaxPenaltyOfQuery(o1) > getMaxPenaltyOfQuery(o2))
                return 1;
            else
                return 0;

        }
    }

    private void updatePenalty(){
        int numOfEliminatedDiags;

        if (answerToLastQuery)
            numOfEliminatedDiags = lastQuery.dnx.size();
        else
            numOfEliminatedDiags = lastQuery.dx.size();

        penalty += (int)Math.floor((double)getNumOfAllDiags(lastQuery)/(double)2) - numOfEliminatedDiags;
    }

    public void setAnswerToLastQuery(boolean answer) {
        answerToLastQuery = answer;
    }

    public Partition<T> run(List<Partition<T>> partitions) {
        List<Partition<T>> c = new LinkedList<Partition<T>>();

        if(lastQuery!=null)
            updatePenalty();

        for (Partition<T> partition : partitions) {
            if (!isGreaterMaxPenalty(partition))
                c.add(partition);
        }
        
        // TODO what if we have no candidates?
        if (c.isEmpty())
            return null;

        Partition<T> result = Collections.min(c,new ScoreComparator());
        lastQuery = result;
        
        return result;
        
    }

}
