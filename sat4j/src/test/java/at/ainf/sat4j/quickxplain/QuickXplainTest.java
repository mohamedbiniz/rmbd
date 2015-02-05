/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.sat4j.quickxplain;

import at.ainf.diagnosis.Searcher;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.MultiQuickXplain;
import at.ainf.diagnosis.quickxplain.QXAxiomSetListener;
import at.ainf.diagnosis.quickxplain.QXSingleAxiomListener;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.sat4j.model.IVecIntComparable;
import at.ainf.sat4j.model.PropositionalTheory;
import at.ainf.sat4j.model.VecIntComparable;
import org.junit.Ignore;
import org.junit.Test;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ISolver;

import java.util.*;

import static org.junit.Assert.*;

public class QuickXplainTest {


    /*@Test
    public void testOld() throws SolverException, InconsistentTheoryException {
        Searcher<IVecIntComparable> quick = new OldQuickXplain<IVecIntComparable>();
        for (int i = 0; i < 1000; i++) {
            runQuick(quick);
        }
    }*/

    @Test
    public void testNew() throws SolverException, InconsistentTheoryException {
        Searcher<IVecIntComparable> quick = new QuickXplain<IVecIntComparable>();
        for (int i = 0; i < 1000; i++) {
            runQuick(quick);
        }
    }


    @Test
    public void testMulti() throws SolverException, InconsistentTheoryException, NoConflictException {
        final int iterations = 1;
        MultiQuickXplain<IVecIntComparable> quick = new MultiQuickXplain<IVecIntComparable>();
        quick.setAxiomListener(new QXSingleAxiomListener<IVecIntComparable>(true));
        for (int i = 0; i < iterations; i++) {
            runQuick(quick);
        }
    }


    public void runQuick(Searcher<IVecIntComparable> quick) throws SolverException, InconsistentTheoryException {

        int[] fm10 = new int[]{10};

        ISolver reasoner = SolverFactory.newDefault();
        reasoner.setExpectedNumberOfClauses(20);
        reasoner.newVar(10);
        PropositionalTheory theory = new PropositionalTheory(reasoner);
        theory.getKnowledgeBase().addBackgroundFormulas(Collections.<IVecIntComparable>singleton(new VecIntComparable(fm10)));

        List<IVecIntComparable> list = new LinkedList<IVecIntComparable>();
        check(quick, theory, list, true);

        List<int[]> vec = new ArrayList<int[]>();

        int[] clause = new int[]{-1, -2, -3, 4};
        vec.add(clause);

        clause = new int[]{1};
        vec.add(clause);

        clause = new int[]{2};
        vec.add(clause);

        clause = new int[]{3};
        vec.add(clause);

        //Collection<IVecIntComparable> test = theory.addClauses(vec);
        Collection<IVecIntComparable> test = new LinkedList<IVecIntComparable>();
        for (int[] e : vec)
            test.add(theory.addClause(e));
        list.addAll(theory.getKnowledgeBase().getFaultyFormulas());
        check(quick, theory, list, true);

        IVecIntComparable fm4 = theory.addClause(new int[]{-4});
        list.add(fm4);

        IVecIntComparable fm5 = theory.addClause(new int[]{5});
        list.add(fm5);

        Collection<IVecIntComparable> fl = check(quick, theory, list, false);

        assertTrue(fl.size() == 5);
        assertTrue(fl.contains(fm4));
        assertFalse(fl.contains(fm5));
        assertTrue(fl.containsAll(test));

        //theory.getKnowledgeBase().removeFormulas(list);
        list.clear();

        IVecIntComparable fm6 = theory.addClause(new int[]{6, -7});
        list.add(fm6);
        IVecIntComparable fm7 = theory.addClause(new int[]{6, 7});
        list.add(fm7);
        IVecIntComparable fm8 = theory.addClause(new int[]{-6});
        list.add(fm8);
        list.add(fm5);

        fl = check(quick, theory, list, false);

        list.remove(fm5);
        assertTrue(fl.size() == 3);
        assertTrue(fl.containsAll(list));
    }

    @Test
    public void run2SingleQuick() throws SolverException, InconsistentTheoryException, NoConflictException {
        Set<FormulaSet<IVecIntComparable>> conflict = Collections.emptySet();
        for (int i = 0; i < 100; i++) {
            MultiQuickXplain<IVecIntComparable> quick = new MultiQuickXplain<IVecIntComparable>();
            quick.setAxiomListener(new QXSingleAxiomListener<IVecIntComparable>(true));
            //if maxCount not set, there will be 6 conflicts on some systems
            quick.setMaxConflictSetCount(5);
            conflict = computeMultipleConflicts(quick);
        }
        assertEquals(conflict.size(), 5);
    }

