package at.ainf.theory;

import at.ainf.theory.model.SolverException;

import java.util.Collection;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 13.07.11
 * Time: 15:03
 * To change this template use File | Settings | File Templates.
 */
public interface Searchable<E> {

    public boolean isConsistent() throws SolverException;

    boolean push(Collection<E> u) throws SolverException;

    boolean push(E u) throws SolverException;

    void pop();

    Set<E> getFormulaStack();

    void pop(int k);
}
