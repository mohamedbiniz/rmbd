package at.ainf.sat4j.model;

import at.ainf.diagnosis.model.AbstractReasoner;
import at.ainf.diagnosis.model.SolverException;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 19.10.12
 * Time: 10:04
 * To change this template use File | Settings | File Templates.
 */
public class ReasonerSat4j extends AbstractReasoner<IVecIntComparable> {

    private int numOfLiterals = 0;

    private ISolver solver;

    public ReasonerSat4j(ISolver solv) {
        this.solver = solv;
    }

    public void sync() {
        solver.reset();
        for (IVecIntComparable stat : getReasonendFormulars()) {
            try {
                solver.addClause(stat);
            } catch (ContradictionException e) {
                e.printStackTrace();
            }
        }
    }

    protected int getNumOfLiterals() {
        int numOfLiterals = 0;
        for (IVecIntComparable formular : getReasonendFormulars())
            numOfLiterals += formular.size();
        return numOfLiterals;

    }

    @Override
    public boolean isConsistent() {
        try {
            solver.reset();
            solver.newVar(getNumOfLiterals());
            solver.setExpectedNumberOfClauses(getReasonendFormulars().size());
            sync();

            return solver.isSatisfiable();

        } catch (TimeoutException e) {
            return false;
        }
    }

}
