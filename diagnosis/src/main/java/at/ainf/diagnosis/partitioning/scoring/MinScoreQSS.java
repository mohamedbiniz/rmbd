package at.ainf.diagnosis.partitioning.scoring;

import static at.ainf.diagnosis.tree.Rounding.*;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.storage.Partition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
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

    private static Logger logger = LoggerFactory.getLogger(MinScoreQSS.class.getName());

    public BigDecimal getScore(Partition<?> partition) {
        if (partition == null || partition.dx.isEmpty())
            return BigDecimal.valueOf(Double.MAX_VALUE);
        if (partition.score.compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) < 0)
            return partition.score;
        BigDecimal pX = sum(partition.dx).add(sum(partition.dz).divide(BigDecimal.valueOf(2)));
        BigDecimal pNX = sum(partition.dnx).add(sum(partition.dz).divide(BigDecimal.valueOf(2)));

        BigDecimal t1 = pX.multiply(log(pX, BigDecimal.valueOf(2)));
        BigDecimal t2 = pNX.multiply(log(pNX, BigDecimal.valueOf(2)));

        BigDecimal sc = t1.add(t2.add(sum(partition.dz).add(BigDecimal.ONE)));

        if (sc.compareTo(BigDecimal.ZERO) < 0) {
            logger.error("Score is less that 0! sc=" + sc);
            sc = BigDecimal.ZERO;

        }

        partition.score = sc;
        return sc;
    }

    private BigDecimal sum(Set<? extends AxiomSet> dx) {
        BigDecimal sum = new BigDecimal("0");
        for (AxiomSet hs : dx)
            sum = sum.add(hs.getMeasure());
        return sum;
    }

    public String toString() {
        return "Entropy";
    }

    public void normalize(Set<? extends AxiomSet<T>> hittingSets) {
        BigDecimal sum = sum(hittingSets);
        for (AxiomSet<T> hs : hittingSets) {
            BigDecimal value = hs.getMeasure().divide(sum,PRECISION,ROUNDING_MODE);
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
