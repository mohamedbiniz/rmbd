/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.diagnosis.tree;

import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.model.UnsatisfiableFormulasException;
import at.ainf.diagnosis.storage.HittingSet;
import at.ainf.diagnosis.storage.Storage;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: student99
 * Date: 04.08.2009
 * Time: 09:01:12
 * To change this template use File | Settings | File Templates.
 */
public class IterativeDeepening<Id> extends DepthLimitedSearch<Id> {

    private final long LIMIT = Integer.MAX_VALUE;
    private int startDepth;
    private int step;

    public IterativeDeepening(Storage<HittingSet<Id>, Set<Id>, Id> storage) {
        super(storage);
        this.startDepth = 1;
        this.step = 1;
    }

    public IterativeDeepening(Storage<HittingSet<Id>, Set<Id>, Id> storage, int startDepth, int step) {
        super(storage);
        this.startDepth = startDepth;
        this.step = step;
    }

    public Set<HittingSet<Id>> run() throws SolverException, NoConflictException, UnsatisfiableFormulasException {
        int iterationDepth = this.startDepth;
        do {
            setLimit(iterationDepth);
            super.run();
            // resets List of openNodes
            clearOpenNodes();
            pushOpenNodes(getRoot());
            iterationDepth += step;

        } while (iterationDepth < LIMIT && isExpandable());
        return getStorage().getValidHittingSets();
    }

    public void setStartDepth(int startDepth) {
        this.startDepth = startDepth;
    }

    public int getStartDepth() {
        return startDepth;
    }
}
