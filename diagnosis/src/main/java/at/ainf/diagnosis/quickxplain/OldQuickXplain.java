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
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.model.SolverException;

import java.util.*;


/**
 * @author kostya TODO To change the template for this generated type comment go
 *         to Window - Preferences - Java - Code Style - Code Templates
 */
public class OldQuickXplain<E> extends BaseQuickXplain<E> {

    private int iterations = 0;

    public OldQuickXplain() {
    }


    @Override
    protected Collection<E> applyChanges(Searchable<E> c, Collection<E> formulas, Set<E> changes) {
        if (changes != null) {
            for (E axiom : changes)
                formulas.remove(axiom);
            //if (logger.isDebugEnabled())
            //    logger.debug("Removing labels from the list: " + changes);
        }
        return formulas;
    }

    @Override
    protected void rollbackChanges(Searchable<E> c, Collection<E> formulas, Set<E> changes) {
        // nothing to rollback here;
    }

    /**
     * @param c c
     * @param u u
     * @return conflict
     * @throws NoConflictException exception
     * @throws at.ainf.diagnosis.model.SolverException
     *                             ex
     */
    public Set<E> quickXplain(final Searchable<E> c, final FormulaList<E> u) throws NoConflictException, SolverException {
        // long subTime = 0;
        iterations++;
        if (!c.verifyRequirements())
            return new TreeSet<E>();
        if (u.isEmpty())
            throw new NoConflictException("The unexlored formulas set is Empty");
        int k = getFaultAxiomIndex(c, u);
        Set<E> x = new TreeSet<E>();
        x.add(u.get(k));
        // TODO bounds are set incorrectly when k = first or last element
        int i = split(k - 1);
        FormulaList<E> u1 = (i >= 0) ? u.setBounds(0, i) : null;
        FormulaList<E> u2 = (k - 1 >= i + 1) ? u.setBounds(i + 1, k - 1) : null;
        if (u2 != null && u.size() != 0) {
            c.getFaultyAxiomsManager().push(x);
            if (u1 != null) c.getFaultyAxiomsManager().push(u1);
            x.addAll(quickXplain(c, u2));
            if (u1 != null) c.getFaultyAxiomsManager().pop();
            c.getFaultyAxiomsManager().pop();
        }
        if (u1 != null && u1.size() != 0) {
            c.getFaultyAxiomsManager().push(x);
            x.addAll(quickXplain(c, u1));
            c.getFaultyAxiomsManager().pop();
        }
        return x;
    }


    public Set<E> quickXplain(Searchable<E> c, Collection<E> u) throws NoConflictException, SolverException {
        this.iterations = 0;
        return quickXplain(c, new FormulaList<E>(u));
    }

    public int getIterations() {
        return iterations;
    }

    private int getFaultAxiomIndex(Searchable<E> c, FormulaList<E> u) throws NoConflictException,
            SolverException {
        int k = 0;
        Iterator<E> iter = u.iterator();
        boolean isCoherent = true;
        // c.push();
        while (isCoherent && iter.hasNext()) {
            E next = iter.next();
            c.getFaultyAxiomsManager().push(Collections.singleton(next));
            isCoherent = c.verifyRequirements();

            k++;
        }
        c.getFaultyAxiomsManager().pop(k);
        // decrease k, because it was unnecessary incremented at the end of the
        // while loop
        k--;
        if (isCoherent)
            throw new NoConflictException("All axioms were added to theory and it is satisfiable.");
        return k;
    }
}
