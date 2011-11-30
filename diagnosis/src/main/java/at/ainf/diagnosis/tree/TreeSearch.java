/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.diagnosis.tree;

import at.ainf.diagnosis.Searcher;
import at.ainf.diagnosis.model.ITheory;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.model.UnsatisfiableFormulasException;
import at.ainf.diagnosis.storage.HittingSet;
import at.ainf.diagnosis.storage.Storage;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: student99
 * Date: 03.08.2009
 * Time: 14:15:23
 * To change this template use File | Settings | File Templates.
 */
public interface TreeSearch<T extends HittingSet<Id>, E extends Set<Id>, Id> {

    public Set<T> run() throws SolverException, NoConflictException, UnsatisfiableFormulasException;

    public void setSearcher(Searcher<Id> searcher);

    public void setTheory(ITheory<Id> theory);

    public ITheory<Id> getTheory();

    public Storage<T, E, Id> getStorage();

    public void setMaxHittingSets(int maxHittingSets);

    public int getMaxHittingSets();

    Set<T> run(int numberOfHittingSets) throws SolverException, NoConflictException, UnsatisfiableFormulasException;
}
