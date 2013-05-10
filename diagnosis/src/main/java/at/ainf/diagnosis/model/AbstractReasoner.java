package at.ainf.diagnosis.model;

import java.util.*;
import java.util.concurrent.locks.Lock;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 19.10.12
 * Time: 10:02
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractReasoner<T> implements IReasoner<T> {

    protected final Set<T> formulasCache = new HashSet<T>();

    protected Set<T> reasonerFormulas = new LinkedHashSet<T>();
    protected Lock lock = null;

    private Set<T> backgroundAxioms = Collections.emptySet();

    private boolean locked = false;

    public void modificationsLock() {
        this.locked = true;
    }

    public void modificationsUnlock() {
        this.locked = false;
    }

    public Set<T> getBackgroundAxioms() {
        if (locked)
            return Collections.unmodifiableSet(backgroundAxioms);
        return backgroundAxioms;
    }

    public void setBackgroundAxioms(Set<T> backgroundAxioms) {
        if (locked)
            throw new UnsupportedOperationException("Background knowledge is locked!");
        this.backgroundAxioms = new LinkedHashSet<T>(backgroundAxioms);
        clearFormulasCache();
    }

    public boolean addFormulasToCache(Collection<T> formulas) {
        if (formulas != null) {
            return this.formulasCache.addAll(formulas);
        }
        return false;
    }

    public boolean removeFormulasFromCache(Collection<T> formulas) {
        return this.formulasCache.removeAll(formulas);
    }

    public void clearFormulasCache() {
        formulasCache.clear();
        if (locked)
            formulasCache.addAll(getBackgroundAxioms());
    }

    public Set<T> getFormulasCache() {
        return Collections.unmodifiableSet(this.formulasCache);
    }


    protected void setReasonerFormulas(Set<T> reasonerFormulas) {
        this.reasonerFormulas = reasonerFormulas;
    }

    protected Set<T> getReasonerFormulas() {
        return Collections.unmodifiableSet(reasonerFormulas);
    }


    protected abstract void updateReasonerModel(Set<T> axiomsToAdd, Set<T> axiomsToRemove);

    public void sync() {
        Set<T> axiomsToAdd = new HashSet<T>();
        Set<T> axiomsToRemove = new HashSet<T>();

        //axiomsToAdd.removeAll(axiomsToRemove);
        //axiomsToRemove.removeAll(getFormulasCache());
        //System.out.println(Thread.currentThread().getId() + " : " + toString());
        //synchronized (this){

        for (T axiom : getFormulasCache()) {
            if (!getReasonerFormulas().contains(axiom))
                axiomsToAdd.add(axiom);
        }
        for (T axiom : getReasonerFormulas()) {
            if (!getFormulasCache().contains(axiom))
                axiomsToRemove.add(axiom);
        }

        reasonerFormulas.clear();
        reasonerFormulas.addAll(getFormulasCache());
        //}
        if (!axiomsToAdd.isEmpty() || !axiomsToRemove.isEmpty())
            updateReasonerModel(axiomsToAdd, axiomsToRemove);
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

    public void setLock(Lock lock) {
        this.lock = lock;
    }
}
