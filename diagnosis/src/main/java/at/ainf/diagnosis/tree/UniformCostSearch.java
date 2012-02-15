package at.ainf.diagnosis.tree;

import at.ainf.theory.model.SolverException;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.storage.AbstrAxiomSet;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.AxiomSetFactory;
import at.ainf.theory.storage.SimpleStorage;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 07.04.11
 * Time: 14:28
 * To change this template use File | Settings | File Templates.
 */
public class UniformCostSearch<Id> extends AbstractTreeSearch<AxiomSet<Id>, Id> {

    private int count = 0;

    private PriorityQueue<Node<Id>> opensNodes = new PriorityQueue<Node<Id>>();

    private NodeCostsEstimator<Id> nodeCostsEstimator;

    public NodeCostsEstimator<Id> getNodeCostsEstimator() {
        return nodeCostsEstimator;
    }

    public void setNodeCostsEstimator(NodeCostsEstimator<Id> nodeCostsEstimator) {
        this.nodeCostsEstimator = nodeCostsEstimator;
    }

    public UniformCostSearch(SimpleStorage<Id> storage) {
        super(storage);
    }

    public UniformCostSearch(SimpleStorage<Id> storage, NodeCostsEstimator<Id> estimator) {
        super(storage);
        this.nodeCostsEstimator = estimator;
    }

    public void createRoot() throws NoConflictException,
            SolverException, InconsistentTheoryException {
        // if there is already a root
        if (getRoot() != null) return;
        Set<Id> conflict = calculateConflict(null);


        CostNode<Id> node = new CostNode<Id>(conflict);
        node.setCostsEstimator(getNodeCostsEstimator());
        //double probability = 1.0;

        //for (Id axiom : getTheory().getActiveFormulas()) {
        //    probability *= (1 - getNodeCostsEstimator().getNodeCosts(axiom));
        //}

        node.setNodePathCosts(node.getRootNodeCosts(getTheory().getActiveFormulas()));
        setRoot(node);
    }

    @Override
    public void expand(Node<Id> node) {
        addNodes(node.expandNode());
    }

    @Override
    protected AxiomSet<Id> createConflictSet(Node<Id> node, Set<Id> quickConflict) throws SolverException {
        double probability = 1d / quickConflict.size();
        Set<Id> entailments = Collections.emptySet();
        if (getTheory().supportEntailments() && getSearcher().isDual()) entailments = getTheory().getEntailments(quickConflict);
        if (entailments==null)
            entailments = Collections.emptySet();
        AbstrAxiomSet<Id> result = (AbstrAxiomSet<Id>) AxiomSetFactory.createConflictSet(probability, quickConflict, entailments );
        result.setNode(node);
        return result;
        // return result;
    }

    @Override
    public void clearSearch() {
        count = 0;
        super.clearSearch();
    }

    @Override
    protected AxiomSet<Id> createHittingSet(Node<Id> node, boolean valid) throws SolverException {
        String name = "D_" + String.valueOf(++count);
        Set<Id> labels = node.getPathLabels();
        double probability = ((CostNode<Id>) node).getNodePathCosts();
        Set<Id> entailments = Collections.emptySet();
        if (getTheory().supportEntailments() && valid && !getSearcher().isDual())
            entailments = getTheory().getEntailments(labels);
        AbstrAxiomSet<Id> result = (AbstrAxiomSet<Id>) AxiomSetFactory.createHittingSet(probability, labels, entailments);
        result.setNode(node);
        return result;
    }

    @Override
    protected void finalizeSearch() {
        getTheory().doBayesUpdate(getStorage().getDiagnoses());
        getStorage().normalizeValidHittingSets();
    }

    @Override
    public Node<Id> getNode() {
        // gets the first open node of the List
        return popOpenNodes();
    }

    @Override
    public void addNodes(ArrayList<Node<Id>> nodeList) {

        for (Node<Id> node : nodeList) {
            pushOpenNodes(node);
        }
        //Collections.sort(getOpenNodes(), new NodeComparator());
    }

    @Override
    public Collection<Node<Id>> getOpenNodes() {
        return this.opensNodes;
    }

    @Override
    public Node<Id> popOpenNodes() {
        for (OpenNodesListener l : oNodesLsteners)
            l.updateOpenNodesRemoved();
        return this.opensNodes.poll();
    }

    @Override
    public void pushOpenNodes(Node<Id> idNode) {
        for (OpenNodesListener l : oNodesLsteners)
            l.updateOpenNodesAdded();
        this.opensNodes.add(idNode);
    }


}
