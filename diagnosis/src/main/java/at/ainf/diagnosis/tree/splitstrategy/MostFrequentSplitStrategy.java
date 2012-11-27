package at.ainf.diagnosis.tree.splitstrategy;

import at.ainf.diagnosis.storage.AxiomSet;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: chicco
 * Date: 27.11.12
 * Time: 12:42
 * To change this template use File | Settings | File Templates.
 */
public class MostFrequentSplitStrategy<Id> implements SplitStrategy<Id> {

    /**
     * Returns the element that occurrs most often in the set of conflicts
     * @param conflicts
     * @return
     */
    public Id getSplitElement(Set<AxiomSet<Id>> conflicts) {

        //Result element
        Id result=null;
        //Number of occurrences of result element
        int maxCount=0;

        for(AxiomSet<Id> c: conflicts){

            for(Id el : c){
                   if(count(el,conflicts)>maxCount){
                       result=el;
                       maxCount=count(el,conflicts);
                   }
            }

        }

        return  result;

    }

    /**
     * Counts the number of occurrences of an element in a set conflicts
     * @param element
     * @param conflicts
     * @return
     */
    private int count(Id element, Set<AxiomSet<Id>> conflicts){

        int cnt=0;

        for(AxiomSet ax:conflicts){
           if(ax.contains(element))
               cnt++;
        }

        return cnt;


    }
}

