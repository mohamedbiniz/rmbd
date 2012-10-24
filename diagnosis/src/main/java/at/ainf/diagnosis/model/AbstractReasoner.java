package at.ainf.diagnosis.model;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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

    public boolean addFormularsToCache(Collection<T> formulas) {
        if (formulas == null)
            return false;

        this.reasonedFormulars.addAll(formulas);
        return true;
    }

    public boolean isCoherent() {

        throw new RuntimeException("This theory does not support coherency checks");
    }

    public boolean isEntailed(Set<T> test) {

        throw new RuntimeException("This theory does not support the verification of entailments");
    }

    public Set<T> getEntailments() {

        throw new RuntimeException("This theory does not support the calculation of entailments");
    }

    public void removeFormularsFromCache(Collection<T> formulas) {
        this.reasonedFormulars.removeAll(formulas);
    }

    public Set<T> getFormularCache() {
        return Collections.unmodifiableSet(this.reasonedFormulars);
    }

    public void clearFormularCache() {
        reasonedFormulars.clear();
    }

}
