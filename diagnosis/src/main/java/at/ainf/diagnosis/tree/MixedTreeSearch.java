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
import at.ainf.theory.storage.AxiomRenderer;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.Storage;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;

import java.util.Collection;
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
public class MixedTreeSearch<Id> implements TreeSearch<AxiomSet<Id>, Id> {

    private AbstractTreeSearch<AxiomSet<Id>, Id> currentStrategy;
    private LinkedList<AbstractTreeSearch<AxiomSet<Id>, Id>> strategys = new LinkedList<AbstractTreeSearch<AxiomSet<Id>, Id>>();
    private int diagnosesCount = 0;
    private ITheory<Id> theory;

    public MixedTreeSearch(Storage<AxiomSet<Id>, Id> storage) {
        strategys.addLast(new BreadthFirstSearch<Id>(storage));
        strategys.addLast(new IterativeDeepening<Id>(storage));
    }

    public MixedTreeSearch(LinkedList<AbstractTreeSearch<AxiomSet<Id>, Id>> strategys) {
        this.strategys = strategys;
    }

    public Set<AxiomSet<Id>> run(int numberOfHittingSets) throws NoConflictException, SolverException, InconsistentTheoryException {
        Set<AxiomSet<Id>> hsSet = new HashSet<AxiomSet<Id>>();
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

    public void addOpenNodesListener(OpenNodesListener l) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeOpenNodesListener(OpenNodesListener l) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Collection<Node<Id>> getOpenNodes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setAxiomRenderer(AxiomRenderer<Id> idAxiomRenderer) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setSearcher(Searcher<Id> searcher) {
        for (AbstractTreeSearch<AxiomSet<Id>, Id> strategy : strategys)
            strategy.setSearcher(searcher);
    }

    public Searcher<Id> getSearcher() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateTree(AxiomSet<Id> conflictSet) throws SolverException, InconsistentTheoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setTheory(ITheory<Id> theory) {
        this.theory = theory;
        for (AbstractTreeSearch<AxiomSet<Id>, Id> strategy : strategys) strategy.setTheory(theory);
    }

    public void setLogic(TreeLogic<AxiomSet<Id>, Id> treeLog) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public ITheory<Id> getTheory() {
        return this.theory;
    }


    public Storage<AxiomSet<Id>, Id> getStorage() {
        return currentStrategy.getStorage();
    }

    public int getHittingSetsCount() {
        return this.diagnosesCount;
    }

    public void setMaxHittingSets(int maxDiagnoses) {
        for (AbstractTreeSearch<AxiomSet<Id>, Id> strategy : strategys)
            strategy.setMaxHittingSets(maxDiagnoses);
    }

    public int getMaxHittingSets() {
        return currentStrategy.getMaxHittingSets();
    }

    public int getNumOfInvalidatedHS() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void clearSearch() {

    }

    public Set<AxiomSet<Id>> continueSearch() throws SolverException, NoConflictException, InconsistentTheoryException {
        return null;
    }

    public Set<AxiomSet<Id>> run() throws SolverException, NoConflictException, InconsistentTheoryException {
        return run(0);
    }
}
