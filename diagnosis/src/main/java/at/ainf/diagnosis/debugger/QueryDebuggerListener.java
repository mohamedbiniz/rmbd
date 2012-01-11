package at.ainf.diagnosis.debugger;

import at.ainf.theory.storage.HittingSet;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.01.12
 * Time: 09:47
 * To change this template use File | Settings | File Templates.
 */
public interface QueryDebuggerListener<Id> {

    public void conflictSetAdded(Set<Set<Id>> conflicts);

    public void hittingSetAdded(Set<? extends HittingSet<Id>> hittingSets);

}
