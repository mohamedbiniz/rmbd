/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.choco2;

import at.ainf.choco2.model.ConstraintTheory;
import at.ainf.choco2.parser.ConstraintReader;
import at.ainf.choco2.parser.ConstraintReaderException;
import at.ainf.diagnosis.Searcher;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.SimpleStorage;
import at.ainf.diagnosis.tree.BreadthFirstSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static choco.Choco.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by IntelliJ IDEA.
 * User: student99
 * Date: 17.08.2009
 * Time: 13:48:23
 * To change this template use File | Settings | File Templates.
 */
public class TreeEvaluation {

    private static Logger logger = Logger.getLogger(TreeEvaluation.class.getName());

    @BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("choco2-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }


    @Test
    public void treeTest() throws SolverException, InconsistentTheoryException {
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
        Constraint r5 = neq(k, l);


        changable.add(r1);
        changable.add(r2);

        changable.add(r3);
        changable.add(r4);
        changable.add(r5);

        unchangable.add(e1);

        CPSolver solver = new CPSolver();
        ConstraintTheory cth = new ConstraintTheory(solver, md);
        cth.setBackgroundFormulas(unchangable);
        cth.addConstraints(changable);

        Constraint ptest = eq(k, l);

        Constraint ntest = gt(k, 1);

        //cth.addNegativeTest(ntest);
        cth.addPositiveTest(ptest);

        // reasoning
        //cth.push(list.subList(0, 2));

        // reasoning
        //cth.push(list.subList(1, 4));

        // reasoning
        //cth.push(list.subList(2, 5));

        SimpleStorage<Constraint> storage =
                new SimpleStorage<Constraint>();
        BreadthFirstSearch<Constraint> strategy = new BreadthFirstSearch<Constraint>(storage);
        Searcher<Constraint> searcher = new NewQuickXplain<Constraint>();
        strategy.setSearcher(searcher);

        strategy.setTheory(cth);

        try {
            strategy.run();
        } catch (NoConflictException e) {
            logger.error("There is no conflict!", e);
            fail();
        }

        Collection<AxiomSet<Constraint>> diagnoses = strategy.getStorage().getValidHittingSets();
        Collection<Set<Constraint>> conflicts = strategy.getStorage().getConflictSets();

        System.out.println(diagnoses);
        logger.info("Diagnoses: " + diagnoses);
        logger.info("Conflicts: " + conflicts);

        assertTrue(searchDub(conflicts));
        assertTrue(searchDub(diagnoses));

    }

    @Test
    public void treeParserTest() throws SolverException, InconsistentTheoryException {
        //String constraints = "d: int [1,20]; [c1,c] d > 5; [c2,c] d >= 6; [c3,c] d <= 5;";
        String constraints =
                "a: int [1,20]; b: int [1,20]; c: int [1,20]; [c1,c] a > b; [c2,c] b > c; [c3,c] c > a;";

        ConstraintReader reader = new ConstraintReader();
        ConstraintTheory cth = null;
        Model md = new CPModel();
        CPSolver solver = new CPSolver();

        try {
            cth = reader.getConstraints(constraints, solver, md);
        } catch (ConstraintReaderException e) {
            e.printStackTrace();
        }

        System.out.println(cth);
        SimpleStorage<Constraint> storage = new SimpleStorage<Constraint>();
        BreadthFirstSearch strategy = new BreadthFirstSearch<Constraint>(storage);
        strategy.setSearcher(new NewQuickXplain<Constraint>());

        strategy.setTheory(cth);

        try {
            strategy.run();
        } catch (NoConflictException e) {
            logger.error("There is no conflict!", e);
            fail();
        }

        Collection<AxiomSet<Constraint>> diagnoses = strategy.getStorage().getValidHittingSets();
        Collection<Set<Constraint>> conflicts = strategy.getStorage().getConflictSets();

        logger.info("Diagnoses: " + diagnoses);
        logger.info("Conflicts: " + conflicts);

        assertTrue(searchDub(conflicts));
        assertTrue(searchDub(diagnoses));

    }

    private boolean searchDub(Collection<? extends Set<Constraint>> conflicts) {
        short k = 0;
        for (Collection<Constraint> conflict1 : conflicts) {
            k = 0;
            for (Collection<Constraint> conflict2 : conflicts) {
                if (conflict1.size() == conflict2.size() && conflict1.containsAll(conflict2))
                    k++;
                if (k > 1)
                    return false;
            }
        }
        return true;
    }

    @Ignore
    @Test
    public void hardTest() throws SolverException, InconsistentTheoryException {
        List<Constraint> changable = new LinkedList<Constraint>();
        List<Constraint> unchangable = new LinkedList<Constraint>();

        //9x9 sudoku contains 81 fields
        int limit = 4;

        IntegerVariable[] vars = new IntegerVariable[limit];
        Model md = new CPModel();
        for (int i = 0; i < limit; i++) {
            IntegerVariable intvar = makeIntVar("i" + i, 1, 9);
            md.addVariable(intvar);
            vars[i] = intvar;
        }

        for (int i = 0; i < limit - 1; i++) {
            for (int j = i + 1; j < limit; j++) {
                Constraint geq = geq(vars[i], vars[j]);
                Constraint leq = leq(vars[i], vars[j]);
                Constraint neq = neq(vars[i], vars[j]);

                changable.add(geq);
                changable.add(leq);
                changable.add(neq);
            }
        }


        CPSolver solver = new CPSolver();
        ConstraintTheory cth = new ConstraintTheory(solver, md);
        cth.setBackgroundFormulas(unchangable);
        cth.addConstraints(changable);


        // reasoning
        //cth.push(list.subList(0, 2));

        // reasoning
        //cth.push(list.subList(1, 4));

        // reasoning
        //cth.push(list.subList(2, 5));

        SimpleStorage<Constraint> storage = new SimpleStorage<Constraint>();
        BreadthFirstSearch<Constraint> strategy = new BreadthFirstSearch<Constraint>(storage);
        strategy.setSearcher(new NewQuickXplain<Constraint>());

        strategy.setTheory(cth);
        //strategy.setMaxHittingSets(1);

        long startTime = System.currentTimeMillis();

        try {
            strategy.run();
        } catch (NoConflictException e) {
            logger.error("There is no conflict!", e);
            fail();
        }

        long totalTime = System.currentTimeMillis() - startTime;

        if (logger.isDebugEnabled()) {
            logger.debug("Test finished in " + totalTime / 60000 + "Min " + totalTime % 60000 / 1000 + "Sec");
            logger.debug("Found " + strategy.getStorage().getHittingSetsCount() + " diagnoses and "
                    + strategy.getStorage().getHittingSetsCount() + " conflicts.");
        }
    }
}
