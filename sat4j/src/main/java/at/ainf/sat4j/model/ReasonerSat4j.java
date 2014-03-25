package at.ainf.sat4j.model;

import at.ainf.diagnosis.model.AbstractReasoner;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.*;

import java.util.Collection;
import java.util.HashSet;
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
    private boolean contradiction;

    public ReasonerSat4j(ISolver solv) {
        this.solver = solv;
    }

    @Override
    protected void updateReasonerModel(Set<IVecIntComparable> axiomsToAdd, Set<IVecIntComparable> axiomsToRemove) {
        //solver.reset();
        setContradiction(false);
        for (IVecIntComparable stat : getReasonerFormulas()) {
            try {
                solver.addClause(stat);
            } catch (ContradictionException e) {
                setContradiction(true);
                e.printStackTrace();
                return;
            }
        }
    }

    public boolean removeFormulasFromCache(Collection<IVecIntComparable> formulas) {
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

            return !hasContradiction() && solver.isSatisfiable();

        } catch (TimeoutException e) {
            return false;
        }
    }

    @Override
    public boolean isEntailed(Set<IVecIntComparable> test) {
        try {
            // process test case
            Set<IVecIntComparable> statement = negate(test);
            addFormulasToCache(statement);

            solver.reset();
            solver.newVar(getNumOfLiterals());
            solver.setExpectedNumberOfClauses(getFormulasCache().size());

            sync();

            boolean result = hasContradiction() || !solver.isSatisfiable();
            removeFormulasFromCache(statement);
            return result;
        } catch (TimeoutException e) {
            return false;
        }
    }

    private Set<IVecIntComparable> negate(Set<IVecIntComparable> test) {
        if (test.size() > 1)
            throw new UnsupportedOperationException("Entailment checking for non-unary CNFs " +
                    "is not supported at teh moment.");
        Set<IVecIntComparable> res = new HashSet<IVecIntComparable>();
        IVecIntComparable el = test.iterator().next();
        IteratorInt iterator = el.iterator();
        while (iterator.hasNext()) {
            int next = iterator.next();
            res.add(new VecIntComparable(new int[]{-1*next}));
        }
        return res;
    }

    @Override
    public ReasonerSat4j newInstance() {
        return new ReasonerSat4j(SolverFactory.newDefault());
    }

    public void setContradiction(boolean contradictionException) {
        this.contradiction = contradictionException;
    }

    public boolean hasContradiction() {
        return contradiction;
    }
}
