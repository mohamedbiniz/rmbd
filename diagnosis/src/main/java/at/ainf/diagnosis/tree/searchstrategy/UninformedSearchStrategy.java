package at.ainf.diagnosis.tree.searchstrategy;

import at.ainf.diagnosis.tree.CostsEstimator;
import at.ainf.diagnosis.tree.Node;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.theory.storage.AxiomSet;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.06.12
 * Time: 15:52
 * To change this template use File | Settings | File Templates.
 */
public abstract class UninformedSearchStrategy<Id> implements SearchStrategy<Id> {

    private final LinkedList<Node<Id>> openNodes = new LinkedList<Node<Id>>();

    public Node<Id> createRootNode(Set<Id> conflict, CostsEstimator<Id> costsEstimator, Collection<Id> act) {
        return new Node<Id>(conflict);
    }

    public double getConflictMeasure(Set<Id> conflict, CostsEstimator<Id> costsEstimator) {
        return 1d / conflict.size();
    }

    public double getDiagnosisMeasure(Node<Id> node) {
        return 1d / node.getPathLabels().size();
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

    public void finalizeSearch(TreeSearch<AxiomSet<Id>, Id> search) {
        // nothing to do here
    }

}
