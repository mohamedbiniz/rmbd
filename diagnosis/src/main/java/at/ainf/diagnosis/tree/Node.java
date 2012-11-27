package at.ainf.diagnosis.tree;

import at.ainf.diagnosis.storage.AxiomSet;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: chicco
 * Date: 27.11.12
 * Time: 13:43
 * To change this template use File | Settings | File Templates.
 */
public interface Node<Id> {
    boolean addChild(SimpleNode<Id> node);

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

    SimpleNode<Id> getParent();

    Id getArcLabel();

    Set<AxiomSet<Id>> getAxiomSets();

    AxiomSet<Id> getAxiomSet();

    void setAxiomSet(Set<AxiomSet<Id>> conflict);

    void setAxiomSet(AxiomSet<Id> conflict);

    int getLevel();

    void removeArcLabel();

    void removeAxioms();

    void setOpen();
}
