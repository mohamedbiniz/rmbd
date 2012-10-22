package at.ainf.choco2.model;

import at.ainf.diagnosis.model.AbstractReasoner;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.solver.Solver;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 18.10.12
 * Time: 10:09
 * To change this template use File | Settings | File Templates.
 */
public class ReasonerConstraint extends AbstractReasoner<Constraint> {

    private Model model;

    public ReasonerConstraint() {
        this.model = new CPModel();
    }

    public ReasonerConstraint(Model model) {
        this.model = model;
    }

    @Override
    public boolean isConsistent() {
        Solver solver = new CPSolver();
        solver.read(this.model);
        return solver.solve();
    }

    private void clearModel() {
        Set<Constraint> contraints = new LinkedHashSet<Constraint>();
        for (Iterator<Constraint> iter = model.getConstraintIterator(); iter.hasNext();)
            contraints.add(iter.next());
        for (Constraint constraint : contraints)
            model.removeConstraint(constraint);
    }

    @Override
    public void sync() {
        clearModel();
        for (Constraint cons : getReasonendFormulars())
            model.addConstraint(cons);

    }

}
