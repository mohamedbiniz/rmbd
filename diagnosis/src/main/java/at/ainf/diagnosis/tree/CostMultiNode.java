package at.ainf.diagnosis.tree;

import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.storage.AxiomSetFactory;
import at.ainf.diagnosis.tree.splitstrategy.SimpleSplitStrategy;
import at.ainf.diagnosis.tree.splitstrategy.SplitStrategy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: chicco
 * Date: 22.11.12
 * Time: 15:05
 * To change this template use File | Settings | File Templates.
 */
public class CostMultiNode<Id> extends MultiNode<Id> implements CostNode<Id>{

    private final CostSimpleNode<Id> cs;

    public CostMultiNode(Set<AxiomSet<Id>> conflict) {
        super(conflict);
        cs = new CostSimpleNode<Id>(conflict);
    }


    public ArrayList<Node<Id>> expandNode(){
        ArrayList<Node<Id>> newNodes= (ArrayList<Node<Id>>) super.expandNode();

        for(Node<Id> node:newNodes){
        cs.computeNodePathCosts((CostNode)node);
         ((CostNode)node).setCostsEstimator(getCostsEstimator());
        }
        return newNodes;
    }

    @Override
    public String getName() {
        return cs.getName();
    }

    @Override
    public BigDecimal getNodePathCosts() {
        return cs.getNodePathCosts();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setNodePathCosts(BigDecimal nodePathCosts) {
       cs.setNodePathCosts(nodePathCosts);
    }

    @Override
    public BigDecimal getRootNodeCosts(Collection<Id> activeFormulars) {
     return cs.getRootNodeCosts(activeFormulars);
    }

    @Override
    public CostsEstimator<Id> getCostsEstimator() {
       return cs.getCostsEstimator();
    }

    @Override
    public void setCostsEstimator(CostsEstimator<Id> costsEstimator) {
        cs.setCostsEstimator(costsEstimator);
    }

    @Override
    public int getPathLabelSize() {
       return cs.getPathLabelSize();
    }

    @Override
    public int compareTo(CostNode<Id> o) {
        return cs.compareTo(o);
    }
}
