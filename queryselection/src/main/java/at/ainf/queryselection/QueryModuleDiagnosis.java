package at.ainf.queryselection;

import at.ainf.theory.storage.AxiomSet;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 10.05.11
 * Time: 15:19
 * To change this template use File | Settings | File Templates.
 */
public interface QueryModuleDiagnosis<Id> extends AxiomSet<Id> {
    double getUserAssignedProbability();

    int getTimesInD_0();

    void incTimesInD_0();

    boolean isInconsistent();

    void setInconsistent();

    void setConsistent();

    DiagnosisMemento saveToMemento();

    void restoreFromMemento(DiagnosisMemento memento);

    double getActualProbability();

    void setActualProbability(double p);

    double getProbability();

    void setProbability(double p);

    boolean isTarget();

    void setIsTarget(boolean b);

    String getName();
}
