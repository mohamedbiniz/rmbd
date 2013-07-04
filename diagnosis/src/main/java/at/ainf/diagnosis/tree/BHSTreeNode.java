package at.ainf.diagnosis.tree;

import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.storage.FormulaSetImpl;
import at.ainf.diagnosis.tree.splitstrategy.MostFrequentSplitStrategy;
import at.ainf.diagnosis.tree.splitstrategy.SplitStrategy;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: chicco
 * Date: 22.11.12
 * Time: 15:05
 * To change this template use File | Settings | File Templates.
 */
public class BHSTreeNode<Id> extends HSTreeNode<Id> {

    private List<Node<Id>> newNodes = new LinkedList<Node<Id>>();
    private SplitStrategy<Id> splitStrategy= new MostFrequentSplitStrategy<Id>();
    private Set<Set<Id>> newConflicts = new LinkedHashSet<Set<Id>>();
    private HashMap<Integer,BHSTreeNode<Id>> allNodes= new HashMap<Integer,BHSTreeNode<Id>>();

 //   private Set<Set<Id>> hittingSetPaths=new LinkedHashSet<Set<Id>>();
  //  private Set<Set<Id>> ignorePaths = new LinkedHashSet<Set<Id>>();
    private Set<Path<Id>> paths= new LinkedHashSet<Path<Id>>();
    public boolean hasPositiveLabel=true;

   /* private class Path<E> {
        private Set<E> positivePath = new LinkedHashSet<E>();
        private Set<E> negativePath = new LinkedHashSet<E>();
        private boolean isExtended = false;

        public Set<E>  getPositivePath(){
            return positivePath;
        }

        public void setPositivePath(Set<E> positivePath){
            this.positivePath=positivePath;

        }

        public Set<E>  getNegativePath(){
            return negativePath;
        }

        public void setNegativePath(Set<E> negativePath){
            this.negativePath=negativePath;

        }

        public boolean isExtended(){
            return isExtended;
        }

        public void setExtended(boolean bool){
            this.isExtended=bool;
        }

    }
     */

    //private Set<Path<Id>> paths= new LinkedHashSet<Array[Set<Id>]>();

   //private HashMap<String,Set<Set<Id>>> allConflicts= new HashMap<String,Set<Set<Id>>>();

    public boolean isDisjoint=false;

    public BHSTreeNode(Set<Set<Id>> conflict) {
        super(conflict);

    }

    /* public MultiNode(Set<Id> conflict) {
       super(conflict);
   } */

    public BHSTreeNode(Node<Id> parent, Id arcLabel) {
        super(parent,arcLabel);
    }


    @Override
    public Set<Id> getAxiomSet() {
        if (getAxiomSets().size() > 1)
            throw new UnsupportedOperationException("The multinode contains a set of axiom sets!");
        return super.getAxiomSet();
    }

    @Override
    public void removePath(Path<Id> path) {
        this.paths.remove(path);
    }

    public List<Node<Id>> expandNode(){
        return expandNode(null);
    }


