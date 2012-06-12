package at.ainf.diagnosis.tree;

import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.AbstrAxiomSet;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.AxiomSetFactory;
import at.ainf.theory.storage.Storage;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 21.04.11
 * Time: 14:36
 * To change this template use File | Settings | File Templates.
 */
public abstract class UninformedSearch<Id> extends AbstractTreeSearch<AxiomSet<Id>, Id>
        implements TreeSearch<AxiomSet<Id>, Id> {

    //private int hscount = 0;

    private final LinkedList<Node<Id>> openNodes = new LinkedList<Node<Id>>();


    public UninformedSearch(Storage<AxiomSet<Id>, Id> storage) {
        super(storage);
        setCostsEstimator(new SimpleCostsEstimator<Id>());
    }

    /*@Override
    protected AxiomSet<Id> createConflictSet(Node<Id> node, Set<Id> quickConflict) throws SolverException {
        Set<Id> entailments = Collections.emptySet();
        if (getTheory().supportEntailments() && getSearcher().isDual()) entailments = getTheory().getEntailments(quickConflict);
        if (entailments==null) entailments = Collections.emptySet();
        double measure = getConflictMeasure(quickConflict);
        AbstrAxiomSet<Id> hs = (AbstrAxiomSet<Id>) AxiomSetFactory.createConflictSet(measure, quickConflict, entailments);
        hs.setNode(node);
        return hs;
    }*/

    protected Node<Id> createRootNode(Set<Id> conflict, CostsEstimator<Id> costsEstimator, Collection<Id> act) {
        return new Node<Id>(conflict);
    }

    protected double getConflictMeasure(Set<Id> conflict, CostsEstimator<Id> costsEstimator) {
        return 1d / conflict.size();
    }

    /*@Override
    protected AxiomSet<Id> createHittingSet(Node<Id> node, boolean valid) throws SolverException {
        Set<Id> labels = node.getPathLabels();
        Set<Id> entailments = Collections.emptySet();
        if (getTheory().supportEntailments() && valid && !getSearcher().isDual())
            entailments = getTheory().getEntailments(labels);
        double measure = getDiagnosisMeasure(node);
        AbstrAxiomSet<Id> hs = (AbstrAxiomSet<Id>) AxiomSetFactory.createHittingSet(measure, labels, entailments);
        hs.setNode(node);
        return hs;
    }*/

    protected double getDiagnosisMeasure(Node<Id> node) {
        return 1d / node.getPathLabels().size();
    }

    public Collection<Node<Id>> getOpenNodes() {
        return openNodes;
    }

    public Node<Id> popOpenNodes() {
        for (OpenNodesListener l : oNodesLsteners)
            l.updateOpenNodesRemoved();
        return this.openNodes.pop();
    }

    public void pushOpenNode(Node<Id> node) {
        for (OpenNodesListener l : oNodesLsteners)
            l.updateOpenNodesAdded();
        this.openNodes.push(node);
    }

    @Override
    protected void finalizeSearch(TreeSearch<AxiomSet<Id>, Id> search) {
        // nothing to do here
    }

}
