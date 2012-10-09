/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.diagnosis.model;

import java.util.*;

public abstract class  AbstractTheory<T> extends AbstractSearchableObject<T> implements
        ITheory<T> {

    private static final String MESSAGE = "The test case cannot be added!";


    protected final Set<T> faultyFormulas = new LinkedHashSet<T>();

    protected final Set<T> allFormulas = new LinkedHashSet<T>();

    protected Set<T> backgroundFormulas = new LinkedHashSet<T>();

    private Collection<Set<T>> positiveTests = new LinkedHashSet<Set<T>>();
    private Collection<Set<T>> negativeTests = new LinkedHashSet<Set<T>>();
    private Collection<Set<T>> entailed = new LinkedHashSet<Set<T>>();
    private Collection<Set<T>> nonentailed = new LinkedHashSet<Set<T>>();

    private List<Set<T>> tests = new LinkedList<Set<T>>();
    private Map<Set<T>, Boolean> typeOfTest = new HashMap<Set<T>, Boolean>();



    protected AbstractTheory() {
        setKnowledgeBase(this);
    }

    public AbstractTheory(Object solver) {
        super(solver);
        setKnowledgeBase(this);
    }


    public Set<T> getAllFormulas() {
        return allFormulas;
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
        /*if (val && !areTestsConsistent()) {
            this.positiveTests.remove(test);
            throw new InconsistentTheoryException(MESSAGE);
        }*/
        if (val)
            addToTestList(test, true);

        return val;
    }

    public boolean addNegativeTest(Set<T> test) {
        boolean val = this.negativeTests.add(test);
        /*if (val && !areTestsConsistent()) {
            this.negativeTests.remove(test);
            throw new InconsistentTheoryException(MESSAGE);
        } */
        if (val)
            addToTestList(test, false);

        return val;
    }

    public boolean addEntailedTest(Set<T> test) {
        boolean val = this.entailed.add(test);
        /*if (val && !areTestsConsistent()) {
            this.entailed.remove(test);
            throw new InconsistentTheoryException(MESSAGE);
        } */
        if (val)
            addToTestList(test, true);

        return val;
    }

    public boolean addNonEntailedTest(Set<T> test) {
        boolean val = this.nonentailed.add(test);
        /*if (val && !areTestsConsistent()) {
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

    public Set<T> getFaultyFormulas() {
        return Collections.unmodifiableSet(faultyFormulas);
    }

    public boolean hasTests() {
        return !(this.positiveTests.isEmpty() && this.negativeTests.isEmpty()
                && this.entailed.isEmpty() && this.nonentailed.isEmpty());
    }

    public void clearTestCases() {
        this.positiveTests.clear();
        this.negativeTests.clear();
        this.entailed.clear();
        this.nonentailed.clear();
    }

    public boolean hasBackgroundTheory() {
        return this.backgroundFormulas.size() > 0;
    }

    protected Integer addFaultyFormula(T expr) {
        Integer formula = faultyFormulas.size();
        faultyFormulas.add(expr);
        return formula;
    }


    protected List<Integer> addFaultyFormulas(Collection<T> exprs) {
        List<Integer> fl = new ArrayList<Integer>(exprs.size());
        int count = this.faultyFormulas.size();
        //this.faultyFormulas.ensureCapacity(exprs.size() + count);
        for (T expr : exprs) {
            boolean isAddedNew = this.faultyFormulas.add(expr);
            if (isAddedNew) {
                Integer formula = count++;
                fl.add(formula);
            }

        }
        return fl;
    }

    public void setEmptyBackgroundFormulas() {
        this.backgroundFormulas = new LinkedHashSet<T>();
    }

    public void setBackgroundFormulas(Collection<T> fs) {
        this.backgroundFormulas = new LinkedHashSet<T>(fs);
        /*if (!verifyRequirements()) {
            this.backgroundFormulas.clear();
            throw new InconsistentTheoryException("The background theory is unsatisfiable!");
        }*/
        this.faultyFormulas.removeAll(backgroundFormulas);
    }

    public void removeBackgroundFormulas(Set<T> tests) {
        this.backgroundFormulas.removeAll(tests);
        addFaultyFormulas(getAllFormulas());
    }

    public void addBackgroundFormulas(Set<T> formulas) {
        this.backgroundFormulas.addAll(formulas);
        this.faultyFormulas.removeAll(formulas);
    }

    public Set<T> getBackgroundFormulas() {
        return Collections.unmodifiableSet(this.backgroundFormulas);
    }
















}
