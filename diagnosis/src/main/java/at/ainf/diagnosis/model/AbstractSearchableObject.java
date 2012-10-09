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

    private final LinkedHashSet<T> formulaStack = new LinkedHashSet<T>();
    private final LinkedList<Integer> stackCount = new LinkedList<Integer>();
    private Boolean result = null;

    private Object solver;

    private ITheory<T> knowledgeBase;

    public AbstractSearchableObject() {

    }

    public AbstractSearchableObject(Object solver) {
        this.solver = solver;
    }

    public Object getSolver() {
        return this.solver;
    }

    public void setSolver(Object solver) {
        this.solver = solver;
    }

    /**
     * Adds a statement to the theory.
     */
    public boolean push(T formula) {
        if (formula == null)
            return false;
        stackCount.add(formulaStack.size());
        resetResult();
        this.formulaStack.add(formula);
        return true;
    }

    public boolean push(Collection<T> formulas) {
        if (formulas == null)
            return false;
        stackCount.add(formulaStack.size());
        resetResult();
        this.formulaStack.addAll(formulas);
        return true;
    }

    /**
     * This method uses to make an
     * empty theory and then adds all statements of the current theory into it
     * using {@link #push(java.util.Collection)}.
     */
    public void pop() {
        pop(1);
    }

    public void pop(int stackCount) {
        if (this.stackCount.isEmpty())
            return;
        resetResult();
        /*
        int btc = 0;
        int size = this.stackCount.size();
        if (size == btc)
            throw new IllegalStateException("Trying to remove the background theory!");
        if (size - stackCount < btc)
            throw new IllegalArgumentException("Illegal stack count value!");
         */

        for (int i = 1; i < stackCount; i++)
            this.stackCount.removeLast();

        int index = this.stackCount.removeLast();
        //int size = this.formulaStack.size();
        int count = 0;
        for (Iterator<T> iterator = formulaStack.iterator(); iterator.hasNext(); ) {
            iterator.next();
            if (count >= index)
                iterator.remove();
            count++;
        }
        //for (int i = index; i < size; i++)
        //    this.formulaStack.removeLast();
    }

    protected void resetResult() {
        this.result = null;
    }

    protected Boolean getResult() {
        return this.result;
    }

    protected void setResult(Boolean result) {
        this.result = result;
    }

    public Set<T> getFormulaStack() {
        return Collections.unmodifiableSet(this.formulaStack);
    }

    public int getTheoryCount() {
        return this.stackCount.size();
    }

    public boolean verifyRequirements() throws SolverException {
        return verifyConsistency();
    }

    public boolean supportEntailments() {
        return false;
    }

    public void addCheckedBackgroundFormulas(Set<T> formulas) throws InconsistentTheoryException, SolverException {
        getKnowledgeBase().addBackgroundFormulas(formulas);
        if (!verifyRequirements()) {
            getKnowledgeBase().removeBackgroundFormulas(formulas);
            throw new InconsistentTheoryException("The background theory is unsatisfiable!");
        }
    }

    public ITheory<T> getKnowledgeBase() {
        return knowledgeBase;
    }

    public void setKnowledgeBase(ITheory<T> knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    public boolean areTestsConsistent() throws SolverException {
        // clear stack
        pop(getTheoryCount());
        for (Set<T> test : getKnowledgeBase().getNegativeTests()) {
            push(negate(test));
        }
        for (Set<T> test : getKnowledgeBase().getPositiveTests()) {
            push(test);
        }
        for (Set<T> test : getKnowledgeBase().getEntailedTests()) {
            push(negate(test));
        }
        for (Set<T> test : getKnowledgeBase().getNonentailedTests()) {
            push(test);
        }
        boolean res = verifyConsistency();
        pop(getTheoryCount());
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
        pop(getTheoryCount());
        // positive test cases are in background theory
        push(getKnowledgeBase().getBackgroundFormulas());
        push(kb);

        for (Set<T> test : getKnowledgeBase().getNegativeTests()) {
            push(test);
            if (verifyRequirements()) {
                pop(getTheoryCount());
                return false;
            }
            pop();
        }
        pop(getTheoryCount());
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
