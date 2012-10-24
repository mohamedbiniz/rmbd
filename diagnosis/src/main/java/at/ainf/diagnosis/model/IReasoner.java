package at.ainf.diagnosis.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 18.10.12
 * Time: 09:39
 * To change this template use File | Settings | File Templates.
 */
public interface IReasoner<T> {

    public boolean addFormularsToCache(Collection<T> formulas);

    public void removeFormularsFromCache(Collection<T> formulas);

    public void clearFormularCache();

    public Set<T> getFormularCache();


    public boolean isConsistent();

    public boolean isCoherent();

    public Set<T> getEntailments();

    public boolean isEntailed(Set<T> test);

}
