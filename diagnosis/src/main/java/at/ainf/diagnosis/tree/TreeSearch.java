/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.diagnosis.tree;

import at.ainf.diagnosis.DiagSearch;
import at.ainf.diagnosis.Searcher;
import at.ainf.diagnosis.tree.searchstrategy.SearchStrategy;
import at.ainf.diagnosis.model.ITheory;
import at.ainf.diagnosis.storage.AxiomRenderer;
import at.ainf.diagnosis.storage.AxiomSet;

/**
 * Created by IntelliJ IDEA.
 * User: student99
 * Date: 03.08.2009
 * Time: 14:15:23
 * To change this template use File | Settings | File Templates.
 */
public interface TreeSearch<T extends AxiomSet<Id>, Id> extends DiagSearch<T,Id> {


    public Searcher<Id> getSearcher();

    public void setSearcher(Searcher<Id> searcher);

    public ITheory<Id> getTheory();

    public void setTheory(ITheory<Id> theory);

    public int getMaxHittingSets();

    public void setMaxHittingSets(int maxHittingSets);

    public CostsEstimator<Id> getCostsEstimator();

    public void setCostsEstimator(CostsEstimator<Id> costsEstimator);

    public SearchStrategy<Id> getSearchStrategy();

    public void setSearchStrategy(SearchStrategy<Id> searchStrategy);

    public void setAxiomRenderer(AxiomRenderer<Id> renderer);

    public void addOpenNodesListener (OpenNodesListener l);

    public void removeOpenNodesListener (OpenNodesListener l);


}
