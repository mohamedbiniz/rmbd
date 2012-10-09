package at.ainf.diagnosis.quickxplain;

import at.ainf.diagnosis.Searchable;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;

import java.util.Collection;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 16.01.12
 * Time: 11:16
 * To change this template use File | Settings | File Templates.
 */
public class DirectDiagnosis<Id> extends NewQuickXplain<Id> {

    @Override
    protected Collection<Id> applyChanges(Searchable<Id> c, Collection<Id> formulas, Set<Id> changes)
            throws InconsistentTheoryException, SolverException {
        // add axioms to the background theory
        if (changes != null) {
            c.addCheckedBackgroundFormulas(changes);
            for (Id axiom : changes)
                formulas.remove(axiom);}

        return formulas;
    }

    @Override
    protected void rollbackChanges(Searchable<Id> c, Collection<Id> formulas, Set<Id> changes)
            throws InconsistentTheoryException, SolverException {
        if (changes != null)
            c.getKnowledgeBase().removeBackgroundFormulas(changes);
    }

    @Override
    public boolean isDual() {
        return true;
    }

}
