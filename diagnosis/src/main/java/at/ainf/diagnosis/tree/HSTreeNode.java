/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.diagnosis.tree;


import java.math.BigDecimal;
import java.util.*;

import static at.ainf.diagnosis.tree.Rounding.PRECISION;
import static at.ainf.diagnosis.tree.Rounding.ROUNDING_MODE;

/**
 * Created by IntelliJ IDEA.
 * User: student99
 * Date: 03.08.2009
 * Time: 14:04:13
 * To change this template use File | Settings | File Templates.
 */
public class HSTreeNode<Id> implements Node<Id> {
    // one node has exactly 3 parameters

    // NODE_CLOSED: is a constant for the conflict if node is closed
    //public final Set<Id> NODE_CLOSED = null;

    //public final Set<Id> NOT_CALCULATED = null;
    // ARC_OF_ROOT: is a constant for the arcLabel if node is root
    //private final Id ARC_OF_ROOT = null;


    // PARENT: if node is the root parent = null
    protected HSTreeNode<Id> parent;

    //A node may have several Parents
   // protected Set<HSTreeNode<Id>> parents;

    protected final Set<Node<Id>> children = new LinkedHashSet<Node<Id>>();

    // ARCLABEL: if node is the root arcLabel = -1
    protected Id arcLabel;

   // protected Set<Id> arcLabels;


    // CONFLICT: if the node is not calculated or closed conflict = null
    protected Set<Set<Id>> conflict = null;

    protected final boolean root;

    private BigDecimal nodePathCosts = null;

    protected CostsEstimator<Id> costsEstimator;

    // constructor for root

    //NEU
    public HSTreeNode(Set<Set<Id>> conflict) {
        this.parent = null;
        // arcLabel = -1
        this.arcLabel = null;


        this.root = true;
        this.conflict = conflict;
    }

    public HSTreeNode(LinkedHashSet<Id> conflict) {
        this.parent = null;
        // arcLabel = -1
        this.arcLabel = null;

        this.root = true;

        Set<Set<Id>> set = new LinkedHashSet<Set<Id>>();
        set.add(conflict);
        this.conflict = set;
    }

    public HSTreeNode(Node<Id> parent, Id arcLabel) {
        parent.addChild(this);
        this.arcLabel = arcLabel;
        //Eventuell entfernen
       // this.arcLabels.add(arcLabel);

        this.root = false;
        //this.conflict = NOT_CALCULATED;
    }

    @Override
    public boolean addChild(HSTreeNode<Id> node) {
        node.parent = this;
       // node.parents.add(this);

        return this.children.add(node);
    }



    @Override
    public boolean removeChild(Node<Id> node) {

        if(node!=null)
            node.removeParent();
        return this.children.remove(node);
    }

    @Override
    public void removeChildren() {
        for (Node<Id> child : children) {
            child.removeParent();
        }
        this.children.clear();
    }

    @Override
    public Set<Node<Id>> getChildren() {
        return Collections.unmodifiableSet(children);
    }

    @Override
    public List<Node<Id>> expandNode() {
        ArrayList<Node<Id>> newNodes = new ArrayList<Node<Id>>();

        //NEU
        for (Id arcLabel : conflict.iterator().next()) {
            if (!hasChild(getChildren(), arcLabel)) {
                Node<Id> node = new HSTreeNode<Id>(this, arcLabel);
                node.setCostsEstimator(this.costsEstimator);
                newNodes.add(node);
            }
        }
        return newNodes;
    }

