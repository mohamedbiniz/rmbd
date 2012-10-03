/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.diagnosis.model;

import at.ainf.diagnosis.storage.AxiomSet;

import java.util.*;

public abstract class AbstractTheory<Solver, T> extends AbstractSearchableObject<T> implements
        ITheory<T> {

    private static final String MESSAGE = "The test case cannot be added!";
    private Solver solver;

    protected final Set<T> activeFormulas = new LinkedHashSet<T>();

    protected Set<T> backgroundFormulas = new LinkedHashSet<T>();

    private Collection<Set<T>> positiveTests = new LinkedHashSet<Set<T>>();
    private Collection<Set<T>> negativeTests = new LinkedHashSet<Set<T>>();
    private Collection<Set<T>> entailed = new LinkedHashSet<Set<T>>();
    private Collection<Set<T>> nonentailed = new LinkedHashSet<Set<T>>();

    private List<Set<T>> tests = new LinkedList<Set<T>>();
    private Map<Set<T>, Boolean> typeOfTest = new HashMap<Set<T>, Boolean>();



    protected AbstractTheory() {

    }

    public AbstractTheory(Solver solver) {
        this.solver = solver;
    }

    protected int getTestsSize() {
        return tests.size();
    }

    protected Set<T> getTest(int i) {
        return tests.get(i);
    }

    protected List<Set<T>> getTests(int from, int to) {
        return tests.subList(from, to);
    }

    protected boolean getTypeOfTest(Set<T> testcase) {
        return typeOfTest.get(testcase);
    }

    private Set<T> getTestSet(T test) {
        LinkedHashSet<T> testSet = new LinkedHashSet<T>();
        testSet.add(test);
        return testSet;
    }

    protected void addToTestList(Set<T> test, boolean type) {
        tests.add(test);
        typeOfTest.put(test,  type);
    }

    public boolean addPositiveTest(Set<T> test) {
        boolean val = this.positiveTests.add(test);
        /*if (val && !isTestConsistent()) {
            this.positiveTests.remove(test);
            throw new InconsistentTheoryException(MESSAGE);
        }*/
        if (val)
            addToTestList(test, true);

        return val;
    }

    public boolean addNegativeTest(Set<T> test) {
        boolean val = this.negativeTests.add(test);
        /*if (val && !isTestConsistent()) {
            this.negativeTests.remove(test);
            throw new InconsistentTheoryException(MESSAGE);
        } */
        if (val)
            addToTestList(test, false);

        return val;
    }

    public boolean addEntailedTest(Set<T> test) {
        boolean val = this.entailed.add(test);
        /*if (val && !isTestConsistent()) {
            this.entailed.remove(test);
            throw new InconsistentTheoryException(MESSAGE);
        } */
        if (val)
            addToTestList(test, true);

        return val;
    }

    public boolean addNonEntailedTest(Set<T> test) {
        boolean val = this.nonentailed.add(test);
        /*if (val && !isTestConsistent()) {
            this.nonentailed.remove(test);
            throw new InconsistentTheoryException(MESSAGE);
        } */
        if (val)
            addToTestList(test, false);

        return val;
    }

    protected void removeFromTestList(Set<T> test) {
        tests.remove(test);
        typeOfTest.remove(test);

    }

    public boolean removeNonEntailedTest(Set<T> test) {
        removeFromTestList(test);
        return this.nonentailed.remove(test);
    }

    public boolean removeEntailedTest(Set<T> test) {
        removeFromTestList(test);
        return this.entailed.remove(test);
    }

    public void removePositiveTest(Set<T> test) {
        removeFromTestList(test);
        this.positiveTests.remove(test);
    }

    public void removeNegativeTest(Set<T> test) {
        removeFromTestList(test);
        this.negativeTests.remove(test);
    }

    public Collection<Set<T>> getNegativeTests() {
        return Collections.unmodifiableCollection(this.negativeTests);
    }

    public Collection<Set<T>> getPositiveTests() {
        return Collections.unmodifiableCollection(positiveTests);
    }

    public Collection<Set<T>> getEntailedTests() {
        return Collections.unmodifiableCollection(entailed);
    }

    public Collection<Set<T>> getNonentailedTests() {
        return Collections.unmodifiableCollection(nonentailed);
    }

    public boolean hasTests() {
        return !(this.positiveTests.isEmpty() && this.negativeTests.isEmpty()
                && this.entailed.isEmpty() && this.nonentailed.isEmpty());
    }

    public boolean isTestConsistent() throws SolverException {
        // clear stack
        pop(getTheoryCount());
        for (Set<T> test : getNegativeTests()) {
            push(negate(test));
        }
        for (Set<T> test : getPositiveTests()) {
            push(test);
        }
        for (Set<T> test : getEntailedTests()) {
            push(negate(test));
        }
        for (Set<T> test : getNonentailedTests()) {
            push(test);
        }
        boolean res = verifyConsistency();
        pop(getTheoryCount());
        return res;
    }

    public Set<T> negate(Set<T> cnf) {
        throw new RuntimeException("Negation of CNFs is not implemented yet.");
        /*Set<T> negated = new TreeSet<T>();
        for (T test : cnf)
            negated.add(negate(test));
        return negated;
        */
    }

    protected abstract T negate(T formula);

    public void registerTestCases() throws SolverException, InconsistentTheoryException {

            for (Set<T> test : getEntailedTests())
                this.addNegativeTest(negate(test));
            for (Set<T> test : getNonentailedTests())
                this.addPositiveTest(negate(test));



        for (Set<T> testCase : positiveTests)
            this.backgroundFormulas.addAll(testCase);
    }

    public void unregisterTestCases() throws SolverException {
        for (Set<T> test : getEntailedTests())
            for (T negatedTest : negate(test))
                this.removeNegativeTest(Collections.singleton(negatedTest));
        for (Set<T> test : getNonentailedTests())
            for (T negatedTest : negate(test))
                this.removePositiveTest(Collections.singleton(negatedTest));
        for (Set<T> testCase : positiveTests)
            this.backgroundFormulas.removeAll(testCase);
    }

    public void clearTestCases() {
        this.positiveTests.clear();
        this.negativeTests.clear();
        this.entailed.clear();
        this.nonentailed.clear();
    }

    public Collection<Collection<T>> testDiagnoses(Collection<Collection<T>> diagnoses) throws SolverException {
        Collection<Collection<T>> res = new LinkedHashSet<Collection<T>>();

        for (Collection<T> diag : diagnoses) {
            if (testDiagnosis(diag))
                res.add(diag);
        }
        return res;
    }

    public boolean testDiagnosis(Collection<T> diag) throws SolverException {
        List<T> kb = new LinkedList<T>(getActiveFormulas());
        // apply diagnosis
        kb.removeAll(diag);
        pop(getTheoryCount());
        // positive test cases are in background theory
        push(getBackgroundFormulas());
        push(kb);

        for (Set<T> test : getNegativeTests()) {
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


    public Set<T> getActiveFormulas() {
        return Collections.unmodifiableSet(activeFormulas);
    }

    public void setBackgroundFormulas(Collection<T> fs) throws InconsistentTheoryException, SolverException {
        this.backgroundFormulas = new LinkedHashSet<T>(fs);
        if (!verifyRequirements()) {
            this.backgroundFormulas.clear();
            throw new InconsistentTheoryException("The background theory is unsatisfiable!");
        }
        this.activeFormulas.removeAll(backgroundFormulas);
    }

    public void removeActiveFormulas(Set<T> formulas) throws InconsistentTheoryException, SolverException {
        this.activeFormulas.remove(formulas);
    }

    public void addBackgroundFormulas(Set<T> formulas) throws InconsistentTheoryException, SolverException {
        this.backgroundFormulas.addAll(formulas);
        if (!verifyRequirements()) {
            this.backgroundFormulas.removeAll(formulas);
            throw new InconsistentTheoryException("The background theory is unsatisfiable!");
        }
        this.activeFormulas.remove(formulas);
    }

    public void addBackgroundFormula(T formula) throws InconsistentTheoryException, SolverException {
        this.backgroundFormulas.add(formula);
        if (!verifyRequirements()) {
            this.backgroundFormulas.remove(formula);
            throw new InconsistentTheoryException("The background theory is unsatisfiable!");
        }
        this.activeFormulas.remove(formula);
    }

    public void removeBackgroundFormulas(Set<T> tests) {
        this.backgroundFormulas.removeAll(tests);
    }

    public void removeBackgroundFormulas() {
        this.backgroundFormulas.clear();
    }
    /*
    public List<T> getFormulaStack() {
        return Collections.unmodifiableList(this.formulaStack);
    }
    */

    public Set<T> getBackgroundFormulas() {
        return Collections.unmodifiableSet(this.backgroundFormulas);
    }


    public Solver getSolver() {
        return this.solver;
    }

    public void setSolver(Solver solver) {
        this.solver = solver;
    }

    public boolean hasBackgroundTheory() {
        return this.backgroundFormulas.size() > 0;
    }

    protected Integer addActiveFormula(T expr) {
        Integer formula = activeFormulas.size();
        activeFormulas.add(expr);
        return formula;
    }


    protected List<Integer> addActiveFormulas(Collection<T> exprs) {
        List<Integer> fl = new ArrayList<Integer>(exprs.size());
        int count = this.activeFormulas.size();
        //this.activeFormulas.ensureCapacity(exprs.size() + count);
        for (T expr : exprs) {
            this.activeFormulas.add(expr);
            Integer formula = count++;
            fl.add(formula);
        }
        return fl;
    }




    public Object getOriginalOntology() {
        throw new RuntimeException("Unimplemented method");
    }

    public Object getOntology() {
        throw new RuntimeException("Unimplemented method");
    }

}
