/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.sat4j.quickxplain;

import at.ainf.diagnosis.Searcher;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.quickxplain.OldQuickXplain;
import at.ainf.sat4j.model.PropositionalTheory;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class QuickXplainTest {


    @Test
    public void testOld() throws SolverException, InconsistentTheoryException {
        Searcher<IVecInt> quick = new OldQuickXplain<IVecInt>();
        for (int i = 0; i < 1000; i++) {
            runQuick(quick);
        }
    }

    @Test
    public void testNew() throws SolverException, InconsistentTheoryException {
        Searcher<IVecInt> quick = new NewQuickXplain<IVecInt>();
        for (int i = 0; i < 1000; i++) {
            runQuick(quick);
        }
    }

    public void runQuick(Searcher<IVecInt> quick) throws SolverException, InconsistentTheoryException {

        int[] fm10 = new int[]{10};


        ISolver reasoner = SolverFactory.newDefault();
        reasoner.setExpectedNumberOfClauses(20);
        reasoner.newVar(10);
        PropositionalTheory theory = new PropositionalTheory(reasoner);
        theory.addBackgroundFormula(new VecInt(fm10));

        List<IVecInt> list = new LinkedList<IVecInt>();
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

        Collection<IVecInt> test = theory.addClauses(vec);
        list.addAll(theory.getActiveFormulas());
        check(quick, theory, list, true);

        IVecInt fm4 = theory.addClause(new int[]{-4});
        list.add(fm4);

        IVecInt fm5 = theory.addClause(new int[]{5});
        list.add(fm5);

        Collection<IVecInt> fl = check(quick, theory, list, false);

        assertTrue(fl.size() == 5);
        assertTrue(fl.contains(fm4));
        assertFalse(fl.contains(fm5));
        assertTrue(fl.containsAll(test));

        list.clear();

        IVecInt fm6 = theory.addClause(new int[]{6, -7});
        list.add(fm6);
        IVecInt fm7 = theory.addClause(new int[]{6, 7});
        list.add(fm7);
        IVecInt fm8 = theory.addClause(new int[]{-6});
        list.add(fm8);
        list.add(fm5);

        fl = check(quick, theory, list, false);

        list.remove(fm5);
        assertTrue(fl.size() == 3);
        assertTrue(fl.containsAll(list));
    }

    private Collection<IVecInt> check(Searcher<IVecInt> quick, PropositionalTheory theory,
                                      List<IVecInt> list, boolean value) throws SolverException,
            InconsistentTheoryException {
        boolean res = false;
        Collection<IVecInt> fl = null;
        try {
            fl = quick.search(theory, list, null);
        } catch (NoConflictException e) {
            res = true;
        }
        assertEquals(value, res);
        if (!value)
            assertNotNull(fl);
        return fl;
    }

}
