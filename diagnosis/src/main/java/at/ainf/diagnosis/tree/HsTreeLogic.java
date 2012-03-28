package at.ainf.diagnosis.tree;

import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.AxiomSetFactory;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 28.03.12
 * Time: 16:07
 * To change this template use File | Settings | File Templates.
 */
public class HsTreeLogic<T extends AxiomSet<Id>,Id> implements TreeLogic<T,Id> {


    private TreeSearch<T, Id> tree;


    public void proveValidnessConflict(T conflictSet) throws SolverException {

    }

    public boolean proveValidnessDiagnosis(Set<Id> diagnosis) throws SolverException {
        
        if (tree.getTheory().hasTests())
            return tree.getTheory().testDiagnosis(diagnosis);

        return true;
        
    }

    public void pruneConflictSets(Node<Id> idNode, T conflictSet) throws SolverException, InconsistentTheoryException {

        // DAG: verify if there is a conflict that is a subset of the new conflict
        Set<T> invalidConflicts = new LinkedHashSet<T>();
        for (T e : tree.getStorage().getConflictSets()) {
            if (e.containsAll(conflictSet) && e.size() > conflictSet.size())
                invalidConflicts.add(e);
        }

        if (!invalidConflicts.isEmpty()) {
            for (T invalidConflict : invalidConflicts) {

                tree.getStorage().removeConflictSet(invalidConflict);
            }
            tree.updateTree(conflictSet);
        }
        
    }

    public void updateHsTree(List<T> invalidHittingSets) throws SolverException, InconsistentTheoryException, NoConflictException {

        for (T ax : tree.getStorage().getConflictSets()) {
            Set<Id> axioms = tree.getSearcher().search(tree.getTheory(), ax, null);
            if (!axioms.equals(ax)) {
                AxiomSet<Id> conflict = AxiomSetFactory.createConflictSet(ax.getMeasure(), axioms, ax.getEntailments());
                tree.updateTree(conflict);
                ax.updateAxioms(conflict);
            }

        }
        
    }

    protected boolean containsOneOf(Set<Id> pathLabels, Set<Id> temp) {
        for (Id t : temp) {
            if (pathLabels.contains(t)) {
                //System.out.println("Contains!");
                return true;
            }
        }
        return false;
    }

    private boolean hasParent(Node<Id> node, Node<Id> parent) {
        if (parent.equals(node))
            return true;
        else if (parent.isRoot())
            return false;
        return hasParent(node, parent.getParent());
    }
    
    public Set<Node<Id>> updateNode(AxiomSet<Id> axSet, Node<Id> node) throws SolverException, InconsistentTheoryException {
        if (node == null || node.getAxiomSet() == null)
            return Collections.emptySet();
        if (node.getAxiomSet().containsAll(axSet)) {
            Set<Id> invalidAxioms = new LinkedHashSet<Id>(node.getAxiomSet());
            //if (!getSearcher().isDual())
            invalidAxioms.removeAll(axSet);
            for (Iterator<Node<Id>> onodeit = tree.getOpenNodes().iterator(); onodeit.hasNext(); ) {
                Node<Id> openNode = onodeit.next();
                if (!openNode.isRoot() && hasParent(node, openNode.getParent())
                        && containsOneOf(openNode.getPathLabels(), invalidAxioms))
                    onodeit.remove();
            }
            
            node.setConflict(axSet);
        }
        return node.getChildren();
    }

    public void setTreeSearch(TreeSearch<T,Id> tree) {
        this.tree = tree;
    }
}
