package at.ainf.theory.storage;


import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 13.06.11
 * Time: 17:27
 * To change this template use File | Settings | File Templates.
 */
public interface NodeCostsEstimator<Id> {

    double getNodeSetCosts(Set<Id> labelSet);

    double getNodeCosts(Id label);


}
