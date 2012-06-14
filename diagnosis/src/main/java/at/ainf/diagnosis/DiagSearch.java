package at.ainf.diagnosis;

import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.AxiomSet;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 14.06.12
 * Time: 10:06
 * To change this template use File | Settings | File Templates.
 */
public interface DiagSearch<T extends AxiomSet<Id>, Id> {

    public Set<T> run() throws SolverException, NoConflictException, InconsistentTheoryException;

    public Set<T> run(int numberOfDiags) throws SolverException, NoConflictException, InconsistentTheoryException;

    public void reset();

    public Set<T> getConflicts();

    public Set<T> getDiagnoses();

}
