package at.ainf.diagnosis.debugger;

import at.ainf.diagnosis.Searchable;

import at.ainf.diagnosis.storage.AxiomSet;


import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 18.11.11
 * Time: 08:36
 * To change this template use File | Settings | File Templates.
 */
public interface QueryDebugger<Id> {

    public Set<? extends AxiomSet<Id>> getConflictSets();

    public Set<? extends AxiomSet<Id>> getHittingSets();

    public Set<? extends AxiomSet<Id>> getValidHittingSets();

    public Searchable<Id> getTheory();

    public void init();

    void updateMaxHittingSets(int number);

    public boolean debug();

    void reset();

}
