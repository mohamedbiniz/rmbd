package at.ainf.diagnosis.tree.splitstrategy;

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
    public Id getSplitElement(Set<Set<Id>> conflicts) {

        //Result element
        Id result=null;
        //Number of occurrences of result element
        int maxCount=0;

        for(Set<Id> c: conflicts){

            for(Id el : c){
                if(count(el,conflicts)>maxCount){
                    result=el;
                    maxCount=count(el,conflicts);
                }
            }

        }

        /**If most frequent Element occurs only one time, the problem is disjoint.
         We notify this by returning null as a result**/
        if(maxCount==1)
            return null;
        else
        return  result;

    }

    /**
     * Counts the number of occurrences of an element in a set of conflicts
     * @param element
     * @param conflicts
     * @return
     */
    private int count(Id element, Set<Set<Id>> conflicts){

        int cnt=0;

        for(Set ax:conflicts){
            if(ax.contains(element))
                cnt++;
        }

        return cnt;


    }
}

