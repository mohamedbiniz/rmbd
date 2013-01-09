package at.ainf.diagnosis.tree;

import at.ainf.diagnosis.storage.FormulaSet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: chicco
 * Date: 27.11.12
 * Time: 13:43
 * To change this template use File | Settings | File Templates.
 */
public interface Node<Id> extends Comparable<Node<Id>>{

    boolean addChild(HSTreeNode<Id> node);

    boolean removeChild(Node<Id> node);



    void removeChildren();

    Set<Node<Id>> getChildren();

    ArrayList<Node<Id>> expandNode();

    ArrayList<Node<Id>> expandNode(boolean bool);

    Set<Id> getPathLabels();

    boolean isClosed();

    void setClosed();

    boolean isRoot();

    void removeParent();

    HSTreeNode<Id> getParent();

    Id getArcLabel();

    Set<Set<Id>> getAxiomSets();

    Set<Id> getAxiomSet();

    void setAxiomSet(Set<Set<Id>> conflict);

    void setAxiomSet(LinkedHashSet<Id> conflict);

    int getLevel();

    void removeAxioms();

    void setOpen();

    BigDecimal getNodePathCosts();

    //void setNodePathCosts(BigDecimal nodePathCosts);

    CostsEstimator<Id> getCostsEstimator();

    void setCostsEstimator(CostsEstimator<Id> costsEstimator);
}