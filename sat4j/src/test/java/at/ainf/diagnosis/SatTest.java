/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.diagnosis;

import static org.junit.Assert.*;
import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import java.util.ArrayList;
import java.util.List;

public class SatTest {

    private static boolean CREATE = false;
    
    public SatTest() {

    }

    @Test
    public void testWithReset() throws TimeoutException, ContradictionException {
        ISolver solver = getSolver(null, 4, 5);
        int[] clause;
        boolean res;

        // test empty
        res = solver.isSatisfiable();
        assertTrue(res);
        solver.reset();

        // test one clause
        solver.newVar(1);
        clause = new int[]{-4};
        solver.addClause(new VecInt(clause));
        res = solver.isSatisfiable();
        assertTrue(res);
        solver.reset();

        // test multiply statements
        res = true;
        solver.newVar(4);
        addData(solver);
        clause = new int[]{-4};
        try {
            solver.addClause(new VecInt(clause));
        } catch (ContradictionException e) {
            res = false;
        }
        if (res) {
            //addData(solver);
            res = solver.isSatisfiable();
        }
        assertFalse(res);
        solver.reset();
    }


    @Test
    public void problemTest() throws TimeoutException, ContradictionException {

        ISolver solver = null;
        if (!CREATE)
            solver = getSolver(solver, 4, 5);
        List<int[]> em = new ArrayList<int[]>();
        List<int[]> cl = new ArrayList<int[]>();
        cl.add(new int[]{-4});
        cl.add(new int[]{5});

        for (int i = 0; i < 10; i++) {
            solve(getSolver(solver, 4, 5), em, true);
            solve(getSolver(solver, 4, 5), cl, false);
            solve(getSolver(solver, 4, 5), em, true);
        }
    }

    private void solve(ISolver solver, List<int[]> clauses, boolean value) throws ContradictionException, TimeoutException {
        boolean res = true;

        try {
            int[] lclause = new int[]{-1, -2, -3, 4};
            solver.addClause(new VecInt(lclause));
            lclause = new int[]{1};
            solver.addClause(new VecInt(lclause));
            lclause = new int[]{2};
            solver.addClause(new VecInt(lclause));
            lclause = new int[]{3};
            solver.addClause(new VecInt(lclause));
            for (int[] clause : clauses)
                solver.addClause(new VecInt(clause));

        } catch (ContradictionException e) {
            res = false;
        }
        if (res) {
            res = solver.isSatisfiable();
        }
        assertEquals(res, value);
    }

    private ISolver getSolver(ISolver solver, int vars, int clauses) {
        if (solver == null) {
            solver = SolverFactory.newDefault();
            solver.setTimeout(3600);
            solver.newVar(vars);
            solver.setExpectedNumberOfClauses(clauses);
        } else {
            solver.reset();
        }
        return solver;
    }

    private void addData(ISolver solver) throws ContradictionException {
        int[] clause = new int[]{-1, -2, -3, 4};
        solver.addClause(new VecInt(clause));

        clause = new int[]{1};
        solver.addClause(new VecInt(clause));

        clause = new int[]{2};
        solver.addClause(new VecInt(clause));

        clause = new int[]{3};
        solver.addClause(new VecInt(clause));

    }

    @Test
    public void testBoth()  throws TimeoutException,
            ContradictionException
    {
          hardTest(null);
          ISolver solver = getSolver(null, 4, 5);
          hardTest(solver);
    }

    public void hardTest(ISolver sol) throws TimeoutException,
            ContradictionException {
        ISolver solver = getSolver(sol, 4, 5);

        boolean res;

        // test empty
        res = solver.isSatisfiable();
        assertTrue(res);

        solver = getSolver(sol, 4, 5);
        // test one clause
        solver.newVar(1);
        int[] clause = new int[]{-4};
        solver.addClause(new VecInt(clause));
        res = solver.isSatisfiable();
        assertTrue(res);

        solver = getSolver(sol, 4, 5);
        // test multiply statements
        solver.newVar(4);
        addData(solver);
        assertTrue(solver.isSatisfiable());

        // test multiply not sat (stress)
        for (int i = 1; i < 1000; i++) {
            solver = getSolver(sol, 4, 5);
            clause = new int[]{-4};
            solver.addClause(new VecInt(clause));
            addData(solver);
            res = solver.isSatisfiable();
            assertFalse(res);
        }

        solver = getSolver(sol, 4, 5);
        solver.newVar(2);
        clause = new int[]{1, -2};
        solver.addClause(new VecInt(clause));

        clause = new int[]{1, 2};
        solver.addClause(new VecInt(clause));

        clause = new int[]{-1};
        solver.addClause(new VecInt(clause));

        res = solver.isSatisfiable();
        assertFalse(res);


    }


}
