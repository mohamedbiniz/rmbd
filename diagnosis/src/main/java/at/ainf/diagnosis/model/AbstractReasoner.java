package at.ainf.diagnosis.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 19.10.12
 * Time: 10:02
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractReasoner<T> implements IReasoner<T> {

    private final LinkedHashSet<T> reasonedFormulars = new LinkedHashSet<T>();

    public boolean addReasonedFormulars(Collection<T> formulas) {
        if (formulas == null)
            return false;

        this.reasonedFormulars.addAll(formulas);
        return true;
    }

    public void removeReasonedFormulars(Collection<T> formulas) {
        this.reasonedFormulars.removeAll(formulas);
    }

    public void cleanReasonedFormulars() {
        reasonedFormulars.clear();
    }


    public Set<T> getReasonendFormulars() {
        return Collections.unmodifiableSet(this.reasonedFormulars);
    }

    @Override
    public void sync() {
        // implement method
    }

}
