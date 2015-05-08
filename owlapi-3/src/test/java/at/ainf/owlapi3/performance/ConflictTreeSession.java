package at.ainf.owlapi3.performance;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.KnowledgeBase;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.MultiQuickXplain;
import at.ainf.diagnosis.quickxplain.PredefinedConflictSearcher;
import at.ainf.diagnosis.quickxplain.QXAxiomSetListener;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.storage.FormulaSetImpl;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.base.CalculateDiagnoses;
import at.ainf.owlapi3.base.SimulatedSession;
import at.ainf.owlapi3.model.OWLTheory;
import com.sun.xml.internal.bind.v2.util.TypeCast;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Created by mjoszt on 16.02.2015.
 */
public class ConflictTreeSession {

    private static Logger logger = LoggerFactory.getLogger(ConflictTreeSession.class.getName());

    private ConflictTreeTest caller;
    private int maximumNumberOfConflicts = 1;
    private Set<OWLLogicalAxiom> diagnosis;
    private OWLTheory theory;
    private OWLTheory queryAnswerTheory = null;
    private TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search;
    private boolean isShortLogging = false;
    private String outputString = "";
    private String messageString = "";
    /**
     * searcher for the tree search
     */
    private PredefinedConflictSearcher<OWLLogicalAxiom> searcher;
    /**
     * searcher to find conflicts ahead
     */
    private MultiQuickXplain<OWLLogicalAxiom> conflictsSearcher;


    public ConflictTreeSession(ConflictTreeTest caller, OWLTheory theory, TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search) {
        this.caller = caller;
        this.theory = theory;
        this.search = search;

        //create conflictsSearcher
        conflictsSearcher = new MultiQuickXplain<OWLLogicalAxiom>();
        conflictsSearcher.setAxiomListener(new QXAxiomSetListener<OWLLogicalAxiom>(true));

        //create searcher
        searcher = new PredefinedConflictSearcher<OWLLogicalAxiom>(null);
        searcher.setIsBHS(false);

        diagnosis = new LinkedHashSet<OWLLogicalAxiom>();
    }

    public long search(FormulaSet<OWLLogicalAxiom> targetDiagnosis, Map<SimulatedSession.QSSType, List<Double>> queries, SimulatedSession.QSSType type, boolean isShortLogging)
            throws SolverException, InconsistentTheoryException, OWLOntologyCreationException {
        this.isShortLogging = isShortLogging;
        return search(targetDiagnosis, queries, type);
    }

