package at.ainf.queryselection.sim;

import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.StorageListener;
import at.ainf.queryselection.DiagnosisMemento;
import at.ainf.queryselection.QueryModuleDiagnosis;
import at.ainf.theory.watchedset.MeasureUpdatedListener;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 04.02.11
 * Time: 17:51
 * To change this template use File | Settings | File Templates.
 */

public class Diagnosis extends TreeSet<Object> implements QueryModuleDiagnosis<Object> {

    private String name;
    private double probability;
    private double userAssignedProbability;
    private double actualProbability;
    private boolean isTarget;
    private boolean isInconsistent;
    private int timesInD_0 = 0;
    //private LinkedList<UpdateType> updates;

    public Diagnosis() {

    }

    public Diagnosis(String n, double p, boolean t) {
        this.name = n;
        this.probability = p;
        this.isTarget = t;

        this.userAssignedProbability = p;
        //this.updates = new LinkedList<UpdateType>();


    }

    public double getUserAssignedProbability() {
        return this.userAssignedProbability;
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
        return new DiagnosisMemento(this.name, this.probability);
    }

    public void restoreFromMemento(DiagnosisMemento memento) {
        this.name = memento.getSavedName();
        this.probability = memento.getSavedProbability();
    }


    public double getActualProbability() {
        return this.actualProbability;
    }

    public void setActualProbability(double p) {
        this.actualProbability = p;
    }

    public double getProbability() {
        return this.probability;
    }

    public void setProbability(double p) {
        this.probability = p;
    }

    public <T extends AxiomSet<Object>> void setListener(StorageListener<T, Object> tObjectStorageListener) {
        throw new IllegalStateException("This class can be used for test purposes only and cannot be stored!");
    }

    public boolean isValid() {
        return false;
    }

    public void setValid(boolean valid) {
        throw new IllegalStateException("This implementation is for simulation purposes only!");
    }

    public void setMeasure(double value) {
        this.probability = value;
    }

    public double getMeasure() {
        return this.probability;
    }

    public Set<Object> getEntailments() {
        throw new IllegalStateException("This implementation is for simulation purposes only!");
    }

    public void setEntailments(Set<Object> entailments) {
        throw new IllegalStateException("This implementation is for simulation purposes only!");
    }

    public String getName() {
        return this.name;
    }

    public void restoreEntailments() {
        throw new IllegalStateException("This implementation is for simulation purposes only!");
    }

    public void setName(String s) {
        this.name = s;
    }

    public boolean isTarget() {
        return this.isTarget;
    }

    public void setIsTarget(boolean b) {
        this.isTarget = b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Diagnosis diagnosis = (Diagnosis) o;

        if (Double.compare(diagnosis.probability, probability) != 0) return false;
        if (name != null ? !name.equals(diagnosis.name) : diagnosis.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        temp = probability != +0.0d ? Double.doubleToLongBits(probability) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public int compareTo(AxiomSet o) {
        if (this == o) return 0;
        if (this.equals(o)) return 0;

        Diagnosis diagnosis = (Diagnosis) o;

        if (diagnosis.probability - probability > 0)
            return -1;
        return 1;
    }

    public void addMeasureUpdatedListener(MeasureUpdatedListener<Double> doubleMeasureUpdatedListener) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeMeasureUpdatedListener(MeasureUpdatedListener<Double> doubleMeasureUpdatedListener) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setWatchedElementMeasure(Double value) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}


