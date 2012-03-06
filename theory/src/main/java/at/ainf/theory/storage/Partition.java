package at.ainf.theory.storage;

import java.math.BigDecimal;
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
    public double score = Double.MAX_VALUE;
    public BigDecimal difference = new BigDecimal(Double.MAX_VALUE);
    public boolean isVerified = false;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Partition partition1 = (Partition) o;

        if (!dnx.equals(partition1.dnx)) return false;
        if (!dx.equals(partition1.dx)) return false;
        if (!dz.equals(partition1.dz)) return false;
        //if (!partition.equals(partition1.partition)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = dx.hashCode();
        result = 31 * result + dnx.hashCode();
        result = 31 * result + dz.hashCode();
        //result = 31 * result + partition.hashCode();
        return result;
    }
}
