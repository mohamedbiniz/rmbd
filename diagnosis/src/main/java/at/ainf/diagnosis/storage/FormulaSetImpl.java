package at.ainf.diagnosis.storage;

import at.ainf.diagnosis.watchedset.MeasureUpdatedListener;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 18.04.11
 * Time: 11:39
 * To change this template use File | Settings | File Templates.
 */
public class FormulaSetImpl<Id> implements FormulaSet<Id>, Comparable<FormulaSet<Id>> {
    protected Set<Id> axioms;
    boolean valid = true;
    private BigDecimal measure = BigDecimal.ZERO;
    private final Set<Id> entailments;
    private Set<Id> tempEntailments = null;
    private Object node;

    private final String name;

    protected StorageListener listener;

    public void updateAxioms(Set<Id> axioms) {
        this.axioms = axioms;
    }

    public <T extends FormulaSet<Id>> void setListener(StorageListener<T, Id> listener) {
        this.listener = listener;
    }

    public Object getNode() {
        return node;
    }

    public void setNode(Object node) {
        this.node = node;
    }

    public void setEntailments(Set<Id> entailments) {
        this.tempEntailments = Collections.unmodifiableSet(entailments);
    }

    public void restoreEntailments() {
        this.tempEntailments = Collections.unmodifiableSet(this.entailments);
    }

    public String getName() {
        return name;
    }

    private static int axCnt = 0;

    public FormulaSetImpl(BigDecimal measure, Set<Id> axioms, Set<Id> entailments) {
        //this.typeOfSet = type;

        this.name = "FormulaSet_" + axCnt++;
        setMeasure(measure);
        this.axioms = Collections.unmodifiableSet(axioms);
        this.entailments = Collections.unmodifiableSet(entailments);
        setEntailments(entailments);
    }


    public boolean isValid() {
        return this.valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }


    //@SuppressWarnings("unchecked")
    public void setMeasure(BigDecimal value) {
        if (value.compareTo(BigDecimal.valueOf(0)) == 0) {
            throw new IllegalArgumentException("Probability of a hitting set is 0!");
        }
        // if (value.compareTo(BigDecimal.valueOf(Double.NaN)) == 0)
        //    throw new IllegalArgumentException("Trying to set measure to NaN!");
        if (measure.compareTo(value) == 0 || this.listener == null) {
            this.measure = value;
            for (MeasureUpdatedListener<BigDecimal> listener : measureUpdatedListenerList)
                listener.notifiyMeasureUpdated(this, value);
        } else {
            boolean addValid = this.listener.remove(this);
            for (MeasureUpdatedListener<BigDecimal> lsn : measureUpdatedListenerList)
                lsn.notifiyMeasureUpdated(this, value);
            this.measure = value;
            this.listener.add(this, addValid);
        }

    }


    public BigDecimal getMeasure() {
        return this.measure;
    }

    public Set<Id> getEntailments() {
        return this.tempEntailments;
    }

    public int size() {
        return axioms.size();
    }

    public boolean isEmpty() {
        return axioms.isEmpty();
    }

    public boolean contains(Object o) {
        return axioms.contains(o);
    }

    public Iterator<Id> iterator() {
        return this.axioms.iterator();
    }

    public Object[] toArray() {
        return axioms.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return axioms.toArray(a);
    }

    public boolean add(Id id) {
        return axioms.add(id);
    }

    public boolean remove(Object o) {
        return axioms.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return axioms.containsAll(c);
    }

    public boolean addAll(Collection<? extends Id> c) {
        return axioms.addAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return axioms.retainAll(c);
    }

    public boolean removeAll(Collection<?> c) {
        return axioms.removeAll(c);
    }

    public void clear() {
        axioms.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        if ( Set.class.isInstance(o)) {
            Set that = (Set) o;
            return axioms != null && axioms.equals(that);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return axioms.hashCode();
    }

    @Override
    public String toString() {
        String str = name + "{valid=" + valid +
                ", measure=" + measure;
        if (entailments != null)
            str += ", entailments=" + this.tempEntailments.size() + "/" + entailments.size();
        for (Id o : this) {
          str += o.toString();
        }
        str += '}';
        return str;
    }

    List<MeasureUpdatedListener<BigDecimal>> measureUpdatedListenerList = new LinkedList<MeasureUpdatedListener<BigDecimal>>();

    public void addMeasureUpdatedListener(MeasureUpdatedListener<BigDecimal> eDoubleMeasureUpdatedListener) {
        measureUpdatedListenerList.add(eDoubleMeasureUpdatedListener);
    }

    public void removeMeasureUpdatedListener(MeasureUpdatedListener<BigDecimal> eDoubleMeasureUpdatedListener) {
        measureUpdatedListenerList.remove(eDoubleMeasureUpdatedListener);
    }

    public void setWatchedElementMeasure(BigDecimal value) {
        measure = value;
    }

    public int compareTo(FormulaSet<Id> that) {
        if (this.equals(that))
            return 0;
        int res = getMeasure().compareTo(that.getMeasure());
        if (res == 0)
            if (-1 * getName().compareTo(that.getName()) >= 0)
                return 1;
            else
                return -1;
        return res;
    }

}