    public List<Node<Id>> expandNode(Set<Id> newConflict) {

        newNodes = new ArrayList<Node<Id>>();

        if(this.isRoot())
            this.paths.add(new Path<Id>());


        /*   System.out.println("HS-Länge:"+ getPathLabels().size());
        System.out.println("Anzahl Konflikte:"+conflict.size());
        System.out.println(conflict.iterator().next().size());
        System.out.println("Expand!");*/
      if(newConflict==null){
        if( !unitRule() ){

            /**
             * If the conflicts are disjoint we don't need to compute
             * a split element. Just set value to null.
             */
            Id e;
            if(!isDisjoint) {
              e=chooseSplitElement();

                //Geht nur mit most frequent:wenn jedes element nur
                //einmal vorkommt, so sind alle sets disjoint
                if(e==null) isDisjoint=true;

            }
            else  e = null;

           // if(!disjointConflictsRule()){
               // if(getArcLabel()==null)
                  //  subSetRule();
                splitRule(e);
            //}
        }
     }
      else{

          newConflicts.add(newConflict);
          //We add a new conflict by creating a new node
          for(Path<Id> path:this.paths){


             if(!path.isExtended())
              if(!intersectsWith(newConflict,path.getPositivePath())){

                  path.setExtended(true);
                  Set<Id> updatedConflict=updateConflict(newConflict,path);

                  if(!updatedConflict.isEmpty()){

                      Set<Set<Id>> newConflictSets = new LinkedHashSet<Set<Id>>();

                      if(this.conflict!=null)
                     newConflictSets.addAll(this.conflict);

                      newConflictSets.add(updatedConflict);

                      BHSTreeNode<Id> newNode = new BHSTreeNode(this,null);
                      newNode.setAxiomSet(newConflictSets);
                      newNode.setCostsEstimator(this.costsEstimator);
                      newNode.setSplitStrategy(this.splitStrategy);
                      newNode.setPaths(copyPath(path));

                       newNodes.add(newNode);
                  }

              }

          }

      }

        /*  for(SimpleNode<Id> n:newNodes){

       if(n.getAxiomSets().iterator().next()!=null)
           System.out.println("ReturnSize: "+ n.getAxiomSets().iterator().next().size());
             else  System.out.println("Returnsize empty");
   }     */

        /**
         * If a child is a duplicate of an earlier node, remove it
         * and substitute it with the older node.
         * Don't add it to "newNodes" as it doesn't need to
         * be processed again.
         */

        /*Set<Node<Id>> addNodes = new LinkedHashSet<Node<Id>>();
       Set<Node<Id>> deleteNodes = new LinkedHashSet<Node<Id>>();
        for(Node<Id> node : newNodes){

            if(node.getAxiomSets()!=null &&!node.getAxiomSets().isEmpty())  {
            int hash = (node.getAxiomSets()).hashCode();

            if(allNodes.containsKey(hash)){

                BHSTreeNode<Id> oldNode  = allNodes.get(hash);

               // newNodes.remove(node);
                deleteNodes.add(node);

                if(!this.getChildren().contains(oldNode))
                  this.addChild((HSTreeNode<Id>)oldNode);

                addNodes.addAll(oldNode.addPaths(node.getPathLabels()));
                //oldNode.addPaths(this.paths);

                //newNodes.add(oldNode);

            }else{
                //If a node is not a duplicate put it into the HashMap
                 allNodes.put(hash,(BHSTreeNode<Id>)node);
            }
            }
        }

        for(Node<Id> delete:deleteNodes)
        newNodes.remove(delete);
          newNodes.addAll(addNodes);
            */

        //Folgende Zeile nur lassen wenn man Duplikate verschmelzen will
          // newNodes=findDuplicates(newNodes);

        return newNodes;
    }

    private void subSetRule(){

        Set<Set<Id>> eraseSet=new LinkedHashSet<Set<Id>>();

        for(Set<Id >c1:conflict){

            if(!eraseSet.contains(c1))
            for(Set<Id>c2:conflict){

                if(!eraseSet.contains(c2)&&!(c1==c2) && c1.containsAll(c2) )   eraseSet.add(c1);


            }
        }

        for(Set<Id> ec:eraseSet){
            this.conflict.remove(ec);
        }
    }


