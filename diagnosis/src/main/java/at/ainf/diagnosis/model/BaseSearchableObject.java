package at.ainf.diagnosis.model;

import at.ainf.diagnosis.Searchable;
import at.ainf.diagnosis.storage.FormulaSet;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 13.07.11
 * Time: 18:52
 * To change this template use File | Settings | File Templates.
 */
public class BaseSearchableObject<T> implements Searchable<T> {

    private IKnowledgeBase<T> knowledgeBase;

    private IReasoner<T> reasoner;

    private Lock lock = new ReentrantLock(true);

    public BaseSearchableObject() {
        setKnowledgeBase(new KnowledgeBase<T>());
    }

    public BaseSearchableObject(IKnowledgeBase<T> knowledgeBase, AbstractReasoner<T> reasoner) {
        setKnowledgeBase(knowledgeBase);
        setReasoner(reasoner);
    }

    public IReasoner<T> getReasoner() {
        return reasoner;
    }

    public void setReasoner(IReasoner<T> reasoner) {
        this.reasoner = reasoner;
    }

    public boolean verifyRequirements() throws SolverException {
        return verifyConsistency();
    }

    public boolean supportEntailments() {
        return false;
    }

    /*public void addCheckedBackgroundFormulas(Set<T> formulas) throws InconsistentTheoryException, SolverException {
        getKnowledgeBase().addBackgroundFormulas(formulas);
        if (!verifyRequirements()) {
            getKnowledgeBase().removeBackgroundFormulas(formulas);
            throw new InconsistentTheoryException("The background theory is unsatisfiable!");
        }
    }*/

    public IKnowledgeBase<T> getKnowledgeBase() {
        return knowledgeBase;
    }

    public void setKnowledgeBase(IKnowledgeBase<T> knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    public boolean areTestsConsistent() throws SolverException {
        // clear stack
        ((AbstractReasoner<T>) getReasoner()).clearFormulasCache();
        for (Set<T> test : getKnowledgeBase().getNegativeTests()) {
            ((AbstractReasoner<T>) getReasoner()).addFormulasToCache(negate(test));
        }
        for (Set<T> test : getKnowledgeBase().getPositiveTests()) {
            ((AbstractReasoner<T>) getReasoner()).addFormulasToCache(test);
        }
        for (Set<T> test : getKnowledgeBase().getEntailedTests()) {
            ((AbstractReasoner<T>) getReasoner()).addFormulasToCache(negate(test));
        }
        for (Set<T> test : getKnowledgeBase().getNonentailedTests()) {
            ((AbstractReasoner<T>) getReasoner()).addFormulasToCache(test);
        }
        boolean res = verifyConsistency();
        //((AbstractReasoner<T>) getReasoner()).clearFormulasCache();
        return res;
    }

    private boolean registered = false;

    public boolean hasRegisteredTests() {
        return registered;
    }

    public final void registerTestCases() throws SolverException, InconsistentTheoryException {
        Set<T> tests = new HashSet<T>();
        for (Set<T> testCase : getKnowledgeBase().getPositiveTests())
            tests.addAll(testCase);
        for (Set<T> testCase : getKnowledgeBase().getEntailedTests())
            tests.addAll(testCase);

        getKnowledgeBase().addBackgroundFormulas(tests);
        this.registered = true;
        getKnowledgeBase().modificationsLock();
    }

    public final void unregisterTestCases() throws SolverException {

        Set<T> tests = new HashSet<T>();
        for (Set<T> testCase : getKnowledgeBase().getPositiveTests())
            tests.addAll(testCase);
        for (Set<T> testCase : getKnowledgeBase().getEntailedTests())
            tests.addAll(testCase);

        getKnowledgeBase().modificationsUnlock();
        getKnowledgeBase().removeBackgroundFormulas(tests);
        this.registered = false;
    }

    protected BaseSearchableObject<T> getNewInstance(IKnowledgeBase<T> knowledgeBase, AbstractReasoner<T> reasoner)
            throws SolverException, InconsistentTheoryException {
        return new BaseSearchableObject<T>(knowledgeBase, reasoner);
    }

    @Override
    public Searchable<T> copy() throws SolverException, InconsistentTheoryException {
        if (isMultiThreading())
            lock.lock();
        try {
            AbstractReasoner<T> reasoner = (AbstractReasoner<T>) getReasoner().newInstance();
            reasoner.setLock(lock);
            reasoner.addFormulasToCache(getKnowledgeBase().getBackgroundFormulas());
            BaseSearchableObject<T> newInstance = getNewInstance(getKnowledgeBase(), reasoner);
            newInstance.setLock(lock);
            return newInstance;
            //cp.setKnowledgeBase();

            //cp.setReasoner(reasoner);
            //return cp;
        } finally {
            if (isMultiThreading())
                lock.unlock();
        }
    }

    public boolean isMultiThreading() {
        return lock!=null;
    }

    public boolean testDiagnosis(Collection<T> diag) throws SolverException {
        List<T> kb = new LinkedList<T>(getKnowledgeBase().getFaultyFormulas());
        // apply diagnosis
        kb.removeAll(diag);
        //((AbstractReasoner<T>) getReasoner()).clearFormulasCache();
        // positive test cases are in background theory
        //((AbstractReasoner<T>) getReasoner()).addFormulasToCache(getKnowledgeBase().getBackgroundFormulas());
        ((AbstractReasoner<T>) getReasoner()).addFormulasToCache(kb);

        for (Set<T> test : getKnowledgeBase().getNegativeTests()) {
            ((AbstractReasoner<T>) getReasoner()).addFormulasToCache(test);
            if (verifyRequirements()) {
                ((AbstractReasoner<T>) getReasoner()).clearFormulasCache();
                return false;
            }
            ((AbstractReasoner<T>) getReasoner()).removeFormulasFromCache(test);
        }
        ((AbstractReasoner<T>) getReasoner()).clearFormulasCache();
        return true;
    }

    public Set<T> getEntailments(Set<T> hittingSet) throws SolverException {
        throw new RuntimeException("This theory does not support computation of entailments!");
    }

    public boolean isEntailed(Set<T> n) {
        throw new RuntimeException("This theory does not support verification of entailments!");
    }

    public void reset() {
        throw new RuntimeException("Unimplemented method");
    }

    public boolean diagnosisEntails(FormulaSet<T> hs, Set<T> ent) {
        throw new RuntimeException("Unimplemented method");
    }

    public boolean diagnosisConsistent(FormulaSet<T> hs, Set<T> ent) {
        throw new RuntimeException("Unimplemented method");
    }

    public void doBayesUpdate(Set<? extends FormulaSet<T>> hittingSets) {
        throw new RuntimeException("Unimplemented method");
    }

    @Override
    public boolean verifyConsistency() throws SolverException {
        return getReasoner().isConsistent();
    }


    /*protected T negate(T formula) {
        throw new RuntimeException("Unimplemented method");
    }
    */


    public Set<T> negate(Set<T> cnf) {
        throw new RuntimeException("Negation of CNFs is not implemented yet.");
        /*Set<T> negated = new TreeSet<T>();
        for (T test : cnf)
            negated.add(negate(test));
        return negated;
        */
    }

    public void setLock(Lock lock) {
        this.lock = lock;
        AbstractReasoner rs = (AbstractReasoner) getReasoner();
        rs.setLock(this.lock);
    }
}
