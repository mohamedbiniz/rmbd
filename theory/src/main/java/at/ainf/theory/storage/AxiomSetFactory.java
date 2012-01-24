package at.ainf.theory.storage;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 19.01.12
 * Time: 15:51
 * To change this template use File | Settings | File Templates.
 */
public class AxiomSetFactory {
    private static int cnt = 0;

    public static <Id> AxiomSet<Id> createAxiomSet(double measure, Set<Id> hittingSet, Set<Id> entailments) {
        cnt++;
        return new AxiomSetImpl<Id>("axiomSet_" + cnt, measure, hittingSet, entailments);
    }
}
