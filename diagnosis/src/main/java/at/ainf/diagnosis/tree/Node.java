/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.diagnosis.tree;


import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.storage.AxiomSetFactory;
import at.ainf.diagnosis.storage.AxiomSetImpl;

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
    protected Set<AxiomSet<Id>> conflict = null;

    protected final boolean root;




    // constructor for root

    //NEU
    public Node(Set<AxiomSet<Id>> conflict) {
        this.parent = null;
        // arcLabel = -1
        this.arcLabel = null;

        this.root = true;
        this.conflict = conflict;
    }

    public Node(AxiomSet<Id> conflict) {
        this.parent = null;
        // arcLabel = -1
        this.arcLabel = null;

        this.root = true;

        Set<AxiomSet<Id>> set = new LinkedHashSet<AxiomSet<Id>>();
        set.add(conflict);
        this.conflict = set;
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

    public Set<AxiomSet<Id>> getAxiomSet() {

        return conflict;
    }

    public void setAxiomSet(Set<AxiomSet<Id>> conflict) {
        /*
        for (Node<Id> child : children) {
            child.setClosed();
        }
        */
        //children.clear();

            this.conflict = conflict;

    }

    public void setAxiomSet(AxiomSet<Id> conflict) {

        Set<AxiomSet<Id>> set = new LinkedHashSet<AxiomSet<Id>>();
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


    protected AxiomSet<Id> copy(AxiomSet<Id> set) {
        Set<Id> hs =  new LinkedHashSet<Id>(set);
        return new AxiomSetImpl<Id>(set.getMeasure(), hs, set.getEntailments());
    }

    protected AxiomSet<Id> removeElement(Id e, AxiomSet<Id> set) {
        Set<Id> hs =  new LinkedHashSet<Id>(set);
        hs.remove(e);
        return new AxiomSetImpl<Id>(set.getMeasure(), hs, set.getEntailments());
    }

    protected Set<AxiomSet<Id>> removeElement(Id e, Set<AxiomSet<Id>> set) {
        Set<AxiomSet<Id>> hs = new LinkedHashSet<AxiomSet<Id>>();
        for (AxiomSet<Id> hset : set) {
            hs.add(removeElement(e, hset));
            if(hset.size()<=0)
                hs.remove(hset);
        }
        return hs;
    }

    protected Set<AxiomSet<Id>> copy2(Set<AxiomSet<Id>> set) {
        Set<AxiomSet<Id>> hs = new LinkedHashSet<AxiomSet<Id>>();
        for (AxiomSet<Id> hset : set)
            hs.add(copy(hset));
        return hs;
    }


}
