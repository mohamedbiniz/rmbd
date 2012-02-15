/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.theory.model;

import at.ainf.theory.Searchable;
import at.ainf.theory.storage.AxiomSet;

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

    public boolean push() throws SolverException;

    public int getTheoryCount();

    public boolean hasTests();

    public boolean testDiagnosis(Collection<Id> diagnosis) throws SolverException;

    public void registerTestCases() throws SolverException, InconsistentTheoryException;

    public void unregisterTestCases() throws SolverException;

    // handling test cases

    boolean addPositiveTest(Id test) throws SolverException, InconsistentTheoryException;

    boolean addNegativeTest(Id test) throws SolverException, InconsistentTheoryException;

    boolean addEntailedTest(Id test) throws SolverException, InconsistentTheoryException;

    boolean addNonEntailedTest(Id test) throws SolverException, InconsistentTheoryException;

    boolean addPositiveTest(Set<Id> test) throws SolverException, InconsistentTheoryException;

    boolean addNegativeTest(Set<Id> test) throws SolverException, InconsistentTheoryException;

    boolean addEntailedTest(Set<Id> test) throws SolverException, InconsistentTheoryException;

    boolean addNonEntailedTest(Set<Id> test) throws SolverException, InconsistentTheoryException;

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

    boolean diagnosisEntails(AxiomSet<Id> hs, Set<Id> ent);

    boolean diagnosisConsistent(AxiomSet<Id> hs, Set<Id> ent);

    boolean supportEntailments();

    boolean isEntailed(Set<Id> n);

    public void doBayesUpdate(Set<? extends AxiomSet<Id>> hittingSets);

    public Object getOriginalOntology();

    public Object getOntology();

    public void reset();

}
