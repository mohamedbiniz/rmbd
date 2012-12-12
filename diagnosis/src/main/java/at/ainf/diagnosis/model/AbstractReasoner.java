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

    protected final Set<T> formulasCache = new HashSet<T>();

    protected Set<T> reasonerFormulas = new LinkedHashSet<T>();

    private Set<T> backgroundFormulas = null;

    public Set<T> getBackgroundFormulas() {
        return backgroundFormulas;
    }

    public void setBackgroundFormulas(Set<T> backgroundFormulas) {
        this.backgroundFormulas = backgroundFormulas;
    }

    public boolean addFormulasToCache(Collection<T> formulas) {
        if (formulas != null){
            setSync(false);
            return this.formulasCache.addAll(formulas);
        }
        return false;
    }

    public boolean removeFormulasFromCache(Collection<T> formulas) {
        setSync(false);
        return this.formulasCache.removeAll(formulas);
    }

    public void clearFormulasCache() {
        setSync(false);
        if (backgroundFormulas != null)
            this.formulasCache.retainAll(this.backgroundFormulas);
        else
            formulasCache.clear();
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
        if (isSync())
            return;
        Set<T> axiomsToAdd = new HashSet<T>();
        Set<T> axiomsToRemove = new HashSet<T>();

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

        if (!axiomsToAdd.isEmpty() || !axiomsToRemove.isEmpty())
            updateReasonerModel(axiomsToAdd, axiomsToRemove);
        setSync(true);
    }

    private boolean sync = false;

    private boolean isSync(){
        return this.sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
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
