package at.ainf.diagnosis.partitioning;

import at.ainf.diagnosis.Searchable;
import at.ainf.diagnosis.model.AbstractSearchableObject;
import at.ainf.diagnosis.model.IKnowledgeBase;
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
    private Searchable<Id> theory;

    public QueryMinimizer(Partition<Id> partition, Searchable<Id> theory) {
        this.theory = theory;
        this.partition = partition;
    }

    public Searchable<Id> getTheory() {
        return theory;
    }

    @Override
    public IKnowledgeBase<Id> getKnowledgeBase() {
        return theory.getKnowledgeBase();
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


    /*public void addBackgroundFormulas(Set<Id> formulas) {
        theory.addBackgroundFormulas(formulas);
    }*/

    public void addCheckedBackgroundFormulas(Set<Id> formulas) throws InconsistentTheoryException, SolverException {
        theory.addCheckedBackgroundFormulas(formulas);
    }

    /*public void removeBackgroundFormulas(Set<Id> formulas) {
        theory.removeBackgroundFormulas(formulas);
    }

    public Set<Id> getBackgroundFormulas() {
        return theory.getBackgroundFormulas();
    } */

}
