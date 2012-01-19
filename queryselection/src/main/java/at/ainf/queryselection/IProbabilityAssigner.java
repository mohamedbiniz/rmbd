package at.ainf.queryselection;

import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 03.03.11
 * Time: 11:34
 * To change this template use File | Settings | File Templates.
 */
public interface IProbabilityAssigner {

    public LinkedList<QueryModuleDiagnosis> assignProbabilities(LinkedList<QueryModuleDiagnosis> diags);

    public QueryModuleDiagnosis getTargetDiag();

}
