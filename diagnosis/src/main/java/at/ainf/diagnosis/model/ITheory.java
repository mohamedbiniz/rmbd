/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.diagnosis.model;

import at.ainf.diagnosis.Searchable;
import at.ainf.diagnosis.storage.HittingSet;
import at.ainf.diagnosis.tree.NodeCostsEstimator;

import java.util.Collection;
import java.util.Set;


/**
 * Implement this interface to let the library manage sets of statements. The
 * implementation uses an implementation of some identifier as a
 * type parameter.
 *
 * @param <Id>
 * @author kostya
 */
public interface ITheory<Id> extends Searchable<Id> {

    /**
     * Returns a list of all formulas registered in the theory except the background ones.
     *
     * @return list of statements
     */
    public Collection<Id> getActiveFormulas();

    /**
     * Returns a list of formulas stored in the
     *
     * @return
     */
    //public Collection<Id> getFormulaStack();

    /**
     * Creates a copy of the theory and adds all formulas from the given list.
     *
     * @return result
     * @throws SolverException an exception that can be used to hanle client related
     *                         problems. This exception will be propagated by the library to
     *                         the diagnosis execution point.
     */
    public boolean push() throws SolverException;

    //public boolean push(Id formulas) throws SolverException;

    //public boolean push(Collection<Id> formulas) throws SolverException;

    //public void pop(int stackCount);

    //public void pop();


    /**
     * Checks if the theory is consistent.
     *
     * @return <code>true</code> is given theory is consistent and
     *         <code>false</code> otherwise.
     * @throws SolverException an exception that can be used to handle reasoner related
     *                         problems. This exception will be propagated by the library to
     *                         the diagnosis execution point.
     */
    public boolean isConsistent() throws SolverException;

    public int getTheoryCount();

    public boolean hasTests();

    public boolean testDiagnosis(Collection<Id> diagnosis) throws SolverException;

    public void registerTestCases() throws SolverException, UnsatisfiableFormulasException;

    public void unregisterTestCases() throws SolverException;

    // handling test cases

    boolean addPositiveTest(Id test) throws SolverException, UnsatisfiableFormulasException;

    boolean addNegativeTest(Id test) throws SolverException, UnsatisfiableFormulasException;

    boolean addEntailedTest(Id test) throws SolverException, UnsatisfiableFormulasException;

    boolean addNonEntailedTest(Id test) throws SolverException, UnsatisfiableFormulasException;

    boolean addPositiveTest(Set<Id> test) throws SolverException, UnsatisfiableFormulasException;

    boolean addNegativeTest(Set<Id> test) throws SolverException, UnsatisfiableFormulasException;

    boolean addEntailedTest(Set<Id> test) throws SolverException, UnsatisfiableFormulasException;

    boolean addNonEntailedTest(Set<Id> test) throws SolverException, UnsatisfiableFormulasException;

    boolean removeNonEntailedTest(Id test);

    boolean removeEntailedTest(Id test);

    void removePositiveTest(Id test);

    void removeNegativeTest(Id test);

    boolean removeNonEntailedTest(Set<Id> test);

    boolean removeEntailedTest(Set<Id> test);

    void removePositiveTest(Set<Id> test);

    void removeNegativeTest(Set<Id> test);

    Collection<Set<Id>> getNegativeTests();

    Collection<Set<Id>> getPositiveTests();

    Collection<Set<Id>> getEntailedTests();

    Collection<Set<Id>> getNonentailedTests();

    Set<Id> getEntailments(Set<Id> hittingSet) throws SolverException;

    boolean diagnosisEntails(HittingSet<Id> hs, Set<Id> ent);

    boolean diagnosisConsistent(HittingSet<Id> hs, Set<Id> ent);

    boolean isEntailed(Set<Id> n);

    public void doBayesUpdate(Set<? extends HittingSet<Id>> hittingSets);
}