    @Test
    public void run2SetQuick() throws SolverException, InconsistentTheoryException, NoConflictException {

        MultiQuickXplain<IVecIntComparable> quick = new MultiQuickXplain<IVecIntComparable>();
        quick.setAxiomListener(new QXAxiomSetListener<IVecIntComparable>(true));

        Set<FormulaSet<IVecIntComparable>> conflict = computeMultipleConflicts(quick);
        assertEquals(conflict.size(), 6);
    }

    @Test
    public void run2SetTerminateQuick() throws SolverException, InconsistentTheoryException, NoConflictException {

        MultiQuickXplain<IVecIntComparable> quick = new MultiQuickXplain<IVecIntComparable>(2,2,3);
        quick.setAxiomListener(new QXAxiomSetListener<IVecIntComparable>(true));

        Set<FormulaSet<IVecIntComparable>> conflict = computeMultipleConflicts(quick);
        assertEquals(conflict.size(), 3);
    }

    private Set<FormulaSet<IVecIntComparable>> computeMultipleConflicts(MultiQuickXplain<IVecIntComparable> quick) throws SolverException, InconsistentTheoryException, NoConflictException {
        int[] fm10 = new int[]{10};

        ISolver reasoner = SolverFactory.newDefault();
        reasoner.setExpectedNumberOfClauses(20);
        reasoner.newVar(10);
        PropositionalTheory theory = new PropositionalTheory(reasoner);
        theory.getKnowledgeBase().addBackgroundFormulas(Collections.<IVecIntComparable>singleton(new VecIntComparable(fm10)));

        List<IVecIntComparable> list = new LinkedList<IVecIntComparable>();
        check(quick, theory, list, true);

        List<int[]> vec = new ArrayList<int[]>();

        vec.add(new int[]{-1, -2, -3, 4});
        vec.add(new int[]{1});
        vec.add(new int[]{2});
        vec.add(new int[]{3});
        vec.add(new int[]{-4});
        vec.add(new int[]{5});

        vec.add(new int[]{16, -17});
        vec.add(new int[]{16, 17});
        vec.add(new int[]{-16});

        vec.add(new int[]{9});
        vec.add(new int[]{-9, 11});
        vec.add(new int[]{-10, -11});
        vec.add(new int[]{-11});

        vec.add(new int[]{6, -7});
        vec.add(new int[]{6, 7});
        vec.add(new int[]{-6});

        vec.add(new int[]{6, 12});
        vec.add(new int[]{-10, -12});

        Collection<IVecIntComparable> test = new LinkedList<IVecIntComparable>();
        for (int[] e : vec)
            test.add(theory.addClause(e));
        list.addAll(theory.getKnowledgeBase().getFaultyFormulas());

        return quick.search(theory, list);
    }

    @Test
    public void run2TestNConflictComputation() throws SolverException, InconsistentTheoryException, NoConflictException {
        int n = 4;

        //create quick
        MultiQuickXplain<IVecIntComparable> quick = new MultiQuickXplain<IVecIntComparable>();
        quick.setAxiomListener(new QXAxiomSetListener<IVecIntComparable>(true));
        //create theory
        PropositionalTheory theory = generateTheory();


        //calculate conflicts
        Set<FormulaSet<IVecIntComparable>> conflicts = computeNConflictsAtTime(quick, theory, n);
        System.out.print("Test: " + conflicts.size());
        assertTrue(n > conflicts.size());
        //create KB'
        PropositionalTheory theoryPrime = getTheoryFromConflicts(conflicts, theory);



    }

    public PropositionalTheory getTheoryFromConflicts(Set<FormulaSet<IVecIntComparable>> conflicts, PropositionalTheory completeTheory) {

        ISolver reasoner = SolverFactory.newDefault();
        reasoner.setExpectedNumberOfClauses(20);
        reasoner.newVar(10);
        PropositionalTheory theory = new PropositionalTheory(reasoner);
        theory.getKnowledgeBase().addBackgroundFormulas(completeTheory.getKnowledgeBase().getBackgroundFormulas());

        Iterator<FormulaSet<IVecIntComparable>> iterator = conflicts.iterator();
        while(iterator.hasNext()) {
            theory.addAll(iterator.next());
        }
        return theory;
    }


