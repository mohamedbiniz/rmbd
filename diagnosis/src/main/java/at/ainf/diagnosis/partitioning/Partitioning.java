package at.ainf.diagnosis.partitioning;

import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.HittingSet;
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
    <E extends HittingSet<T>> Partition<T> generatePartition(Set<E> hittingSets) throws SolverException, InconsistentTheoryException;

    void setPostprocessor(Postprocessor proc);

    public double getThreshold();

    public void setThreshold(double threshold);
}
