package at.ainf.diagnosis.partitioning;

import at.ainf.diagnosis.Searchable;
import at.ainf.diagnosis.model.AbstractSearchableObject;
import at.ainf.diagnosis.model.ITheory;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.storage.Partition;

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

    public boolean verifyConsistency() throws SolverException {
        return verifyQuery(getFormulaStack());
    }

    public void addBackgroundFormulas(Set<Id> formulas) throws InconsistentTheoryException, SolverException {
        theory.addBackgroundFormulas(formulas);
    }

    public void removeBackgroundFormulas(Set<Id> formulas) throws InconsistentTheoryException, SolverException {
        theory.removeBackgroundFormulas(formulas);
    }

    public Set<Id> getBackgroundFormulas() {
        return theory.getBackgroundFormulas();
    }
}
