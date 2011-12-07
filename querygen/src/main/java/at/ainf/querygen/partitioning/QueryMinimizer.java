package at.ainf.querygen.partitioning;

import at.ainf.theory.Searchable;
import at.ainf.theory.model.AbstractSearchableObject;
import at.ainf.theory.model.ITheory;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.HittingSet;
import at.ainf.theory.storage.Partition;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 13.07.11
 * Time: 18:46
 * To change this template use File | Settings | File Templates.
 */
public class QueryMinimizer<Id> extends AbstractSearchableObject<Id> implements Searchable<Id> {

    private Partition<Id> partition;
    private ITheory<Id> theory;

    public QueryMinimizer(Partition<Id> partition, ITheory<Id> theory) {
        this.theory = theory;
        this.partition = partition;
    }

    public ITheory<Id> getTheory() {
        return theory;
    }

    private Boolean verifyQuery(Set<Id> query) {
        for (HittingSet<Id> hs : partition.dx) {
            if (!getTheory().diagnosisEntails(hs, query))
                return true;
        }
        for (HittingSet<Id> hs : partition.dnx) {
            if (getTheory().diagnosisConsistent(hs, query))
                return true;
        }
        for (HittingSet<Id> hs : partition.dz) {
            if (getTheory().diagnosisEntails(hs, query) || !getTheory().diagnosisConsistent(hs, query))
                return true;

        }
        return false;
    }

    @Override
    protected boolean verifyConsistency() throws SolverException {
        return verifyQuery(getFormulaStack());
    }
}
