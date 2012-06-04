package at.ainf.jsolver.model;

import at.ainf.theory.model.AbstractTheory;
import at.ainf.theory.model.ITheory;
import at.ainf.theory.model.SolverException;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 30.03.12
 * Time: 13:03
 * To change this template use File | Settings | File Templates.
 */
public class JSolverTheory<R,A> extends AbstractTheory<R, A>
        implements ITheory<A> {

    @Override
    protected A negate(A formula) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Set<A> getEntailments(Set<A> hittingSet) throws SolverException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean supportEntailments() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isEntailed(Set<A> n) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean verifyConsistency() throws SolverException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
