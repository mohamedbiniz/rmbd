package at.ainf.sat4j.model;

import at.ainf.diagnosis.model.AbstractReasoner;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 19.10.12
 * Time: 10:04
 * To change this template use File | Settings | File Templates.
 */
public class ReasonerSat4j extends AbstractReasoner<IVecIntComparable> {

    private ISolver solver;

    public ReasonerSat4j(ISolver solv) {
        this.solver = solv;
    }

    @Override
    protected void updateReasonerModel(Set<IVecIntComparable> axiomsToAdd, Set<IVecIntComparable> axiomsToRemove) {
        //solver.reset();
        for (IVecIntComparable stat : getReasonerFormulas()) {
            try {
                solver.addClause(stat);
            } catch (ContradictionException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean removeFormulasFromCache(Collection<IVecIntComparable> formulas) {
        setSync(false);
        boolean res = false;
        for (IVecIntComparable next : formulas) {
            res |= formulasCache.remove(next);
        }
        return res;
    }

    protected int getNumOfLiterals() {
        int numOfLiterals = 0;
        for (IVecIntComparable formular : getFormulasCache())
            numOfLiterals += formular.size();
        return numOfLiterals;

    }

    @Override
    public boolean isConsistent() {
        try {
            solver.reset();
            solver.newVar(getNumOfLiterals());
            solver.setExpectedNumberOfClauses(getFormulasCache().size());
            sync();

            return solver.isSatisfiable();

        } catch (TimeoutException e) {
            return false;
        }
    }

    @Override
    public ReasonerSat4j newInstance() {
        return new ReasonerSat4j(SolverFactory.newDefault());
    }

}
