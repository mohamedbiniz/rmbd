package at.ainf.diagnosis.tree;

import  at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 12.06.12
 * Time: 11:31
 * To change this template use File | Settings | File Templates.
 */
public class InvHsTreeSearch<T extends FormulaSet<Id>,Id> extends AbstractTreeSearch<T,Id> implements TreeSearch<T,Id>{



    public void proveValidnessConflict(T conflictSet) throws SolverException {
        boolean valid = true;
        if (getSearchable().getKnowledgeBase().hasTests()) {
//                getSearchable().addCheckedBackgroundFormulas(pathLabels);
            valid = getSearchable().testDiagnosis(conflictSet);
            //              getSearchable().removeBackgroundFormulas(pathLabels);
        }
        conflictSet.setValid(valid);
    }

    public boolean proveValidnessDiagnosis(Set<Id> diagnosis) throws SolverException {
        return true;
    }

    public void pruneConflictSets(Node<Id> idNode, T conflictSet) throws SolverException, InconsistentTheoryException {

    }

    protected Set<Id> calculateEntailmentsForConflictSet(FormulaSet<Id> quickConflict) throws SolverException {
        Set<Id> entailments = Collections.emptySet();
        if (getSearchable().supportEntailments())
            entailments = getSearchable().getEntailments(quickConflict);
        if (entailments==null)
            entailments = Collections.emptySet();
        return entailments;
    }

    protected Set<Id> calculateEntailmentsForHittingSet(Set<Id> labels, boolean valid) throws SolverException {

        return Collections.emptySet();
    }

    public void updateTree(List<T> invalidAxiomSets) throws SolverException, InconsistentTheoryException, NoConflictException {
        if (invalidAxiomSets.isEmpty()) {
            return;
        }
        for (T ax : invalidAxiomSets) {
            //ArrayList<Id> formulas = new ArrayList<Id>(tree.getSearchable().getFaultyFormulas());
            //Set<Id> axioms = tree.getSearcher().start(tree.getSearchable(), formulas, ax, null);
            //if (axioms.equals(ax))
            removeNodeLabel(ax);
        }
        Set<T> cs = getConflicts();
        getSearchStrategy().getOpenNodes().clear();
        Node<Id> root = getRoot();
        for (Node<Id> idNode : root.getChildren()) {
            idNode.removeParent();
        }
        root.removeChildren();
        if (!cs.isEmpty()) {
            //UNBEDINGT AUSBESSERN
           /** root.setAxiomSet(Collections.max(cs, new Comparator<T>() {
                public int compare(T o1, T o2) {
                    return o1.compareTo(o2);
                }
            }));       **/
            getSearchStrategy().addNodes(root.expandNode());
        }
        else
            root.setAxiomSet((LinkedHashSet<Id>)null);

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
        return getValidAxiomSets(copy(getNodeLabels()));
    }

    public Set<T> getConflicts() {
        return getValidAxiomSets(copy(getHittingSets()));
    }

    public Set<Node<Id>> updateNode(FormulaSet<Id> axSet, HSTreeNode<Id> node) throws SolverException, InconsistentTheoryException {
        if (node == null || node.getAxiomSets() == null)
            return Collections.emptySet();
        if (node.getAxiomSets().containsAll(axSet)) {
            //NEU iterator
            Set<Id> invalidAxioms = new LinkedHashSet<Id>(node.getAxiomSets().iterator().next());
            //if (!getSearcher().isDual())
            invalidAxioms.removeAll(axSet);
            for (Iterator<Node<Id>> onodeit = getSearchStrategy().getOpenNodes().iterator(); onodeit.hasNext(); ) {
                Node<Id> openNode = onodeit.next();
                if (!openNode.isRoot() && hasParent(node, openNode.getParent())
                        && containsOneOf(openNode.getPathLabels(), invalidAxioms))
                    onodeit.remove();
            }
            if (node.getAxiomSets().equals(axSet)) {
                /*if (node.isRoot())
                    this.root = null;
                else{
                    SimpleNode<Id> parent = node.getParent();
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
                            for (T hs : getStorage().getDiagnoses())
                            {
                                if (hs.containsAll(pathLabels)){
                                    Set<Id> axioms = new LinkedHashSet<Id>(hs);
                                    axioms.remove(cnode.getArcLabel());
                                    hs.updateAxioms(axioms);
                                }
                            }
                        }
                        getSearchable().addPositiveTest(cnode.getArcLabel());
                        cnode.removeArcLabel();
                        */
                    }
                }
                node.removeAxioms();
            } else
                node.setAxiomSet(new LinkedHashSet<Id>(axSet));
        }
        return node.getChildren();
    }

    protected Set<Set<Id>> calculateNode(Node<Id> node) throws SolverException, InconsistentTheoryException, NoConflictException{
        return calculateConflict(node);
    }

}
