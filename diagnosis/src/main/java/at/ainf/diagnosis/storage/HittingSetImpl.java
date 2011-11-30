package at.ainf.diagnosis.storage;

import at.ainf.diagnosis.tree.Node;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 18.04.11
 * Time: 11:39
 * To change this template use File | Settings | File Templates.
 */
public class HittingSetImpl<Id> implements HittingSet<Id>, Comparable<HittingSet<Id>> {
    protected final Set<Id> hittingSet;
    boolean valid = false;
    private double measure = 0;
    private final Set<Id> entailments;
    private Set<Id> tempEntailments = null;
    private Node<Id> node;

    private final String name;
    protected StorageListener listener;

    public <T extends HittingSet<Id>> void setListener(StorageListener<T, Id> listener) {
        this.listener = listener;
    }

    public Node<Id> getNode() {
        return node;
    }

    public void setNode(Node<Id> node) {
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

    public HittingSetImpl(String name, double measure) {
        this(name, measure, Collections.<Id>emptySet(), Collections.<Id>emptySet());
    }

    public HittingSetImpl(String name, double measure, Set<Id> hittingSet, Set<Id> entailments) {
        this.name = name;
        setMeasure(measure);
        this.hittingSet = Collections.unmodifiableSet(hittingSet);
        this.entailments = Collections.unmodifiableSet(entailments);
        setEntailments(entailments);
    }

    public HittingSetImpl(String name, double measure, Set<Id> hittingSet, Set<Id> entailments, boolean valid) {
        this(name, measure, hittingSet, entailments);
        this.setValid(valid);
    }

    public boolean isValid() {
        return this.valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }


    @SuppressWarnings("unchecked")
    public void setMeasure(double value) {
        if (value == 0) {
            throw new IllegalArgumentException("Probability of a hitting set is 0!");
        }
        if (((Double) value).isNaN())
            throw new IllegalArgumentException("Trying to set measure to NaN!");
        if (this.measure == value || this.listener == null) {
            this.measure = value;
        } else {
            boolean addValid = this.listener.remove(this);
            this.measure = value;
            this.listener.add(this, addValid);
        }
    }

    public double getMeasure() {
        return this.measure;
    }

    public Set<Id> getEntailments() {
        return this.tempEntailments;
    }

    public int size() {
        return hittingSet.size();
    }

    public boolean isEmpty() {
        return hittingSet.isEmpty();
    }

    public boolean contains(Object o) {
        return hittingSet.contains(o);
    }

    public Iterator<Id> iterator() {
        return this.hittingSet.iterator();
    }

    public Object[] toArray() {
        return hittingSet.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return hittingSet.toArray(a);
    }

    public boolean add(Id id) {
        return hittingSet.add(id);
    }

    public boolean remove(Object o) {
        return hittingSet.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return hittingSet.containsAll(c);
    }

    public boolean addAll(Collection<? extends Id> c) {
        return hittingSet.addAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return hittingSet.retainAll(c);
    }

    public boolean removeAll(Collection<?> c) {
        return hittingSet.removeAll(c);
    }

    public void clear() {
        hittingSet.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (hittingSet == null)
            return false;
        HittingSetImpl that = (HittingSetImpl) o;
        return hittingSet.equals(that.hittingSet);
    }

    @Override
    public int hashCode() {
        return hittingSet.hashCode();
    }

    @Override
    public String toString() {
        String str = name + "{valid=" + valid +
                ", measure=" + measure;
        if (entailments != null)
            str += ", entailments=" + this.tempEntailments.size() + "/" + entailments.size();
        str += '}';
        return str;
    }

    public int compareTo(HittingSet<Id> that) {
        if (this.equals(that))
            return 0;
        int res = Double.valueOf(getMeasure()).compareTo(that.getMeasure());
        if (res == 0)
            if (-1 * getName().compareTo(that.getName()) >= 0)
                return 1;
            else
                return -1;
        return res;
    }
}
