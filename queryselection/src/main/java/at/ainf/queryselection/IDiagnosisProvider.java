package at.ainf.queryselection;

import at.ainf.theory.model.SolverException;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 14.02.11
 * Time: 16:55
 * To change this template use File | Settings | File Templates.
 */
public interface IDiagnosisProvider<Id extends QueryModuleDiagnosis> {

    public LinkedList<Id> getDiagnoses(int numOfDiags);

    public List<Query> getAllQueries(List<Id> diags) throws SolverException;

    public void assignActualProbabilities();
}
