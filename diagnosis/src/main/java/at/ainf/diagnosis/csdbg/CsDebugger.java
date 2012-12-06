package at.ainf.diagnosis.csdbg;

import at.ainf.diagnosis.AbstractDebugger;
import at.ainf.diagnosis.Debugger;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 05.12.12
 * Time: 12:02
 * To change this template use File | Settings | File Templates.
 */
public class CsDebugger<T extends FormulaSet<Id>, Id> extends AbstractDebugger<T, Id> {

    private int maxD;

    @Override
    public void setMaxDiagnosesNumber(int number) {
        maxD = number;
    }

    @Override
    public int getMaxDiagnosesNumber() {
        return maxD;
    }

    @Override
    public Set<T> start() throws SolverException, NoConflictException, InconsistentTheoryException {
        return null;  //
    }

    @Override
    public void reset() {

    }

    @Override
    public Set<T> getConflicts() {
        return null;
    }

    @Override
    public Set<T> getDiagnoses() {
        return null;
    }

}
