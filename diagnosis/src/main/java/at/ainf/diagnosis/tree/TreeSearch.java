/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.diagnosis.tree;

import at.ainf.diagnosis.Searcher;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.SearchStrategy;
import at.ainf.theory.model.ITheory;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.AxiomRenderer;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.Storage;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: student99
 * Date: 03.08.2009
 * Time: 14:15:23
 * To change this template use File | Settings | File Templates.
 */
public interface TreeSearch<T extends AxiomSet<Id>, Id> {

    public int getNumOfInvalidatedHS();
    
    public void clearSearch();

    public Set<T> continueSearch() throws SolverException, NoConflictException, InconsistentTheoryException;

    public Set<T> run() throws SolverException, NoConflictException, InconsistentTheoryException;

    public void setSearcher(Searcher<Id> searcher);

    public Searcher<Id> getSearcher();

    //public void updateTree(AxiomSet<Id> conflictSet) throws SolverException, InconsistentTheoryException ;

    public void setTheory(ITheory<Id> theory);

    //public void setLogic(TreeLogic<T,Id> treeLog);

    public ITheory<Id> getTheory();

    public void setMaxHittingSets(int maxHittingSets);

    public int getMaxHittingSets();

    Set<T> run(int numberOfHittingSets) throws SolverException, NoConflictException, InconsistentTheoryException;

    public void addOpenNodesListener (OpenNodesListener l);

    public void removeOpenNodesListener (OpenNodesListener l);

    //public Collection<Node<Id>> getOpenNodes();

    public void setAxiomRenderer(AxiomRenderer<Id> renderer);

    public Node<Id> getRoot();

    //public void addNodes(List<Node<Id>> nodeList);

    //void pushOpenNode(Node<Id> node);

    public void setCostsEstimator(CostsEstimator<Id> costsEstimator);

    public void setSearchStrategy(SearchStrategy<Id> searchStrategy);

    public Set<T> getConflicts();

    public Set<T> getDiagnoses();

    public CostsEstimator<Id> getCostsEstimator();

    public SearchStrategy<Id> getSearchStrategy();

}
