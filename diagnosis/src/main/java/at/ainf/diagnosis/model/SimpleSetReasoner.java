package at.ainf.diagnosis.model;

import java.util.HashSet;
import java.util.Set;
import at.ainf.diagnosis.storage.FormulaSet;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created with IntelliJ IDEA.
 * User: chicco
 * Date: 26.11.12
 * Time: 15:10
 * To change this template use File | Settings | File Templates.
 */
public class SimpleSetReasoner<T> extends AbstractReasoner<T> {

    private Set<T> model = new HashSet<T>();
    private final Set<FormulaSet<T>> conflicts;



    public SimpleSetReasoner(Set<FormulaSet<T>> conflicts){
        this.conflicts = conflicts;

    }

    @Override
    public boolean isConsistent() {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isCoherent() {
        return isConsistent();
    }

    @Override
    public Set<T> getEntailments() {
        return this.model;
    }

    @Override
    public boolean isEntailed(Set<T> test) {
        return this.model.containsAll(test);
    }

    @Override
    public SimpleSetReasoner<T> newInstance() {
        throw new IllegalStateException("This reasoner can not give a new instance");
    }

    @Override
    protected void updateReasonerModel(Set<T> axiomsToAdd, Set<T> axiomsToRemove) {
        model.addAll(axiomsToAdd);
        model.removeAll(axiomsToAdd);
    }
}
