package at.ainf.diagnosis.tree.searchstrategy;

import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.*;

import java.math.BigDecimal;
import java.util.*;

import static at.ainf.diagnosis.tree.Rounding.PRECISION;
import static at.ainf.diagnosis.tree.Rounding.ROUNDING_MODE;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.06.12
 * Time: 15:41
 * To change this template use File | Settings | File Templates.
 */
public class UniformCostSearchStrategy<Id> implements SearchStrategy<Id> {

    private PriorityQueue<Node<Id>> opensNodes = new PriorityQueue<Node<Id>>();



    public Node<Id> createRootNode(LinkedHashSet<Id> conflict, CostsEstimator<Id> costsEstimator, Collection<Id> act) {
        Node<Id> node = new HSTreeNode<Id>(conflict);
        node.setCostsEstimator(costsEstimator);
        return node;
    }

    public void expand(Node<Id> node) {
        addNodes(node.expandNode());
    }

    public BigDecimal getConflictMeasure(FormulaSet<Id> conflict, CostsEstimator<Id> costsEstimator) {
        return costsEstimator.getFormulaSetCosts(conflict);
    }

    public BigDecimal getDiagnosisMeasure(Node<Id> node) {
        return node.getNodePathCosts();
    }

    public void finalizeSearch(TreeSearch<FormulaSet<Id>, Id> search) {
        search.getSearchable().doBayesUpdate(search.getDiagnoses());
        normalizeValidHittingSets(search);
    }

    protected void normalizeValidHittingSets(TreeSearch<FormulaSet<Id>, Id> search) {
        Set<FormulaSet<Id>> hittingSets = search.getDiagnoses();
        BigDecimal sum = new BigDecimal("0");

        for (FormulaSet<Id> hittingSet : hittingSets) {
            sum = sum.add(hittingSet.getMeasure());
        }

        if (sum.compareTo(BigDecimal.ZERO)==0 && hittingSets.size() != 0)
            throw new IllegalStateException("Sum of probabilities of all diagnoses is 0!");

        for (FormulaSet<Id> hittingSet : hittingSets) {
            // the decimal expansion is inf we need round
            hittingSet.setMeasure(hittingSet.getMeasure().divide(sum,PRECISION, ROUNDING_MODE));
        }
    }

    public Node<Id> getNode() {
        // gets the first open node of the List
        return popOpenNodes();
    }

    public void addNodes(List<Node<Id>> nodeList) {

        for (Node<Id> node : nodeList) {
            pushOpenNode(node);
        }
        //Collections.sort(getOpenNodes(), new NodeComparator());
    }

    public Collection<Node<Id>> getOpenNodes() {
        return this.opensNodes;
    }

    public Node<Id> popOpenNodes() {
        return this.opensNodes.poll();
    }

    public void pushOpenNode(Node<Id> idNode) {
        this.opensNodes.add(idNode);
    }

}