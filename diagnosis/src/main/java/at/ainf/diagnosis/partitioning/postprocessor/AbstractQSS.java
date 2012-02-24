package at.ainf.diagnosis.partitioning.postprocessor;

import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.Partition;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 10.02.12
 * Time: 11:22
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractQSS<T> implements QSS<T> {

    protected int numOfLeadingDiags;
    protected Partition<T> lastQuery = null;
    protected boolean answerToLastQuery;
    protected int numOfEliminatedLeadingDiags = 0;


    protected double log(double value, double base) {
        if (value == 0)
            return 0;
        return Math.log(value) / Math.log(base);
    }

    protected int getMinNumOfElimDiags(Partition<T> partition) {
        return Math.min(partition.dx.size(), partition.dnx.size());
    }

    public void updateNumOfLeadingDiags(Partition<T> partition) {
        numOfLeadingDiags = getNumOfLeadingDiags(partition);
    }

    public void updateAnswerToLastQuery(boolean answer) {
        answerToLastQuery = answer;
    }

    protected void updateNumOfEliminatedLeadingDiags(boolean answer){
        if(lastQuery != null)
            numOfEliminatedLeadingDiags = getNumOfEliminatedLeadingDiags(answer);
    }

    protected void preprocessBeforeUpdate(boolean answer){
        updateAnswerToLastQuery(answer);
        updateNumOfEliminatedLeadingDiags(answer);
    }

    protected void preprocessBeforeRun(List<Partition<T>> partitions){
        updateNumOfLeadingDiags(partitions.get(0));
    }

    protected int getNumOfLeadingDiags(Partition<T> partition){
        return partition.dx.size() + partition.dnx.size() + partition.dz.size();
    }

    protected int getNumOfEliminatedLeadingDiags(boolean answer){
        if (answer)
            return lastQuery.dnx.size();
        else
            return lastQuery.dx.size();
    }

    public void updateParameters(boolean answer) {
        preprocessBeforeUpdate(answer);
    }


    public class MinNumOfElimDiagsComparator implements Comparator<Partition<T>> {
        public int compare(Partition<T> o1, Partition<T> o2) {
            if (getMinNumOfElimDiags(o1) < getMinNumOfElimDiags(o2))
                return -1;
            else if (getMinNumOfElimDiags(o1) > getMinNumOfElimDiags(o2))
                return 1;
            else
                return 0;

        }
    }

    protected double getScore(Partition<T> partition) {
        double sumDx = getSumProb(partition.dx);
        double sumDnx = getSumProb(partition.dnx);
        double sumD0 = getSumProb(partition.dz);

        return sumDx * log(sumDx, 2d)
               + sumDnx * log( sumDnx, 2d)
               + sumD0 + 1d;
    }

    protected double getSumProb(Set<AxiomSet<T>> set) {
        double pr = 0;
        for (AxiomSet<T> diagnosis : set)
            pr += diagnosis.getMeasure();

        return pr;
    }

    public class ScoreComparator implements Comparator<Partition<T>> {
        public int compare(Partition<T> o1, Partition<T> o2) {
            if (getScore(o1) < getScore(o2))
                return -1;
            else if (getScore(o1) > getScore(o2))
                return 1;
            else
                return -1*((Integer)o1.dx.size()).compareTo(o2.dx.size());

        }
    }
    
}
