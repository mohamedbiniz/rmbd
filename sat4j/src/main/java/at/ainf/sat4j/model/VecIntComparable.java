package at.ainf.sat4j.model;

import org.sat4j.core.VecInt;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 20.01.12
 * Time: 17:49
 * To change this template use File | Settings | File Templates.
 */
public class VecIntComparable implements IVecInt, IVecIntComparable {

    VecInt vector;

    public VecIntComparable(int[] vector) {
        this.vector = new VecInt(vector);
    }

    public VecIntComparable() {
        super();
    }

    public int compareTo(IVecIntComparable o) {
        if (size() < o.size())
            return -1;
        else if (size() > o.size())
            return 1;
        else {
            for (int j = 0; j < size(); j++) {
                if (get(j) < o.get(j))
                    return -1;
                else if (get(j) > o.get(j))
                    return 1;
            }
            return 0;

        }
    }

    @Override
    public int size() {
        return vector.size();
    }

    @Override
    public void shrink(int nofelems) {
        vector.shrink(nofelems);
    }

    @Override
    public void shrinkTo(int newsize) {
        vector.shrinkTo(newsize);
    }

    @Override
    public IVecInt pop() {
        return vector.pop();
    }

    @Override
    public void growTo(int newsize, int pad) {
        vector.growTo(newsize, pad);
    }

    @Override
    public void ensure(int nsize) {
        vector.ensure(nsize);
    }

    @Override
    public IVecInt push(int elem) {
        return vector.push(elem);
    }

    @Override
    public void unsafePush(int elem) {
        vector.unsafePush(elem);
    }

    @Override
    public void clear() {
        vector.clear();
    }

    @Override
    public int last() {
        return vector.last();
    }

    @Override
    public int get(int i) {
        return vector.get(i);
    }

    @Override
    public int unsafeGet(int i) {
        return vector.unsafeGet(i);
    }

    @Override
    public void set(int i, int o) {
        vector.set(i, o);
    }

    @Override
    public boolean contains(int e) {
        return vector.contains(e);
    }

    @Override
    public int indexOf(int e) {
        return vector.indexOf(e);
    }

    @Override
    public int containsAt(int e) {
        return vector.containsAt(e);
    }

    @Override
    public int containsAt(int e, int from) {
        return vector.containsAt(e, from);
    }

    @Override
    public void copyTo(IVecInt copy) {
        vector.copyTo(copy);
    }

    @Override
    public void copyTo(int[] is) {
        vector.copyTo(is);
    }

    @Override
    public void moveTo(IVecInt dest) {
        vector.moveTo(dest);
    }

    @Override
    public void moveTo2(IVecInt dest) {
        vector.moveTo2(dest);
    }

    @Override
    public void moveTo(int dest, int source) {
        vector.moveTo(dest, source);
    }

    @Override
    public void moveTo(int[] dest) {
        vector.moveTo(dest);
    }

    @Override
    public void moveTo(int sourceStartingIndex, int[] dest) {
        vector.moveTo(sourceStartingIndex, dest);
    }

    @Override
    public void insertFirst(int elem) {
        vector.insertFirst(elem);
    }

    @Override
    public void remove(int elem) {
        vector.remove(elem);
    }

    @Override
    public int delete(int i) {
        return vector.delete(i);
    }

    @Override
    public String toString() {
        return vector.toString();
    }

    @Override
    public void sort() {
        vector.sort();
    }

    @Override
    public void sortUnique() {
        vector.sortUnique();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof VecIntComparable) {
            for (int j = 0; j < size(); j++) {
                if (!vector.contains(((VecIntComparable) obj).get(j)))
                    return false;
            }
            return true;
        }
        return vector.equals(obj);
    }

    @Override
    public int hashCode() {
        return vector.hashCode();
    }

    public void pushAll(IVecInt vec) {
        vector.pushAll(vec);
    }

    public boolean isSubsetOf(VecInt vec) {
        return vector.isSubsetOf(vec);
    }

    @Override
    public IteratorInt iterator() {
        return vector.iterator();
    }

    @Override
    public boolean isEmpty() {
        return vector.isEmpty();
    }

    @Override
    public int[] toArray() {
        return vector.toArray();
    }

    @Override
    public IVecInt[] subset(int cardinal) {
        return vector.subset(cardinal);
    }
}
