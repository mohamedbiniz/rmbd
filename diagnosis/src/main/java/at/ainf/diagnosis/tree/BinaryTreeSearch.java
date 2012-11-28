package at.ainf.diagnosis.tree;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 13.11.12
 * Time: 17:01
 * To change this template use File | Settings | File Templates.
 */
public class BinaryTreeSearch<T extends FormulaSet<Id>,Id> extends AbstractTreeSearch<T,Id> implements TreeSearch<T,Id> {


    @Override
    protected void pruneConflictSets(Node<Id> node, T conflictSet) throws SolverException, InconsistentTheoryException {

    }

    @Override
    protected void proveValidnessConflict(T conflictSet) throws SolverException {

    }

    public boolean proveValidnessDiagnosis(Set<Id> diagnosis) throws SolverException {

        if (getSearchable().getKnowledgeBase().hasTests())
            return getSearchable().testDiagnosis(diagnosis);

        return true;

    }

    public void createRoot() throws NoConflictException,
            SolverException, InconsistentTheoryException {
        // if there is already a root
        if (getRoot() != null) return;
        Set<FormulaSet<Id>> conflict = calculateConflict(null);

        BHSTreeNode<Id> node = new BHSTreeNode<Id>(conflict);

        setRoot(node);
    }

    public Set<FormulaSet<Id>> calculateNode(Node<Id> node) throws NoConflictException,SolverException,InconsistentTheoryException{
      if(node.getAxiomSets()==null || node.getAxiomSets().isEmpty())
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


    }

/*  public Set<Set<Id>> computeDiagnoses() throws NoConflictException,SolverException,InconsistentTheoryException{

     createRoot();



     return computeDiagnoses(getRoot());
 }

 public Set<Set<Id>> computeDiagnoses(SimpleNode<Id> node){

     Set<Set<Id>> diagnoses =new LinkedHashSet<Set<Id>>();
     Set<Set<Id>> conflicts=node.getAxiomSets();

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
             SimpleNode node =new SimpleNode(addToHS(e,conflicts));
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
    SimpleNode node1=new SimpleNode(addToHS(e,conflicts));
    SimpleNode node2=new SimpleNode(ignore(e,conflicts));
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
         SimpleNode node1=new SimpleNode(addToHS(e,conflicts));
        // tree.addNode(newNode1)
         //tree.addEdge(conflicts,newNode1,e)

         if(!(conflict.size()==1)){
             SimpleNode node2=new SimpleNode(ignore(e,conflicts));
           //  tree.addNode(newNode2)
            // tree.addEdge(conflicts,newNode2,null)
            // openNodes.add(newNode2)
         }
     }
     return foundElement;
 }

 protected Set<Set<Id>> calculateNode(SimpleNode<Id> node) throws SolverException, InconsistentTheoryException, NoConflictException{
     return calculateConflict(node);
 }


private Id chooseSplitElement(Set<Set<Id>> conflicts){
    return null;
}
*/
}
