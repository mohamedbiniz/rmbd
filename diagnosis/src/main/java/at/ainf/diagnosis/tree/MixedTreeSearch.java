/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.diagnosis.tree;


import at.ainf.diagnosis.Searcher;
import at.ainf.theory.model.ITheory;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.HittingSet;
import at.ainf.theory.storage.Storage;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: student99
 * Date: 12.08.2009
 * Time: 08:18:39
 * To change this template use File | Settings | File Templates.
 */
public class MixedTreeSearch<Id> implements TreeSearch<HittingSet<Id>, Set<Id>, Id> {

    private AbstractTreeSearch<HittingSet<Id>, Set<Id>, Id> currentStrategy;
    private LinkedList<AbstractTreeSearch<HittingSet<Id>, Set<Id>, Id>> strategys = new LinkedList<AbstractTreeSearch<HittingSet<Id>, Set<Id>, Id>>();
    private int diagnosesCount = 0;
    private ITheory<Id> theory;

    public MixedTreeSearch(Storage<HittingSet<Id>, Set<Id>, Id> storage) {
        strategys.addLast(new BreadthFirstSearch<Id>(storage));
        strategys.addLast(new IterativeDeepening<Id>(storage));
    }

    public MixedTreeSearch(LinkedList<AbstractTreeSearch<HittingSet<Id>, Set<Id>, Id>> strategys) {
        this.strategys = strategys;
    }

    public Set<HittingSet<Id>> run(int numberOfHittingSets) throws NoConflictException, SolverException, InconsistentTheoryException {
        Set<HittingSet<Id>> hsSet = new HashSet<HittingSet<Id>>();
        while (!strategys.isEmpty()) {
            currentStrategy = strategys.removeFirst();
            //currentStrategy.getStorage().setHittingSetsCount(diagnosesCount);
            try {
                hsSet.addAll(currentStrategy.run(numberOfHittingSets));
                strategys.clear();
            } catch (OutOfMemoryError omr) {
                currentStrategy.clearOpenNodes();
                this.diagnosesCount = currentStrategy.getStorage().getHittingSetsCount();
            }
        }
        return hsSet;
    }

    public void setSearcher(Searcher<Id> searcher) {
        for (AbstractTreeSearch<HittingSet<Id>, Set<Id>, Id> strategy : strategys)
            strategy.setSearcher(searcher);
    }

    public void setTheory(ITheory<Id> theory) {
        this.theory = theory;
        for (AbstractTreeSearch<HittingSet<Id>, Set<Id>, Id> strategy : strategys) strategy.setTheory(theory);
    }

    public ITheory<Id> getTheory() {
        return this.theory;
    }


    public Storage<HittingSet<Id>, Set<Id>, Id> getStorage() {
        return currentStrategy.getStorage();
    }

    public int getHittingSetsCount() {
        return this.diagnosesCount;
    }

    public void setMaxHittingSets(int maxDiagnoses) {
        for (AbstractTreeSearch<HittingSet<Id>, Set<Id>, Id> strategy : strategys)
            strategy.setMaxHittingSets(maxDiagnoses);
    }

    public int getMaxHittingSets() {
        return currentStrategy.getMaxHittingSets();
    }

    public Set<HittingSet<Id>> run() throws SolverException, NoConflictException, InconsistentTheoryException {
        return run(0);
    }
}
