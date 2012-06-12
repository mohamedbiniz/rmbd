/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.diagnosis.tree;

import at.ainf.diagnosis.Searcher;
import at.ainf.diagnosis.tree.searchstrategy.BreadthFirstSearchStrategy;
import at.ainf.theory.model.ITheory;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.Storage;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: student99
 * Date: 03.08.2009
 * Time: 14:21:53
 * To change this template use File | Settings | File Templates.
 */
public class BreadthFirstSearch<Id> extends AbstractTreeSearch<AxiomSet<Id>, Id>
        implements TreeSearch<AxiomSet<Id>, Id> {

    public BreadthFirstSearch(Storage<AxiomSet<Id>, Id> storage) {
        super(storage);
        setCostsEstimator(new SimpleCostsEstimator<Id>());
        setLogic(new HsTreeLogic<AxiomSet<Id>, Id>());
        setSearchStrategy(new BreadthFirstSearchStrategy<Id>());
    }

    /*public BreadthFirstSearch(Storage<AxiomSet<Id>,Id> storage, Searcher<Id> idNewQuickXplain, ITheory<Id> theory) {
        super(storage);
        setCostsEstimator(new SimpleCostsEstimator<Id>());
        setLogic(new HsTreeLogic<AxiomSet<Id>, Id>());
        setSearchStrategy(new BreadthFirstSearchStrategy<Id>());
        setSearcher(idNewQuickXplain);
        setTheory(theory);
    }*/

    /* moved public void expand(Node<Id> node) {
        getSearchStrategy().addNodes(node.expandNode());
    } */

    /* moved public Node<Id> getNode() {
        // gets the first open node of the List
        return getSearchStrategy().popOpenNodes();
    } */

    /* moved public void addNodes(List<Node<Id>> nodeList) {
        // adds the new open nodes at the end of the List
        for (Node<Id> node : nodeList) addLastOpenNodes(node);
    }*/

}
