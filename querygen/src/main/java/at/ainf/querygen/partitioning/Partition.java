package at.ainf.querygen.partitioning;

import at.ainf.theory.storage.HittingSet;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 11.05.11
 * Time: 09:33
 * To change this template use File | Settings | File Templates.
 */
public class Partition<T> {
    public Set<HittingSet<T>> dx = new LinkedHashSet<HittingSet<T>>();
    public Set<HittingSet<T>> dnx = new LinkedHashSet<HittingSet<T>>();
    public Set<HittingSet<T>> dz = new LinkedHashSet<HittingSet<T>>();

    public Set<T> partition;
    public double score = 0;
    public double difference = Double.MAX_VALUE;
}
