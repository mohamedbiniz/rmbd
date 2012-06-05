package at.ainf.theory.storage;

import at.ainf.theory.watchedset.WatchedElement;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 21.04.11
 * Time: 09:31
 * To change this template use File | Settings | File Templates.
 */
public interface AxiomSet<Id> extends Set<Id>, Comparable<AxiomSet<Id>>, WatchedElement<Double> {

    void updateAxioms(Set<Id> axioms);

    Object getNode();

    public enum TypeOfSet { HITTING_SET, CONFLICT_SET, OTHER };

    public <T extends AxiomSet<Id>> void setListener(StorageListener<T, Id> listener);

    boolean isValid();

    void setValid(boolean valid);

    void setMeasure(double value);

    double getMeasure();

    Set<Id> getEntailments();

    void setEntailments(Set<Id> entailments);

    String getName();

    TypeOfSet getType();

    void restoreEntailments();


}
