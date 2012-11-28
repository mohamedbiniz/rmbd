package at.ainf.diagnosis.tree;

import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.storage.AxiomSetImpl;
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
public class MultiNode<Id> extends SimpleNode<Id> {

    private ArrayList<Node<Id>> newNodes = new ArrayList<Node<Id>>();
    private SplitStrategy<Id> splitStrategy= new SimpleSplitStrategy<Id>();

    public MultiNode(Set<AxiomSet<Id>> conflict) {
        super(conflict);
    }

   /* public MultiNode(Set<Id> conflict) {
        super(conflict);
    } */

    public MultiNode(Node<Id> parent, Id arcLabel) {
        super(parent,arcLabel);
    }


    @Override
    public AxiomSet<Id> getAxiomSet() {
        if (getAxiomSets().size() > 1)
            throw new UnsupportedOperationException("The multinode contains a set of axiom sets!");
        return super.getAxiomSet();
    }

    public ArrayList<Node<Id>> expandNode() {

         newNodes = new ArrayList<Node<Id>>();

        System.out.println("HS-Länge:"+ getPathLabels().size());
        System.out.println("Anzahl Konflikte:"+conflict.size());
        System.out.println(conflict.iterator().next().size());
        System.out.println("Expand!");

        if( !unitRule() ){
            if(!lastConflictRule()){
                subSetRule();
                splitRule();
            }
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


    private Set<AxiomSet<Id>> ignore(Id e, Set<AxiomSet<Id>>conflicts){

        Set<AxiomSet<Id>> newConflicts= removeElement(e, conflicts);

       /* for(AxiomSet<Id >c:newConflicts){
            c.remove(e);

            if(c.size()<=0)
                newConflicts.remove(c);
        }       */

        System.out.println("Ignore size:" + newConflicts.iterator().next().size());

        if(newConflicts.size()>0)
        return newConflicts;
        else return null;
    }


    private Set<AxiomSet<Id>> addToHS(Id e, Set<AxiomSet<Id>>conflicts){

        Set<AxiomSet<Id>> newConflicts= copy2(conflicts);
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
                MultiNode node1=new MultiNode(this,e);
                node1.setAxiomSet(addToHS(e,conflict));
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
        MultiNode node1=new MultiNode(this,e);
        node1.setAxiomSet(addToHS(e,conflict));

        MultiNode node2=new MultiNode(this,null);
        node2.setAxiomSet(ignore(e,conflict));

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
            MultiNode node1=new MultiNode(this,e);
            node1.setAxiomSet(addToHS(e,conflict));
            newNodes.add(node1);

            // tree.addNode(newNode1)
            //tree.addEdge(conflicts,newNode1,e)
            if(!(c.size()==1)){
                MultiNode node2=new MultiNode(this,null);
                node2.setAxiomSet(ignore(e,conflict));
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


    private AxiomSet<Id> removeElement(Id e, AxiomSet<Id> set) {
        Set<Id> hs =  new LinkedHashSet<Id>(set);
        //edited, eventually without "if"
        if(hs.remove(e))
            return new AxiomSetImpl<Id>(set.getMeasure(), hs, set.getEntailments());
        else return set;
    }

    private Set<AxiomSet<Id>> removeElement(Id e, Set<AxiomSet<Id>> set) {
        Set<AxiomSet<Id>> hs = new LinkedHashSet<AxiomSet<Id>>();
        for (AxiomSet<Id> hset : set) {
            hs.add(removeElement(e, hset));
            if(hset.size()<=0)
                hs.remove(hset);
        }
        return hs;
    }
    private AxiomSet<Id> copy(AxiomSet<Id> set) {
        Set<Id> cs =  new LinkedHashSet<Id>(set);
        return new AxiomSetImpl<Id>(set.getMeasure(), cs, set.getEntailments());
    }

    private Set<AxiomSet<Id>> copy2(Set<AxiomSet<Id>> set) {
        Set<AxiomSet<Id>> css = new LinkedHashSet<AxiomSet<Id>>();
        for (AxiomSet<Id> cs : set)
            //hier vielleicht copy(cs)  oder nicht
            css.add(cs);
        return css;
    }


}
