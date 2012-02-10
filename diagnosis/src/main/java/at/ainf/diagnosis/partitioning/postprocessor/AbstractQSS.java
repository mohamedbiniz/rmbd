package at.ainf.diagnosis.partitioning.postprocessor;

import at.ainf.theory.storage.Partition;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 10.02.12
 * Time: 11:22
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractQSS<T> implements Postprocessor<T> {

    protected double log(double value, double base) {
        if (value == 0)
            return 0;
        return Math.log(value) / Math.log(base);
    }
    
}
