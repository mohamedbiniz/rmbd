package at.ainf.diagnosis.tree;

import at.ainf.diagnosis.storage.AxiomSet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 13.06.11
 * Time: 17:05
 * To change this template use File | Settings | File Templates.
 */
public class CostSimpleNode<T> extends SimpleNode<T> implements CostNode<T> {

    private BigDecimal nodePathCosts = BigDecimal.ZERO;
    private CostsEstimator<T> costsEstimator;

    private static int counter;

    private String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public BigDecimal getNodePathCosts() {
        return nodePathCosts;
    }

    @Override
    public void setNodePathCosts(BigDecimal nodePathCosts) {
        this.nodePathCosts = nodePathCosts;
    }

    public CostSimpleNode(SimpleNode<T> parent, T arcLabel) {
        super(parent, arcLabel);
        name = "CostSimpleNode" + counter++;

    }

    public CostSimpleNode(AxiomSet<T> conflict) {
        super(conflict);
        name = "CostSimpleNode" + counter++;

    }

    public CostSimpleNode(Set<AxiomSet<T>> conflict) {
        super(conflict);
        name = "CostSimpleNode" + counter++;

    }


    public ArrayList<Node<T>> expandNode() {
        ArrayList<Node<T>> newNodes = new ArrayList<Node<T>>();
        //EDITED
        for (T arcLabel : getAxiomSets().iterator().next()) {
            CostSimpleNode<T> node = new CostSimpleNode<T>(this, arcLabel);
            newNodes.add(node);
            CostNode<T> parent = (CostNode<T>) node.getParent();
            T axiom = node.getArcLabel();
            BigDecimal fProb = getCostsEstimator().getAxiomCosts(axiom);
            BigDecimal t = BigDecimal.ONE.subtract(fProb);
            t = parent.getNodePathCosts().divide(t);
            BigDecimal nodePathCosts = t.multiply(fProb);
            node.setNodePathCosts(nodePathCosts);
            node.setCostsEstimator(getCostsEstimator());
        }
        return newNodes;
    }

    @Override
    public BigDecimal getRootNodeCosts(Collection<T> activeFormulars) {
        BigDecimal probability = BigDecimal.ONE;

        for (T axiom : activeFormulars) {
            BigDecimal invCosts = BigDecimal.ONE.subtract(getCostsEstimator().getAxiomCosts(axiom));
            probability = probability.multiply(invCosts);
        }

        return probability;
    }

    @Override
    public CostsEstimator<T> getCostsEstimator() {
        return costsEstimator;
    }

    @Override
    public void setCostsEstimator(CostsEstimator<T> costsEstimator) {
        this.costsEstimator = costsEstimator;
    }

    @Override
    public int getPathLabelSize() {
        return getPathLabels().size();
    }

    public int compareTo(CostNode<T> o) {
        if (this == o || this.equals(o))
            return 0;
        if (getNodePathCosts().compareTo(o.getNodePathCosts()) == 0)
            return Integer.valueOf(getPathLabelSize()).compareTo(o.getPathLabelSize());
        return -1 * getNodePathCosts().compareTo(o.getNodePathCosts());
    }


    public String toString() {
        return (isRoot()) ? "Root" : getArcLabel().toString();
    }

    public void computeNodePathCosts(CostNode<T> node){
        CostNode<T> parent = (CostNode<T>) node.getParent();
        T axiom = node.getArcLabel();
        BigDecimal fProb = getCostsEstimator().getAxiomCosts(axiom);
        BigDecimal t = BigDecimal.ONE.subtract(fProb);
        t = parent.getNodePathCosts().divide(t);
        BigDecimal nodePathCosts = t.multiply(fProb);
        node.setNodePathCosts(nodePathCosts);
        node.setCostsEstimator(getCostsEstimator());
    }
}
