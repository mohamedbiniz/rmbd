package at.ainf.diagnosis.tree;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: chicco
 * Date: 27.11.12
 * Time: 14:11
 * To change this template use File | Settings | File Templates.
 */
public interface CostNode<T> extends Node<T>, Comparable<CostNode<T>> {
    String getName();

    BigDecimal getNodePathCosts();

    void setNodePathCosts(BigDecimal nodePathCosts);

    BigDecimal getRootNodeCosts(Collection<T> activeFormulars);

    CostsEstimator<T> getCostsEstimator();

    void setCostsEstimator(CostsEstimator<T> costsEstimator);

    int getPathLabelSize();
}
