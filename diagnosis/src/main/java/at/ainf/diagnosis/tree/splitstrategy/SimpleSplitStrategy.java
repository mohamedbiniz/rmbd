package at.ainf.diagnosis.tree.splitstrategy;

import at.ainf.diagnosis.storage.FormulaSet;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: chicco
 * Date: 27.11.12
 * Time: 12:40
 * To change this template use File | Settings | File Templates.
 */
public class SimpleSplitStrategy<Id> implements SplitStrategy<Id> {


    @Override
    /**
     * Returns the first element from the set of conflicts
     */
    public Id getSplitElement(Set<Set<Id>> conflicts) {
        return conflicts.iterator().next().iterator().next();
    }
}
