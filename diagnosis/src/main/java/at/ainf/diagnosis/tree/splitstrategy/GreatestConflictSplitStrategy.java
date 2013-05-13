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

        return getGreatestConflict(conflicts).iterator().next();

    }

    private Set<Id> getGreatestConflict(Set<Set<Id>> conflicts){

        Set<Id> result= new LinkedHashSet<Id>();
        int max=0;

        for(Set<Id> c:conflicts){
              if(c.size()>max){
                  result=c;
                  max=c.size();
              }

        }
         return result;

    }
}
