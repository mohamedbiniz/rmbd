package at.ainf.theory.storage;

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
    public Set<AxiomSet<T>> dx = new LinkedHashSet<AxiomSet<T>>();
    public Set<AxiomSet<T>> dnx = new LinkedHashSet<AxiomSet<T>>();
    public Set<AxiomSet<T>> dz = new LinkedHashSet<AxiomSet<T>>();

    public Set<T> partition;
    public double score = 0;
    public double difference = Double.MAX_VALUE;
}
