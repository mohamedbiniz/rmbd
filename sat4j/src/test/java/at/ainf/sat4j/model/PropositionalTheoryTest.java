/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.sat4j.model;


import at.ainf.diagnosis.model.SolverException;
import org.sat4j.specs.ContradictionException;

import java.util.List;
import java.util.ArrayList;

public class PropositionalTheoryTest {

    /*@Test
    public void testTheory() throws ContradictionException, SolverException, InconsistentTheoryException {
        int[] clause = new int[]{5, 6};
        PropositionalTheory th = new PropositionalTheory(SolverFactory.newDefault());
        th.getKnowledgeBase().addBackgroundFormulas(Collections.<IVecIntComparable>singleton(new VecIntComparable(clause)));
        assertTrue(th.getKnowledgeBase().hasBackgroundTheory());

        //int count = th.getFaultyAxiomsManager().getTheoryCount();
        //assertEquals(0, count);

        insertConflicts(th);
        assertTrue(th.verifyRequirements());

        //assertEquals(1, th.getFaultyAxiomsManager().getTheoryCount());

        clause = new int[]{-3};
        IVecIntComparable fl = th.addClause(clause);
        th.getFaultyAxiomsManager().add(Collections.singleton(fl));
        //assertEquals(2, th.getFaultyAxiomsManager().getTheoryCount());
        assertFalse(th.verifyRequirements());
        th.getFaultyAxiomsManager().remove();

        //assertEquals(1, th.getFaultyAxiomsManager().getTheoryCount());

        addTheories(3, 7, th);
        //assertEquals(4, th.getFaultyAxiomsManager().getTheoryCount());
        th.getFaultyAxiomsManager().remove(4);

        fl = th.addClause(clause);
        th.getFaultyAxiomsManager().add(Collections.singleton(fl));
        assertTrue(th.verifyRequirements()); }*/

    private void addTheories(int numberOfTheories, int from, PropositionalTheory th) throws SolverException {
        if (numberOfTheories == 0)
            return;

        int ncl = (int) Math.round(Math.random() * 10);
        List<IVecIntComparable> list = new ArrayList<IVecIntComparable>(ncl);
        for (int i = 0; i < ncl; i++) {
            int length = (int) Math.round(Math.random() * 10);
            int[] lclause = new int[ncl];
            for (int j = 0; j < length; j++) {
                from += i * j;
                lclause[i] = from;

            }
            IVecIntComparable fl = th.addClause(lclause);
            list.add(fl);
        }
        th.getFaultyAxiomsManager().add(list);
        addTheories(--numberOfTheories, from, th);
    }

    private void insertConflicts(PropositionalTheory th) throws ContradictionException {

        // simple conflict c1-c4
        List<IVecIntComparable> list = new ArrayList<IVecIntComparable>(3);

        int[] clause = new int[]{-1, -2, 3};
        list.add(th.addClause(clause));

        clause = new int[]{1};
        list.add(th.addClause(clause));

        clause = new int[]{2};
        list.add(th.addClause(clause));

        th.getFaultyAxiomsManager().add(list);
    }
}
