package at.ainf.diagnosis.tree;

import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.AxiomSet;

import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 28.03.12
 * Time: 16:03
 * To change this template use File | Settings | File Templates.
 */
public interface TreeLogic<T extends AxiomSet<Id>,Id> {
    
    public void setTreeSearch(TreeSearch<T,Id> tree);

    public void proveValidnessConflict(T conflictSet) throws SolverException;

    public boolean proveValidnessDiagnosis(Set<Id> diagnosis) throws SolverException;

    public void pruneConflictSets(Node<Id> node, T conflictSet) throws SolverException, InconsistentTheoryException;

    public void updateTree(List<T> invalidHittingSets) throws SolverException, InconsistentTheoryException, NoConflictException;

    public Set<Node<Id>> updateNode(AxiomSet<Id> axSet, Node<Id> node) throws SolverException, InconsistentTheoryException;
    
}
