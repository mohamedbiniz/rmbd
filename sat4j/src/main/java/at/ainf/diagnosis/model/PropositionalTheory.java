/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.diagnosis.model;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class PropositionalTheory extends AbstractTheory<ISolver, IVecInt> implements
        ITheory<IVecInt> {

    private boolean createNew = false;
    private int numOfLiterals = 0;

    public PropositionalTheory(ISolver solver) {
        super(solver);
    }

    @Override
    public void setBackgroundFormulas(Collection<IVecInt> expressions) throws UnsatisfiableFormulasException, SolverException {
        super.setBackgroundFormulas(expressions);
        for (IVecInt formula : expressions) {
            this.numOfLiterals += formula.size();
        }
    }

    @Override
    protected IVecInt negate(IVecInt formula) {
        IVecInt res = new VecInt();
        for (IteratorInt iter = formula.iterator(); iter.hasNext(); ) {
            int val = iter.next() * -1;
            res.push(val);
        }
        return res;
    }

    public boolean verifyConsistency() throws SolverException {
        try {
            ISolver solver = getSolver();

            if (createNew)
                solver = SolverFactory.newDefault();
            else
                solver.reset();

            solver.newVar(numOfLiterals);
            solver.setExpectedNumberOfClauses(getFormulaStack().size() + getBackgroundFormulas().size());
            boolean res;
            try {
                addFormulas(solver, getBackgroundFormulas());
                addFormulas(solver, getFormulaStack());
                res = solver.isSatisfiable();
            } catch (ContradictionException e) {
                res = false;
            }
            if (createNew)
                solver = null;
            return res;

        } catch (TimeoutException e) {
            throw new SolverException(e);
        }
    }

    private void addFormulas(ISolver solver, Collection<IVecInt> formulas) throws ContradictionException {
        for (IVecInt stat : formulas) {
            IVecInt literals = stat;
            solver.addClause(literals);
        }
    }

    protected int getStorageId(Integer id) {
        return id;
    }

    protected Integer createFormula(int position) {
        return position;
    }

    @Override
    /**
     * Saves the approximate number of variables in {@link #DefaultTheory.numOfLiterals}
     */
    public boolean push(Collection<IVecInt> formulas) {
        boolean res = super.push(formulas);
        if (res)
            for (IVecInt formula : formulas) {
                this.numOfLiterals += (formula).size();
            }
        return res;
    }

    @Override
    public boolean push(IVecInt formula) {
        boolean res = super.push(formula);
        if (res)
            this.numOfLiterals += (formula).size();
        return res;

    }

    public IVecInt addClause(int[] vector) {
        VecInt anInt = new VecInt(vector);
        addActiveFormula(anInt);
        return anInt;
    }

    public List<IVecInt> addClauses(List<int[]> vectors) {
        List<IVecInt> res = new LinkedList<IVecInt>();
        for (int[] vec : vectors)
            res.add(addClause(vec));
        return res;
    }

    public Set<IVecInt> getEntailments(Set<IVecInt> hittingSet) throws SolverException {
        throw new RuntimeException("This theory does not support computation of entailments!");
    }

    public boolean isEntailed(Set<IVecInt> n) {
        throw new RuntimeException("This theory does not support verification of entailments!");
    }
}
