package at.ainf.diagnosis.tree;

import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.AxiomSet;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 28.03.12
 * Time: 16:07
 * To change this template use File | Settings | File Templates.
 */
public class DualTreeLogic<T extends AxiomSet<Id>, Id> implements TreeLogic<T, Id> {

    private TreeSearch<T, Id> tree;

    public void proveValidnessConflict(T conflictSet) throws SolverException {
        boolean valid = true;
        if (tree.getTheory().hasTests()) {
//                getTheory().addBackgroundFormulas(pathLabels);
            valid = tree.getTheory().testDiagnosis(conflictSet);
            //              getTheory().removeBackgroundFormulas(pathLabels);
        }
        conflictSet.setValid(valid);
    }

    public boolean proveValidnessDiagnosis(Set<Id> diagnosis) throws SolverException {
        return true;
    }

    public void pruneConflictSets(Node<Id> idNode, T conflictSet) throws SolverException, InconsistentTheoryException {

    }

    public void updateTree(List<T> invalidAxiomSets) throws SolverException, InconsistentTheoryException, NoConflictException {
        if (invalidAxiomSets.isEmpty()) {
            return;
        }
        for (T ax : invalidAxiomSets) {
            tree.getStorage().removeConflictSet(ax);
        }
        Set<T> cs = tree.getStorage().getConflictSets();
        tree.getOpenNodes().clear();
        Node<Id> root = tree.getRoot();
        for (Node<Id> idNode : root.getChildren()) {
            idNode.removeParent();
        }
        root.removeChildren();
        if (!cs.isEmpty()) {
            root.setAxiomSet(Collections.max(cs, new Comparator<T>() {
                public int compare(T o1, T o2) {
                    return o1.compareTo(o2);
                }
            }));
            tree.addNodes(root.expandNode());
        }
        else
            root.setAxiomSet(null);

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
            if (node.getAxiomSet().equals(axSet)) {
                /*if (node.isRoot())
                    this.root = null;
                else{
                    Node<Id> parent = node.getParent();
                    parent.removeChild(node);
                    addNodes(parent.expandNode());
                }
                return Collections.emptySet();*/
                Set<Node<Id>> children = new LinkedHashSet<Node<Id>>(node.getChildren());
                for (Node<Id> cnode : children) {
                    if (tree.getOpenNodes().contains(cnode)) {
                        tree.getOpenNodes().remove(cnode);
                        node.removeChild(cnode);
                    } else {
                        // update conflicts
                        /*
                        if (cnode.isClosed())
                        {                            
                            Set<Id> pathLabels = cnode.getPathLabels();
                            for (T hs : getStorage().getHittingSets())
                            {
                                if (hs.containsAll(pathLabels)){
                                    Set<Id> axioms = new LinkedHashSet<Id>(hs);
                                    axioms.remove(cnode.getArcLabel());
                                    hs.updateAxioms(axioms);
                                }
                            }
                        }
                        getTheory().addPositiveTest(cnode.getArcLabel());
                        cnode.removeArcLabel();
                        */
                    }
                }
                node.removeAxioms();
            } else
                node.setAxiomSet(axSet);
        }
        return node.getChildren();
    }

    public void setTreeSearch(TreeSearch<T, Id> tree) {
        this.tree = tree;
    }


}
