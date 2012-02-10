package at.ainf.diagnosis.partitioning.postprocessor;

import at.ainf.theory.storage.Partition;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 10.02.12
 * Time: 11:50
 * To change this template use File | Settings | File Templates.
 */
public class SplitInHalfQSS<T> extends AbstractQSS<T> {

    public int getMinNumOfElimDiags(Partition<T> partition) {
        return Math.min(partition.dx.size(), partition.dnx.size());
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

    public Partition<T> run(List<Partition<T>> partitions) {
        return Collections.max(partitions, new MinNumOfElimDiagsComparator());
    }

}
