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
    protected Node<Id> parent;

    protected final Set<Node<Id>> children = new LinkedHashSet<Node<Id>>();

    // ARCLABEL: if node is the root arcLabel = -1
    protected Id arcLabel;

    // CONFLICT: if the node is not calculated or closed conflict = null
    protected Set<Set<Id>> conflict = null;

    protected final boolean root;




    // constructor for root

    //NEU
    public Node(LinkedHashSet<Set<Id>> conflict) {
        this.parent = null;
        // arcLabel = -1
        this.arcLabel = null;
        this.conflict = conflict;
        this.root = true;
    }

    public Node(Set<Id> conflict) {
        this.parent = null;
        // arcLabel = -1
        this.arcLabel = null;

        LinkedHashSet<Set<Id>> set = new LinkedHashSet<Set<Id>>();
        set.add(conflict);

        this.conflict = set;
        this.root = true;
    }

    public Node(Node<Id> parent, Id arcLabel) {
        parent.addChild(this);
        this.arcLabel = arcLabel;
        this.root = false;
        //this.conflict = NOT_CALCULATED;
    }

    public boolean addChild(Node<Id> node) {
        node.parent = this;
        return this.children.add(node);
    }

    public boolean removeChild(Node<Id> node) {
        node.removeParent();
        return this.children.remove(node);
    }

    public void removeChildren() {
        for (Node<Id> child : children) {
            child.removeParent();
        }
        this.children.clear();
    }

    public Set<Node<Id>> getChildren() {
        return Collections.unmodifiableSet(children);
    }

    public ArrayList<Node<Id>> expandNode() {
        ArrayList<Node<Id>> newNodes = new ArrayList<Node<Id>>();

        //NEU
        for (Id arcLabel : conflict.iterator().next()) {
            if (!hasChild(getChildren(), arcLabel)) {
                Node<Id> node = new Node<Id>(this, arcLabel);
                newNodes.add(node);
            }
        }
        return newNodes;
    }

    public ArrayList<Node<Id>> expandNode(boolean bool) {
        ArrayList<Node<Id>> newNodes = new ArrayList<Node<Id>>();

        //NEU
        for (Id arcLabel : conflict.iterator().next()) {
            if (!hasChild(getChildren(), arcLabel)) {
                Node<Id> node = new Node<Id>(this, arcLabel);
                newNodes.add(node);
            }
        }
        return newNodes;
    }


    protected boolean hasChild(Set<Node<Id>> children, Id arcLabel) {
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

    protected boolean closed = false;

    public boolean isClosed() {
        return this.closed;
    }

    public void setClosed() {
        this.closed = true;
    }

    public boolean isRoot() {
        return this.root;
    }

    public void removeParent(){
        this.parent = null;
    }

    public Node<Id> getParent() {
        return parent;
    }

    public Id getArcLabel() {
        return arcLabel;
    }

    public Set<Set<Id>> getAxiomSet() {

        return conflict;
    }

    public void setAxiomSet(LinkedHashSet<Set<Id>> conflict) {
        /*
        for (Node<Id> child : children) {
            child.setClosed();
        }
        */
        //children.clear();

            this.conflict = conflict;

    }

    public void setAxiomSet(Set<Id> conflict) {

        Set<Set<Id>> set = new LinkedHashSet<Set<Id>>();
        set.add(conflict);
        this.conflict=set;
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

    public void setOpen() {
        this.closed = false;
    }

    protected Set<Id> copy(Set<Id> set) {
        Set<Id> hs = new LinkedHashSet<Id>();
        for (Id hset : set)
            hs.add(hset);
        return hs;
    }

    protected Set<Set<Id>> copy2(Set<Set<Id>> set) {
        Set<Set<Id>> hs = new LinkedHashSet<Set<Id>>();
        for (Set<Id> hset : set)
            hs.add(copy(hset));
        return hs;
    }

}
