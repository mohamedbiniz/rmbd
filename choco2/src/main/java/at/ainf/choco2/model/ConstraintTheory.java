/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.choco2.model;


import at.ainf.diagnosis.model.AbstractSearchableObject;
import at.ainf.diagnosis.model.IReasoner;
import at.ainf.diagnosis.model.SolverException;
import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.solver.Solver;

import java.util.Collection;


public class ConstraintTheory extends AbstractSearchableObject<Constraint> {


    public ConstraintTheory(Solver solver) {
        this(solver,new CPModel());
    }

    public ReasonerConstraint getReasoner() {
        return (ReasonerConstraint) super.getReasoner();
    }

    @Override
    protected Constraint negate(Constraint formulas) {
        return Choco.not(formulas);
    }

    public ConstraintTheory(Solver solver, Model model) {
        super();
        setReasoner(new ReasonerConstraint(model));
    }

    public boolean verifyConsistency() throws SolverException {
        return getReasoner().isConsistent();

    }



    public void addConstraints(Collection<Constraint> cnts) {
        getKnowledgeBase().addFormular(cnts);
    }


}
