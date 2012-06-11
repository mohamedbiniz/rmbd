/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.diagnosis.quickxplain;

import java.util.*;

class FormulaList<Id> implements Collection<Id> {

    private class Itr implements Iterator<Id> {
        private int cursor = lower;

        public boolean hasNext() {
            return cursor <= upper;
        }

        public Id next() {
            return formulas.get(cursor++);

        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private final List<Id> formulas;
    private final int upper;
    private final int lower;

    protected FormulaList(Collection<Id> formulas) {
        this.formulas = Collections.unmodifiableList(new ArrayList<Id>(formulas));
        this.lower = 0;
        this.upper = formulas.size() - 1;
    }

    private FormulaList(List<Id> formulas, int lower, int upper) {
        this.formulas = formulas;
        this.lower = lower;
        this.upper = upper;
    }

    public FormulaList<Id> setBounds(int lower, int upper) {
        if (lower < 0 || lower >= size() || lower > upper)
            throw new IllegalArgumentException("Trying to assign an incorrect lower bound!");
        if (upper < 0 || upper > size() || upper < lower)
            throw new IllegalArgumentException("Trying to assign an incorrect upper bound!");
        if (upper - lower > this.upper - this.lower)
            throw new IllegalArgumentException("Trying to assign incorrect bounds!");
        return new FormulaList<Id>(this.formulas, this.lower + lower, this.lower + upper);
    }

    public int size() {
        return (this.formulas.size() != 0) ? this.upper - this.lower + 1 : 0;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean contains(Object o) {
        int ind = this.formulas.indexOf(o);
        return ind >= lower && ind <= upper;
    }

    public Iterator<Id> iterator() {
        return new Itr();
    }

    public Id get(int index) {
        index += this.lower;
        if (!(index >= lower && index <= upper))
            throw new IndexOutOfBoundsException();
        return this.formulas.get(index);
    }

    public Object[] toArray() {
        Object[] arr = new Object[size()];
        for (int i = lower; i <= upper; i++)
            arr[i - lower] = this.formulas.get(i);
        return arr;
    }

    public <T> T[] toArray(T[] a) {
        if (a.length < size())
            throw new IllegalArgumentException("Given array is smaller then the list!");
        for (int i = lower; i <= upper; i++)
            a[i] = (T) this.formulas.get(i);
        return a;
    }

    public boolean containsAll(Collection<?> c) {
        for (Object el : c)
            if (!contains(el))
                return false;
        return true;
    }

    public boolean add(Id f) {
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(Collection<? extends Id> c) {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public Id getFirstElement() {
        return formulas.get(lower);
    }

    public Id getLastElement() {
        return formulas.get(upper);
    }
}
