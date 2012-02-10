package at.ainf.queryselection;

import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 04.02.11
 * Time: 17:57
 * To change this template use File | Settings | File Templates.
 */
public interface IDiagGenerator {

    public LinkedList<QueryModuleDiagnosis> getDiags(int numOfDiags) throws NoDiagnosisFoundException;

}
