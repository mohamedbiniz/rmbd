/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

/*
 * Created on 02.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package at.ainf.diagnosis.quickxplain;

import at.ainf.diagnosis.Searchable;
import at.ainf.diagnosis.Searcher;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;

import static _dev.TimeLog.start;

/**
 * @author kostya TODO To change the template for this generated type comment go
 *         to Window - Preferences - Java - Code Style - Code Templates
 */
public class PredefinedConflictSearcher<Id> implements Searcher<Id> {

    private static Logger logger = LoggerFactory.getLogger(PredefinedConflictSearcher.class.getName());
    private boolean isUsed=false;

    public Set<FormulaSet<Id>> getConflictSets() {
        return conflictSets;
    }

    private Set<FormulaSet<Id>> conflictSets;

    public PredefinedConflictSearcher(Set<FormulaSet<Id>> conflictSets) {
        this.conflictSets = conflictSets;
    }

    @Override
    public Set<FormulaSet<Id>> search(Searchable<Id> searchable, Collection<Id> formulas, Set<Id> changes) throws NoConflictException, SolverException, InconsistentTheoryException {

        if(isUsed) throw new NoConflictException("No conflicts available!");

        isUsed=true ;

        return conflictSets;
    }

    @Override
    public Set<FormulaSet<Id>> search(Searchable<Id> searchable, Collection<Id> formulas) throws NoConflictException, SolverException, InconsistentTheoryException {
        return search(searchable, formulas, null);
    }

    //public boolean isDual() {
    //    return false;
    //}


}
