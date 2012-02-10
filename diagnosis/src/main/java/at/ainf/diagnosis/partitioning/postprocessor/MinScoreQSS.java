package at.ainf.diagnosis.partitioning.postprocessor;

import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.Partition;

import java.util.Collections;
import java.util.Comparator;
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
                return 0;

        }
    }
    
    public Partition<T> run(List<Partition<T>> partitions) {
        return Collections.min(partitions,new ScoreComparator());
    }


}
