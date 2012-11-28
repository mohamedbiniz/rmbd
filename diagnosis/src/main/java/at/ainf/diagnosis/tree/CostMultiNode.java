package at.ainf.diagnosis.tree;

import at.ainf.diagnosis.storage.AxiomSet;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: chicco
 * Date: 22.11.12
 * Time: 15:05
 * To change this template use File | Settings | File Templates.
 */
public class CostMultiNode<Id> extends MultiNode<Id> implements Node<Id> {

    public CostMultiNode(Set<AxiomSet<Id>> conflict) {
        super(conflict);
    }

    @Override
    public ArrayList<Node<Id>> expandNode() {
        ArrayList<Node<Id>> newNodes = super.expandNode();

        for (Node<Id> node : newNodes) {

            node.setCostsEstimator(getCostsEstimator());
        }
        return newNodes;
    }

    @Override
    public int compareTo(Node<Id> o) {
        return super.compareTo(o);
    }
}