        @Test
    public void run2TestSingleConflictComputation() throws SolverException, InconsistentTheoryException, NoConflictException {
        //create quick
        MultiQuickXplain<IVecIntComparable> quick = new MultiQuickXplain<IVecIntComparable>();
        quick.setAxiomListener(new QXAxiomSetListener<IVecIntComparable>(true));
        //create theory
        PropositionalTheory theory = generateTheory();
        //calculate conflict
        Set<FormulaSet<IVecIntComparable>> conflicts = computeSingleConflictAtTime(quick, theory);
        assertEquals(conflicts.size(), 1);

        //remove 1 axiom of conflict from theory
        FormulaSet<IVecIntComparable> conflict = conflicts.iterator().next();
        IVecIntComparable diagnose = conflict.iterator().next();
        theory.removeClause(diagnose);
        //calculate conflict
        conflicts = computeSingleConflictAtTime(quick, theory);
        assertEquals(conflicts.size(), 1);

        //remove 1 axiom of conflict from theory
        conflict = conflicts.iterator().next();
        diagnose = conflict.iterator().next();
        theory.removeClause(diagnose);
        //calculate conflict
        List<IVecIntComparable> list = new LinkedList<IVecIntComparable>();
        list.addAll(theory.getKnowledgeBase().getFaultyFormulas());
        check(quick, theory, list, true); //passes test if no more conflicts

    }

    /**
     * searches for 1 conflict and returns it
     * @param MultiQuickXplain<IVecIntComparable> quick
     * @param PropositionalTheory theory
     * @return Set<FormulaSet<IVecIntComparable>> conflict
     * @throws at.ainf.diagnosis.model.InconsistentTheoryException
     * @throws at.ainf.diagnosis.model.SolverException
     * @throws at.ainf.diagnosis.tree.exceptions.NoConflictException
     */
    public Set<FormulaSet<IVecIntComparable>> computeSingleConflictAtTime(MultiQuickXplain<IVecIntComparable> quick, PropositionalTheory theory) throws InconsistentTheoryException, SolverException, NoConflictException {
        List<IVecIntComparable> list = new LinkedList<IVecIntComparable>();
        list.addAll(theory.getKnowledgeBase().getFaultyFormulas());

        //set maxConflict to 1 to get only one conflict
        quick.setMaxConflictSetCount(1);
        Set<FormulaSet<IVecIntComparable>> conflict = quick.search(theory, list);
        return conflict;
    }


    /**
     * searches for n conflict and returns them
     * @param MultiQuickXplain<IVecIntComparable> quick
     * @param PropositionalTheory theory
     * @param numConflicts maximum number of computed conflicts
     * @return Set<FormulaSet<IVecIntComparable>> conflicts
     * @throws at.ainf.diagnosis.model.InconsistentTheoryException
     * @throws at.ainf.diagnosis.model.SolverException
     * @throws at.ainf.diagnosis.tree.exceptions.NoConflictException
     */
    public Set<FormulaSet<IVecIntComparable>> computeNConflictsAtTime(MultiQuickXplain<IVecIntComparable> quick, PropositionalTheory theory, int numConflicts) throws InconsistentTheoryException, SolverException, NoConflictException {
        List<IVecIntComparable> list = new LinkedList<IVecIntComparable>();
        list.addAll(theory.getKnowledgeBase().getFaultyFormulas());

        //set maxConflict to 1 to get only one conflict
        quick.setMaxConflictSetCount(numConflicts);
        Set<FormulaSet<IVecIntComparable>> conflicts = quick.search(theory, list);
        return conflicts;
    }


    /**
     * generates theory for the test run2TestSingleConflictComputation()
     * @return PropositionalTheory theory
     */
    public PropositionalTheory generateTheory() {
        //array with background formula
        int[] fm10 = new int[]{10};

        ISolver reasoner = SolverFactory.newDefault();
        reasoner.setExpectedNumberOfClauses(20);
        reasoner.newVar(10);
        PropositionalTheory theory = new PropositionalTheory(reasoner);
        //adds the above background formula
        theory.getKnowledgeBase().addBackgroundFormulas(Collections.<IVecIntComparable>singleton(new VecIntComparable(fm10)));
        List<int[]> vec = new ArrayList<int[]>();

        //add clauses
        vec.add(new int[]{-1, -2, -3, 4});
        vec.add(new int[]{1});
        vec.add(new int[]{2});
        vec.add(new int[]{3});
        vec.add(new int[]{-4});
        vec.add(new int[]{5});

        vec.add(new int[]{16, -17});
        vec.add(new int[]{16, 17});
        vec.add(new int[]{-16});

        vec.add(new int[]{9});
        vec.add(new int[]{-10, -11});
        vec.add(new int[]{-11});

        vec.add(new int[]{6, -7});
        vec.add(new int[]{6, 7});

        vec.add(new int[]{-10, -12});

        for (int[] e : vec)
            theory.addClause(e);

        return theory;
    }

