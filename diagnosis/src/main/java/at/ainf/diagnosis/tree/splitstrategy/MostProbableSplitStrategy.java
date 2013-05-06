package at.ainf.diagnosis.tree.splitstrategy;

import at.ainf.diagnosis.tree.AbstractCostEstimator;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: chicco
 * Date: 11.01.13
 * Time: 14:31
 * To change this template use File | Settings | File Templates.
 */
public class MostProbableSplitStrategy<Id> implements SplitStrategy<Id> {


    AbstractCostEstimator<Id> costsEstimator;

    public Id getSplitElement(Set<Set<Id>> conflicts) {




        //Result element
        Id result=null;

        //Number of occurrences of result element
        BigDecimal maxCost= new BigDecimal("0");

        for(Set<Id> c: conflicts){

            for(Id el : c){

                if(costsEstimator.getFormulaCosts(el).compareTo(maxCost)>0){
                    result=el;
                    maxCost=costsEstimator.getFormulaCosts(el);
                }
            }

        }

        return  result;

    }

    public void setCostsEstimator(AbstractCostEstimator<Id> ce){
        this.costsEstimator=ce;
    }
}