    @Override
    public ArrayList<Node<Id>> expandNode(boolean bool) {
        ArrayList<Node<Id>> newNodes = new ArrayList<Node<Id>>();

        //NEU
        for (Id arcLabel : conflict.iterator().next()) {
            if (!hasChild(getChildren(), arcLabel)) {
                Node<Id> node = new HSTreeNode<Id>(this, arcLabel);
                node.setCostsEstimator(getCostsEstimator());
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

    @Override
    public Set<Path<Id>> getPathLabels() {
        Set<Id> pathLabels = new TreeSet<Id>();
        HSTreeNode<Id> node = this;
        // steps from this node to root and adds the arcLables of each node
        while (node.parent != null) {
            if (node.getArcLabel() != null)
                pathLabels.add(node.getArcLabel());
            node = node.getParent();
        }

        //NEW to accommodate MultiParentGraph

        Set<Path<Id>> result= new LinkedHashSet<Path<Id>>();
            Path<Id> path= new Path<Id>();
        path.setPositivePath(pathLabels);
        result.add(path);
        return result;
        //return pathLabels;

    }

    /**
     * May be applied if a node can have multiple Parents. Returns the labels of
     * all paths from the current node to the root.
     * @return
     */
    /*
    @Override
    public Set<TreeSet<Id>> getPathLabels(){
        return getPathLabels(new TreeSet<Id>());
    }


    public Set<TreeSet<Id>> getPathLabels(TreeSet<Id> initial) {
       Set<TreeSet<Id>> result = new LinkedHashSet<TreeSet<Id>>();
        HSTreeNode<Id> node = this;
        // steps from this node to root and adds the arcLables of each node

        if(parents==null||parents.isEmpty())
            result.add(initial);
        else
        for (Node<Id> parent:node.parents) {

            TreeSet<Id> initial1= (TreeSet<Id>)copy((Set<Id>)initial);

            //Create a new path for each parent
            Set<Id> pathLabels = new TreeSet<Id>();

            if (node.getArcLabels().iterator().hasNext())
                initial1.add(node.getArcLabels().iterator().next());

            Set<TreeSet<Id>>tempResult=node.getPathLabels(initial1);
            for(TreeSet<Id> set:tempResult){
                result.add(set);
            }
            //node = node.getParent();
        }
        return result;
    }
    */

    protected boolean closed = false;

    protected Set<Id> copy(Set<Id> set) {
        Set<Id> cs =  new LinkedHashSet<Id>(set);

        return cs;
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public void setClosed() {
        this.closed = true;
    }

    @Override
    public boolean isRoot() {
        return this.root;
    }

    @Override
    public void removeParent() {
        this.parent = null;
    }

   /* @Override
    public HSTreeNode<Id> getParent() {
        return parent;
    } */

    @Override
    public HSTreeNode<Id> getParent() {
        return parent;
    }

    /**@Override
    public Id getArcLabel() {
        return arcLabel;
    }  **/

    @Override
    public Id getArcLabel() {
        return arcLabel;
    }


    /*public Set<Id> getArcLabels() {
        return arcLabels;
    } */

    @Override
    public Set<Set<Id>> getAxiomSets() {
        return conflict;
    }

    @Override
    public Set<Id> getAxiomSet() {
        return conflict.iterator().next();
    }

    @Override
    public void setAxiomSet(Set<Set<Id>> conflict) {
        /*
        for (SimpleNode<Id> child : children) {
            child.setClosed();
        }
        */
        //children.clear();

        this.conflict = conflict;

    }

    @Override
    public void setAxiomSet(LinkedHashSet<Id> conflict) {

        Set<Set<Id>> set = new LinkedHashSet<Set<Id>>();
        set.add(conflict);
        this.conflict = set;
    }

    @Override
    public int getLevel() {
        int level = 0;
        HSTreeNode<Id> node = this;
        while (node.parent != null) {
            level++;
            node = node.getParent();
        }
        return level;
    }

    @Override
    public void removeAxioms() {
        this.conflict = null;
    }

    @Override
    public void setOpen() {
        this.closed = false;
    }

    @Override
    public void removePath(Path<Id> path) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public BigDecimal getNodePathCosts() {
        if (nodePathCosts == null) {
            BigDecimal cost;
            if (isRoot()) {
                cost = getCostsEstimator().getFormulaSetCosts(Collections.<Id>emptySet());
            }   else {
                Id axiom = getArcLabel();
                BigDecimal fProb = getCostsEstimator().getFormulaCosts(axiom);
                cost = BigDecimal.ONE.subtract(fProb);
                cost = parent.getNodePathCosts().divide(cost,PRECISION,ROUNDING_MODE);
                cost = cost.multiply(fProb);
            }
            this.nodePathCosts = cost;
        }
        return nodePathCosts;
    }

    @Override
    public CostsEstimator<Id> getCostsEstimator() {
        return costsEstimator;
    }

    @Override
    public void setCostsEstimator(CostsEstimator<Id> costsEstimator) {
        this.costsEstimator = costsEstimator;
    }

    @Override
    public int compareTo(Node<Id> o) {
        if (this == o || this.equals(o))
            return 0;
        if (getNodePathCosts().compareTo(o.getNodePathCosts()) == 0)
            return Integer.valueOf(getPathLabels().size()).compareTo(o.getPathLabels().size());
        return -1 * getNodePathCosts().compareTo(o.getNodePathCosts());
    }

    public String toString() {
        return (isRoot()) ? "Root" : getArcLabel().toString();
    }

    public Set<HSTreeNode> getLeaves(){

        Set<HSTreeNode> result = new LinkedHashSet<HSTreeNode>();

        if(getChildren().isEmpty())
            result.add(this);

        else{
            for(Node<Id> child:getChildren()){
                result.addAll(((HSTreeNode) child).getLeaves());
            }
        }
        return result;

    }

    public Set<Set<Id>> getHittingSets(){

        Set<Set<Id>> result = new LinkedHashSet<Set<Id>>();

        for(Node<Id> node:getLeaves()){
            result.add(node.getPathLabels().iterator().next().getPositivePath());
        }
        return result;
    }

    /*
    public Set<Set<Id>> getHittingSets(){

        Set<Set<Id>> result = new LinkedHashSet<Set<Id>>();

        for(Node<Id> node:getLeaves()){
            for(TreeSet<Id> path:node.getPathLabels())
            result.add(path);
        }
        return result;
    }
    */


    public Set<Set<Id>> getConflicts(){
        return conflict;
    }

    public int countNodes(){

        int result=1;

        for(Node node:getChildren()){
            result+=((HSTreeNode)node).countNodes();
        }
        return result;
    }

    public boolean isConnectedToRoot(){

        if(this.parent.isRoot())  return true;
        else if(this.parent==null) return false;
        else return this.parent.isConnectedToRoot();

    }
}