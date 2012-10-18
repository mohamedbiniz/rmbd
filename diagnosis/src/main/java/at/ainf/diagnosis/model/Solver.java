package at.ainf.diagnosis.model;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 18.10.12
 * Time: 09:39
 * To change this template use File | Settings | File Templates.
 */
public interface Solver<T> {

    public void updateModell(Set<T> formulas);

    public boolean isConsistent();

}
