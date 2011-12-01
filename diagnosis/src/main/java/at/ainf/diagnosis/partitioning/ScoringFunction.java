package at.ainf.diagnosis.partitioning;

import at.ainf.theory.storage.HittingSet;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 11.05.11
 * Time: 09:26
 * To change this template use File | Settings | File Templates.
 */
public interface ScoringFunction<Id> {

    double getScore(Partition<?> part);

    void normalize(Set<? extends HittingSet<Id>> hittingSets);
}
