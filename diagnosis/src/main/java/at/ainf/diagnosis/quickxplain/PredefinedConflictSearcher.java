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
import java.util.LinkedHashSet;
import java.util.Set;

import static _dev.TimeLog.start;

/**
 * @author kostya TODO To change the template for this generated type comment go
 *         to Window - Preferences - Java - Code Style - Code Templates
 */
public class PredefinedConflictSearcher<Id> implements Searcher<Id> {

    private static Logger logger = LoggerFactory.getLogger(PredefinedConflictSearcher.class.getName());
    private boolean isUsed=false;
    private int count=1;
    private boolean isBHS=true;

    public Set<FormulaSet<Id>> getConflictSets() {
        return conflictSets;
    }

    public void setConflictSets(Set<FormulaSet<Id>> conflictSets) {
        this.conflictSets = conflictSets;
    }

    private Set<FormulaSet<Id>> conflictSets;

    public PredefinedConflictSearcher(Set<FormulaSet<Id>> conflictSets) {
        this.conflictSets = conflictSets;
    }

    @Override
    public Set<FormulaSet<Id>> search(Searchable<Id> searchable, Collection<Id> formulas, Set<Id> changes) throws NoConflictException, SolverException, InconsistentTheoryException {



        if(conflictSets.isEmpty()) throw new NoConflictException("No conflicts available!");

        Set<FormulaSet<Id>> result = new LinkedHashSet<FormulaSet<Id>>();

        int i=0;



        for(FormulaSet<Id> conflict:conflictSets){

            if(i==count) break;

            if(i<count && !intersectsWith(conflict,changes)){
                result.add(conflict);
                i++;


            }



        }

        if(isBHS){
            for(FormulaSet<Id> delete : result){
                conflictSets.remove(delete);
            }
        }

        if(result.isEmpty()  ) throw new NoConflictException("No conflicts available!");

        return result;
    }

    @Override
    public Set<FormulaSet<Id>> search(Searchable<Id> searchable, Collection<Id> formulas) throws NoConflictException, SolverException, InconsistentTheoryException {
        return search(searchable, formulas, null);
    }

    //public boolean isDual() {
    //    return false;
    //}

    public void setCount(int cnt){
        this.count=cnt;
    }

    public void setIsBHS(boolean isBHS){
        this.isBHS=isBHS;
    }


    private boolean intersectsWith(Collection<Id> pathLabels, Collection<Id> localConflict) {

        if(pathLabels==null || localConflict==null)
            return false;

        for (Id label : pathLabels) {
            //if (localConflict.contains(label))
            //    return true;
            for (Id axiom : localConflict) {
                if (axiom.equals(label))
                    return true;
            }
        }
        return false;
    }

}
