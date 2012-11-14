package at.ainf.diagnosis.tree;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;

import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 13.11.12
 * Time: 17:01
 * To change this template use File | Settings | File Templates.
 */
public class BinaryTreeSearch<T extends AxiomSet<Id>,Id> extends AbstractTreeSearch<T,Id> implements TreeSearch<T,Id> {
    @Override
    protected void updateTree(List<T> invalidHittingSets) throws SolverException, InconsistentTheoryException, NoConflictException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void pruneConflictSets(Node<Id> node, T conflictSet) throws SolverException, InconsistentTheoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void proveValidnessConflict(T conflictSet) throws SolverException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<T> getConflicts() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<T> getDiagnoses() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
