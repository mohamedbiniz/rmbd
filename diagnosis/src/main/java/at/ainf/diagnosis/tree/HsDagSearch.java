package at.ainf.diagnosis.tree;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.storage.AxiomSetFactory;
import at.ainf.diagnosis.storage.AxiomSetImpl;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 12.06.12
 * Time: 11:31
 * To change this template use File | Settings | File Templates.
 */
public class HsDagSearch<T extends AxiomSet<Id>,Id> extends HsTreeSearch<T,Id> implements TreeSearch<T,Id>{


    public void proveValidnessConflict(T conflictSet) throws SolverException {

    }

    public boolean proveValidnessDiagnosis(Set<Id> diagnosis) throws SolverException {

        if (getSearchable().getKnowledgeBase().hasTests())
            return getSearchable().testDiagnosis(diagnosis);

        return true;

    }

    public Set<T> getDiagnoses() {
        return getValidAxiomSets(copy(getHittingSets()));
    }

    public Set<T> getConflicts() {
        return getValidAxiomSets(copy(getNodeLabels()));
    }


    protected Set<AxiomSet<Id>> calculateNode(Node<Id> node) throws SolverException, InconsistentTheoryException, NoConflictException{
        return calculateConflict(node);
    }

    public void pruneConflictSets(Node<Id> idNode, T conflictSet) throws SolverException, InconsistentTheoryException {

        // DAG: verify if there is a conflict that is a subset of the new conflict
        Set<T> invalidConflicts = new LinkedHashSet<T>();
        for (T e : getConflicts()) {
            if (e.containsAll(conflictSet) && e.size() > conflictSet.size())
                invalidConflicts.add(e);
        }

        if (!invalidConflicts.isEmpty()) {
            for (T invalidConflict : invalidConflicts) {

                removeNodeLabel(invalidConflict);
            }
            updateTree(conflictSet);
        }

    }

    public void updateTree(List<T> invalidHittingSets) throws SolverException, InconsistentTheoryException, NoConflictException {

        for (T ax : getConflicts()) {
            Set<Id> axioms = getSearcher().search(getSearchable(), ax, null).iterator().next();
            if (!axioms.equals(ax)) {
                AxiomSet<Id> conflict = new AxiomSetImpl<Id>(ax.getMeasure(), axioms, ax.getEntailments());
                updateTree(conflict);
                ax.updateAxioms(conflict);
            }
        }


        for (T invalidHittingSet : invalidHittingSets) {
            Node<Id> node = (Node<Id>) invalidHittingSet.getNode();
            if (node.isRoot())
                throw new IllegalStateException("Impossible source of a hitting set");
            if (isConnectedToRoot(node)) {
                node.setOpen();
                getSearchStrategy().pushOpenNode(node);
                removeHittingSet(invalidHittingSet);
            }
        }

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

}
