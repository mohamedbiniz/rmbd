package at.ainf.diagnosis.partitioning;

import at.ainf.diagnosis.partitioning.postprocessor.Postprocessor;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.Partition;

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

    void setPostprocessor(Postprocessor<T> proc);

    public double getThreshold();

    public void setThreshold(double threshold);
}
