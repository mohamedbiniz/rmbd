package at.ainf.owlapi3.module;

import at.ainf.diagnosis.Searchable;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.ModuleProvider;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.model.OWLTheory;
import org.semanticweb.owlapi.model.OWLClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static Logger logger = LoggerFactory.getLogger(SatisfiableQuickXplain.class.getName());

    private OWLClass unsatClass;


    // module test

    private ModuleProvider<Id> moduleProvider;

    public ModuleProvider<Id> getModuleProvider() {
        return moduleProvider;
    }

    public void setModuleProvider(ModuleProvider<Id> moduleProvider) {
        this.moduleProvider = moduleProvider;
    }

    protected void makeSmaller(final Collection<Id> u,Set<Id> backgroundFormulars) {
        if (getModuleProvider() != null && !u.isEmpty()) {
            HashSet<Id> toMakeSmaller = new HashSet<Id>();
            toMakeSmaller.addAll(u);
            toMakeSmaller.addAll(backgroundFormulars);
            Set<Id> smaller = getModuleProvider().getSmallerModule(toMakeSmaller);
            int sizeofU = u.size();
            u.retainAll(smaller);
            logger.info("axioms was: " + sizeofU + " reduced to: " + u.size());
            unsatClass = ((OtfModuleProvider)getModuleProvider()).getUnsatClass();
        }
    }

    public boolean verifyKnowledgeBase(Searchable<Id> c, Collection<Id> u) throws SolverException, InconsistentTheoryException, NoConflictException {
        if (!c.verifyRequirements())
            throw new InconsistentTheoryException("Background theory or test cases are inconsistent! Finding conflicts is impossible!");
        getReasoner().addFormulasToCache(u);
        // here we have to make u smaller
        boolean isCons = false;
        Collection<Id> backup = new HashSet<Id>(u);
        if (getModuleProvider() != null && !u.isEmpty()) {
            makeSmaller(u,c.getKnowledgeBase().getBackgroundFormulas());
            if (u.isEmpty()) {
                u.addAll(backup);
                isCons = true;
            }
        }


        if (isCons) {
            throw new NoConflictException("The theory is satisfiable!");
        }
        if (u.isEmpty()) {
            return true;
        }
        getReasoner().removeFormulasFromCache(backup);
        return false;
    }

    /*protected boolean checkRequirements (Searchable<Id> b) throws SolverException {
        if (unsatClass == null)
            return super.checkRequirements(b);
        else
            return ((OWLTheory)b).verifySatisfiability(unsatClass);
    }*/

}