    @Test
    public void run2SetQuickPresentationExample() throws SolverException, InconsistentTheoryException, NoConflictException {

        MultiQuickXplain<IVecIntComparable> quick = new MultiQuickXplain<IVecIntComparable>();
        quick.setAxiomListener(new QXAxiomSetListener<IVecIntComparable>(true));

        Set<FormulaSet<IVecIntComparable>> conflict = computeMultipleConflictsPresentationExample(quick);
        assertEquals(5, conflict.size());
    }

    private Set<FormulaSet<IVecIntComparable>> computeMultipleConflictsPresentationExample(MultiQuickXplain<IVecIntComparable> quick) throws SolverException, InconsistentTheoryException, NoConflictException {
        ISolver reasoner = SolverFactory.newDefault();
        reasoner.setExpectedNumberOfClauses(20);
        reasoner.newVar(10);
        PropositionalTheory theory = new PropositionalTheory(reasoner);
        List<IVecIntComparable> list = new LinkedList<IVecIntComparable>();
        check(quick, theory, list, true);

        List<int[]> vec = new ArrayList<int[]>();

        //add clauses
        vec.add(new int[]{1});
        vec.add(new int[]{2});
        vec.add(new int[]{3});
        vec.add(new int[]{-1, 2});
        vec.add(new int[]{-2, -1});
        vec.add(new int[]{-3, 1});
        vec.add(new int[]{-1, -3});

        vec.add(new int[]{1});
        vec.add(new int[]{2});
        vec.add(new int[]{-1, 2});
        vec.add(new int[]{-2, -1});
        vec.add(new int[]{-2, 3});
        vec.add(new int[]{-3, -2});


        Collection<IVecIntComparable> test = new LinkedList<IVecIntComparable>();
        for (int[] e : vec)
            test.add(theory.addClause(e));
        list.addAll(theory.getKnowledgeBase().getFaultyFormulas());

        return quick.search(theory, list);
    }

    private Collection<IVecIntComparable> check(Searcher<IVecIntComparable> quick, PropositionalTheory theory,
                                                List<IVecIntComparable> list, boolean value) throws SolverException,
            InconsistentTheoryException {
        boolean res = false;
        Collection<IVecIntComparable> fl = null;
        try {
            Set<FormulaSet<IVecIntComparable>> conflict = quick.search(theory, list);
            if (conflict != null && !conflict.isEmpty())
                fl = conflict.iterator().next();
            else
                res = true;
        } catch (NoConflictException e) {
            res = true;
        }
        assertEquals(value, res);
        if (!value)
            assertNotNull(fl);
        return fl;
    }

    @Test
    public void runVerifyTestCases() throws SolverException, InconsistentTheoryException {

        Searcher<IVecIntComparable> quick = new QuickXplain<IVecIntComparable>();
        ISolver reasoner = SolverFactory.newDefault();
        reasoner.setExpectedNumberOfClauses(20);
        reasoner.newVar(10);
        PropositionalTheory theory = new PropositionalTheory(reasoner);

        // add some background knowledge
        int[] fm10 = new int[]{-5};
        theory.getKnowledgeBase().addBackgroundFormulas(Collections.<IVecIntComparable>singleton(new VecIntComparable(fm10)));

        List<IVecIntComparable> list = new LinkedList<IVecIntComparable>();
        check(quick, theory, list, true);

        List<int[]> vec = new ArrayList<int[]>();

        vec.add(new int[]{-1, 2});

        int[] clause = new int[]{3};
        vec.add(clause);

        vec.add(new int[]{-3,4});
        vec.add(new int[]{-4,5});

        //Collection<IVecIntComparable> test = theory.addClauses(vec);
        Collection<IVecIntComparable> test = new LinkedList<IVecIntComparable>();
        for (int[] e : vec)
            test.add(theory.addClause(e));
        list.addAll(theory.getKnowledgeBase().getFaultyFormulas());
        check(quick, theory, list, false);

        IVecIntComparable fm4 = theory.addClause(new int[]{3});
        theory.getKnowledgeBase().addNonEntailedTest(Collections.singleton(fm4));

        Collection<IVecIntComparable> fl = check(quick, theory, list, false);

        assertTrue(fl.size() == 1);
        assertTrue(fl.contains(fm4));
    }


}
