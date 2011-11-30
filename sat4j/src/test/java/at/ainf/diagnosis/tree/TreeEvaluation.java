/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.diagnosis.tree;

import at.ainf.diagnosis.model.PropositionalTheory;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.model.UnsatisfiableFormulasException;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.storage.HittingSet;
import at.ainf.diagnosis.storage.SimpleStorage;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: student99
 * Date: 05.08.2009
 * Time: 14:28:52
 * To change this template use File | Settings | File Templates.
 */
public class TreeEvaluation {
    private static Logger logger = Logger.getLogger(TreeEvaluation.class.getName());

    @Before
    public void setUp() {
        String conf = ClassLoader.getSystemResource("sat4j-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }

    @Test
    public void createTree() throws SolverException, ContradictionException,
            NoConflictException, UnsatisfiableFormulasException {

        if (logger.isInfoEnabled())
            logger.info("Starting the tree creation test.");
        SimpleStorage<IVecInt> storage = new SimpleStorage<IVecInt>();
        List<TreeSearch<HittingSet<IVecInt>, Set<IVecInt>, IVecInt>> search = new ArrayList<TreeSearch<HittingSet<IVecInt>, Set<IVecInt>, IVecInt>>();
        search.add(new BreadthFirstSearch<IVecInt>(storage));
        search.add(new DepthFirstSearch<IVecInt>(storage));
        search.add(new DepthLimitedSearch<IVecInt>(storage));
        search.add(new IterativeDeepening<IVecInt>(storage));
        search.add(new MixedTreeSearch<IVecInt>(storage));

        for (TreeSearch<HittingSet<IVecInt>, Set<IVecInt>, IVecInt> sr : search)
            run(sr);

    }

    private void run(TreeSearch<HittingSet<IVecInt>, Set<IVecInt>, IVecInt> search) throws SolverException, ContradictionException, NoConflictException, UnsatisfiableFormulasException {
        search.setSearcher(new NewQuickXplain<IVecInt>());

        int[] clause = new int[]{5, 6};
        PropositionalTheory th = new PropositionalTheory(SolverFactory.newDefault());
        th.addBackgroundFormula(new VecInt(clause));

        List<IVecInt> conflict1 = new LinkedList<IVecInt>();
        Set<IVecInt> diagnosis1 = new TreeSet<IVecInt>();
        Set<IVecInt> diagnosis2 = new TreeSet<IVecInt>();
        Set<IVecInt> diagnosis3 = new TreeSet<IVecInt>();
        Set<IVecInt> diagnosis4 = new TreeSet<IVecInt>();

        // simple conflict conflict1-c4
        IVecInt var = th.addClause(new int[]{-1, -2, 3});
        conflict1.add(var);
        diagnosis1.add(var);

        var = th.addClause(new int[]{1});
        conflict1.add(var);
        diagnosis2.add(var);

        var = th.addClause(new int[]{2});
        conflict1.add(var);
        diagnosis3.add(var);

        //Storage.getStorage().setTheory(th);
        // fails to create a root since th is sat
        //search.run();

        //Storage.getStorage().resetStorage();

        List<IVecInt> conflict2 = new LinkedList<IVecInt>();
        var = th.addClause(new int[]{-3});
        conflict1.add(var);
        diagnosis4.add(var);
        conflict2.add(var);

        var = th.addClause(new int[]{3});
        conflict2.add(var);
        diagnosis1.add(var);
        diagnosis2.add(var);
        diagnosis3.add(var);

        search.setTheory(th);
        //search.setMaxHittingSets(2);
        // succeeds to create a root since th is unsat
        search.run();

        Collection<HittingSet<IVecInt>> diagnoses = search.getStorage().getValidHittingSets();
        logger.debug("Diagnoses: " + diagnoses.toString());
        assertTrue(searchDub(diagnoses));
        assertTrue(diagnoses.size() == 4);
        assertTrue(diagnoses.contains(diagnosis1));
        assertTrue(diagnoses.contains(diagnosis2));
        assertTrue(diagnoses.contains(diagnosis3));
        assertTrue(diagnoses.contains(diagnosis4));

        Collection<Set<IVecInt>> conflicts = search.getStorage().getConflictSets();
        logger.debug("Conflict: " + conflicts.toString());
        assertTrue(searchDub(conflicts));
        assertTrue(conflicts.size() == 2);
        assertTrue(conflicts.contains(conflict1));
        assertTrue(conflicts.contains(conflict2));
    }


    private boolean searchDub(Collection<? extends Set<IVecInt>> conflicts) {
        short k = 0;
        for (Collection<IVecInt> conflict1 : conflicts) {
            k = 0;
            for (Collection<IVecInt> conflict2 : conflicts) {
                if (conflict1.size() == conflict2.size() && conflict1.containsAll(conflict2))
                    k++;
                if (k > 1)
                    return false;
            }
        }
        return true;
    }

    @Test
    public void testTests() throws SolverException, NoConflictException, UnsatisfiableFormulasException {
        SimpleStorage<IVecInt> storage = new SimpleStorage<IVecInt>();
        BreadthFirstSearch<IVecInt> search = new BreadthFirstSearch<IVecInt>(storage);
        search.setSearcher(new NewQuickXplain<IVecInt>());
        PropositionalTheory th = new PropositionalTheory(SolverFactory.newDefault());
        VecInt vecInt = new VecInt(new int[]{-6});
        LinkedList<IVecInt> bg = new LinkedList<IVecInt>();
        bg.add(vecInt);
        th.setBackgroundFormulas(bg);


        // create unsat theory
        th.addClause(new int[]{-1, -2, 3});
        th.addClause(new int[]{-4, -5, -3});
        th.addClause(new int[]{-1, 5});
        th.addClause(new int[]{-4, 2});
        th.addClause(new int[]{4});
        th.addClause(new int[]{1});

        search.setTheory(th);
        search.run();

        assertEquals(6, search.getStorage().getHittingSetsCount());
        search.getStorage().resetStorage();


        th.addPositiveTest(new VecInt(new int[]{2}));
        boolean test = false;
        try {
            th.addNegativeTest(new VecInt(new int[]{2}));
        } catch (UnsatisfiableFormulasException e) {
            test = true;
        }
        assertTrue(test);

        // specify 4 types of tests
        IVecInt ntest = new VecInt(new int[]{-4});
        th.addNegativeTest(ntest);
        IVecInt ptest = new VecInt(new int[]{2});
        th.addPositiveTest(ptest);
        th.addEntailedTest(new VecInt(new int[]{3}));
        // this is unsat with background
        VecInt net = new VecInt(new int[]{-6});

        // verify the results
        test = false;
        try {
            th.addNonEntailedTest(net);
        } catch (UnsatisfiableFormulasException e) {
            test = true;
        }
        assertTrue(test);

        th.removeNonEntailedTest(net);
        th.addNonEntailedTest(new VecInt(new int[]{5}));

        search.setTheory(th);
        search.run();

        assertEquals(search.getStorage().getHittingSetsCount(), 1);

        for (Collection<IVecInt> hs : search.getStorage().getValidHittingSets()) {
            logger.info(hs);
            assertTrue(hs.toString().equals("[-1,5]"));
        }

    }

    @Test
    public void testStopAndGo() throws SolverException, NoConflictException, UnsatisfiableFormulasException {
        SimpleStorage<IVecInt> storage = new SimpleStorage<IVecInt>();
        BreadthFirstSearch<IVecInt> search = new BreadthFirstSearch<IVecInt>(storage);
        search.setSearcher(new NewQuickXplain<IVecInt>());
        PropositionalTheory th = new PropositionalTheory(SolverFactory.newDefault());


        // create unsat theory  with 6 diagnoses
        // create unsat theory
        th.addClause(new int[]{-1, -2, 3});
        th.addClause(new int[]{-4, -5, -3});
        th.addClause(new int[]{-1, 5});
        th.addClause(new int[]{-4, 2});
        th.addClause(new int[]{4});
        th.addClause(new int[]{1});


        // find 2 first diagnoses
        search.setTheory(th);
        search.setMaxHittingSets(2);
        search.run();
        assertEquals(2, search.getStorage().getHittingSetsCount());

        // find next 3 diagnoses
        search.setMaxHittingSets(5);
        search.run();
        assertEquals(5, search.getStorage().getHittingSetsCount());
        // find next one diagnosis
        search.setMaxHittingSets(0);
        search.run();
        assertEquals(6, search.getStorage().getHittingSetsCount());

        // reset strategies and find all 6 at once
        search.run();
        assertEquals(6, search.getStorage().getHittingSetsCount());
    }

}
