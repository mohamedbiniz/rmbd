package at.ainf.diagnosis.partitioning;

import at.ainf.diagnosis.model.AbstractReasoner;
import at.ainf.diagnosis.model.IReasoner;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 24.10.12
 * Time: 15:34
 * To change this template use File | Settings | File Templates.
 */
public class MinimizerReasoner<T> extends AbstractReasoner<T> {

    @Override
    protected void updateReasonerModel(Set<T> axiomsToAdd, Set<T> axiomsToRemove) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isConsistent() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public MinimizerReasoner<T> newInstance() {
        throw new IllegalStateException("This reasoner can not give a new instance");
    }

}
