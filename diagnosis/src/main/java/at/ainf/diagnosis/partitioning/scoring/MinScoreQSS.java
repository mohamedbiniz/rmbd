package at.ainf.diagnosis.partitioning.scoring;

import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.Partition;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 10.02.12
 * Time: 11:19
 * To change this template use File | Settings | File Templates.
 */
public class MinScoreQSS<T> extends AbstractQSS<T> {

    private static Logger logger = Logger.getLogger(MinScoreQSS.class.getName());

    public double getScore(Partition<?> partition) {
        if (partition == null || partition.dx.isEmpty())
            return Double.MAX_VALUE;
        if (partition.score < Double.MAX_VALUE)
            return partition.score;
        double pX = sum(partition.dx) + sum(partition.dz) / 2;
        double pNX = sum(partition.dnx) + sum(partition.dz) / 2;

        double sc = pX * log(pX, 2) + pNX * log(pNX, 2) + sum(partition.dz) + 1;


        if (sc < 0) {
            logger.error("Score is less that 0! sc=" + sc);
            sc = 0;
        }

        partition.score = sc;
        return sc;
    }

    private double sum(Set<? extends AxiomSet> dx) {
        double sum = 0;
        for (AxiomSet hs : dx)
            sum += hs.getMeasure();
        return sum;
    }

    public String toString() {
        return "Entropy";
    }

    public void normalize(Set<? extends AxiomSet<T>> hittingSets) {
        double sum = sum(hittingSets);
        for (AxiomSet<T> hs : hittingSets) {
            double value = hs.getMeasure() / sum;
            hs.setMeasure(value);
        }

    }
    
    public Partition<T> runPostprocessor(List<Partition<T>> partitions, Partition<T> currentBest) throws SolverException, InconsistentTheoryException {
        //if (partitions!=null && partitions.size() > 0) {
        //    lastQuery = Collections.min(partitions,new ScoreComparator());
        //    return lastQuery;
        //}

        return currentBest;
    }


}
