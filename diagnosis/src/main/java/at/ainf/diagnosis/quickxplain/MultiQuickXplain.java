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
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.storage.FormulaSetImpl;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static _dev.TimeLog.start;
import static _dev.TimeLog.stop;

/**
 * @author kostya TODO To change the template for this generated type comment go
 *         to Window - Preferences - Java - Code Style - Code Templates
 */
public class MultiQuickXplain<Id> extends BaseQuickXplain<Id> {

    private static Logger logger = LoggerFactory.getLogger(MultiQuickXplain.class.getName());
    private final int minThreads;
    private final int maxThreads;
    private final int maxConflicts;
    private final Set<FormulaSet<Id>> results = new LinkedHashSet<FormulaSet<Id>>();
    private ThreadPoolExecutor pool;
    private int count = 0;
    private ReadWriteLock resultsLock = new ReentrantReadWriteLock(true);

    /**
     * Default constructor that initializes max threads to 2,
     * min threads to 1 and has queue of the size 10
     */
    public MultiQuickXplain() {

        //Turn off logging


        this(2, 10, 10);
    }

    public MultiQuickXplain(int minThreads, int maxThreads, int maxConflicts) {
        this.maxConflicts = maxConflicts;
        this.maxThreads = maxThreads;
        this.minThreads = minThreads;
    }

    @Override
    protected Collection<Id> applyChanges(Searchable<Id> c, Collection<Id> formulas, Set<Id> changes)
            throws InconsistentTheoryException, SolverException {
        formulas = super.applyChanges(c, formulas, changes);

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
        super.rollbackChanges(c, formulas, changes);
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
        setReasoner(searchable.getReasoner());
        if (changes != null)
            formulas = applyChanges(searchable, formulas, changes);

        long time = 0;
        try {
            start("QX");
            resetIterations();
            this.results.clear();

            if (verifyKnowledgeBase(searchable, formulas)) {
                addConflict(new FormulaSetImpl<Id>(new BigDecimal(1), new TreeSet<Id>(), new TreeSet<Id>()));
                return getResults();
            }

            this.pool = new ThreadPoolExecutor(minThreads, maxThreads, 1,
                    TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(maxConflicts, true));

            if (!searchable.allowsMultiThreading())
                throw new IllegalStateException("Trying to apply multi-threaded searcher to a " +
                        "searchable object that does not support multi-threading!");
            searchable.setLock(new ReentrantLock());
            quickXplain(searchable, formulas);

            try {
                while (!pool.awaitTermination(10, TimeUnit.MILLISECONDS)) {
                }
                if (logger.isDebugEnabled())
                    logger.debug("Pool terminated (size/tasks/conflicts): " + pool.getLargestPoolSize() + " / " +
                            pool.getCompletedTaskCount() + " / " + getResults().size());
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

    /**
     * Multi-threaded version of QX that computes a set of conflicts
     *
     * @param c
     * @param u
     * @return
     * @throws NoConflictException
     * @throws SolverException
     * @throws InconsistentTheoryException
     */
    public FormulaSet<Id> quickXplain(final Searchable<Id> c, final Collection<Id> u)
            throws NoConflictException, SolverException, InconsistentTheoryException {

        incCount();
        //FormulaSet<Id> formulaSet = null;

        try {

            if (getResults().size() >= getMaxConflictSetCount()) {
                if (!this.pool.isShutdown())
                    this.pool.shutdownNow();
                return null;
            }

            QXThread qxThread = new QXThread();
            qxThread.c = c;
            qxThread.u = u;
            QXAxiomListener<Id> listener = getAxiomListener().newInstance();

            qxThread.qx.setAxiomListener(listener);
            Future<FormulaSet<Id>> fqx = getThreadsPool().submit(qxThread);

            while (!listener.isReleased() || listener.hasAxioms()) {
                Id foundAxiom = listener.getFoundAxiom();
                if (foundAxiom != null) {
                    if (logger.isDebugEnabled())
                        logger.debug("Level " + getCount() + " - found axiom " + foundAxiom);

                    Set<Id> cu = new HashSet<Id>(u);
                    cu.remove(foundAxiom);
                    if (!containsConflict(cu)) {
                        if (logger.isDebugEnabled())
                            logger.debug("Starting a new task. Active "
                                    + pool.getActiveCount() + " complete " + pool.getCompletedTaskCount()
                                    + " threads " + pool.getLargestPoolSize());

                        Searchable<Id> ct = c.copy();
                        quickXplain(ct, cu);
                    } else if (logger.isDebugEnabled())
                        logger.debug("Duplicate conflict possible. The branch is ignored!");
                }
            }
            if (!fqx.isCancelled()) {
                fqx.get();
            }
            //if (formulaSet != null)
            //    addConflict(formulaSet);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ExecutionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            if (decCount() == 0)
                this.pool.shutdown();
        }
        return null;
    }

    public int getMaxConflictSetCount() {
        return this.maxConflicts;
    }

    private int getCount() {
        return this.count;
    }

    private void addConflict(FormulaSet<Id> formulaSet) {
        if (results.size() >= maxConflicts)
            return;
        resultsLock.writeLock().lock();
        try {
            if (formulaSet != null)
                results.add(formulaSet);
        } finally {
            resultsLock.writeLock().unlock();
        }

    }

    private boolean containsConflict(Set<Id> cu) {
        resultsLock.readLock().lock();
        try {
            for (FormulaSet<Id> ids : getResults()) {
                if (cu.containsAll(ids))
                    return true;
            }
            return false;
        } finally {
            resultsLock.readLock().unlock();
        }
    }

    public Set<FormulaSet<Id>> getResults() {
        return results;
    }

    public ThreadPoolExecutor getThreadsPool() {
        return pool;
    }

    public int decCount() {
        return --this.count;
    }

    public int incCount() {
        return ++this.count;
    }

    private class QXThread implements Callable<FormulaSet<Id>> {
        private Searchable<Id> c;
        private Collection<Id> u;
        private QuickXplain<Id> qx = new QuickXplain<Id>();


        @Override
        public FormulaSet<Id> call() {
            qx.setReasoner(c.getReasoner());
            try {
                FormulaSet<Id> ids = qx.quickXplain(c, u);
                addConflict(ids);
                return ids;
            } catch (NoConflictException e) {
                return null;
            } catch (SolverException e) {
                throw new RuntimeException(e);
            } catch (InconsistentTheoryException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
