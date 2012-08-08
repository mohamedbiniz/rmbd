package at.ainf.owlapi3.utils;

import at.ainf.diagnosis.storage.AxiomSet;
import org.semanticweb.owlapi.model.*;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 07.08.12
 * Time: 11:21
 * To change this template use File | Settings | File Templates.
 */
public class StatisticsTools {


    public static <X> Set<X> getIntersection (Set<X> axioms1, Set<X> axioms2) {
        Set<X> intersection = new LinkedHashSet<X>();
        intersection.addAll(axioms1);
        intersection.retainAll(axioms2);

        return intersection;
    }

    public static <X> int minCard(Set<? extends Set<X>> s) {
        int r = -1;

        try {
            for (Set<X> set : s)
                if (r == -1 || set.size() < r)
                    r = set.size();
        } catch (NoSuchElementException e) {

        }

        return r;
    }

    public static <X> int maxCard(Set<? extends Set<X>> s) {
        int r = -1;

        try {
            for (Set<X> set : s)
                if (set.size() > r)
                    r = set.size();
        } catch (NoSuchElementException e) {

        }

        return r;
    }

    public static <X> double meanCard(Set<? extends Set<X>> s) {
        double sum = 0;
        int cnt = 0;

        for (Set<X> set : s) {
            sum += set.size();
            cnt++;
        }

        if (cnt == 0) return -1;
        return sum / cnt;
    }
}
