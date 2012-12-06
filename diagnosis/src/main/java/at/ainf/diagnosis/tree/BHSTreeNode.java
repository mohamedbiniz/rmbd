package at.ainf.diagnosis.tree;

import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.storage.FormulaSetImpl;
import at.ainf.diagnosis.tree.splitstrategy.MostFrequentSplitStrategy;
import at.ainf.diagnosis.tree.splitstrategy.SimpleSplitStrategy;
import at.ainf.diagnosis.tree.splitstrategy.SplitStrategy;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: chicco
 * Date: 22.11.12
 * Time: 15:05
 * To change this template use File | Settings | File Templates.
 */
public class BHSTreeNode<Id> extends HSTreeNode<Id> {

    private ArrayList<Node<Id>> newNodes = new ArrayList<Node<Id>>();
    private SplitStrategy<Id> splitStrategy= new MostFrequentSplitStrategy<Id>();

    public BHSTreeNode(Set<FormulaSet<Id>> conflict) {
        super(conflict);
    }

   /* public MultiNode(Set<Id> conflict) {
        super(conflict);
    } */

    public BHSTreeNode(Node<Id> parent, Id arcLabel) {
        super(parent,arcLabel);
    }


    @Override
    public FormulaSet<Id> getAxiomSet() {
        if (getAxiomSets().size() > 1)
            throw new UnsupportedOperationException("The multinode contains a set of axiom sets!");
        return super.getAxiomSet();
    }

    public ArrayList<Node<Id>> expandNode() {

         newNodes = new ArrayList<Node<Id>>();

     /*   System.out.println("HS-Länge:"+ getPathLabels().size());
        System.out.println("Anzahl Konflikte:"+conflict.size());
        System.out.println(conflict.iterator().next().size());
        System.out.println("Expand!");*/

        if( !unitRule() ){
           // if(!lastConflictRule2()){
            if(getArcLabel()==null)
               subSetRule();
                splitRule();
            //}
        }

      /*  for(SimpleNode<Id> n:newNodes){

            if(n.getAxiomSets().iterator().next()!=null)
                System.out.println("ReturnSize: "+ n.getAxiomSets().iterator().next().size());
                  else  System.out.println("Returnsize empty");
        }     */

        return newNodes;
    }

    private void subSetRule(){

        Set<Set<Id>> eraseSet=new LinkedHashSet<Set<Id>>();

        for(Set<Id >c1:conflict){
            for(Set<Id>c2:conflict){
                if(!c1.equals(c2) && c1.containsAll(c2) )   eraseSet.add(c1);;
            }
        }

        for(Set<Id> ec:eraseSet){
            this.conflict.remove(ec);
        }
    }


    private Set<FormulaSet<Id>> ignore(Id e, Set<FormulaSet<Id>>conflicts){

        Set<FormulaSet<Id>> newConflicts= removeElement(e, conflicts);

       /* for(AxiomSet<Id >c:newConflicts){
            c.remove(e);

            if(c.size()<=0)
                newConflicts.remove(c);
        }       */

        //System.out.println("Ignore size:" + newConflicts.iterator().next().size());

        if(newConflicts.size()>0)
        return newConflicts;
        else return null;
    }


    private Set<FormulaSet<Id>> addToHS(Id e, Set<FormulaSet<Id>>conflicts){

        Set<FormulaSet<Id>> newConflicts= copy2(conflicts);
        Set<Set<Id>> eraseSet=new LinkedHashSet<Set<Id>>();

        for(Set<Id> c : newConflicts){
            if (c.contains(e))
                eraseSet.add(c);
        }

        for(Set<Id> ec:eraseSet){
                newConflicts.remove(ec);
        }

        if(newConflicts.size()>0)
        return newConflicts;
        else return null;
    }


    public boolean unitRule(){

        boolean foundElement=false;

        for (Set<Id> c:conflict){
            if (c.size()==1 && !foundElement) {
                Id e=c.iterator().next();
                BHSTreeNode node1=new BHSTreeNode(this,e);
                node1.setAxiomSet(addToHS(e,conflict));
                node1.setCostsEstimator(this.costsEstimator);

                newNodes.add(node1);
                //tree.addNode(newConflicts);
                //Edge edge= new Edge();
                //edge.from=conflicts;
                //edge.to=newConflicts;
                //edge.value=e;
                //tree.addEdge(e);
                //openNodes.add(newConflicts);
                foundElement=true;
                return true;

            }
        }
        return foundElement;
    }

    public void splitRule(){

        Id e=chooseSplitElement();
        BHSTreeNode node1=new BHSTreeNode(this,e);
        node1.setAxiomSet(addToHS(e,conflict));
        node1.setCostsEstimator(this.costsEstimator);

        BHSTreeNode node2=new BHSTreeNode(this,null);
        node2.setAxiomSet(ignore(e,conflict));
        node2.setCostsEstimator(this.costsEstimator);

        newNodes.add(node1);
        newNodes.add(node2);
        //tree.addNode(node1)
        //tree.addNode(node2)
        //tree.addEdge(conflicts,node1,e)
        //tree.addEdge(conflicts,node2,null)
        //openNodes.add(node1)
        //openNodes.add(node2)
    }

