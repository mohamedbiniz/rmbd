/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.diagnosis.model;

import java.util.*;

public class KnowledgeBase<T> implements IKnowledgeBase<T> {

    protected final Set<T> knowledgeBase = new LinkedHashSet<T>();

    protected Set<T> backgroundFormulas = new LinkedHashSet<T>();

    private Collection<Set<T>> positiveTests = new LinkedHashSet<Set<T>>();
    private Collection<Set<T>> negativeTests = new LinkedHashSet<Set<T>>();
    private Collection<Set<T>> entailed = new LinkedHashSet<Set<T>>();
    private Collection<Set<T>> nonentailed = new LinkedHashSet<Set<T>>();

    private List<Set<T>> tests = new LinkedList<Set<T>>();
    private Map<Set<T>, Boolean> typeOfTest = new HashMap<Set<T>, Boolean>();

    private short locked = 0;

    public boolean isLocked() {
        return locked == 0;
    }

    public void modificationsLock() {
        this.locked++;
    }

    public void modificationsUnlock() {
        if (this.locked > 0)
            this.locked--;
    }


    public Set<T> getKnowledgeBase() {
        return Collections.unmodifiableSet(knowledgeBase);
    }

    public void addFormulas(Collection<T> formulas) {
        if (locked > 0)
            throw new UnsupportedOperationException();
        knowledgeBase.addAll(formulas);
    }

    public void removeFormulas(Collection<T> formulas) {
        if (locked > 0)
            throw new UnsupportedOperationException();
        knowledgeBase.removeAll(formulas);
    }

    public int getTestsSize() {
        return tests.size();
    }

    public Set<T> getTest(int i) {
        if (locked > 0)
            return Collections.unmodifiableSet(tests.get(i));
        return tests.get(i);
    }

    public List<Set<T>> getTests(int from, int to) {
        if (locked > 0)
            return Collections.unmodifiableList(tests.subList(from, to));
        return tests.subList(from, to);
    }

    public boolean getTypeOfTest(Set<T> testcase) {
        return typeOfTest.get(testcase);
    }

    /*
    private Set<T> getTestSet(T test) {
        LinkedHashSet<T> testSet = new LinkedHashSet<T>();
        testSet.add(test);
        return testSet;
    }
    */

    protected void addToTestList(Set<T> test, boolean type) {
        if (locked > 0)
            throw new UnsupportedOperationException();
        tests.add(test);
        typeOfTest.put(test, type);
    }

    public boolean addPositiveTest(Set<T> test) {
        if (locked > 0)
            throw new UnsupportedOperationException();
        boolean val = this.positiveTests.add(test);
        /*if (val && !areTestsConsistent()) {
            this.positiveTests.remove(test);
            throw new InconsistentTheoryException(MESSAGE);
        }*/

        //if (val)
        addToTestList(test, true);

        return val;
    }

    public boolean addNegativeTest(Set<T> test) {
        if (locked > 0)
            throw new UnsupportedOperationException();
        boolean val = this.negativeTests.add(test);
        /*if (val && !areTestsConsistent()) {
            this.negativeTests.remove(test);
            throw new InconsistentTheoryException(MESSAGE);
        } */

        //if (val)
        addToTestList(test, false);

        return val;
    }

    public boolean addEntailedTest(Set<T> test) {
        if (locked > 0)
            throw new UnsupportedOperationException();

        boolean val = this.entailed.add(test);
        /*if (val && !areTestsConsistent()) {
            this.entailed.remove(test);
            throw new InconsistentTheoryException(MESSAGE);
        } */

        //if (val)
        addToTestList(test, true);

        return val;
    }

    public boolean addNonEntailedTest(Set<T> test) {
        if (locked > 0)
            throw new UnsupportedOperationException();

        boolean val = this.nonentailed.add(test);
        /*if (val && !areTestsConsistent()) {
            this.nonentailed.remove(test);
            throw new InconsistentTheoryException(MESSAGE);
        } */

        //if (val)
        addToTestList(test, false);

        return val;
    }

    protected void removeFromTestList(Set<T> test) {
        if (locked > 0)
            throw new UnsupportedOperationException();
        tests.remove(test);
        typeOfTest.remove(test);

    }

    public boolean removeNonEntailedTest(Set<T> test) {
        if (locked > 0)
            throw new UnsupportedOperationException();
        removeFromTestList(test);
        return this.nonentailed.remove(test);
    }

    public boolean removeEntailedTest(Set<T> test) {
        if (locked > 0)
            throw new UnsupportedOperationException();
        removeFromTestList(test);
        return this.entailed.remove(test);
    }

    public void removePositiveTest(Set<T> test) {
        if (locked > 0)
            throw new UnsupportedOperationException();
        removeFromTestList(test);
        this.positiveTests.remove(test);
    }

    public void removeNegativeTest(Set<T> test) {
        if (locked > 0)
            throw new UnsupportedOperationException();
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
        Set<T> result = new LinkedHashSet<T>();
        for (T formular : knowledgeBase)
            if (!backgroundFormulas.contains(formular))
                result.add(formular);
        return Collections.unmodifiableSet(result);
    }

    public boolean hasTests() {
        return !(this.positiveTests.isEmpty() && this.negativeTests.isEmpty()
                && this.entailed.isEmpty() && this.nonentailed.isEmpty());
    }

    public void clearTestCases() {
        if (locked > 0)
            throw new UnsupportedOperationException();

        this.positiveTests.clear();
        this.negativeTests.clear();
        this.entailed.clear();
        this.nonentailed.clear();
    }

    public boolean hasBackgroundTheory() {
        return this.backgroundFormulas.size() > 0;
    }

    public void setEmptyBackgroundFormulas() {
        if (locked > 0)
            throw new UnsupportedOperationException();
        this.backgroundFormulas = new LinkedHashSet<T>();
    }

    public void setBackgroundFormulas(Collection<T> fs) {
        if (locked > 0)
            throw new UnsupportedOperationException();
        this.backgroundFormulas = new LinkedHashSet<T>(fs);
        /*if (!verifyRequirements()) {
            this.backgroundFormulas.clear();
            throw new InconsistentTheoryException("The background theory is unsatisfiable!");
        }
        this.faultyFormulas.removeAll(backgroundFormulas);*/
    }

    public void removeBackgroundFormulas(Set<T> tests) {
        if (locked > 0)
            throw new UnsupportedOperationException();
        this.backgroundFormulas.removeAll(tests);

    }

    public void addBackgroundFormulas(Set<T> formulas) {
        if (locked > 0)
            throw new UnsupportedOperationException();
        this.backgroundFormulas.addAll(formulas);
        //this.faultyFormulas.removeAll(formulas);
    }

    public Set<T> getBackgroundFormulas() {
        return Collections.unmodifiableSet(this.backgroundFormulas);
    }
}
