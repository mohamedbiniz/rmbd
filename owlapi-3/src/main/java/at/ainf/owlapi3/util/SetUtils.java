package at.ainf.owlapi3.util;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 16.05.13
 * Time: 12:54
 * To change this template use File | Settings | File Templates.
 */
public class SetUtils {

    public static <X> Set<X> createIntersection (Collection<Set<X>> collection) {
        if (collection.isEmpty())
            return Collections.emptySet();
        final Iterator<Set<X>> iterator = collection.iterator();
        Set<X> intersection = new LinkedHashSet<X>(iterator.next());
        while (iterator.hasNext())
            intersection.retainAll(iterator.next());
        return intersection;
    }

    public static <X> Set<X> createUnion (Collection<Set<X>> collection) {
        Set<X> union = new LinkedHashSet<X>();
        for (Set<X> set : collection)
            union.addAll(set);
        return union;
    }

}
