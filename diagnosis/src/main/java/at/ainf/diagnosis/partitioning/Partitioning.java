package at.ainf.diagnosis.partitioning;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.storage.Partition;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 02.05.11
 * Time: 16:19
 * To change this template use File | Settings | File Templates.
 */
public interface Partitioning<T> {
    <E extends AxiomSet<T>> Partition<T> generatePartition(Set<E> hittingSets) throws SolverException, InconsistentTheoryException;

    <E extends AxiomSet<T>> Partition<T> nextPartition(Partition<T> partition) throws SolverException, InconsistentTheoryException;

    public double getThreshold();

    public void setThreshold(double threshold);

    public int getNumOfHittingSets();

    boolean verifyPartition(Partition<T> partition) throws SolverException, InconsistentTheoryException;
}
