package at.ainf.diagnosis.tree;

import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.Storage;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 12.06.12
 * Time: 11:31
 * To change this template use File | Settings | File Templates.
 */
public class InvHsTreeSearch<T extends AxiomSet<Id>,Id> extends AbstractTreeSearch<T,Id> implements TreeSearch<T,Id>{

    public InvHsTreeSearch(Storage<T, Id> tIdStorage) {
        super(tIdStorage);
        //setLogic(new DualTreeLogic<T, Id>());
    }

    public void proveValidnessConflict(T conflictSet) throws SolverException {
        boolean valid = true;
        if (getTheory().hasTests()) {
//                getTheory().addBackgroundFormulas(pathLabels);
            valid = getTheory().testDiagnosis(conflictSet);
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
            //ArrayList<Id> formulas = new ArrayList<Id>(tree.getTheory().getActiveFormulas());
            //Set<Id> axioms = tree.getSearcher().search(tree.getTheory(), formulas, ax, null);
            //if (axioms.equals(ax))
            getStorage().removeNodeLabel(ax);
        }
        Set<T> cs = getConflicts();
        getSearchStrategy().getOpenNodes().clear();
        Node<Id> root = getRoot();
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
            getSearchStrategy().addNodes(root.expandNode());
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

    public Set<T> getDiagnoses() {
        return getValidAxiomSets(copy(getStorage().getNodeLabels()));
    }

    public Set<T> getConflicts() {
        return getValidAxiomSets(copy(getStorage().getHittingSets()));
    }

    public Set<Node<Id>> updateNode(AxiomSet<Id> axSet, Node<Id> node) throws SolverException, InconsistentTheoryException {
        if (node == null || node.getAxiomSet() == null)
            return Collections.emptySet();
        if (node.getAxiomSet().containsAll(axSet)) {
            Set<Id> invalidAxioms = new LinkedHashSet<Id>(node.getAxiomSet());
            //if (!getSearcher().isDual())
            invalidAxioms.removeAll(axSet);
            for (Iterator<Node<Id>> onodeit = getSearchStrategy().getOpenNodes().iterator(); onodeit.hasNext(); ) {
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
                    if (getSearchStrategy().getOpenNodes().contains(cnode)) {
                        getSearchStrategy().getOpenNodes().remove(cnode);
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



}
