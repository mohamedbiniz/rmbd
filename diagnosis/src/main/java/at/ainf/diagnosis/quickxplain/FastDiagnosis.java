package at.ainf.diagnosis.quickxplain;

import at.ainf.theory.Searchable;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;

import java.util.Collection;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 16.01.12
 * Time: 11:16
 * To change this template use File | Settings | File Templates.
 */
public class FastDiagnosis<Id> extends NewQuickXplain<Id> {

    @Override
    protected Collection<Id> applyChanges(Searchable<Id> c, Collection<Id> formulas, Set<Id> changes)
            throws InconsistentTheoryException, SolverException {
        // add axioms to the background theory
        if (changes != null) {
            c.addBackgroundFormulas(changes);
            for (Id axiom : changes)
                formulas.remove(axiom);}

        return formulas;
    }

    @Override
    protected void rollbackChanges(Searchable<Id> c, Collection<Id> formulas, Set<Id> changes)
            throws InconsistentTheoryException, SolverException {
        if (changes != null)
            c.removeBackgroundFormulas(changes);
    }

    @Override
    public boolean isDual() {
        return true;
    }

}
