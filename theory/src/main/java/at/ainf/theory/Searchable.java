package at.ainf.theory;

import at.ainf.theory.model.InconsistentTheoryException;
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

    public boolean verifyRequirements() throws SolverException;

    public boolean verifyConsistency() throws SolverException;

    boolean push(Collection<E> u) throws SolverException;

    boolean push(E u) throws SolverException;

    void pop();

    Set<E> getFormulaStack();

    void pop(int k);

    public void addBackgroundFormulas(Set<E> formulas) throws InconsistentTheoryException, SolverException;

    public void removeBackgroundFormulas(Set<E> formulas) throws InconsistentTheoryException, SolverException;

    public Set<E> getBackgroundFormulas();

}
