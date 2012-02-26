/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.diagnosis.tree;


import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: student99
 * Date: 03.08.2009
 * Time: 14:04:13
 * To change this template use File | Settings | File Templates.
 */
public class Node<Id> {
    // one node has exactly 3 parameters

    // NODE_CLOSED: is a constant for the conflict if node is closed
    //public final Set<Id> NODE_CLOSED = null;

    //public final Set<Id> NOT_CALCULATED = null;
    // ARC_OF_ROOT: is a constant for the arcLabel if node is root
    //private final Id ARC_OF_ROOT = null;

    // PARENT: if node is the root parent = null
    private Node<Id> parent;

    private final Set<Node<Id>> children = new LinkedHashSet<Node<Id>>();

    // ARCLABEL: if node is the root arcLabel = -1
    private Id arcLabel;

    // CONFLICT: if the node is not calculated or closed conflict = null
    private Set<Id> conflict = null;

    public Node(Node<Id> parent, Id arcLabel) {
        parent.addChild(this);
        this.arcLabel = arcLabel;
        //this.conflict = NOT_CALCULATED;
    }

    public boolean addChild(Node<Id> node) {
        node.parent = this;
        return this.children.add(node);
    }

    public boolean removeChild(Node<Id> node) {
        return this.children.remove(node);
    }

    public Set<Node<Id>> getChildren() {
        return Collections.unmodifiableSet(children);
    }

    // constructor for root
    public Node(Set<Id> conflict) {
        this.parent = null;
        // arcLabel = -1
        this.arcLabel = null;
        this.conflict = conflict;
    }

    public ArrayList<Node<Id>> expandNode() {
        ArrayList<Node<Id>> newNodes = new ArrayList<Node<Id>>();
        for (Id arcLabel : this.conflict) {
            if (!hasChild(getChildren(), arcLabel)) {
                Node<Id> node = new Node<Id>(this, arcLabel);
                newNodes.add(node);
            }
        }
        return newNodes;
    }

    private boolean hasChild(Set<Node<Id>> children, Id arcLabel) {
        for (Node<Id> child : children) {
            if (child.getArcLabel().equals(arcLabel))
                return true;
        }
        return false;
    }

    // setter and getter

    public Set<Id> getPathLabels() {
        Set<Id> pathLabels = new TreeSet<Id>();
        Node<Id> node = this;
        // steps from this node to root and adds the arcLables of each node
        while (node.parent != null) {
            if (node.getArcLabel() != null)
                pathLabels.add(node.getArcLabel());
            node = node.getParent();
        }
        return pathLabels;
    }

    public boolean isClosed() {
        return conflict == null;
    }

    public void setClosed() {
        this.conflict = null;
    }

    public boolean isRoot() {
        return this.parent == null;
    }

    public Node<Id> getParent() {
        return parent;
    }

    public Id getArcLabel() {
        return arcLabel;
    }

    public Set<Id> getAxiomSet() {
        return conflict;
    }

    public void setConflict(Set<Id> conflict) {
        for (Node<Id> child : children) {
            child.setClosed();
        }

        children.clear();
        this.conflict = conflict;
    }

    public int getLevel() {
        int level = 0;
        Node<Id> node = this;
        while (node.parent != null) {
            level++;
            node = node.getParent();
        }
        return level;
    }

    public void removeArcLabel() {
        this.arcLabel = null;
    }

    public void removeAxioms() {
        this.conflict = null;
    }
}
