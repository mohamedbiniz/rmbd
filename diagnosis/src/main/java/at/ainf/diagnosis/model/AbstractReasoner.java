package at.ainf.diagnosis.model;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 19.10.12
 * Time: 10:02
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractReasoner<T> implements IReasoner<T> {

    private final LinkedHashSet<T> reasonedFormularsCache = new LinkedHashSet<T>();

    protected Set<T> reasonedFormulars = new LinkedHashSet<T>();


    public boolean addFormularsToCache(Collection<T> formulas) {
        if (formulas == null)
            return false;

        this.reasonedFormularsCache.addAll(formulas);
        return true;
    }

    public void removeFormularsFromCache(Collection<T> formulas) {
        this.reasonedFormularsCache.removeAll(formulas);
    }

    public void clearFormularCache() {
        reasonedFormularsCache.clear();
    }

    public Set<T> getFormularCache() {
        return Collections.unmodifiableSet(this.reasonedFormularsCache);
    }


    public void setReasonedFormulars(Set<T> reasonedFormulars) {
        this.reasonedFormulars = reasonedFormulars;
    }

    public Set<T> getReasonedFormulars() {
        return Collections.unmodifiableSet(reasonedFormulars);
    }


    protected abstract void updateReasonerModel(Set<T> axiomsToAdd, Set<T> axiomsToRemove);

    public void sync() {
        Set<T> axiomsToAdd = new HashSet<T>();
        Set<T> axiomsToRemove = new HashSet<T>();

        for (T axiom : getFormularCache()) {
            if (!getReasonedFormulars().contains(axiom))
                axiomsToAdd.add(axiom);
        }

        for (T axiom : getReasonedFormulars()) {
            if (!getFormularCache().contains(axiom))
                axiomsToRemove.add(axiom);
        }

        reasonedFormulars.clear();
        reasonedFormulars.addAll(getFormularCache());

        if (!axiomsToAdd.isEmpty() || !axiomsToRemove.isEmpty())
            updateReasonerModel(axiomsToAdd,axiomsToRemove);

    }


    public boolean isEntailed(Set<T> test) {

        throw new RuntimeException("This theory does not support the verification of entailments");
    }

    public Set<T> getEntailments() {

        throw new RuntimeException("This theory does not support the calculation of entailments");
    }

    public boolean isCoherent() {

        throw new RuntimeException("This theory does not support coherency checks");
    }

}
