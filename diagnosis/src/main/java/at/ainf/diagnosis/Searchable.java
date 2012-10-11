package at.ainf.diagnosis;

import at.ainf.diagnosis.model.FaultyAxiomsManager;
import at.ainf.diagnosis.model.IKnowledgeBase;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.AxiomSet;

import java.util.Collection;
import java.util.Set;

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

    public void doBayesUpdate(Set<? extends AxiomSet<E>> hittingSets);



    public boolean verifyConsistency() throws SolverException;

    public boolean supportEntailments();

    Set<E> getEntailments(Set<E> hittingSet) throws SolverException;

    boolean isEntailed(Set<E> n);

    // ?

    public void reset();

    // split method in O \ D to manager and verifyCons here

    boolean diagnosisEntails(AxiomSet<E> hs, Set<E> ent);

    boolean diagnosisConsistent(AxiomSet<E> hs, Set<E> ent);

    // here the two methods should be unified

    public boolean areTestsConsistent() throws SolverException;

    public boolean testDiagnosis(Collection<E> diagnosis) throws SolverException;


    public void registerTestCases() throws SolverException, InconsistentTheoryException;

    public void unregisterTestCases() throws SolverException;

    public IKnowledgeBase<E> getKnowledgeBase();

    public FaultyAxiomsManager<E> getFaultyAxiomsManager();
}
