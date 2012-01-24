package at.ainf.diagnosis.partitioning;

import at.ainf.theory.Searchable;
import at.ainf.theory.model.AbstractSearchableObject;
import at.ainf.theory.model.ITheory;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.AxiomSet;
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
        for (AxiomSet<Id> hs : partition.dx) {
            if (!getTheory().diagnosisEntails(hs, query))
                return true;
        }
        for (AxiomSet<Id> hs : partition.dnx) {
            if (getTheory().diagnosisConsistent(hs, query))
                return true;
        }
        for (AxiomSet<Id> hs : partition.dz) {
            if (getTheory().diagnosisEntails(hs, query) || !getTheory().diagnosisConsistent(hs, query))
                return true;

        }
        return false;
    }

    @Override
    protected boolean verifyConsistency() throws SolverException {
        return verifyQuery(getFormulaStack());
    }

    public void addBackgroundFormulas(Set<Id> formulas) throws InconsistentTheoryException, SolverException {
        theory.addBackgroundFormulas(formulas);
    }

    public void removeBackgroundFormulas(Set<Id> formulas) throws InconsistentTheoryException, SolverException {
        theory.removeBackgroundFormulas(formulas);
    }
}
