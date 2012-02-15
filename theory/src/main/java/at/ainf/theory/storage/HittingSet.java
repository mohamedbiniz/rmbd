package at.ainf.theory.storage;

import at.ainf.theory.model.ITheory;
import at.ainf.theory.model.SolverException;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 03.02.12
 * Time: 10:42
 * To change this template use File | Settings | File Templates.
 */
public class HittingSet<Id> extends AbstrAxiomSet<Id> {

    protected HittingSet(String name, double measure, Set<Id> hittingSet, Set<Id> entailments) {
        super(TypeOfSet.HITTING_SET, name, measure, hittingSet, entailments);
    }

}
