/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.theory.model;

import at.ainf.theory.storage.HittingSet;

import java.util.*;

public abstract class AbstractTheory<Solver, T> extends AbstractSearchableObject<T> implements
        ITheory<T> {

    private static final String MESSAGE = "The test case cannot be added!";
    private Solver solver;

    private final Set<T> activeFormulas = new LinkedHashSet<T>();

    private Set<T> backgroundFormulas = new LinkedHashSet<T>();

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
        TreeSet<T> testSet = new TreeSet<T>();
        testSet.add(test);
        return testSet;
    }

    protected void addToTestList(Set<T> test, boolean type) {
        tests.add(test);
        typeOfTest.put(test, true);
    }

    public boolean addPositiveTest(T test) throws SolverException, UnsatisfiableFormulasException {
        return addPositiveTest(getTestSet(test));
    }

    public boolean addNegativeTest(T test) throws SolverException, UnsatisfiableFormulasException {
        return addNegativeTest(getTestSet(test));
    }

    public boolean addEntailedTest(T test) throws SolverException, UnsatisfiableFormulasException {
        return addEntailedTest(getTestSet(test));
    }

    public boolean addNonEntailedTest(T test) throws SolverException, UnsatisfiableFormulasException {
        return addNonEntailedTest(getTestSet(test));
    }

    public boolean addPositiveTest(Set<T> test) throws SolverException, UnsatisfiableFormulasException {
        boolean val = this.positiveTests.add(test);
        if (val && !isTestConsistent()) {
            this.positiveTests.remove(test);
            throw new UnsatisfiableFormulasException(MESSAGE);
        }
        if (val)
            addToTestList(test, true);

        return val;
    }

    public boolean addNegativeTest(Set<T> test) throws SolverException, UnsatisfiableFormulasException {
        boolean val = this.negativeTests.add(test);
        if (val && !isTestConsistent()) {
            this.negativeTests.remove(test);
            throw new UnsatisfiableFormulasException(MESSAGE);
        }
        if (val)
            addToTestList(test, false);

        return val;
    }

    public boolean addEntailedTest(Set<T> test) throws SolverException, UnsatisfiableFormulasException {
        boolean val = this.entailed.add(test);
        if (val && !isTestConsistent()) {
            this.entailed.remove(test);
            throw new UnsatisfiableFormulasException(MESSAGE);
        }
        if (val)
            addToTestList(test, true);

        return val;
    }

    public boolean addNonEntailedTest(Set<T> test) throws SolverException, UnsatisfiableFormulasException {
        boolean val = this.nonentailed.add(test);
        if (val && !isTestConsistent()) {
            this.nonentailed.remove(test);
            throw new UnsatisfiableFormulasException(MESSAGE);
        }
        if (val)
            addToTestList(test, false);

        return val;
    }

    protected void removeFromTestList(Set<T> test) {
        tests.remove(test);
        typeOfTest.remove(test);

    }

    public boolean removeNonEntailedTest(T test) {
        return removeNonEntailedTest(getTestSet(test));
    }

    public boolean removeEntailedTest(T test) {
        return removeEntailedTest(getTestSet(test));
    }

    public void removePositiveTest(T test) {
        removePositiveTest(getTestSet(test));
    }

    public void removeNegativeTest(T test) {
        removeNegativeTest(getTestSet(test));
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

    protected boolean isTestConsistent() throws SolverException {
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
        boolean res = isConsistent();
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

    public void registerTestCases() throws SolverException, UnsatisfiableFormulasException {
        try {
            for (Set<T> test : getEntailedTests())
                this.addNegativeTest(negate(test));
            for (Set<T> test : getNonentailedTests())
                this.addPositiveTest(negate(test));

        } catch (UnsatisfiableFormulasException e) {
            throw new RuntimeException("Invalid tests or background knowledge are saved in the theory!");
        }

        for (Set<T> testCase : positiveTests)
            this.backgroundFormulas.addAll(testCase);
    }

    public void unregisterTestCases() throws SolverException {
        for (Set<T> test : getEntailedTests())
            for (T negatedTest : negate(test))
                this.removeNegativeTest(negatedTest);
        for (Set<T> test : getNonentailedTests())
            for (T negatedTest : negate(test))
                this.removePositiveTest(negatedTest);
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
        Collection<Collection<T>> res = new TreeSet<Collection<T>>();

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
            if (isConsistent()) {
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

    public void setBackgroundFormulas(Collection<T> fs) throws UnsatisfiableFormulasException, SolverException {
        this.backgroundFormulas = new LinkedHashSet<T>(fs);
        if (!isConsistent()) {
            this.backgroundFormulas.clear();
            throw new UnsatisfiableFormulasException("The background theory is unsatisfiable!");
        }
        this.activeFormulas.removeAll(backgroundFormulas);
    }


    public <E extends Set<T>> void addBackgroundFormulas(E formulas) throws UnsatisfiableFormulasException, SolverException {
        this.backgroundFormulas.addAll(formulas);
        if (!isConsistent()) {
            this.backgroundFormulas.removeAll(formulas);
            throw new UnsatisfiableFormulasException("The background theory is unsatisfiable!");
        }
        this.activeFormulas.remove(formulas);
    }

    public void addBackgroundFormula(T formula) throws UnsatisfiableFormulasException, SolverException {
        this.backgroundFormulas.add(formula);
        if (!isConsistent()) {
            this.backgroundFormulas.remove(formula);
            throw new UnsatisfiableFormulasException("The background theory is unsatisfiable!");
        }
        this.activeFormulas.remove(formula);
    }

    protected void removeBackgroundFormulas(Set<T> tests) {
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

    public boolean diagnosisEntails(HittingSet<T> hs, Set<T> ent) {
        throw new RuntimeException("Unimplemented method");
    }

    public boolean diagnosisConsistent(HittingSet<T> hs, Set<T> ent) {
        throw new RuntimeException("Unimplemented method");
    }

    public void doBayesUpdate(Set<? extends HittingSet<T>> hittingSets) {
        throw new RuntimeException("Unimplemented method");
    }

    public Object getOriginalOntology() {
        throw new RuntimeException("Unimplemented method");
    }

    public Object getOntology() {
        throw new RuntimeException("Unimplemented method");
    }



    public void reset() {
        throw new RuntimeException("Unimplemented method");
    }


}
