package at.ainf.diagnosis.partitioning.scoring;

import at.ainf.diagnosis.partitioning.Partitioning;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.storage.Partition;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 01.06.11
 * Time: 08:44
 * To change this template use File | Settings | File Templates.
 */
public interface Scoring<T> {
    Partition<T> runPostprocessor(List<Partition<T>> partitions, Partition<T> currentBest) throws SolverException, InconsistentTheoryException;

    void setPartitionSearcher(Partitioning<T> partitioning);

    BigDecimal getScore(Partition<?> part);

    void normalize(Set<? extends AxiomSet<T>> hittingSets);
}