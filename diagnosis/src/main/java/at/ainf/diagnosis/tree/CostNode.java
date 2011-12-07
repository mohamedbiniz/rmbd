package at.ainf.diagnosis.tree;

import at.ainf.theory.storage.NodeCostsEstimator;

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
public class CostNode<T> extends Node<T> implements Comparable<CostNode<T>> {

    private double nodePathCosts = 0;
    private NodeCostsEstimator<T> costsEstimator;

    private static int counter;

    private String name;

    public String getName() {
        return name;
    }

    public double getNodePathCosts() {
        return nodePathCosts;
    }

    public void setNodePathCosts(double nodePathCosts) {
        this.nodePathCosts = nodePathCosts;
    }

    public CostNode(Node<T> parent, T arcLabel) {
        super(parent, arcLabel);
        name = "CostNode" + counter++;

    }

    public CostNode(Set<T> conflict) {
        super(conflict);
        name = "CostNode" + counter++;

    }


    public ArrayList<Node<T>> expandNode() {
        ArrayList<Node<T>> newNodes = new ArrayList<Node<T>>();
        for (T arcLabel : getConflict()) {
            CostNode<T> node = new CostNode<T>(this, arcLabel);
            newNodes.add(node);
            CostNode<T> parent = (CostNode<T>) node.getParent();
            T axiom = node.getArcLabel();
            double fProb = getCostsEstimator().getNodeCosts(axiom);
            double nodePathCosts = (parent.getNodePathCosts() / (1 - fProb)) * fProb;
            if (nodePathCosts == 0)
                node.setNodePathCosts(Double.MIN_VALUE);
            else
                node.setNodePathCosts(nodePathCosts);
            node.setCostsEstimator(getCostsEstimator());
        }
        return newNodes;
    }

    public double getRootNodeCosts(Collection<T> activeFormulars) {
        double probability = 1.0;

        for (T axiom : activeFormulars) {
            double invCosts = 1 - getCostsEstimator().getNodeCosts(axiom);
            probability *= invCosts;
        }
        // in some cases double is not big enough...
        if (probability == 0)
            probability = Double.MIN_VALUE;
        return probability;

    }

    public NodeCostsEstimator<T> getCostsEstimator() {
        return costsEstimator;
    }

    public void setCostsEstimator(NodeCostsEstimator<T> costsEstimator) {
        this.costsEstimator = costsEstimator;
    }

    public int getPathLabelSize() {
        return getPathLabels().size();
    }

    public int compareTo(CostNode<T> o) {
        if (this == o || this.equals(o))
            return 0;
        if (getNodePathCosts() == o.getNodePathCosts())
            return Integer.valueOf(getPathLabelSize()).compareTo(o.getPathLabelSize());
        return -1 * Double.valueOf(getNodePathCosts()).compareTo(o.getNodePathCosts());
    }


    public String toString() {
        return (isRoot()) ? "Root" : getArcLabel().toString();
    }
}
