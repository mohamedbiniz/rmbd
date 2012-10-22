package at.ainf.diagnosis.model;

import at.ainf.diagnosis.Searchable;
import at.ainf.diagnosis.storage.AxiomSet;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 13.07.11
 * Time: 18:52
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractSearchableObject<T> implements Searchable<T> {










    private Object solver;

    private IKnowledgeBase<T> knowledgeBase;

    private IReasoner<T> reasoner;

    public AbstractSearchableObject() {
        this(null);
    }


    public AbstractSearchableObject(Object solver) {
        this.solver = solver;
        setKnowledgeBase(new KnowledgeBase<T>());
    }

    public Object getSolver() {
        return this.solver;
    }

    public void setSolver(Object solver) {
        this.solver = solver;
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
        getReasoner().cleanReasonedFormulars();
        for (Set<T> test : getKnowledgeBase().getNegativeTests()) {
            getReasoner().addReasonedFormulars(negate(test));
        }
        for (Set<T> test : getKnowledgeBase().getPositiveTests()) {
            getReasoner().addReasonedFormulars(test);
        }
        for (Set<T> test : getKnowledgeBase().getEntailedTests()) {
            getReasoner().addReasonedFormulars(negate(test));
        }
        for (Set<T> test : getKnowledgeBase().getNonentailedTests()) {
            getReasoner().addReasonedFormulars(test);
        }
        boolean res = verifyConsistency();
        getReasoner().cleanReasonedFormulars();
        return res;
    }

    public void registerTestCases() throws SolverException, InconsistentTheoryException {

        for (Set<T> test : getKnowledgeBase().getEntailedTests())
            getKnowledgeBase().addNegativeTest(negate(test));
        for (Set<T> test : getKnowledgeBase().getNonentailedTests())
            getKnowledgeBase().addPositiveTest(negate(test));



        for (Set<T> testCase : getKnowledgeBase().getPositiveTests())
            getKnowledgeBase().addBackgroundFormulas(testCase);

    }

    public void unregisterTestCases() throws SolverException {
        for (Set<T> test : getKnowledgeBase().getEntailedTests())
            for (T negatedTest : negate(test))
                getKnowledgeBase().removeNegativeTest(Collections.singleton(negatedTest));
        for (Set<T> test : getKnowledgeBase().getNonentailedTests())
            for (T negatedTest : negate(test))
                getKnowledgeBase().removePositiveTest(Collections.singleton(negatedTest));
        for (Set<T> testCase : getKnowledgeBase().getPositiveTests())
            getKnowledgeBase().removeBackgroundFormulas(testCase);

    }

    public boolean testDiagnosis(Collection<T> diag) throws SolverException {
        List<T> kb = new LinkedList<T>(getKnowledgeBase().getFaultyFormulas());
        // apply diagnosis
        kb.removeAll(diag);
        getReasoner().cleanReasonedFormulars();
        // positive test cases are in background theory
        getReasoner().addReasonedFormulars(getKnowledgeBase().getBackgroundFormulas());
        getReasoner().addReasonedFormulars(kb);

        for (Set<T> test : getKnowledgeBase().getNegativeTests()) {
            getReasoner().addReasonedFormulars(test);
            if (verifyRequirements()) {
                getReasoner().cleanReasonedFormulars();
                return false;
            }
            getReasoner().removeReasonedFormulars(test);
        }
        getReasoner().cleanReasonedFormulars();
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

    public boolean diagnosisEntails(AxiomSet<T> hs, Set<T> ent) {
        throw new RuntimeException("Unimplemented method");
    }

    public boolean diagnosisConsistent(AxiomSet<T> hs, Set<T> ent) {
        throw new RuntimeException("Unimplemented method");
    }

    public void doBayesUpdate(Set<? extends AxiomSet<T>> hittingSets) {
        throw new RuntimeException("Unimplemented method");
    }





    protected T negate(T formula) {
        throw new RuntimeException("Unimplemented method");
    }

    public Set<T> negate(Set<T> cnf) {
        throw new RuntimeException("Negation of CNFs is not implemented yet.");
        /*Set<T> negated = new TreeSet<T>();
        for (T test : cnf)
            negated.add(negate(test));
        return negated;
        */
    }

}
