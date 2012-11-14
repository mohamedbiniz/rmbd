/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.choco2;

import at.ainf.choco2.model.ConstraintTheory;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import static choco.Choco.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TheoryTest {

    private static Logger logger = LoggerFactory.getLogger(TheoryTest.class.getName());

    @Test
    public void unsatTheory() throws SolverException, InconsistentTheoryException {

        List<Constraint> changable = new LinkedList<Constraint>();
        List<Constraint> unchangable = new LinkedList<Constraint>();

        IntegerVariable i = makeIntVar("i", new int[]{0, 1});
        IntegerVariable j = makeIntVar("j", new int[]{0, 1});
        IntegerVariable k = makeIntVar("k", new int[]{0, 1, 2});
        IntegerVariable l = makeIntVar("l", new int[]{0, 1, 2});
        IntegerVariable z = makeIntVar("z", new int[]{0, 1});
        Model md = new CPModel();
        md.addVariables(i, j, k, z);

        Constraint e1 = gt(i, j);

        Constraint r1 = gt(z, j);
        Constraint r2 = gt(i, z);

        Constraint r3 = leq(k, j);
        Constraint r4 = leq(l, j);
        Constraint r5 = neq(k, j);


        changable.add(r1);
        changable.add(r2);

        changable.add(r3);
        changable.add(r4);
        changable.add(r5);

        unchangable.add(e1);
        CPSolver solver = new CPSolver();
        ConstraintTheory cth = new ConstraintTheory(solver, md);
        cth.getKnowledgeBase().setBackgroundFormulas(unchangable);
        if (!cth.verifyRequirements())
            cth.getKnowledgeBase().setEmptyBackgroundFormulas();
        cth.addConstraints(changable);

        // reasoning
        cth.getReasoner().addFormularsToCache(changable.subList(0, 2));
        boolean res = cth.verifyRequirements();
        assertFalse(res);
        cth.getReasoner().removeFormularsFromCache(changable.subList(0, 2));

        // reasoning
        cth.getReasoner().addFormularsToCache(changable.subList(1, 4));
        res = cth.verifyRequirements();
        assertTrue(res);
        cth.getReasoner().removeFormularsFromCache(changable.subList(1, 4));

        // reasoning
        // cth.add(changable.subList(2, 5));
        // res = cth.verifyRequirements();
        // assertFalse(res);
        // cth.remove();

    }

    @Test
    public void testSolver1() {
        IntegerVariable i = makeIntVar("i", new int[]{0, 1});
        IntegerVariable j = makeIntVar("j", new int[]{0, 1});
        IntegerVariable z = makeIntVar("z", new int[]{0, 1});
        Model md = new CPModel();
        md.addVariables(i, j, z);

        Constraint e1 = gt(i, j);
        Constraint r1 = gt(z, j);
        Constraint r2 = gt(i, z);

        md.addConstraints(e1, r1, r2);

        CPSolver solver = new CPSolver();
        boolean res = solve(solver, md);
        if (res) {
            logger.info("i" + solver.getVar(i).getVal());
            logger.info("j" + solver.getVar(j).getVal());
            logger.info("z" + solver.getVar(z).getVal());
        }

    }

    private boolean solve(Solver solver, Model model) {
        // solver = new CPSolver();
        solver.read(model);
        boolean res = true;
        try {
            solver.propagate();
        } catch (ContradictionException e) {
            res = false;
        }
        if (res)
            res = solver.solve();
        return res;
    }

    @Test
    public void testSolver2() {

        Model md = new CPModel();
        CPSolver solver = new CPSolver();

        CPSolver solver2 = new CPSolver();

        IntegerVariable i = makeIntVar("i", new int[]{0, 1});
        IntegerVariable j = makeIntVar("j", new int[]{0, 1});
        IntegerVariable k = makeIntVar("k", new int[]{0, 1, 2});
        IntegerVariable l = makeIntVar("l", new int[]{0, 1, 2});
        IntegerVariable z = makeIntVar("z", new int[]{0, 1});

        md.addVariables(i, j, k, z);

        Constraint e1 = gt(i, j);
        Constraint r1 = gt(z, j);
        Constraint r2 = gt(i, z);

        Constraint r3 = leq(k, j);
        Constraint r4 = leq(l, j);
        Constraint r5 = neq(k, j);

        md.addConstraints(e1, r1, r2);

        assertFalse(solve(solver, md));

        md.removeConstraint(r1);
        md.removeConstraint(r2);

        md.addConstraints(r2, r3, r4);
        assertTrue(solve(solver2, md));

        md.removeConstraint(r2);
        md.removeConstraint(r3);
        md.removeConstraint(r4);

        md.addConstraints(r3, r4, r5);
        assertFalse(solve(solver, md));

    }
}

