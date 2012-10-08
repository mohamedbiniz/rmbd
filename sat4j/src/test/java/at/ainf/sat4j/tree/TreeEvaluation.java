/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.sat4j.tree;

import at.ainf.diagnosis.tree.*;
import at.ainf.diagnosis.tree.searchstrategy.BreadthFirstSearchStrategy;
import at.ainf.sat4j.model.IVecIntComparable;
import at.ainf.sat4j.model.PropositionalTheory;
import at.ainf.sat4j.model.VecIntComparable;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import org.junit.Test;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static Logger logger = LoggerFactory.getLogger(TreeEvaluation.class.getName());

    /*@Before
    public void setUp() {
        String conf = ClassLoader.getSystemResource("sat4j-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }*/

    @Test
    public void createTree() throws SolverException, ContradictionException,
            NoConflictException, InconsistentTheoryException {

        if (logger.isInfoEnabled())
            logger.info("Starting the tree creation test.");
        //SimpleStorage<IVecIntComparable> storage = new SimpleStorage<IVecIntComparable>();
        List<TreeSearch<AxiomSet<IVecIntComparable>, IVecIntComparable>> search = new ArrayList<TreeSearch<AxiomSet<IVecIntComparable>, IVecIntComparable>>();
        //search.add(new BreadthFirstSearch<IVecIntComparable>(storage));
        //search.add(new DepthFirstSearch<IVecIntComparable>(storage));
        //search.add(new DepthLimitedSearch<IVecIntComparable>(storage));
        //search.add(new IterativeDeepening<IVecIntComparable>(storage));
        //search.add(new MixedTreeSearch<IVecIntComparable>(storage));

        for (TreeSearch<AxiomSet<IVecIntComparable>, IVecIntComparable> sr : search)
            run(sr);

    }

    private void run(TreeSearch<AxiomSet<IVecIntComparable>, IVecIntComparable> search) throws SolverException, ContradictionException, NoConflictException, InconsistentTheoryException {
        search.setSearcher(new NewQuickXplain<IVecIntComparable>());

        int[] clause = new int[]{5, 6};
        PropositionalTheory th = new PropositionalTheory(SolverFactory.newDefault());
        th.addCheckedBackgroundFormulas(Collections.<IVecIntComparable>singleton(new VecIntComparable(clause)));

        List<IVecIntComparable> conflict1 = new LinkedList<IVecIntComparable>();
        Set<IVecIntComparable> diagnosis1 = new LinkedHashSet<IVecIntComparable>();
        Set<IVecIntComparable> diagnosis2 = new LinkedHashSet<IVecIntComparable>();
        Set<IVecIntComparable> diagnosis3 = new LinkedHashSet<IVecIntComparable>();
        Set<IVecIntComparable> diagnosis4 = new LinkedHashSet<IVecIntComparable>();

        // simple conflict conflict1-c4
        IVecIntComparable var = th.addClause(new int[]{-1, -2, 3});
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
        //search.runPostprocessor();

        //Storage.getStorage().resetStorage();

        List<IVecIntComparable> conflict2 = new LinkedList<IVecIntComparable>();
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

        Collection<AxiomSet<IVecIntComparable>> diagnoses = search.getDiagnoses();
        logger.debug("Diagnoses: " + diagnoses.toString());
        assertTrue(searchDub(diagnoses));
        assertTrue(diagnoses.size() == 4);
        assertTrue(contains(diagnoses,diagnosis1));
        assertTrue(contains(diagnoses,diagnosis2));
        assertTrue(contains(diagnoses,diagnosis3));
        assertTrue(contains(diagnoses,diagnosis4));

        Collection<AxiomSet<IVecIntComparable>> conflicts = search.getConflicts();
        logger.debug("Conflict: " + conflicts.toString());
        assertTrue(searchDub(conflicts));
        assertTrue(conflicts.size() == 2);
        //assertTrue(contains(conflicts,conflict1));
        //assertTrue(contains(conflicts,conflict2));

    }

    private boolean contains (Collection<? extends Set<IVecIntComparable>> set, Collection<IVecIntComparable> e) {
        for (Set<IVecIntComparable> i : set)
            if (e.equals(i)) return true;

        return false;

    }

    private boolean searchDub(Collection<? extends Set<IVecIntComparable>> conflicts) {
        short k = 0;
        for (Collection<IVecIntComparable> conflict1 : conflicts) {
            k = 0;
            for (Collection<IVecIntComparable> conflict2 : conflicts) {
                if (conflict1.size() == conflict2.size() && conflict1.containsAll(conflict2))
                    k++;
                if (k > 1)
                    return false;
            }
        }
        return true;
    }

    @Test
    public void testTests() throws SolverException, NoConflictException, InconsistentTheoryException {
        //SimpleStorage<IVecIntComparable> storage = new SimpleStorage<IVecIntComparable>();
        HsTreeSearch<AxiomSet<IVecIntComparable>,IVecIntComparable> search = new HsTreeSearch<AxiomSet<IVecIntComparable>,IVecIntComparable>();
        search.setCostsEstimator(new SimpleCostsEstimator<IVecIntComparable>());
        search.setSearchStrategy(new BreadthFirstSearchStrategy<IVecIntComparable>());
        search.setSearcher(new NewQuickXplain<IVecIntComparable>());
        PropositionalTheory th = new PropositionalTheory(SolverFactory.newDefault());
        VecIntComparable vecInt = new VecIntComparable(new int[]{-6});
        LinkedList<IVecIntComparable> bg = new LinkedList<IVecIntComparable>();
        bg.add(vecInt);
        th.setBackgroundFormulas(bg);
        if (th.verifyRequirements())
            th.setNumOfLiterals(bg);


        // create unsat theory
        th.addClause(new int[]{-1, -2, 3});
        th.addClause(new int[]{-4, -5, -3});
        th.addClause(new int[]{-1, 5});
        th.addClause(new int[]{-4, 2});
        th.addClause(new int[]{4});
        th.addClause(new int[]{1});

        search.setTheory(th);
        search.run();

        assertEquals(6, search.getDiagnoses().size());
        // search.getStorage().resetStorage();


        th.addPositiveTest(Collections.<IVecIntComparable>singleton(new VecIntComparable(new int[]{2})));
        boolean test = false;

            th.addNegativeTest(Collections.<IVecIntComparable>singleton(new VecIntComparable(new int[]{2})));
        if (!th.isTestConsistent()) {
            test = true;
        }
        assertTrue(test);

        // specify 4 types of tests
        IVecIntComparable ntest = new VecIntComparable(new int[]{-4});
        th.addNegativeTest(Collections.<IVecIntComparable>singleton(ntest));
        IVecIntComparable ptest = new VecIntComparable(new int[]{2});
        th.addPositiveTest(Collections.singleton(ptest));
        th.addEntailedTest(Collections.<IVecIntComparable>singleton(new VecIntComparable(new int[]{3})));
        // this is unsat with background
        VecIntComparable net = new VecIntComparable(new int[]{-6});

        // verify the results
        test = false;

            th.addNonEntailedTest(Collections.<IVecIntComparable>singleton(net));
        if (!th.isTestConsistent()) {
            test = true;
        }
        assertTrue(test);

        th.removeNonEntailedTest(Collections.<IVecIntComparable>singleton(net));
        th.addNonEntailedTest(Collections.<IVecIntComparable>singleton(new VecIntComparable(new int[]{5})));

        search.setTheory(th);
        search.run();

        assertEquals(search.getDiagnoses().size(), 1);

        for (Collection<IVecIntComparable> hs : search.getDiagnoses()) {
            logger.info(hs.toString());
            assertTrue(hs.toString().equals("[-1,5]"));
        }

    }

    @Test
    public void testStopAndGo() throws SolverException, NoConflictException, InconsistentTheoryException {
        //SimpleStorage<IVecIntComparable> storage = new SimpleStorage<IVecIntComparable>();
        HsTreeSearch<AxiomSet<IVecIntComparable>,IVecIntComparable> search = new HsTreeSearch<AxiomSet<IVecIntComparable>,IVecIntComparable>();
        search.setCostsEstimator(new SimpleCostsEstimator<IVecIntComparable>());
        search.setSearchStrategy(new BreadthFirstSearchStrategy<IVecIntComparable>());
        search.setSearcher(new NewQuickXplain<IVecIntComparable>());
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
        assertEquals(2, search.getDiagnoses().size());

        // find next 3 diagnoses
        search.setMaxHittingSets(5);
        search.run();
        assertEquals(5, search.getDiagnoses().size());
        // find next one diagnosis
        search.setMaxHittingSets(0);
        search.run();
        assertEquals(6, search.getDiagnoses().size());

        // reset strategies and find all 6 at once
        search.run();
        assertEquals(6, search.getDiagnoses().size());
    }

}
