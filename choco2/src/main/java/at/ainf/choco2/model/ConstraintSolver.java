package at.ainf.choco2.model;

import at.ainf.diagnosis.model.Solver;
import choco.cp.model.CPModel;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 18.10.12
 * Time: 10:09
 * To change this template use File | Settings | File Templates.
 */
public class ConstraintSolver implements Solver<Constraint> {

    private Model model;

    public ConstraintSolver() {
        this.model = new CPModel();
    }

    public ConstraintSolver(Model model) {
        this.model = model;
    }

    @Override
    public void updateModell(Set<Constraint> formulas) {

        for (Iterator<Constraint> iterator = model.getConstraintIterator(); iterator.hasNext(); ) {
            iterator.next();
            iterator.remove();
        }

        for (Constraint cons : formulas)
            model.addConstraint(cons);
    }

    @Override
    public boolean isConsistent() {
        return false;
    }

}
