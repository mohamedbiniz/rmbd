package at.ainf.diagnosis.tree;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 13.11.12
 * Time: 17:01
 * To change this template use File | Settings | File Templates.
 */
public class BinaryTreeSearch<T extends AxiomSet<Id>,Id> extends AbstractTreeSearch<T,Id> implements TreeSearch<T,Id> {


    @Override
    protected void pruneConflictSets(Node<Id> node, T conflictSet) throws SolverException, InconsistentTheoryException {

    }

    @Override
    protected void proveValidnessConflict(T conflictSet) throws SolverException {

    }



    public void createRoot() throws NoConflictException,
            SolverException, InconsistentTheoryException {
        // if there is already a root
        if (getRoot() != null) return;
        Set<AxiomSet<Id>> conflict = calculateConflict(null);
        //muss später verändert werden, damit es für mehrere Konflikte funktioniert
        MultiNode<Id> node = new MultiNode<Id>(conflict);


        setRoot(node);
    }

    public Set<AxiomSet<Id>> calculateNode(Node<Id> node) throws NoConflictException,SolverException,InconsistentTheoryException{
      if(node.getAxiomSet()==null || node.getAxiomSet().isEmpty())
        return calculateConflict(node);

       else return null;
    }

    public Set<T> getDiagnoses() {
        return getValidAxiomSets(copy(getHittingSets()));
    }

    public Set<T> getConflicts() {
        return getValidAxiomSets(copy(getNodeLabels()));
    }

    public void updateTree(List<T> invalidHittingSets) throws SolverException, InconsistentTheoryException, NoConflictException {



        for (T invalidHittingSet : invalidHittingSets) {
            MultiNode<Id> node = (MultiNode<Id>) invalidHittingSet.getNode();
            if (node.isRoot())
                throw new IllegalStateException("Impossible source of a hitting set");

            if (isConnectedToRoot(node)) {
                node.setOpen();
                node.setCalculateConflict(true);
                getSearchStrategy().pushOpenNode(node);
                removeHittingSet(invalidHittingSet);
            }
        }
    }

    @Override
    protected boolean proveValidnessDiagnosis(Set<Id> diagnosis) throws SolverException {

        if (getSearchable().getKnowledgeBase().hasTests())
            return getSearchable().testDiagnosis(diagnosis);

        return true;
    }

    private boolean isConnectedToRoot(Node<Id> node) {
        if (node == null) return false;
        return node.isRoot() || isConnectedToRoot(node.getParent());
    }

    private void updateTree(AxiomSet<Id> conflictSet) throws SolverException, InconsistentTheoryException {
        Node<Id> root = getRoot();
        if (getRoot() == null) {
            return;
        }
        LinkedList<Node<Id>> children = new LinkedList<Node<Id>>();
        children.add(root);
        while (!children.isEmpty()) {
            Node<Id> node = children.removeFirst();
            Set<Node<Id>> nodeChildren = updateNode(conflictSet, node);
            children.addAll(nodeChildren);
        }
    }

    public Set<Node<Id>> updateNode(AxiomSet<Id> axSet, Node<Id> node) throws SolverException, InconsistentTheoryException {
        if (node == null || node.getAxiomSet() == null)
            return Collections.emptySet();
        if (node.getAxiomSet().containsAll(axSet)) {
            //EDITED
            Set<Id> invalidAxioms = new LinkedHashSet<Id>(node.getAxiomSet().iterator().next());
            //if (!getSearcher().isDual())
            invalidAxioms.removeAll(axSet);

            for (Id invalidAxiom : invalidAxioms) {
                Node<Id> invalidChild = findInvalidChild(node, invalidAxiom);
                node.removeChild(invalidChild);
            }

            node.setAxiomSet(axSet);
        }
        return node.getChildren();
    }

    private Node<Id> findInvalidChild(Node<Id> node, Id invalidAxiom) {
        for (Node<Id> idNode : node.getChildren()) {
            if (idNode.getArcLabel().equals(invalidAxiom)) {
                removeChildren(idNode);
                return idNode;
            }
        }
        throw new IllegalStateException("Invalid child does not exists!");
    }

