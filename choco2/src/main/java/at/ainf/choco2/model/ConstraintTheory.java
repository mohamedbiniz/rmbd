/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.choco2.model;


import at.ainf.diagnosis.model.AbstractSearchableObject;
import at.ainf.diagnosis.model.SolverException;
import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.solver.Solver;

import java.util.Collection;
import java.util.List;


public class ConstraintTheory extends AbstractSearchableObject<Constraint> {

    private final Model model;


    public ConstraintTheory(Solver solver) {
        super(solver);
        model = new CPModel();
    }


    @Override
    protected Constraint negate(Constraint formulas) {
        return Choco.not(formulas);
    }

    public ConstraintTheory(Solver solver, Model model) {
        super(solver);
        this.model = model;
    }

    public boolean verifyConsistency() throws SolverException {
        modifyModel(true);
        //Solver solver = getSolver();
        Solver solver = new CPSolver();
        solver.read(this.model);
        boolean res = solver.solve();
        modifyModel(false);
        return res;
    }

    /**
     * Adds/removes constraints from the model.
     * Background constraints remain in the model.
     *
     * @param add if <code>true</code>, the method will add all
     *            formulas to the model, and remove in the opposite case.
     */
    private void modifyModel(boolean add) {
        for (Constraint cnst : getReasonerKB().getFormularSet()) {
            if (add)
                this.model.addConstraint(cnst);
            else
                this.model.removeConstraint(cnst);
        }
    }

    public void addConstraint(Constraint cnt) {
        getKnowledgeBase().addFaultyFormula(cnt);
    }

    public List<Integer> addConstraints(Collection<Constraint> cnts) {
        return getKnowledgeBase().addFaultyFormulas(cnts);
    }

    public Model getModel() {
        return this.model;
    }


}
