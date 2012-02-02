package at.ainf.diagnosis.tree;

import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.AxiomSetFactory;
import at.ainf.theory.storage.AxiomSetImpl;
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

    private int hscount = 0;

    private final LinkedList<Node<Id>> openNodes = new LinkedList<Node<Id>>();


    public UninformedSearch(Storage<AxiomSet<Id>, Id> storage) {
        super(storage);
    }

    @Override
    protected AxiomSet<Id> createConflictSet(Node<Id> node, Set<Id> quickConflict) {
        Set<Id> entailments = Collections.emptySet();
        double measure = 1d / quickConflict.size();
        AxiomSetImpl<Id> hs = (AxiomSetImpl<Id>) AxiomSetFactory.createAxiomSet(AxiomSet.TypeOfSet.CONFLICT_SET, measure, quickConflict, entailments);
        hs.setNode(node);
        return hs;
    }

    @Override
    protected AxiomSet<Id> createHittingSet(Node<Id> node, boolean valid) throws SolverException {
        Set<Id> labels = node.getPathLabels();
        Set<Id> entailments = Collections.emptySet();
        if (getTheory().supportEntailments() && valid) entailments = getTheory().getEntailments(labels);
        double measure = 1d / labels.size();
        AxiomSetImpl<Id> hs = (AxiomSetImpl<Id>) AxiomSetFactory.createAxiomSet(AxiomSet.TypeOfSet.HITTING_SET, measure, labels, entailments);
        hs.setNode(node);
        this.hscount++;
        return hs;
    }


    public Collection<Node<Id>> getOpenNodes() {
        return openNodes;
    }

    public Node<Id> popOpenNodes() {
        for (OpenNodesListener l : oNodesLsteners)
            l.updateOpenNodesRemoved();
        return this.openNodes.pop();
    }

    public void pushOpenNodes(Node<Id> node) {
        for (OpenNodesListener l : oNodesLsteners)
            l.updateOpenNodesAdded();
        this.openNodes.push(node);
    }

    @Override
    protected void finalizeSearch() {
        // nothing to do here
    }
}
