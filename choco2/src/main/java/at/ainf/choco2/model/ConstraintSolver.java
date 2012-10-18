package at.ainf.choco2.model;

import at.ainf.diagnosis.model.Solver;
import choco.kernel.model.constraints.Constraint;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 18.10.12
 * Time: 10:09
 * To change this template use File | Settings | File Templates.
 */
public class ConstraintSolver implements Solver<Constraint> {

    @Override
    public void updateModell(Set<Constraint> formulas) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isConsistent() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
