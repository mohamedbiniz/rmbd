package at.ainf.diagnosis.storage;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 21.04.11
 * Time: 09:31
 * To change this template use File | Settings | File Templates.
 */
public interface HittingSet<Id> extends Set<Id>, Comparable<HittingSet<Id>> {

    public <T extends HittingSet<Id>> void setListener(StorageListener<T, Id> listener);

    boolean isValid();

    void setValid(boolean valid);

    void setMeasure(double value);

    double getMeasure();

    Set<Id> getEntailments();

    void setEntailments(Set<Id> entailments);

    String getName();

    void restoreEntailments();
}
