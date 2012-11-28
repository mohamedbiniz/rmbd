package at.ainf.diagnosis.tree;

import at.ainf.diagnosis.storage.AxiomSet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 13.06.11
 * Time: 17:05
 * To change this template use File | Settings | File Templates.
 */
public class CostSimpleNode<T> extends SimpleNode<T> implements Node<T> {

    public CostSimpleNode(SimpleNode<T> parent, T arcLabel) {
        super(parent, arcLabel);
    }

    public CostSimpleNode(AxiomSet<T> conflict) {
        super(conflict);
    }

    public CostSimpleNode(Set<AxiomSet<T>> conflict) {
        super(conflict);
    }


    public ArrayList<Node<T>> expandNode() {
        ArrayList<Node<T>> newNodes = new ArrayList<Node<T>>();
        //EDITED
        for (T arcLabel : getAxiomSets().iterator().next()) {
            CostSimpleNode<T> node = new CostSimpleNode<T>(this, arcLabel);
            newNodes.add(node);
            Node<T> parent = node.getParent();
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

    public int compareTo(Node<T> o) {
        if (this == o || this.equals(o))
            return 0;
        if (getNodePathCosts().compareTo(o.getNodePathCosts()) == 0)
            return Integer.valueOf(getPathLabels().size()).compareTo(o.getPathLabels().size());
        return -1 * getNodePathCosts().compareTo(o.getNodePathCosts());
    }
}
