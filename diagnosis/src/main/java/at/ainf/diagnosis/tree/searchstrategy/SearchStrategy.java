package at.ainf.diagnosis.tree.searchstrategy;

import at.ainf.diagnosis.tree.CostsEstimator;
import at.ainf.diagnosis.tree.Node;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.storage.FormulaSet;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.06.12
 * Time: 14:07
 * To change this template use File | Settings | File Templates.
 */
public interface SearchStrategy<Id> {



    public void expand(Node<Id> node);

    public Node<Id> getNode();

    public void addNodes(List<Node<Id>> nodeList);

    public Node<Id> createRootNode(LinkedHashSet<Id> conflict, CostsEstimator<Id> costsEstimator, Collection<Id> act);

    public BigDecimal getConflictMeasure(FormulaSet<Id> conflict, CostsEstimator<Id> costsEstimator);

    public BigDecimal getDiagnosisMeasure(Node<Id> node);

    public Collection<Node<Id>> getOpenNodes();

    public Node<Id> popOpenNodes();

    public void pushOpenNode(Node<Id> node);

    public void finalizeSearch(TreeSearch<FormulaSet<Id>, Id> search);

}
