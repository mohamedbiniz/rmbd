package at.ainf.diagnosis.tree.splitstrategy;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: chicco
 * Date: 19.12.12
 * Time: 22:48
 * To change this template use File | Settings | File Templates.
 */
public class GreatestConflictSplitStrategy<Id> implements SplitStrategy<Id> {


    @Override
    public Id getSplitElement(Set<Set<Id>> conflicts) {

        Set<Id> minConflict= getGreatestConflict(conflicts);

       /* int maxCount=0;
        Id res = null;

        for(Id id:minConflict){

            int tempCount=count(id,conflicts);

              if(tempCount>maxCount){
                      maxCount=tempCount;
                  res=id;
              }
        } */

       return minConflict.iterator().next();
    }

    private Set<Id> getGreatestConflict(Set<Set<Id>> conflicts){

        Set<Id> result= new LinkedHashSet<Id>();
        int min=Integer.MAX_VALUE;

        for(Set<Id> c:conflicts){
              if(c.size()<min){
                  result=c;
                  min=c.size();
              }

        }
         return result;

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
