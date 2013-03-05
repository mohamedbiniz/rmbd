package at.ainf.owlapi3.test.modules;

import java.util.Comparator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 05.03.13
 * Time: 09:34
 * To change this template use File | Settings | File Templates.
 */
public class SetSizeComparator<T extends Set<?>> implements Comparator<T> {

    @Override
    public int compare(T o1, T o2) {
        return new Integer(o1.size()).compareTo(o2.size());
    }

}
