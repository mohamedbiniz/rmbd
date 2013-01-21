package at.ainf.diagnosis;

import at.ainf.diagnosis.model.IKnowledgeBase;
import at.ainf.diagnosis.model.IReasoner;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.FormulaSet;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.locks.Lock;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 13.07.11
 * Time: 15:03
 * To change this template use File | Settings | File Templates.
 */
public interface Searchable<E> {

    public boolean verifyRequirements() throws SolverException;

    // manager

    public void doBayesUpdate(Set<? extends FormulaSet<E>> hittingSets);



    public boolean verifyConsistency() throws SolverException;

    public boolean supportEntailments();

    Set<E> getEntailments(Set<E> hittingSet) throws SolverException;

    boolean isEntailed(Set<E> n);

    // ?

    public void reset();

    // split method in O \ D to manager and verifyCons here

    boolean diagnosisEntails(FormulaSet<E> hs, Set<E> ent);

    boolean diagnosisConsistent(FormulaSet<E> hs, Set<E> ent);

    // here the two methods should be unified

    public boolean areTestsConsistent() throws SolverException;

    public boolean testDiagnosis(Collection<E> diagnosis) throws SolverException;

    public IKnowledgeBase<E> getKnowledgeBase();

    public IReasoner<E> getReasoner();

    public void registerTestCases() throws SolverException, InconsistentTheoryException;

    public void unregisterTestCases() throws SolverException;

    public Searchable<E> copy() throws SolverException, InconsistentTheoryException;

    boolean isMultiThreading();

    void setLock(Lock lock);
}
