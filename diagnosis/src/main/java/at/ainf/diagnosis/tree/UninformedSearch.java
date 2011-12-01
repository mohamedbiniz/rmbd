package at.ainf.diagnosis.tree;

import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.HittingSet;
import at.ainf.theory.storage.HittingSetImpl;
import at.ainf.theory.storage.Storage;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 21.04.11
 * Time: 14:36
 * To change this template use File | Settings | File Templates.
 */
public abstract class UninformedSearch<Id> extends AbstractTreeSearch<HittingSet<Id>, Set<Id>, Id>
        implements TreeSearch<HittingSet<Id>, Set<Id>, Id> {

    private int hscount = 0;

    private final LinkedList<Node<Id>> openNodes = new LinkedList<Node<Id>>();


    public UninformedSearch(Storage<HittingSet<Id>, Set<Id>, Id> storage) {
        super(storage);
    }

    @Override
    protected Set<Id> createConflictSet(Set<Id> quickConflict) {
        return quickConflict;
    }

    @Override
    protected HittingSet<Id> createHittingSet(Node<Id> node, boolean valid) throws SolverException {
        Set<Id> labels = node.getPathLabels();
        Set<Id> entailments = Collections.emptySet();
        if (valid) entailments = getTheory().getEntailments(labels);
        double measure = 1d / labels.size();
        HittingSetImpl<Id> hs = new HittingSetImpl<Id>("HS" + this.hscount, measure, labels, entailments);
        hs.setNode(node);
        this.hscount++;
        return hs;
    }

    protected Collection<Node<Id>> getOpenNodes() {
        return openNodes;
    }

    public Node<Id> popOpenNodes() {
        return this.openNodes.pop();
    }

    public void pushOpenNodes(Node<Id> node) {
        this.openNodes.push(node);
    }

    @Override
    protected void finalizeSearch() {
        // nothing to do here
    }
}
