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

    public Partition<T> run(List<Partition<T>> partitions) {
        lastQuery =  Collections.max(partitions, new MinNumOfElimDiagsComparator());
        return lastQuery;
    }

}
