package at.ainf.diagnosis.tree;

import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.AxiomSet;
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
public abstract class UninformedSearch<Id> extends AbstractTreeSearch<AxiomSet<Id>, Set<Id>, Id>
        implements TreeSearch<AxiomSet<Id>, Set<Id>, Id> {

    private int hscount = 0;

    private final LinkedList<Node<Id>> openNodes = new LinkedList<Node<Id>>();


    public UninformedSearch(Storage<AxiomSet<Id>, Set<Id>, Id> storage) {
        super(storage);
    }

    @Override
    protected Set<Id> createConflictSet(Set<Id> quickConflict) {
        return quickConflict;
    }

    @Override
    protected AxiomSet<Id> createHittingSet(Node<Id> node, boolean valid) throws SolverException {
        Set<Id> labels = node.getPathLabels();
        Set<Id> entailments = Collections.emptySet();
        if (getTheory().supportEntailments() && valid) entailments = getTheory().getEntailments(labels);
        double measure = 1d / labels.size();
        AxiomSetImpl<Id> hs = new AxiomSetImpl<Id>("HS" + this.hscount, measure, labels, entailments);
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