    public long search(FormulaSet<OWLLogicalAxiom> targetDiagnosis, Map<SimulatedSession.QSSType, List<Double>> queries, SimulatedSession.QSSType type)
            throws SolverException, InconsistentTheoryException, OWLOntologyCreationException {

        Set<OWLLogicalAxiom> partialDiagnoses = new LinkedHashSet<OWLLogicalAxiom>();

        queryAnswerTheory = getQueryAnswerTheory(targetDiagnosis);
        boolean conflictExists = true;
        long completeTime = System.currentTimeMillis();
        while (conflictExists) {
            Set<FormulaSet<OWLLogicalAxiom>> conflicts = null;
            try {
                //calculate n conflicts
                logger.info("maximum number of conflicts: " + maximumNumberOfConflicts);
                conflicts = computeNConflictsAtTime(conflictsSearcher, theory);
                logger.info("number of calculated conflicts: " + conflicts.size());
                int i = 0;
                for (Set<OWLLogicalAxiom> conflict : conflicts) {
                    i++;
                    logger.info("found conflict " + i + ": " + CalculateDiagnoses.renderAxioms(conflict));
                }
            } catch (NoConflictException e) {
                logger.info("no more conflicts");
                break;
            }
//            logger.info("maxNumberOfConflicts: " + maximumNumberOfConflicts + ", numberReturnedConflicts: " + conflicts.size());
//            assertTrue(maximumNumberOfConflicts >= conflicts.size());
//            logger.info("\nconflict: " + conflicts.toString() + "\n");

            //create KB' from conflicts
            OWLTheory theoryPrime = getTheoryFromConflicts(conflicts, theory);

            //setup for simulated session
            searcher.setConflictSets(conflicts);
            search.setSearcher(searcher);
            search.setSearchable(theoryPrime);

            long getTargetDiagTime = System.currentTimeMillis();
            FormulaSet<OWLLogicalAxiom> reducedTargetDiagnosis = getIntersectingDiagnosisAxiom(targetDiagnosis, conflicts);
            logger.info("reducedTargetDiagnosis: " + CalculateDiagnoses.renderAxioms(reducedTargetDiagnosis) + "\n");
            getTargetDiagTime = System.currentTimeMillis() - getTargetDiagTime;
            //remove time needed to get target diagnosis by increasing the start time
            completeTime += getTargetDiagTime;

            if (isShortLogging) {
                outputString = "," + type + ",";
                outputString += caller.computeHSShortLog(search, theoryPrime, reducedTargetDiagnosis, queries.get(type), type, messageString, queryAnswerTheory);
            } else {
                caller.computeHS(search, theoryPrime, reducedTargetDiagnosis, queries.get(type), type, queryAnswerTheory);
            }

            //if test doesn't fail, the found diagnosis is equal the targetDiagnosis
            partialDiagnoses.addAll(reducedTargetDiagnosis);

            //save changed P' and N' in theory and remove target diagnosis from theory
            copyPositiveAndNegativeTests(theory, theoryPrime);
            theory.getKnowledgeBase().removeFormulas(reducedTargetDiagnosis);
        }
        completeTime = System.currentTimeMillis() - completeTime;
        diagnosis.addAll(partialDiagnoses);
        return completeTime;
    }


    private OWLTheory getQueryAnswerTheory(Set<OWLLogicalAxiom> axiomsToRemove) throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {
        Set<OWLAxiom> axiomSet = new HashSet<OWLAxiom>();
        axiomSet.addAll(theory.getKnowledgeBase().getKnowledgeBase());
        if (!axiomSet.removeAll(axiomsToRemove)) {
            return null;
        }
        OWLTheory newTheory = theory.copyChangedTheory(axiomSet);
        copyPositiveAndNegativeTests(newTheory, theory); //TODO check if needed

        newTheory.getReasoner().addFormulasToCache(newTheory.getKnowledgeBase().getFaultyFormulas());
        boolean consistent = newTheory.verifyConsistency();
        newTheory.reset();
        logger.info("queryAnswerTheory is consistent: " + consistent);
        return newTheory;
    }

    /**
     * Creates a copy of the complete theory, that only contains the axioms of the conflicts in the knowledge base
     *
     * @param conflicts
     * @param completeTheory
     * @return
     * @throws OWLOntologyCreationException
     * @throws SolverException
     * @throws InconsistentTheoryException
     */
    private OWLTheory getTheoryFromConflicts(Set<FormulaSet<OWLLogicalAxiom>> conflicts, OWLTheory completeTheory) throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {
        Set<OWLAxiom> axiomSet = new HashSet<OWLAxiom>();
        Iterator<FormulaSet<OWLLogicalAxiom>> iterator = conflicts.iterator();
        while (iterator.hasNext()) {
            axiomSet.addAll(iterator.next());
        }

        OWLTheory theory = completeTheory.copyChangedTheory(axiomSet);
        copyPositiveAndNegativeTests(theory, completeTheory);

        return theory;
    }

