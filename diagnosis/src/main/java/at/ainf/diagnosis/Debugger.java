package at.ainf.diagnosis;

import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.AxiomSet;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 14.06.12
 * Time: 10:06
 * To change this template use File | Settings | File Templates.
 */
public interface Debugger<T extends AxiomSet<Id>, Id> {

    public static int ALL_DIAGNOSES = -1;

    public void setMaxDiagnosesNumber(int number);

    public int getMaxDiagnosesNumber();

    public Set<T> start() throws SolverException, NoConflictException, InconsistentTheoryException;

    public Set<T> resume() throws SolverException, NoConflictException, InconsistentTheoryException;

    public void reset();

    public Set<T> getConflicts();

    public Set<T> getDiagnoses();

}
