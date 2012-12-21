package at.ainf.diagnosis.quickxplain;

import at.ainf.diagnosis.Searchable;
import at.ainf.diagnosis.model.AbstractReasoner;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.FormulaSet;

import java.util.Collection;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 16.01.12
 * Time: 11:16
 * To change this template use File | Settings | File Templates.
 */
public class DirectDiagnosis<Id> extends QuickXplain<Id> {

    @Override
    protected Collection<Id> applyChanges(Searchable<Id> c, Collection<Id> formulas, Set<Id> changes)
            throws InconsistentTheoryException, SolverException {
        // add axioms to the background theory
        //formulas = super.applyChanges(c, formulas, changes);

        getReasoner().setBackgroundAxioms(c.getKnowledgeBase().getBackgroundFormulas());
        if (changes != null){
            getReasoner().getBackgroundAxioms().addAll(changes);
            for (Id axiom : changes)
                formulas.remove(axiom);
        }
        getReasoner().lock();
        c.getKnowledgeBase().lock();

        return formulas;
    }

    @Override
    protected void rollbackChanges(Searchable<Id> c, Collection<Id> formulas, Set<Id> changes)
            throws InconsistentTheoryException, SolverException {
        super.rollbackChanges(c,formulas,changes);
    }

    @Override
    public void postProcessFormulas(FormulaSet<Id> formulas, Searchable<Id> searchable) throws SolverException {
        ((AbstractReasoner<Id>)searchable.getReasoner()).addFormulasToCache(formulas);
        searchable.verifyRequirements();
    }

    //public boolean isDual() {
    //    return true;
    //}

}
