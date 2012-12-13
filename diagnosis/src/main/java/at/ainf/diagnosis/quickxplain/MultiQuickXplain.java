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
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.FormulaRenderer;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

import static _dev.TimeLog.start;
import static _dev.TimeLog.stop;

/**
 * @author kostya TODO To change the template for this generated type comment go
 *         to Window - Preferences - Java - Code Style - Code Templates
 */
public class MultiQuickXplain<Id> extends BaseQuickXplain<Id> {

    private int iterations = 0;

    private static Logger logger = LoggerFactory.getLogger(MultiQuickXplain.class.getName());

    private FormulaRenderer<Id> formulaRenderer;
    private final Set<FormulaSet<Id>> results = new LinkedHashSet<FormulaSet<Id>>();
    private ThreadPoolExecutor pool;
    private Set<Id> defaultLocalChanges;
    private final int minThreads;
    private final int maxThreads;
    private final int maxConflicts;

    /**
     * Default constructor that initializes max threads to 2,
     * min threads to 1 and has queue of the size 10
     */
    public MultiQuickXplain() {
        this(2, 10, 10);
    }

    public MultiQuickXplain(int minThreads, int maxThreads, int maxConflicts){
        this.maxConflicts = maxConflicts;
        this.maxThreads = maxThreads;
        this.minThreads = minThreads;
    }

    @Override
    protected Collection<Id> applyChanges(Searchable<Id> c, Collection<Id> formulas, Set<Id> changes)
            throws InconsistentTheoryException, SolverException {
        if (changes != null) {
            for (Id axiom : changes)
                formulas.remove(axiom);
            //if (logger.isDebugEnabled())
            //    logger.start("Removing labels from the list: " + changes);
        }
        return formulas;
    }

    @Override
    protected void rollbackChanges(Searchable<Id> c, Collection<Id> formulas, Set<Id> changes) throws InconsistentTheoryException, SolverException {
        // nothing to rollback here;
    }

    /**
     * @return conflict
     * @throws at.ainf.diagnosis.tree.exceptions.NoConflictException
     *
     */
    @Override
    public Set<FormulaSet<Id>> search(Searchable<Id> searchable, Collection<Id> formulas, Set<Id> changes)
            throws NoConflictException, SolverException, InconsistentTheoryException {
        FormulaSet<Id> conflictFormulas = null;

        if (changes != null)
            formulas = applyChanges(searchable, formulas, changes);

        setDefaultLocalChanges(changes);
        long time = 0;
        try {
            start("QX");
            iterations = 0;
            this.pool = new ThreadPoolExecutor(minThreads, maxThreads, 1,
                    TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(maxConflicts, true));
            this.results.clear();
            quickXplain(searchable, formulas);

            try {
                while (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                }
                if (logger.isInfoEnabled())
                    logger.info("Pool terminated: " + pool.getLargestPoolSize() + " / " + pool.getCompletedTaskCount());
            } catch (InterruptedException e) {
                throw new SolverException("Computation of conflicts was interrupted!");
            }

        } finally {
            //if (MEASURING) {

            if (getIterations() > 0)
                stop("qx");

            stop();

            postProcessFormulas(conflictFormulas, searchable);
            rollbackChanges(searchable, formulas, changes);
        }

        return getResults();

    }

    private void setDefaultLocalChanges(Set<Id> changes) {
        this.defaultLocalChanges = changes;
    }


    public FormulaSet<Id> quickXplain(final Searchable<Id> c, final Collection<Id> u)
            throws NoConflictException, SolverException, InconsistentTheoryException {

        QXThread qxThread = new QXThread();
        qxThread.c = c;
        qxThread.u = u;
        QXAxiomListener<Id> listener = new QXAxiomListener<Id>(true);

        qxThread.qx.setAxiomListener(listener);
        Future<FormulaSet<Id>> fqx = getThreadsPool().submit(qxThread);
        FormulaSet<Id> formulaSet = null;
        try {
            Id foundAxiom = listener.getFoundAxiom();
            if(foundAxiom == null) this.pool.shutdown();
            else{
                if (logger.isInfoEnabled())
                    logger.info("Found axiom " + foundAxiom + " and starting a new task. Active "
                            + pool.getActiveCount() + " complete " + pool.getCompletedTaskCount()
                            + " threads " + pool.getLargestPoolSize());
                //this.pool.shutdown();
                Searchable<Id> ct = c.copy();
                Set<Id> cu = new LinkedHashSet<Id>(u);
                cu.remove(foundAxiom);
                quickXplain(ct, cu);
            }

            formulaSet = fqx.get();
            if (formulaSet != null)
                results.add(formulaSet);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ExecutionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return formulaSet;
    }

    public Set<FormulaSet<Id>> getResults() {
        return results;
    }

    public ThreadPoolExecutor getThreadsPool() {
        return pool;
    }

    public Set<Id> getDefaultLocalChanges() {
        return defaultLocalChanges;
    }

    private class QXThread implements Callable<FormulaSet<Id>> {
        private Searchable<Id> c;
        private Collection<Id> u;
        private QuickXplain<Id> qx = new QuickXplain<Id>();

        @Override
        public FormulaSet<Id> call() {
            try {
                return qx.quickXplain(c, u);
            } catch (NoConflictException e) {
                return null;
            } catch (SolverException e) {
                throw new RuntimeException(e);
            } catch (InconsistentTheoryException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setFormulaRenderer(FormulaRenderer<Id> formulaRenderer) {
        this.formulaRenderer = formulaRenderer;
    }

    public int getIterations() {
        return iterations;
    }
}
