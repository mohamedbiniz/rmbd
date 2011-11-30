package at.ainf.diagnosis.querydebug;

import at.ainf.diagnosis.model.ITheory;

import at.ainf.diagnosis.partitioning.Partition;
import at.ainf.diagnosis.partitioning.ScoringFunction;
import at.ainf.diagnosis.storage.HittingSet;


import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 18.11.11
 * Time: 08:36
 * To change this template use File | Settings | File Templates.
 */
public interface QueryDebugger<Id> {

    public Set<Set<Id>> getConflictSets();

    public Set<? extends HittingSet<Id>> getHittingSets();

    public Set<? extends HittingSet<Id>> getValidHittingSets();

    public ITheory<Id> getTheory();

    public void init();

    Partition<Id> getQuery(ScoringFunction<Id> f, boolean minimizeQuery, double acceptanceThreshold);

    void updateMaxHittingSets(int number);

    public boolean debug();

    void dispose();

    public boolean resume();

}
