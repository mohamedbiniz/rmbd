package at.ainf.diagnosis.model;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 10.10.12
 * Time: 16:05
 * To change this template use File | Settings | File Templates.
 */
public class ReasonerKB<T> {

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






}
