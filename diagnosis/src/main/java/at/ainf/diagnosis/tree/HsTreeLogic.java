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
public class HsTreeLogic<T extends AxiomSet<Id>, Id> implements TreeLogic<T, Id> {


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
            updateTree(conflictSet);
        }

    }

    public void updateTree(List<T> invalidHittingSets) throws SolverException, InconsistentTheoryException, NoConflictException {

        for (T ax : tree.getStorage().getConflictSets()) {
            Set<Id> axioms = tree.getSearcher().search(tree.getTheory(), ax, null);
            if (!axioms.equals(ax)) {
                AxiomSet<Id> conflict = AxiomSetFactory.createConflictSet(ax.getMeasure(), axioms, ax.getEntailments());
                updateTree(conflict);
                ax.updateAxioms(conflict);
            }
        }
    }

    private void updateTree(AxiomSet<Id> conflictSet) throws SolverException, InconsistentTheoryException {
        Node<Id> root = tree.getRoot();
        if (tree.getRoot() == null) {
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
            Set<Id> invalidAxioms = new LinkedHashSet<Id>(node.getAxiomSet());
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
        if (!tree.getOpenNodes().remove(idNode))
        {
            for (Node<Id> node : idNode.getChildren()) {
                removeChildren(node);
            }
        }
    }

    public void setTreeSearch(TreeSearch<T, Id> tree) {
        this.tree = tree;
    }
}
