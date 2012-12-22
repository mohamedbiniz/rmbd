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

            conflict = computeMultipleConflicts(quick);
        }
        assertEquals(conflict.size(), 5);
    }

    @Test
    @Ignore
    public void run2SetQuick() throws SolverException, InconsistentTheoryException, NoConflictException {

        MultiQuickXplain<IVecIntComparable> quick = new MultiQuickXplain<IVecIntComparable>();
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

        vec.add(new int[]{6, -7});
        vec.add(new int[]{6, 7});
        vec.add(new int[]{-6});

        vec.add(new int[]{16, -17});
        vec.add(new int[]{16, 17});
        vec.add(new int[]{-16});

        vec.add(new int[]{9});
        vec.add(new int[]{-9, 11});
        vec.add(new int[]{-10, -11});
        vec.add(new int[]{-11});

        vec.add(new int[]{6, 11});
        vec.add(new int[]{-10, -11});

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


}
