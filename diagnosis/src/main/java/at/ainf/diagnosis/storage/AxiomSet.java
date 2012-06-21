package at.ainf.diagnosis.storage;

import at.ainf.diagnosis.watchedset.WatchedElement;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 21.04.11
 * Time: 09:31
 * To change this template use File | Settings | File Templates.
 */
public interface AxiomSet<Id> extends Set<Id>, Comparable<AxiomSet<Id>>, WatchedElement<BigDecimal> {

    void updateAxioms(Set<Id> axioms);

    Object getNode();

    public enum TypeOfSet { HITTING_SET, CONFLICT_SET, OTHER };

    public <T extends AxiomSet<Id>> void setListener(StorageListener<T, Id> listener);

    boolean isValid();

    void setValid(boolean valid);

    void setMeasure(BigDecimal value);

    BigDecimal getMeasure();

    Set<Id> getEntailments();

    void setEntailments(Set<Id> entailments);

    String getName();

    void restoreEntailments();

    public void setNode(Object node);

}
