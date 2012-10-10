/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.sat4j.model;

import at.ainf.diagnosis.model.*;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class PropositionalTheory extends AbstractSearchableObject<IVecIntComparable> {

    private boolean createNew = false;

    public PropositionalTheory(ISolver solver) {
        super(solver);
        setFaultyAxiomsManager(new Sat4jFaultyAxiomsManager());
    }

    public Sat4jFaultyAxiomsManager getFaultyAxiomsManager() {
        return (Sat4jFaultyAxiomsManager) super.getFaultyAxiomsManager();
    }

    public void setNumOfLiterals(Collection<IVecIntComparable> expressions) {
        for (IVecIntComparable formula : expressions) {
            getFaultyAxiomsManager().setNumOfLiterals(getFaultyAxiomsManager().getNumOfLiterals()+formula.size());

        }
    }

    @Override
    protected IVecIntComparable negate(IVecIntComparable formula) {
        IVecIntComparable res = new VecIntComparable();
        for (IteratorInt iter = formula.iterator(); iter.hasNext(); ) {
            int val = iter.next() * -1;
            res.push(val);
        }
        return res;
    }

    public boolean verifyConsistency() throws SolverException {
        try {
            ISolver solver = (ISolver) getSolver();

            if (createNew)
                solver = SolverFactory.newDefault();
            else
                solver.reset();

            solver.newVar(getFaultyAxiomsManager().getNumOfLiterals());
            solver.setExpectedNumberOfClauses(getFaultyAxiomsManager().getFormulaStack().size() + getKnowledgeBase().getBackgroundFormulas().size());
            boolean res;
            try {
                addFormulas(solver, getKnowledgeBase().getBackgroundFormulas());
                addFormulas(solver, getFaultyAxiomsManager().getFormulaStack());
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

    private void addFormulas(ISolver solver, Collection<IVecIntComparable> formulas) throws ContradictionException {
        for (IVecIntComparable stat : formulas) {
            IVecIntComparable literals = stat;
            solver.addClause(literals);
        }
    }

    protected int getStorageId(Integer id) {
        return id;
    }

    protected Integer createFormula(int position) {
        return position;
    }



    public IVecIntComparable addClause(int[] vector) {
        VecIntComparable anInt = new VecIntComparable(vector);
        getKnowledgeBase().addFaultyFormula(anInt);
        return anInt;
    }

    public List<IVecIntComparable> addClauses(List<int[]> vectors) {
        List<IVecIntComparable> res = new LinkedList<IVecIntComparable>();
        for (int[] vec : vectors)
            res.add(addClause(vec));
        return res;
    }


}
