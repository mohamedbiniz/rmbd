package at.ainf.theory.storage;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 19.01.12
 * Time: 15:51
 * To change this template use File | Settings | File Templates.
 */
public class AxiomSetFactory {
    
    private static int hsCnt = 0;
    
    private static int csCnt = 0;


    public static <Id> AxiomSet<Id> createHittingSet(BigDecimal measure, Set<Id> hittingSet, Set<Id> entailments) {
        String name = AxiomSet.TypeOfSet.HITTING_SET.toString() + hsCnt++;
        return new AxiomSetImpl<Id>(AxiomSet.TypeOfSet.HITTING_SET, name, measure, hittingSet, entailments);
    }

    public static <Id> AxiomSet<Id> createConflictSet(BigDecimal measure, Set<Id> hittingSet, Set<Id> entailments) {
        String name = AxiomSet.TypeOfSet.CONFLICT_SET.toString() + csCnt++ ;
        return new AxiomSetImpl<Id>(AxiomSet.TypeOfSet.CONFLICT_SET, name, measure, hittingSet, entailments);
    }

}
