package at.ainf.theory.storage;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 03.02.12
 * Time: 10:42
 * To change this template use File | Settings | File Templates.
 */
public class ConflictSet<Id> extends AbstrAxiomSet<Id> {

    protected ConflictSet(String name, double measure, Set<Id> hittingSet, Set<Id> entailments) {
        super(TypeOfSet.CONFLICT_SET, name, measure, hittingSet, entailments);
    }

}
