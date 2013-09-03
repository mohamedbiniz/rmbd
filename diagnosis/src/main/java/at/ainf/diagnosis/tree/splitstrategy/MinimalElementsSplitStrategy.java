package at.ainf.diagnosis.tree.splitstrategy;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: chicco
 * Date: 09.01.13
 * Time: 13:54
 * To change this template use File | Settings | File Templates.
 */
public class MinimalElementsSplitStrategy<Id> implements SplitStrategy<Id> {


    /**
     * Re
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

        return  result;

    }

    /**
     * Counts the number of occurrences of an element in a set conflicts
     * @param element
     * @param conflicts
     * @return
     */
    private int count(Id element, Set<Set<Id>> conflicts){


        Set<Id> sum=new LinkedHashSet<Id>();


        for(Set<Id> ax:conflicts){
            if(ax.contains(element))  {
                for(Id id:ax){
                    if(!sum.contains(id))
                        sum.add(id);
                }
            }
              //  sum.addAll(ax);
        }

        return sum.size();


    }


}
