package at.ainf.theory.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 19.01.12
 * Time: 15:51
 * To change this template use File | Settings | File Templates.
 */
public class AxiomSetFactory {

    private static Map<AxiomSet.TypeOfSet,Integer> map = new HashMap<AxiomSet.TypeOfSet, Integer>();

    private static int getCnt(AxiomSet.TypeOfSet typeOfSet) {
        if (!map.containsKey(typeOfSet))
            map.put(typeOfSet,0);
        map.put(typeOfSet,map.get(typeOfSet)+1);
        return map.get(typeOfSet);
    }

    public static <Id> AxiomSet<Id> createAxiomSet(AxiomSet.TypeOfSet typeOfSet, double measure, Set<Id> hittingSet, Set<Id> entailments) {
        String name = typeOfSet.toString() +     getCnt(typeOfSet);
        return new AxiomSetImpl<Id>(typeOfSet, name, measure, hittingSet, entailments);
    }
}
