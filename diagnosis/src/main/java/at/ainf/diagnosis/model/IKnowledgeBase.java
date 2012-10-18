/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.diagnosis.model;

import at.ainf.diagnosis.Searchable;
import at.ainf.diagnosis.storage.AxiomSet;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


/**
 * Implement this interface to let the library manage sets of statements. The
 * implementation uses an implementation of some identifier as a
 * type parameter.
 *
 * @param <Id>
 * @author kostya
 */
public interface IKnowledgeBase<Id> {

    /**
     * Returns a list of all formulas registered in the theory except the background ones.
     *
     * @return list of statements
     */
    public Set<Id> getFaultyFormulas();

    //public int getTheoryCount();

    public boolean hasTests();



    // handling test cases


    boolean addPositiveTest(Set<Id> test);

    boolean addNegativeTest(Set<Id> test);

    boolean addEntailedTest(Set<Id> test);

    boolean addNonEntailedTest(Set<Id> test);

    boolean removeNonEntailedTest(Set<Id> test);

    boolean removeEntailedTest(Set<Id> test);

    void removePositiveTest(Set<Id> test);

    void removeNegativeTest(Set<Id> test);

    Collection<Set<Id>> getNegativeTests();

    Collection<Set<Id>> getPositiveTests();

    Collection<Set<Id>> getEntailedTests();

    Collection<Set<Id>> getNonentailedTests();

    public void removeBackgroundFormulas(Set<Id> formulas);

    public Set<Id> getBackgroundFormulas();

    public void addBackgroundFormulas(Set<Id> formulas);

    //public void addFaultyFormula(Id expr);

    //public void addFaultyFormulas(Collection<Id> exprs);

    public int getTestsSize();

    public Set<Id> getTest(int i);

    public List<Set<Id>> getTests(int from, int to);

    public boolean getTypeOfTest(Set<Id> testcase);

    public Set<Id> getAllFormulas();

    public void removeFormular(Collection<Id> formular);

    public void addFormular(Collection<Id> formular);

    public void clearTestCases();

    public void setEmptyBackgroundFormulas();

    public void setBackgroundFormulas(Collection<Id> fs);

    public boolean hasBackgroundTheory();


    //---------------------------------------------------









    //boolean testDiagnosis(Id conflictSet, Set<Id> pathLabels);
}
