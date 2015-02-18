package at.ainf.owlapi3.performance;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.MultiQuickXplain;
import at.ainf.diagnosis.quickxplain.PredefinedConflictSearcher;
import at.ainf.diagnosis.quickxplain.QXAxiomSetListener;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.storage.FormulaSetImpl;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.SimpleCostsEstimator;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.model.OWLTheory;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: mjoszt
 * Date: 07.12.2014
 * Time: 10:15
 */

public class ConflictTreeTest extends OntologyTests {

    private int maximumNumberOfConflicts = 1;
    private static Logger logger = LoggerFactory.getLogger(ConflictTreeTest.class.getName());
    static OWLTheory theory;
    private OWLTheory origTheory;

    /**
     * Set of diagnoses found during search
     */
    private Set<Set<OWLLogicalAxiom>> foundDiagnoses;

//    static Set<Searcher<OWLLogicalAxiom>> searchers = new LinkedHashSet<Searcher<OWLLogicalAxiom>>();


    /**
     * Sets up theory and search
     * @return search
     * @throws SolverException
     * @throws InconsistentTheoryException
     * @throws NoConflictException
     * @param strOntologyFile
     */
    private TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> testSetup(String strOntologyFile) throws SolverException, InconsistentTheoryException, NoConflictException {
        //create theory
        OWLOntology ontology = getOntologySimple(strOntologyFile);
        theory = getSimpleTheory(ontology, false);
        origTheory = (OWLTheory) theory.copy();

        //setup search
        TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        //get target diagnosis
        search.setSearcher(new QuickXplain<OWLLogicalAxiom>());
        search.setSearchable(theory);
        search.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory));

        //set for all found diagnoses during search
        foundDiagnoses = new LinkedHashSet<Set<OWLLogicalAxiom>>();

        return search;
    }

    @Ignore
    @Test
    public void testCompareSearchMethods() throws SolverException, InconsistentTheoryException, NoConflictException {

        TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = testSetup("ontologies/ecai2010.owl");
        Set<? extends FormulaSet<OWLLogicalAxiom>> diagnoses = getDiagnoses(search);

        Map<QSSType, DurationStat> nTimes = new HashMap<QSSType, DurationStat>();
        Map<QSSType, List<Double>> nQueries = new HashMap<QSSType, List<Double>>();
        Map<QSSType, DurationStat> ctTimes = new HashMap<QSSType, DurationStat>();
        Map<QSSType, List<Double>> ctQueries = new HashMap<QSSType, List<Double>>();

        for (QSSType type : QSSType.values()) { //run for each scoring function
            logger.info("QSSType: " + type);

            //run normal simulated session
            logger.info("NormalSimulatedSession\n");
            nTimes.put(type, new DurationStat());
            nQueries.put(type, new LinkedList<Double>());
            for (FormulaSet<OWLLogicalAxiom> targetDiagnosis : diagnoses) { //run for each possible target diagnosis
                long completeTime = System.currentTimeMillis();

                long time = computeHS(search, theory, targetDiagnosis, nQueries.get(type), type);
                theory.getKnowledgeBase().removeFormulas(targetDiagnosis);

                completeTime = System.currentTimeMillis() - completeTime;
                nTimes.get(type).add(completeTime);

                theory.getKnowledgeBase().addFormulas(origTheory.getKnowledgeBase().getKnowledgeBase());
            }
            logStatistics(nQueries, nTimes, type);
            // end (run normal simulated session)

            //run conflict tree simulated session
            logger.info("ConflictTreeSimulatedSession\n");
            ctTimes.put(type, new DurationStat());
            ctQueries.put(type, new LinkedList<Double>());
            for (FormulaSet<OWLLogicalAxiom> targetDiagnosis : diagnoses) { //run for each possible target diagnosis

            }
        }
    }

    /**
     * Logs the time and the number of needed queries for a specific QSS type
     * @param queries
     * @param times
     * @param type
     */
    private void logStatistics(Map<QSSType, List<Double>> queries, Map<QSSType, DurationStat> times, QSSType type){
        List<Double> queriesOfType = queries.get(type);
        double res = 0;
        for (Double qs : queriesOfType) {
            res += qs;
        }
        logger.info("needed normal " + type + " " + getStringTime(times.get(type).getOverall()) +
                        " max " + getStringTime(times.get(type).getMax()) +
                        " min " + getStringTime(times.get(type).getMin()) +
                        " avg2 " + getStringTime(times.get(type).getMean()) +
                        " Queries max " + Collections.max(queries.get(type)) +
                        " min " + Collections.min(queries.get(type)) +
                        " avg2 " + res / queriesOfType.size()
        );

    }


    @Test
    public void testNormalSimulatedSession() throws SolverException, InconsistentTheoryException, NoConflictException, OWLOntologyCreationException {
        logger.info("NormalSimulatedSession\n");

        TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = testSetup("ontologies/ecai2010.owl");
        Set<? extends FormulaSet<OWLLogicalAxiom>> diagnoses = getDiagnoses(search);

        Map<QSSType, DurationStat> nTimes = new HashMap<QSSType, DurationStat>();
        Map<QSSType, List<Double>> nQueries = new HashMap<QSSType, List<Double>>();
        foundDiagnoses.clear();

        for (QSSType type : QSSType.values()) { //run for each scoring function
            logger.info("QSSType: " + type);

            nTimes.put(type, new DurationStat());
            nQueries.put(type, new LinkedList<Double>());
            for (FormulaSet<OWLLogicalAxiom> targetDiagnosis : diagnoses) { //run for each possible target diagnosis
                logger.info("\ntargetD: " + targetDiagnosis.toString() + "\n");
                long completeTime = System.currentTimeMillis();

                long time = computeHS(search, theory, targetDiagnosis, nQueries.get(type), type);
                theory.getKnowledgeBase().removeFormulas(targetDiagnosis);

                completeTime = System.currentTimeMillis() - completeTime;
                nTimes.get(type).add(completeTime);
                assertTrue(theory.verifyConsistency());

                theory.getKnowledgeBase().addFormulas(origTheory.getKnowledgeBase().getKnowledgeBase());
            }
            logStatistics(nQueries, nTimes, type);
        }
    }


    @Test
    public void testConflictTreeSimulatedSession() throws SolverException, InconsistentTheoryException, OWLOntologyCreationException, NoConflictException {
        logger.info("ConflictTreeSimulatedSession\n");

        TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = testSetup("ontologies/ecai2010.owl");
        Set<? extends FormulaSet<OWLLogicalAxiom>> diagnoses = getDiagnoses(search);

        Map<QSSType, DurationStat> ctTimes = new HashMap<QSSType, DurationStat>();
        Map<QSSType, List<Double>> ctQueries = new HashMap<QSSType, List<Double>>();
        foundDiagnoses.clear();

        for (QSSType type : QSSType.values()) { //run for each scoring function
            logger.info("QSSType: " + type);

            ctTimes.put(type, new DurationStat());
            ctQueries.put(type, new LinkedList<Double>());
            for (FormulaSet<OWLLogicalAxiom> targetDiagnosis : diagnoses) {
                logger.info("\ntargetD: " + targetDiagnosis.toString() + "\n");

                ConflictTreeSession conflictTreeSearch = new ConflictTreeSession(this, theory, search);
                long time = conflictTreeSearch.search(targetDiagnosis, ctQueries, type);

                //todo mit normalsimulatedsession abgleichen und zeitmessung anppassen
                foundDiagnoses.addAll(conflictTreeSearch.getDiagnosis());
                logger.info("search terminated, time: " + time + ", number of found diagnoses: " + foundDiagnoses.size() + ", found diagnoses: " + foundDiagnoses.toString());
                assertTrue(theory.verifyConsistency());
                theory.getKnowledgeBase().addFormulas(origTheory.getKnowledgeBase().getKnowledgeBase());
            }
        }
        logger.info(foundDiagnoses.toString());
    }


    private Set<? extends FormulaSet<OWLLogicalAxiom>> getDiagnoses(TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search) throws SolverException, InconsistentTheoryException, NoConflictException {
        //cost estimator
        SimpleCostsEstimator<OWLLogicalAxiom> es = new SimpleCostsEstimator<OWLLogicalAxiom>();
        search.setCostsEstimator(es);

        //searching diagnoses
        search.start();
        Set<? extends FormulaSet<OWLLogicalAxiom>> diagnoses = search.getDiagnoses();

        //resets history
        theory.getKnowledgeBase().clearTestCases();
        search.reset();

        return diagnoses;
    }
}