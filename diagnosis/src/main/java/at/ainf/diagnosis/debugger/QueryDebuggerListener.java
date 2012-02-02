package at.ainf.diagnosis.debugger;

import at.ainf.theory.storage.AxiomSet;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.01.12
 * Time: 09:47
 * To change this template use File | Settings | File Templates.
 */
public interface QueryDebuggerListener<Id> {

    public void conflictSetAdded(Set<? extends AxiomSet<Id>> conflicts);

    public void hittingSetAdded(Set<? extends AxiomSet<Id>> hittingSets);

}