    /**
     * Copies the positive, negative, entailed and non entailed tests from the knowledge base of the source theory
     * to the target theory
     *
     * @param targetTheory the target theory
     * @param sourceTheory the source theory
     */
    public void copyPositiveAndNegativeTests(OWLTheory targetTheory, OWLTheory sourceTheory) {
        Iterator<Set<OWLLogicalAxiom>> iterator = sourceTheory.getKnowledgeBase().getPositiveTests().iterator();
        while (iterator.hasNext()) {
            targetTheory.getKnowledgeBase().addPositiveTest(iterator.next());
        }
        iterator = sourceTheory.getKnowledgeBase().getNegativeTests().iterator();
        while (iterator.hasNext()) {
            targetTheory.getKnowledgeBase().addNegativeTest(iterator.next());
        }

        iterator = sourceTheory.getKnowledgeBase().getEntailedTests().iterator();
        while (iterator.hasNext()) {
            targetTheory.getKnowledgeBase().addEntailedTest(iterator.next());
        }
        iterator = sourceTheory.getKnowledgeBase().getNonentailedTests().iterator();
        while (iterator.hasNext()) {
            targetTheory.getKnowledgeBase().addNonEntailedTest(iterator.next());
        }
    }

    /**
     * searches for n conflict and returns them
     *
     * @param MultiQuickXplain<OWLLogicalAxiom> conflictsSearcher
     * @param OWLTheory                         theory
     * @return Set<FormulaSet<OWLLogicalAxiom>> conflicts
     * @throws InconsistentTheoryException
     * @throws SolverException
     * @throws NoConflictException
     */
    public Set<FormulaSet<OWLLogicalAxiom>> computeNConflictsAtTime(MultiQuickXplain<OWLLogicalAxiom> conflictsSearcher, OWLTheory theory) throws InconsistentTheoryException, SolverException, NoConflictException {
        List<OWLLogicalAxiom> list = new LinkedList<OWLLogicalAxiom>();
        list.addAll(theory.getKnowledgeBase().getFaultyFormulas());

        //set maxConflict to 1 to get only one conflict
        conflictsSearcher.setMaxConflictSetCount(maximumNumberOfConflicts);
        Set<FormulaSet<OWLLogicalAxiom>> conflicts = conflictsSearcher.search(theory, list);
        return conflicts;
    }

    /**
     * Returns a formula set of the axioms, that are present in the conflict sets
     *
     * @param diagnosis
     * @param conflicts conflict sets
     * @return intersecting axioms FormulaSet<OWLLogicalAxiom>
     */
    private FormulaSet<OWLLogicalAxiom> getIntersectingDiagnosisAxiom(FormulaSet<OWLLogicalAxiom> diagnosis, Set<FormulaSet<OWLLogicalAxiom>> conflicts) {
        Set<OWLLogicalAxiom> axiomSet = new HashSet<OWLLogicalAxiom>();
        if (diagnosis == null || conflicts == null)
            return null;

        for (OWLLogicalAxiom diagAxiom : diagnosis) {
            for (FormulaSet<OWLLogicalAxiom> conflict : conflicts) {
                if (conflict.contains(diagAxiom))
                    axiomSet.add(diagAxiom);
            }
        }
        FormulaSet<OWLLogicalAxiom> reducedDiagnosis = new FormulaSetImpl<OWLLogicalAxiom>(diagnosis.getMeasure(), axiomSet, null);
        return reducedDiagnosis;
    }


    public static void setLogger(Logger logger) {
        ConflictTreeSession.logger = logger;
    }

    public int getMaximumNumberOfConflicts() {
        return maximumNumberOfConflicts;
    }

    public void setMaximumNumberOfConflicts(int maximumNumberOfConflicts) {
        this.maximumNumberOfConflicts = maximumNumberOfConflicts;
    }

    public Set<OWLLogicalAxiom> getDiagnosis() {
        return diagnosis;
    }

    public void setOutputString(String outputString) {
        this.outputString = outputString;
    }

    public String getOutputString() {
        return outputString;
    }

    public void setMessageString(String messageString) {
        this.messageString = messageString;
    }

    public String getMessageString() {
        return messageString;
    }


}