    private Set<Set<Id>> ignore(Id e, Set<Set<Id>>conflicts){

        Set<Set<Id>> newConflicts= removeElement2(e, conflicts);

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


    private Set<Set<Id>> addToHS(Id e, Set<Set<Id>>conflicts){

        //wohl copy2(conflicts)
        Set<Set<Id>> newConflicts= copy2(conflicts);
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

                //Könnte man als eigene Funktion machen, da es sich wiederholt
                BHSTreeNode node1=new BHSTreeNode(this,e);
                node1.setAxiomSet(addToHS(e,conflict));
                node1.setCostsEstimator(this.costsEstimator);
                node1.setSplitStrategy(this.splitStrategy);
                  node1.setPaths(copyPaths(this.paths));
                  node1.extendPositivePaths(e);


                 //Nur für MultiParentGraph
                /*
                  node1.setHittingSetPaths(extendPaths(this.hittingSetPaths,e));
                  node1.setIgnorePaths(this.ignorePaths);
                    */

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

    public void splitRule(Id e){

      //  Id e=chooseSplitElement();
        BHSTreeNode node1=new BHSTreeNode(this,e);
        node1.setAxiomSet(addToHS(e,conflict));
        node1.setCostsEstimator(this.costsEstimator);
        node1.setSplitStrategy(this.splitStrategy);
        node1.setPaths(copyPaths(this.paths));
        node1.extendPositivePaths(e);

        //Nur für MultiParentGraph
        /*
      node1.setHittingSetPaths(extendPaths(this.hittingSetPaths,e));
      node1.setIgnorePaths(this.ignorePaths);
        */


        BHSTreeNode node2=new BHSTreeNode(this,null);
        node2.setAxiomSet(ignore(e,conflict));
        node2.setCostsEstimator(this.costsEstimator);
        node2.setSplitStrategy(this.splitStrategy);
        node2.setPaths(copyPaths(this.paths));
        node2.extendNegativePaths(e);
        node2.hasPositiveLabel= false;

        //Nur für MultiParentGraph
        /*
      node1.setHittingSetPaths(this.hittingSetPaths);
      node1.setIgnorePaths(extendPaths(this.ignorePaths,e));
        */

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
            node1.setSplitStrategy(this.splitStrategy);
            node1.setPaths(copyPaths(this.paths));
            node1.extendPositivePaths(e);

            newNodes.add(node1);

            // tree.addNode(newNode1)
            //tree.addEdge(conflicts,newNode1,e)
            if(!(c.size()==1)){
                BHSTreeNode node2=new BHSTreeNode(this,null);
                node2.setAxiomSet(ignore(e,conflict));
                node2.setCostsEstimator(this.costsEstimator);
                node2.setSplitStrategy(this.splitStrategy);
                node2.setPaths(copyPaths(this.paths));
                node2.extendNegativePaths(e);
                node2.hasPositiveLabel= false;
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

   //Alt,funktioniert aber

    private Set<Id> removeElement(Id e, Set<Id> set) {
        Set<Id> hs =  new LinkedHashSet<Id>(set);
        //edited, eventually without "if"
        if(hs.remove(e))
            return hs;
        else return set;
    }


    /*private Set<Id> removeElement(Id e, Set<Id> set) {
         set.remove(e);
        return set;
    } */


    //Alt aber funktioniert

    private Set<Set<Id>> removeElement2(Id e, Set<Set<Id>> set) {
        Set<Set<Id>> hs = new LinkedHashSet<Set<Id>>();

        if(set!=null){
            for (Set<Id> hset : set) {
                hs.add(removeElement(e, hset));
                if(hset.size()<=0)
                    hs.remove(hset);
            }
        }
        return hs;
    }


    /*
    private Set<Set<Id>> removeElement2(Id e, Set<Set<Id>> set) {


        for(Set<Id> c:set){
            removeElement(e,c);
        }

        return set;
    }
   */

    public Set<Set<Id>> copy2(Set<Set<Id>> set) {
        Set<Set<Id>> cses = new LinkedHashSet<Set<Id>>();
        for (Set<Id> cs : set)
            //hier vielleicht copy(cs)  oder nicht
            cses.add(cs);
        return cses;
    }

    public Set<FormulaSet<Id>> copy3(Set<FormulaSet<Id>> set) {
        Set<FormulaSet<Id>> cses = new LinkedHashSet<FormulaSet<Id>>();
        for (FormulaSet<Id> cs : set)
            //hier vielleicht copy(cs)  oder nicht
            cses.add(copy4(cs));
        return cses;
    }

    private FormulaSet<Id> copy4(FormulaSet<Id> set) {
        Set<Id> cs =  new LinkedHashSet<Id>(set);


        return new FormulaSetImpl<Id>(new BigDecimal("1"),cs, Collections.<Id>emptySet());
    }


    //Update rules
    public Set<Node<Id>> updateNode(Set<Id> delete){


       Set<Node<Id>> removedNode = new LinkedHashSet<Node<Id>>();

        //Checks if the splitElement is still in the conflicts
        boolean containsSplitelement=false;
        //Remove deleted elements from all conflicts
        for(Id id:delete){
            conflict=ignore(id,conflict);
        }

        Id splitElement=null;

        if(this.getLeftChild()!=null)
            splitElement= this.getLeftChild().getArcLabel();

        //Rule 1: If a Unit Conflict Exists, remove right Child
        //Unschön
        if(conflict!=null && splitElement!=null){
            for(Set<Id> set: conflict){

                if(set.contains(splitElement)){
                    if(set.size()==1){
                        //remove right Subtree
                        Node<Id> rightChild=getRightChild();
                        removeChild(rightChild);
                        removedNode.add(rightChild);
                        //return war vorher nach der klammer?
                        return removedNode;
                    }
                  containsSplitelement=true;
                }
            }
        }


        //no set Contains splitElement => remove left Subtree
        if(!containsSplitelement){
        Node<Id> leftChild=getLeftChild();
        removeChild(leftChild);
        removedNode.add(leftChild);

        Node<Id> rightChild=getRightChild();
        Set<Node<Id>> removeChild= new LinkedHashSet<Node<Id>>();

        //unschön
        if(rightChild!=null){
            for(Node<Id> node :rightChild.getChildren()){
                removeChild.add(node);
            }
        }

            for(Node<Id> node:removeChild){
                rightChild.removeChild(node);
                this.addChild((HSTreeNode)node);
            }

        this.removeChild(rightChild);
            removedNode.add(rightChild);
        }
      /*  for(Node<Id> child: removeChild)
            rightChild.removeChild(child);

        }  */
        //Update all Succesors

        /*for(Node<Id> node:getChildren()){
            ((BHSTreeNode)node).updateNode(delete);
        } */
     return removedNode;
    }


    //Update rules
    public Set<Node<Id>> updateNode2(Set<Id> delete){


        Set<Node<Id>> removedNode = new LinkedHashSet<Node<Id>>();

        //Checks if the splitElement is still in the conflicts
        boolean containsSplitelement=false;
        //Remove deleted elements from all conflicts
        for(Id id:delete){
            conflict=ignore(id,conflict);
        }

        Set<Id> splitElements=null;

        Set<Node<Id>> hittedChildren=this.getHittedChildren();

        if(!hittedChildren.isEmpty()){

            for(Node<Id> hittedChild:hittedChildren)
            splitElements.add(hittedChild.getArcLabel());

        }

        //If there are several Conflicts only one Splitelement was chosen

           // Id splitElement=splitElements.iterator().next();

        //Rule 1: If a Unit Conflict Exists, remove right Child
        //Unschön
        /*Number of conflicts must be greater than 1 to apply this rule.
          Otherwise there would be no "right Child"
        */
           //Check if there is only one conflict
        boolean hasSingleConflict=conflict.size()==1;

        if(!hasSingleConflict){


        if(conflict!=null && !splitElements.isEmpty()){

            //In this case there is only one SplitElement available
            Id splitElement=splitElements.iterator().next();

            for(Set<Id> set: conflict){

                if(set.contains(splitElement)){
                    if(set.size()==1){
                        //remove right Subtree
                        Node<Id> rightChild=getRightChild();
                        removeChild(rightChild);
                        removedNode.add(rightChild);
                        //return war vorher nach der klammer?
                        return removedNode;
                    }
                    containsSplitelement=true;
                }
            }
        }


        //no set Contains splitElement => remove left Subtree
        if(!containsSplitelement){
            Node<Id> leftChild=getLeftChild();
            removeChild(leftChild);
            removedNode.add(leftChild);

            Node<Id> rightChild=getRightChild();
            Set<Node<Id>> removeChild= new LinkedHashSet<Node<Id>>();

            //unschön
            if(rightChild!=null){
                for(Node<Id> node :rightChild.getChildren()){
                    removeChild.add(node);
                }
            }

            for(Node<Id> node:removeChild){
                rightChild.removeChild(node);
                this.addChild((HSTreeNode)node);
            }

            this.removeChild(rightChild);
            removedNode.add(rightChild);
        }
        }
        /* If there is only one conflict several hitting children may be present. We remove all the children whose
        arclabels are not present in the updated node.
         */
        //Sollte noch angepasst werden für dies DISJOINT-Regel
        else if(hasSingleConflict){

            for(Id splitElement:splitElements){
                if(!conflict.contains(splitElement)){
                     Node<Id> child=getChildByLabel(splitElement);
                    this.removeChild(child);
                    removedNode.add(child);
                }
            }

        }


        /*  for(Node<Id> child: removeChild)
          rightChild.removeChild(child);

      }  */
        //Update all Succesors

        /*for(Node<Id> node:getChildren()){
            ((BHSTreeNode)node).updateNode(delete);
        } */
        return removedNode;
        }




    private Node<Id> getLeftChild(){
        for(Node<Id> node : getChildren()){
            if(node.getArcLabel()!=null)
                return node;

        }
        return null;
    }

    private Set<Node<Id>> getHittedChildren(){

        Set<Node<Id>> result= new LinkedHashSet<Node<Id>>();

        for(Node<Id> node : getChildren()){
            if(node.getArcLabel()!=null)
                result.add(node);

        }
        return result;
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

    public LinkedHashSet<Id> updateConflict(Set<Id> conflict,Path<Id> path){

        Set<Id> result=conflict;


        for(Id id : path.getNegativePath()){
            if(conflict.contains(id))
                result=removeElement(id, conflict);
        }

        LinkedHashSet<Id> result2 =new LinkedHashSet<Id>(result);

        return result2;
    }

    public boolean lastConflictRule2(){

        boolean foundElement=false;

        if(conflict.size()==1){

            foundElement=true;
            Set<Id> c=conflict.iterator().next();

            for(Id e:c){
                //Id e=c.iterator().next();
                BHSTreeNode node1=new BHSTreeNode(this,e);

                //Könnte man optimieren(einfach nur leere Konfliktmenge)
                //node1.setAxiomSet(null);
                node1.setAxiomSet(addToHS(e,conflict));
                node1.setCostsEstimator(this.costsEstimator);
                node1.setSplitStrategy(this.splitStrategy);
                node1.setPaths(copyPaths(this.paths));
                node1.extendPositivePaths(e);
                newNodes.add(node1);

                //Nur für MultiParentGraph
                /*
                  node1.setHittingSetPaths(extendPaths(this.hittingSetPaths,e));
                  node1.setIgnorePaths(this.ignorePaths);
                    */

            }
        }
        return foundElement;
    }

    public boolean disjointConflictsRule(){

        Set<Id> splitConflict=conflict.iterator().next();

        Set<Set<Id>> successorConflicts=addToHS(splitConflict.iterator().next(),conflict);


       if(isDisjoint){

        for(Id e:splitConflict){
            //Id e=c.iterator().next();
            BHSTreeNode node1=new BHSTreeNode(this,e);

            //Könnte man optimieren(einfach nur leere Konfliktmenge)
            //node1.setAxiomSet(null);
            //node1.setAxiomSet(addToHS(e,conflict));
            node1.setAxiomSet(successorConflicts);
            node1.setCostsEstimator(this.costsEstimator);
            node1.setSplitStrategy(this.splitStrategy);
            node1.setPaths(copyPaths(this.paths));
            node1.extendPositivePaths(e);

            if(!(conflict.size()==1))
            node1.isDisjoint=true;
            newNodes.add(node1);

            //Nur für MultiParentGraph
            /*
          node1.setHittingSetPaths(extendPaths(this.hittingSetPaths,e));
          node1.setIgnorePaths(this.ignorePaths);
            */

        }

        return true;
    }
     return false;
    }

    public Set<Set<Id>> getNewConflicts(){
        return newConflicts;
    }

    public void addNewConflict(LinkedHashSet<Id> set){
        newConflicts.add(set);
    }

    /**
     * Returns a hitted Child with a given arc label
     * @param label
     * @return
     */
    private Node<Id> getChildByLabel(Id label){

        for(Node<Id> child:getHittedChildren()){
               if(((BHSTreeNode<Id>)child).getArcLabel().equals(label))
                   return child;

        }
        return null;
    }


    public Set<Set<Id>> getPathLabels2(){

        Set<Set<Id>> result= new LinkedHashSet<Set<Id>>();

        for(Path<Id> path : this.paths){
            result.add(path.getPositivePath());
        }

        return result;
    }

    public Set<Set<Id>> getNegativePaths(){

        Set<Set<Id>> result= new LinkedHashSet<Set<Id>>();

        for(Path<Id> path : this.paths){
            result.add(path.getNegativePath());
        }

        return result;
    }


    public Set<Path<Id>> getPaths(){
        return paths;
    }

    public void setPaths(Set<Path<Id>> paths){
        this.paths = paths;
    }


    public void setPaths(Path<Id> path){

        Set<Path<Id>> paths = new LinkedHashSet<Path<Id>>();
        paths.add(path);

        this.paths = paths;
    }

    public void addPath(Path<Id> path){

        this.paths.add(path);
    }





    public Set<Node<Id>> addPaths(Set<Path<Id>> paths){

        Set<Node<Id>> results=new LinkedHashSet<Node<Id>>();

        for(Path<Id> path:paths)
            addPath(path);



         if(!newConflicts.isEmpty())   {
             Set<Id> newConflict=newConflicts.iterator().next();
        for(Path<Id> path:paths){


            if(!path.isExtended())
                if(!intersectsWith(newConflict,path.getPositivePath())){

                    path.setExtended(true);
                    Set<Id> updatedConflict=updateConflict(newConflict,path);

                    if(!updatedConflict.isEmpty()){

                        Set<Set<Id>> newConflictSets = new LinkedHashSet<Set<Id>>();

                        if(this.conflict!=null)
                            newConflictSets.addAll(this.conflict);

                        newConflictSets.add(updatedConflict);

                        BHSTreeNode<Id> newNode = new BHSTreeNode(this,null);
                        newNode.setAxiomSet(newConflictSets);
                        newNode.setCostsEstimator(this.costsEstimator);
                        newNode.setSplitStrategy(this.splitStrategy);
                        newNode.setPaths(copyPath(path));

                       // newNodes.add(newNode);
                        results.add(newNode);
                    }

                }

        }
         }



        if(newConflicts.isEmpty())
        for(Node<Id> child:this.getChildren())   {

        Set<Path<Id>> newPaths = copyPaths(paths);
            newPaths=extendPaths(newPaths,child.getArcLabel(),((BHSTreeNode<Id>)child).hasPositiveLabel);
            results.addAll(((BHSTreeNode<Id>) child).addPaths(newPaths));
        }
       return results;
    }



    /**
     * Extends each path in a given set by a given element.
     * @param paths
     * @param newElement
     * @return
     */
    public Set<Path<Id>> extendPaths(Set<Path<Id>> paths, Id newElement,boolean sign){

        Set<Path<Id>> newPaths= copyPaths(paths);

        for(Path<Id> path:newPaths){
            if(sign)
            path.getPositivePath().add(newElement);
            else path.getNegativePath().add(newElement);
        }
        return newPaths;
    }


    public void extendPositivePaths(Id newElement){



        for(Path<Id> path:paths){
            path.getPositivePath().add(newElement);
        }

    }

    public void extendNegativePaths(Id newElement){

        for(Path<Id> path:paths){
            path.getNegativePath().add(newElement);
        }

    }



    private boolean intersectsWith(Collection<Id> pathLabels, Collection<Id> localConflict) {
        for (Id label : pathLabels) {
            //if (localConflict.contains(label))
            //    return true;
            for (Id axiom : localConflict) {
                if (axiom.equals(label))
                    return true;
            }
        }
        return false;
    }

    private Set<Path<Id>> copyPaths(Set<Path<Id>> paths){
        Set<Path<Id>>   newPaths= new LinkedHashSet<Path<Id>>();

             for(Path<Id> path:paths){
                 newPaths.add(copyPath(path));
             }

        return newPaths;
    }

    /**
     * Creates a new path which contains the same positive
     * and negative paths as a given path. Note that the
     * variable "isExtended" is still set to "false" by default.
     * @param path
     * @return
     */
    private Path<Id> copyPath(Path<Id> path){

        Path<Id> newPath = new Path<Id>();
        newPath.setPositivePath(copy(path.getPositivePath()));
        newPath.setNegativePath(copy(path.getNegativePath()));
        return newPath;

    }

    @Override
    public Set<Path<Id>> getPathLabels(){
        return paths;
    }



    private List<Node<Id>> findDuplicates(List<Node<Id>> newNodes){

    List<Node<Id>> addNodes = new LinkedList<Node<Id>>();
    Set<Node<Id>> deleteNodes = new LinkedHashSet<Node<Id>>();

        for(Node<Id> node : newNodes){

        if(node.getAxiomSets()!=null &&!node.getAxiomSets().isEmpty())  {
            int hash = (node.getAxiomSets()).hashCode();

            if(allNodes.containsKey(hash)){

                BHSTreeNode<Id> oldNode  = allNodes.get(hash);

                // newNodes.remove(node);
                deleteNodes.add(node);

                if(!this.getChildren().contains(oldNode))
                    this.addChild((HSTreeNode<Id>)oldNode);

                addNodes.addAll(oldNode.addPaths(node.getPathLabels()));
                oldNode.addPaths(node.getPathLabels());

                //newNodes.add(oldNode);

            }else{
                //If a node is not a duplicate put it into the HashMap
                allNodes.put(hash,(BHSTreeNode<Id>)node);
            }
        }
    }

    for(Node<Id> delete:deleteNodes)
            newNodes.remove(delete);
        if(!addNodes.isEmpty())
    newNodes.addAll(findDuplicates(addNodes));

    return newNodes;

}


}
