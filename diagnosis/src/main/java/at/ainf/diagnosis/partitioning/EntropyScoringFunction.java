package at.ainf.diagnosis.partitioning;

import at.ainf.theory.storage.HittingSet;
import org.apache.log4j.Logger;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 11.05.11
 * Time: 09:29
 * To change this template use File | Settings | File Templates.
 */
public class EntropyScoringFunction<Id> implements ScoringFunction<Id> {

    private static Logger logger = Logger.getLogger(EntropyScoringFunction.class.getName());

    public double getScore(Partition<?> partition) {
        if (partition == null || partition.dx.isEmpty())
            return Double.MAX_VALUE;
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

    private double log(double value, double base) {
        if (value == 0)
            return 0;
        return Math.log(value) / Math.log(base);
    }

    private double sum(Set<? extends HittingSet> dx) {
        double sum = 0;
        for (HittingSet hs : dx)
            sum += hs.getMeasure();
        return sum;
    }

    public String toString() {
        return "Entropy";
    }

    public void normalize(Set<? extends HittingSet<Id>> hittingSets) {
        double sum = sum(hittingSets);
        for (HittingSet<Id> hs : hittingSets) {
            double value = hs.getMeasure() / sum;
            hs.setMeasure(value);
        }

    }


}
