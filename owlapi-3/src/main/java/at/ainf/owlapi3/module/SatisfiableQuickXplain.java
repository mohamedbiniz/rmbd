package at.ainf.owlapi3.module;

import at.ainf.diagnosis.Searchable;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.owlapi3.model.OWLTheory;
import org.semanticweb.owlapi.model.OWLClass;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.02.13
 * Time: 14:45
 * To change this template use File | Settings | File Templates.
 */
public class SatisfiableQuickXplain<Id> extends QuickXplain<Id> {

    private OWLClass unsatClass;

    protected void makeSmaller(final Collection<Id> u,Set<Id> backgroundFormulars) {
        super.makeSmaller(u, backgroundFormulars);
        if (getModuleProvider() != null && !u.isEmpty()) {
             unsatClass = ((OtfModuleProvider)getModuleProvider()).getUnsatClass();
        }
    }

    protected boolean checkRequirements (Searchable<Id> b) throws SolverException {
        if (unsatClass == null)
            return super.checkRequirements(b);
        else
            return ((OWLTheory)b).verifySatisfiability(unsatClass);
    }

}