    private void removeChildren(Node<Id> idNode) {
        if (!getSearchStrategy().getOpenNodes().remove(idNode)) {
            for (Node<Id> node : idNode.getChildren()) {
                removeChildren(node);
            }
        }
    }





/*  public Set<Set<Id>> computeDiagnoses() throws NoConflictException,SolverException,InconsistentTheoryException{

     createRoot();



     return computeDiagnoses(getRoot());
 }

 public Set<Set<Id>> computeDiagnoses(Node<Id> node){

     Set<Set<Id>> diagnoses =new LinkedHashSet<Set<Id>>();
     Set<Set<Id>> conflicts=node.getAxiomSet();

     if(conflicts.size()==0){
      diagnoses.add(node.getPathLabels());
     }
    else{
         //openNodes.remove(conflicts)
         if( !unitRule(conflicts) ){
             if(!lastConflictRule(conflicts)){
                 subSetRule(conflicts);
                 splitRule(conflicts);
             }



         }
     }
     return diagnoses;
 }

 private void subSetRule(Set<Set<Id>> conflicts){
     for(Set<Id >c1:conflicts){
         for(Set<Id>c2:conflicts){
             if(c1!=c2 && c1.containsAll(c2) )  conflicts.remove(c2);
         }
     }
 }


private Set<Set<Id>> ignore(Id e, Set<Set<Id>>conflicts){

    Set<Set<Id>> newConflicts=conflicts;

     for(Set<Id >c:newConflicts){
         c.remove(e);
     }
     return newConflicts;
 }

private Set<Set<Id>>  addToHS(Id e, Set<Set<Id>>conflicts){
    Set<Set<Id>> newConflicts=conflicts;
     for(Set<Id> c : newConflicts){
         if (c.contains(e))
          newConflicts.remove(c);
     }
     return newConflicts;
 }


 public boolean unitRule(Set<Set<Id>>conflicts){

     boolean foundElement=false;

     for (Set<Id> conflict:conflicts){
         if (conflict.size()==1 && !foundElement) {
             Id e=conflict.iterator().next();
             Node node =new Node(addToHS(e,conflicts));
             //tree.addNode(newConflicts);
             //Edge edge= new Edge();
             //edge.from=conflicts;
             //edge.to=newConflicts;
             //edge.value=e;
             //tree.addEdge(e);
             //openNodes.add(newConflicts);
             foundElement=true;

         }
     }
     return foundElement;
 }

 public void splitRule(Set<Set<Id>> conflicts){
     Id e=chooseSplitElement(conflicts);
    Node node1=new Node(addToHS(e,conflicts));
    Node node2=new Node(ignore(e,conflicts));
     //tree.addNode(node1)
     //tree.addNode(node2)
     //tree.addEdge(conflicts,node1,e)
     //tree.addEdge(conflicts,node2,null)
     //openNodes.add(node1)
     //openNodes.add(node2)
 }

 public boolean lastConflictRule(Set<Set<Id>> conflicts){

     boolean foundElement=false;

     if(conflicts.size()==1){

         foundElement=true;
         Set<Id> conflict=conflicts.iterator().next();
         Id e=conflict.iterator().next();
         Node node1=new Node(addToHS(e,conflicts));
        // tree.addNode(newNode1)
         //tree.addEdge(conflicts,newNode1,e)

         if(!(conflict.size()==1)){
             Node node2=new Node(ignore(e,conflicts));
           //  tree.addNode(newNode2)
            // tree.addEdge(conflicts,newNode2,null)
            // openNodes.add(newNode2)
         }
     }
     return foundElement;
 }

 protected Set<Set<Id>> calculateNode(Node<Id> node) throws SolverException, InconsistentTheoryException, NoConflictException{
     return calculateConflict(node);
 }


private Id chooseSplitElement(Set<Set<Id>> conflicts){
    return null;
}
*/
}