    public boolean lastConflictRule(){

        boolean foundElement=false;

        if(conflict.size()==1){

            foundElement=true;
            Set<Id> c=conflict.iterator().next();
            Id e=c.iterator().next();
            BHSTreeNode node1=new BHSTreeNode(this,e);
            node1.setAxiomSet(addToHS(e,conflict));
            node1.setCostsEstimator(this.costsEstimator);
            newNodes.add(node1);

            // tree.addNode(newNode1)
            //tree.addEdge(conflicts,newNode1,e)
            if(!(c.size()==1)){
                BHSTreeNode node2=new BHSTreeNode(this,null);
                node2.setAxiomSet(ignore(e,conflict));
                node2.setCostsEstimator(this.costsEstimator);
                newNodes.add(node2);
                //  tree.addNode(newNode2)
                // tree.addEdge(conflicts,newNode2,null)
                // openNodes.add(newNode2)
            }
        }
        return foundElement;
    }


    private Id chooseSplitElement(){

        return splitStrategy.getSplitElement(conflict);
    }

    public void setSplitStrategy(SplitStrategy<Id> splitStrategy){
        this.splitStrategy=splitStrategy;
    }

    public SplitStrategy<Id> getSplitStrategy(){
        return splitStrategy;
    }


    private FormulaSet<Id> removeElement(Id e, FormulaSet<Id> set) {
        Set<Id> hs =  new LinkedHashSet<Id>(set);
        //edited, eventually without "if"
        if(hs.remove(e))
            return new FormulaSetImpl<Id>(set.getMeasure(), hs, set.getEntailments());
        else return set;
    }

    private Set<FormulaSet<Id>> removeElement(Id e, Set<FormulaSet<Id>> set) {
        Set<FormulaSet<Id>> hs = new LinkedHashSet<FormulaSet<Id>>();
        for (FormulaSet<Id> hset : set) {
            hs.add(removeElement(e, hset));
            if(hset.size()<=0)
                hs.remove(hset);
        }
        return hs;
    }
    private FormulaSet<Id> copy(FormulaSet<Id> set) {
        Set<Id> cs =  new LinkedHashSet<Id>(set);
        return new FormulaSetImpl<Id>(set.getMeasure(), cs, set.getEntailments());
    }

    private Set<FormulaSet<Id>> copy2(Set<FormulaSet<Id>> set) {
        Set<FormulaSet<Id>> cses = new LinkedHashSet<FormulaSet<Id>>();
        for (FormulaSet<Id> cs : set)
            //hier vielleicht copy(cs)  oder nicht
            cses.add(cs);
        return cses;
    }

    //Update rules

    public void updateNode(Set<Id> delete){

         //Remove deleted elements from all conflicts
        for(Id id:delete){
            conflict=ignore(id,conflict);
        }

        Id splitElement= this.getLeftChild().getArcLabel();

        //Rule 1
        for(FormulaSet<Id> set: conflict){

            if(set.contains(splitElement)){
                if(set.size()==1){
                    //remove right Subtree
                   Node<Id> rightChild=getRightChild();
                    removeChild(rightChild);
                }
                return;
            }
        }

        //no set Contains splitElement => remove left Subtree
           Node<Id> leftChild=getLeftChild();
            removeChild(leftChild);

        Node<Id> rightChild=getRightChild();
        for(Node<Id> node :rightChild.getChildren()){
            rightChild.removeChild(node);
            this.addChild((HSTreeNode)node);

        }
        this.removeChild(rightChild);

        //Update all Succesors

        for(Node<Id> node:getChildren()){
            ((BHSTreeNode)node).updateNode(delete);
        }

    }

    private Node<Id> getLeftChild(){
        for(Node<Id> node : getChildren()){
            if(node.getArcLabel()!=null)
                return node;

        }
        return null;
    }

    private Node<Id> getRightChild(){
        for(Node<Id> node : getChildren()){
            if(node.getArcLabel()==null)
                return node;

        }
        return null;
    }

    public Set<Id> getIgnoredElements(){

        Set<Id> result = new LinkedHashSet<Id>();

        if(this.getArcLabel()==null&&this.getParent()!=null){
            result.add((Id)((BHSTreeNode)getParent()).getLeftChild().getArcLabel());
    }

        if(getParent()!=null)
        result.addAll(((BHSTreeNode)getParent()).getIgnoredElements());
        return result;

    }

    public FormulaSet<Id> updateConflict(FormulaSet<Id> conflict){

        FormulaSet<Id> result=conflict;


        for(Id id : getIgnoredElements()){
           if(conflict.contains(id))
            result=removeElement(id,conflict);
        }
          return result;
    }

    public boolean lastConflictRule2(){

        boolean foundElement=false;

        if(conflict.size()==1){


            foundElement=true;
            Set<Id> c=conflict.iterator().next();

            for(Id e:c){
            //Id e=c.iterator().next();
            BHSTreeNode node1=new BHSTreeNode(this,e);
            node1.setAxiomSet(addToHS(e,conflict));
            node1.setCostsEstimator(this.costsEstimator);
            newNodes.add(node1);

            }
        }
        return foundElement;
    }


}
