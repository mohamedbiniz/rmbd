package at.ainf.diagnosis.tree;

import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 07.04.11
 * Time: 14:28
 * To change this template use File | Settings | File Templates.
 */
public class UniformCostSearch<Id> extends AbstractTreeSearch<AxiomSet<Id>, Id> {

    //private int count = 0;

    //private PriorityQueue<Node<Id>> opensNodes = new PriorityQueue<Node<Id>>();

    public UniformCostSearch(SimpleStorage<Id> storage) {
        super(storage);
        setLogic(new HsTreeLogic<AxiomSet<Id>, Id>());
        setSearchStrategy(new UniformCostSearchStrategy<Id>());
    }

    /*public void createRoot() throws NoConflictException,
            SolverException, InconsistentTheoryException {
        // if there is already a root
        if (getRoot() != null) return;
        Set<Id> conflict = calculateConflict(null);


        CostNode<Id> node = new CostNode<Id>(conflict);
        node.setCostsEstimator(getCostsEstimator());
        //double probability = 1.0;

        //for (Id axiom : getTheory().getActiveFormulas()) {
        //    probability *= (1 - getNodeCostsEstimator().getAxiomCosts(axiom));
        //}

        node.setNodePathCosts(node.getRootNodeCosts(getTheory().getActiveFormulas()));
        setRoot(node);
    }*/

    // moved
    /*protected Node<Id> createRootNode(Set<Id> conflict, CostsEstimator<Id> costsEstimator, Collection<Id> act) {
        CostNode<Id> node = new CostNode<Id>(conflict);
        node.setCostsEstimator(costsEstimator);
        node.setNodePathCosts(node.getRootNodeCosts(act));
        return node;
    }*/

    // moved
    /*@Override
    public void expand(Node<Id> node) {
        addNodes(node.expandNode());
    }*/

    /*@Override
    protected AxiomSet<Id> createConflictSet(Node<Id> node, Set<Id> quickConflict) throws SolverException {
        Set<Id> entailments = Collections.emptySet();
        if (getTheory().supportEntailments() && getSearcher().isDual()) entailments = getTheory().getEntailments(quickConflict);
        if (entailments==null) entailments = Collections.emptySet();
        double measure =  getConflictMeasure(quickConflict);
        AbstrAxiomSet<Id> hs = (AbstrAxiomSet<Id>) AxiomSetFactory.createConflictSet(measure, quickConflict, entailments );
        hs.setNode(node);
        return hs;
    }*/

    // moved
    /*protected double getConflictMeasure(Set<Id> conflict, CostsEstimator<Id> costsEstimator) {
        return costsEstimator.getAxiomSetCosts(conflict);
    }*/

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

    //moved
    /*protected double getDiagnosisMeasure(Node<Id> node) {
        return ((CostNode<Id>) node).getNodePathCosts();
    }*/

    // moved
    /*@Override
    protected void finalizeSearch(TreeSearch<AxiomSet<Id>, Id> search) {
        search.getTheory().doBayesUpdate(search.getStorage().getDiagnoses());
        search.getStorage().normalizeValidHittingSets();
    }*/

    // moved
    /*@Override
    public Node<Id> getNode() {
        // gets the first open node of the List
        return popOpenNodes();
    }*/

    // moved
    /*public void addNodes(List<Node<Id>> nodeList) {

        for (Node<Id> node : nodeList) {
            pushOpenNode(node);
        }
        //Collections.sort(getOpenNodes(), new NodeComparator());
    }*/

    //moved
    /*@Override
    public Collection<Node<Id>> getOpenNodes() {
        return this.opensNodes;
    }*/

    //moved
    /*
    @Override
    public Node<Id> popOpenNodes() {
        for (OpenNodesListener l : oNodesLsteners)
            l.updateOpenNodesRemoved();
        return this.opensNodes.poll();
    }*/

    //moved
    /*
    @Override
    public void pushOpenNode(Node<Id> idNode) {
        for (OpenNodesListener l : oNodesLsteners)
            l.updateOpenNodesAdded();
        this.opensNodes.add(idNode);
    }*/


}
