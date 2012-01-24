package at.ainf.theory.watchedset;

import java.util.Collection;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 19.01.12
 * Time: 12:18
 * To change this template use File | Settings | File Templates.
 */
public class WatchedTreeSet<E extends WatchedElement<X>,X> extends TreeSet<E> {

    private MeasureUpdatedListener<X> listener;

    public WatchedTreeSet() {
         listener = new MeasureUpdatedListener<X>() {
             public void notifiyMeasureUpdated(WatchedElement<X> updatedSet, X value ) {
                 updateElement((E)updatedSet,value);
             }

         };
    }

    @Override
    public boolean add(E e) {
        boolean added = super.add(e);
        if (added) {
            e.addMeasureUpdatedListener(listener);
        }
        return added;    //To change body of overridden methods use File | Settings | File Templates.
    }

    private void updateElement(E e, X value) {
        super.remove(e);
        e.setWatchedElementMeasure(value);
        super.add(e);
    }

    @Override
    public boolean remove(Object o) {
        boolean r = super.remove(o);    //To change body of overridden methods use File | Settings | File Templates.
        if (r) {
            ((E)o).removeMeasureUpdatedListener(listener);
        }
        return r;
    }

    @Override
    public void clear() {
        for (E elem : this)
            elem.removeMeasureUpdatedListener(listener);
        super.clear();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean res = super.addAll(c);
        if (res) {
            for (E elem : c)
                elem.addMeasureUpdatedListener(listener);
        }
        return res;
    }

    @Override
    public E pollFirst() {
        E el = super.pollFirst();
        if (el != null)
            el.removeMeasureUpdatedListener(listener);
        return el;
    }

    @Override
    public E pollLast() {
        E el = super.pollLast();
        if (el != null)
            el.removeMeasureUpdatedListener(listener);
        return el;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        for (Object elem : c)
            ((E)elem).removeMeasureUpdatedListener(listener);
        return super.removeAll(c);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public boolean retainAll(Collection<?> c) {
	    for(E elem : this) {
            if (!c.contains(elem))
                elem.removeMeasureUpdatedListener(listener);
	    }
        return super.retainAll(c);    //To change body of overridden methods use File | Settings | File Templates.

    }
}
