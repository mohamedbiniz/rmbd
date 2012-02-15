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

import at.ainf.theory.Searchable;
import at.ainf.diagnosis.Searcher;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Set;

import static _dev.TimeLog.start;
import static _dev.TimeLog.stop;


/**
 * @author kostya TODO To change the template for this generated type comment go
 *         to Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class BaseQuickXplain<Id> implements Searcher<Id> {

    private static final Logger logger = Logger.getLogger(BaseQuickXplain.class.getName());

    /*
    private long minTime = 10000000;

    private long maxTime = 0;

    private long totalTime = 0;

    private long calls = 0;
    */
    protected abstract Set<Id> quickXplain(final Searchable<Id> c, final Collection<Id> u) throws NoConflictException, SolverException, InconsistentTheoryException;

    protected abstract int getIterations();

    public BaseQuickXplain() {

    }

    /**
     * @param sp sp
     * @return int
     */
    int split(int sp) {
        if (sp >= 0) {
            return Math.round(sp / 2);
        }
        return -1;
    }

    protected abstract Collection<Id> applyChanges(Searchable<Id> c, Collection<Id> formulas, Set<Id> changes)
            throws InconsistentTheoryException, SolverException;

    protected abstract void rollbackChanges(Searchable<Id> c, Collection<Id> formulas, Set<Id> changes) throws InconsistentTheoryException, SolverException;


    public Set<Id> search(Searchable<Id> searchable, Collection<Id> formulas, Set<Id> changes)
            throws NoConflictException, SolverException, InconsistentTheoryException {

        Set<Id> conflictFormulas = null;

        if (changes != null)
            formulas = applyChanges(searchable, formulas, changes);

        long time = 0;
        try {
            //if (MEASURING)
            start("QX");
            conflictFormulas = quickXplain(searchable, formulas);
            //if (logger.isDebugEnabled())
            //logger.debug("Within " + getIterations() + " iterations.");
        } finally {
            //if (MEASURING) {

            if (getIterations() > 0)
                stop("qx");

            stop();
            /*time = System.currentTimeMillis() - time;
            maxTime = Math.max(maxTime, time);
            minTime = Math.min(minTime, time);
            totalTime += time;
            */
            //}
            rollbackChanges(searchable, formulas, changes);
        }


        return conflictFormulas;
    }

    public boolean isDual() {
        return false;
    }

    /*
    public void logStatistics() {
        if (logger.isInfoEnabled()) {
            logger.info("QuickXPlain calls: " + getCalls());
            logger.info("QuickXPlain max time: " + getMaxTime());
            logger.info("QuickXPlain min time: " + getMinTime());
            logger.info("QuickXPlain avg time: " + getAvgTime());
            logger.info("QuickXPlain total time: " + getTotalTime());
        }
    }

    long getMaxTime() {
        return maxTime;
    }

    long getMinTime() {
        return minTime;
    }

    long getAvgTime() {
        return (calls == 0) ? 0 : totalTime / calls;
    }

    long getCalls() {
        return calls;
    }

    long getTotalTime() {
        return totalTime;
    }
    */
}
