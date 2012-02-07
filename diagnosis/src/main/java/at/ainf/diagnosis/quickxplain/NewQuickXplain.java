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
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import static _dev.TimeLog.start;

/**
 * @author kostya TODO To change the template for this generated type comment go
 *         to Window - Preferences - Java - Code Style - Code Templates
 */
public class NewQuickXplain<Id> extends BaseQuickXplain<Id> {

    private int iterations = 0;

    public NewQuickXplain() {
    }

    @Override
    protected Collection<Id> applyChanges(Searchable<Id> c, Collection<Id> formulas, Set<Id> changes)
            throws InconsistentTheoryException, SolverException {
        if (changes != null) {
            for (Id axiom : changes)
                formulas.remove(axiom);
            //if (logger.isDebugEnabled())
            //    logger.debug("Removing labels from the list: " + changes);
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
    
    public Set<Id> quickXplain(final Searchable<Id> c, final Collection<Id> u)
            throws NoConflictException, SolverException, InconsistentTheoryException {
        iterations = 0;

        if (!c.verifyRequirements())
            throw new InconsistentTheoryException("Background theory or test cases are inconsistent! Finding conflicts is impossible!");
        c.push(u);
        final boolean isCons = c.verifyRequirements();
        c.pop();

        if (isCons) {
            throw new NoConflictException("The theory is satisfiable!");
        }
        if (u.isEmpty()) {
            return new TreeSet<Id>();
        }
        start("Conflict", "qx");
        return qqXPlain(c, c.getFormulaStack(), new FormulaList<Id>(u));

    }

    private Set<Id> qqXPlain(Searchable<Id> b, Collection<Id> d, FormulaList<Id> c)
            throws SolverException {
        iterations++;
        if (d != null && d.size() != 0 && ! b.verifyRequirements())
            return null;

        if (c.size() == 1) {
            return new TreeSet<Id>(c);
        }
        int k = split(c.size());

        FormulaList<Id> c1 = c.setBounds(0, k - 1);
        FormulaList<Id> c2 = c.setBounds(k, c.size() - 1);

        boolean res = b.push(c1);
        Set<Id> d2 = qqXPlain(b, c1, c2);
        if (res) b.pop();
        res = b.push(d2);
        Set<Id> d1 = qqXPlain(b, d2, c1);
        if (res) b.pop();

        if (d2 != null)
            if (d1 == null)
                return d2;
            else
                d1.addAll(d2);
        return d1;
    }

    public int getIterations() {
        return iterations;
    }

}
