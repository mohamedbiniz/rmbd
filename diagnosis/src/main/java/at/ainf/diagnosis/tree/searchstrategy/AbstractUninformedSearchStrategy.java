package at.ainf.diagnosis.tree.searchstrategy;

import at.ainf.diagnosis.tree.CostsEstimator;
import at.ainf.diagnosis.tree.HSTreeNode;
import at.ainf.diagnosis.tree.Node;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.storage.FormulaSet;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.06.12
 * Time: 15:52
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractUninformedSearchStrategy<Id> implements SearchStrategy<Id> {

    private final LinkedList<Node<Id>> openNodes = new LinkedList<Node<Id>>();

    public HSTreeNode<Id> createRootNode(FormulaSet<Id> conflict, CostsEstimator<Id> costsEstimator, Collection<Id> act) {
        return new HSTreeNode<Id>(conflict);
    }



    public BigDecimal getConflictMeasure(FormulaSet<Id> conflict, CostsEstimator<Id> costsEstimator) {
        return BigDecimal.valueOf(1d / conflict.size());
    }

    public BigDecimal getDiagnosisMeasure(Node<Id> node) {
        return BigDecimal.valueOf(1d / node.getPathLabels().size());
    }

    public Collection<Node<Id>> getOpenNodes() {
        return openNodes;
    }

    public Node<Id> popOpenNodes() {
        return this.openNodes.pop();
    }

    public void pushOpenNode(Node<Id> node) {
        this.openNodes.push(node);
    }

    public void finalizeSearch(TreeSearch<FormulaSet<Id>, Id> search) {
        // nothing to do here
    }

}
