package at.ainf.queryselection;

import at.ainf.theory.storage.AbstrAxiomSet;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.diagnosis.tree.Node;

import java.util.Collections;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 10.05.11
 * Time: 14:43
 * To change this template use File | Settings | File Templates.
 */
public class ProbabilisticHittingSet<Id> extends AbstrAxiomSet<Id> implements AxiomSet<Id>, QueryModuleDiagnosis<Id> {

    private double userAssignedProbability;
    private double actualProbability;
    private boolean isTarget;
    private boolean isInconsistent;
    private int timesInD_0 = 0;

    //private LinkedList<UpdateType> updates;

    /*
    public ProbabilisticHittingSet() {
        super();
    }
      */
    public ProbabilisticHittingSet(String name, double probability, Set<Id> hittingSet, Set<Id> entailments,Node<Id> node) {
        this(name, probability, false, hittingSet, entailments,node);
    }

    public ProbabilisticHittingSet(String name, double probability, boolean t, Set<Id> hittingSet, Set<Id> entailments,Node<Id> node) {

        super(TypeOfSet.HITTING_SET,name, probability, hittingSet, entailments);
        this.isTarget = t;
        //this.node = node;

        this.userAssignedProbability = probability;
        //this.updates = new LinkedList<UpdateType>();


    }

    public ProbabilisticHittingSet(String name, double probability, boolean t, Set<Id> hittingSet, Set<Id> entailments) {
        super(TypeOfSet.HITTING_SET,name, probability, hittingSet, entailments);
    }


    public ProbabilisticHittingSet(String name, double probability, Set<Id> hittingSet, Set<Id> entailments) {
        super(TypeOfSet.HITTING_SET,name, probability, hittingSet, entailments);

    }


    public ProbabilisticHittingSet(String name, double value) {
        super(TypeOfSet.HITTING_SET,name, value, Collections.<Id>emptySet(), Collections.<Id>emptySet());
    }

    public double getUserAssignedProbability() {
        return this.userAssignedProbability;
    }

    public void setUserAssignedProbability(double userAssignedProbability) {
        this.userAssignedProbability = userAssignedProbability;
    }

    public int getTimesInD_0() {
        return timesInD_0;
    }

    public void incTimesInD_0() {
        this.timesInD_0++;
    }

    public boolean isInconsistent() {
        return isInconsistent;
    }

    public void setInconsistent() {
        isInconsistent = true;
    }

    public void setConsistent() {
        isInconsistent = false;
    }

    public DiagnosisMemento saveToMemento() {
        return new DiagnosisMemento(getName(), getMeasure());
    }

    public void restoreFromMemento(DiagnosisMemento memento) {
        throw new RuntimeException("Why is this method here?");
        /*
        setName(memento.getSavedName());
        setMeasure(memento.getSavedProbability());
        */
    }


    public double getActualProbability() {
        return this.actualProbability;
    }

    public void setActualProbability(double p) {
        this.actualProbability = p;
    }

    public double getProbability() {
        return getMeasure();
    }

    public void setProbability(double p) {
        setMeasure(p);
    }


    public boolean isTarget() {
        return this.isTarget;
    }

    public void setIsTarget(boolean b) {
        this.isTarget = b;
    }

}
