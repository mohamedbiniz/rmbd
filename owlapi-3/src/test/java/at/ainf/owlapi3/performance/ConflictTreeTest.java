package at.ainf.owlapi3.performance;

import at.ainf.diagnosis.Searcher;
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
import at.ainf.owlapi3.model.OWLTheory;
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


    static Set<Searcher<OWLLogicalAxiom>> searchers = new LinkedHashSet<Searcher<OWLLogicalAxiom>>();


    @Test
    /**
     * Tests if SimulatedSession works right with this example
     */
    public void SimulatedSessionTest() throws SolverException, InconsistentTheoryException, NoConflictException, OWLOntologyCreationException {
        logger.info("SimulatedSessionTest");

        //create theory
        theory = getSimpleTheory(getOntologySimple("ontologies/ecai2010.owl"),false);
        origTheory = (OWLTheory) theory.copy();

        TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        search.setSearcher(new QuickXplain<OWLLogicalAxiom>());
        search.setSearchable(theory);

        Set<? extends FormulaSet<OWLLogicalAxiom>> diags = getDiagnoses(search);

        //run for each possible target diagnosis
        for (FormulaSet<OWLLogicalAxiom> targetDiagnosis : diags) {

            QSSType type = QSSType.SPLITINHALF;
            logger.info("QSSType: " + type);
            //Map<QSSType, DurationStat> ntimes = new HashMap<QSSType, DurationStat>();
            Map<QSSType, List<Double>> nqueries = new HashMap<QSSType, List<Double>>();
            nqueries.put(type, new LinkedList<Double>());

            long completeTime = System.currentTimeMillis();
            long time = computeHS(search, theory, targetDiagnosis, nqueries.get(type), type);
            theory.getKnowledgeBase().removeFormulas(targetDiagnosis);

            completeTime = System.currentTimeMillis() - completeTime;
            logger.info("time: " + time + ", completeTime: " + completeTime);

            theory.getKnowledgeBase().addFormulas(origTheory.getKnowledgeBase().getKnowledgeBase());
        }
    }


    @Test
    public void ConflictTreeTest() throws SolverException, InconsistentTheoryException, OWLOntologyCreationException, NoConflictException {
        logger.info("ConflictTreeTest");

        //set for all found diagnoses during search
        Set<Set<OWLLogicalAxiom>> foundDiagnoses = new LinkedHashSet<Set<OWLLogicalAxiom>>();

        //create theory
        OWLOntology ontology = getOntologySimple("ontologies/ecai2010.owl");
        theory = getSimpleTheory(ontology, false);
        origTheory = (OWLTheory) theory.copy();

        //setup search
        TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        //get target diagnosis
        search.setSearcher(new QuickXplain<OWLLogicalAxiom>());
        search.setSearchable(theory);
        Set<? extends FormulaSet<OWLLogicalAxiom>> diags = getDiagnoses(search);

        //run for each possible target diagnosis
        for (FormulaSet<OWLLogicalAxiom> targetDiagnosis : diags) {
            logger.info("\ntargetD: " + targetDiagnosis.toString() + "\n");

            long time = runConflictTreeSearch(targetDiagnosis, search, foundDiagnoses);

            logger.info("search terminated, time: " + time + ", number of found diagnoses: " + foundDiagnoses.size() + ", found diagnoses: " + foundDiagnoses.toString() );
            assertTrue(theory.verifyConsistency());
            theory.getKnowledgeBase().addFormulas(origTheory.getKnowledgeBase().getKnowledgeBase());
        }

    }

    private long runConflictTreeSearch(FormulaSet<OWLLogicalAxiom> targetDiagnosis, TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search, Set<Set<OWLLogicalAxiom>> foundDiagnoses) throws SolverException, InconsistentTheoryException, OWLOntologyCreationException {
        //create quick
        MultiQuickXplain<OWLLogicalAxiom> quick = new MultiQuickXplain<OWLLogicalAxiom>();
        quick.setAxiomListener(new QXAxiomSetListener<OWLLogicalAxiom>(true));

        //create searcher
        PredefinedConflictSearcher<OWLLogicalAxiom> conflictSearcher = new PredefinedConflictSearcher<OWLLogicalAxiom>(null);
        conflictSearcher.setIsBHS(false);

        //set query selection type
        QSSType type = QSSType.SPLITINHALF;
        logger.info("QSSType: " + type);
        Map<QSSType, List<Double>> nqueries = new HashMap<QSSType, List<Double>>();
        nqueries.put(type, new LinkedList<Double>());

        boolean conflictExists = true;
        Set<OWLLogicalAxiom> partialDiagnoses = new LinkedHashSet<OWLLogicalAxiom>();
        long time = System.currentTimeMillis();
        while(conflictExists) {
            maximumNumberOfConflicts = 1;
            Set<FormulaSet<OWLLogicalAxiom>> conflicts = null;
            try {
                //calculate n conflicts
                conflicts = computeNConflictsAtTime(quick, theory, maximumNumberOfConflicts);
            } catch (NoConflictException e) {
                conflictExists = false;
                logger.info("no more conflicts");
                break;
            }
            logger.info("maxNumberOfConflicts: " + maximumNumberOfConflicts + ", numberReturnedConflicts: " + conflicts.size());
            assertTrue(maximumNumberOfConflicts >= conflicts.size());

            if (conflicts.size() == 0) {
                conflictExists = false;
            } else {
                logger.info("\nconflict: " + conflicts.toString() + "\n");
                //create KB' from conflicts
                OWLTheory theoryPrime = getTheoryFromConflicts(conflicts, theory);

                //setup for simulated session
                conflictSearcher.setConflictSets(conflicts);
                search.setSearcher(conflictSearcher);
                search.setSearchable(theoryPrime);

                long calcTargetDiagTime = System.currentTimeMillis();
                FormulaSet<OWLLogicalAxiom> reducedTargetDiagnosis = getIntersectingDiagnosisAxiom(targetDiagnosis, conflicts);
                calcTargetDiagTime = System.currentTimeMillis() - calcTargetDiagTime;
                //remove time needed to get target diagnosis
                time = time + calcTargetDiagTime;

                computeHS(search, theoryPrime, reducedTargetDiagnosis, nqueries.get(type), type);

                logger.info("\nreducedTargetDiagnosis: " + reducedTargetDiagnosis.toString() + "\n");
                //if test doesn't fail, the found diagnosis is equal the targetDiagnosis
                partialDiagnoses.addAll(reducedTargetDiagnosis);

                //save changed P' and N' in theory and remove target diagnosis from theory
                copyPositiveAndNegativeTests(theory, theoryPrime);
                theory.getKnowledgeBase().removeFormulas(reducedTargetDiagnosis);
            }
        }
        time = System.currentTimeMillis() - time;
        foundDiagnoses.add(partialDiagnoses);
        return time;
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


    public OWLTheory getTheoryFromConflicts(Set<FormulaSet<OWLLogicalAxiom>> conflicts, OWLTheory completeTheory) throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {
        Set<OWLAxiom> axiomSet = new HashSet<OWLAxiom>();
        Iterator<FormulaSet<OWLLogicalAxiom>> iterator = conflicts.iterator();
        while(iterator.hasNext()) {
            Iterator<OWLLogicalAxiom> axiomIterator = iterator.next().iterator();
            while (axiomIterator.hasNext()) {
                OWLLogicalAxiom ax = axiomIterator.next();
                axiomSet.add(ax);
            }
        }

        OWLTheory theory = completeTheory.copyChangedTheory(axiomSet);
        copyPositiveAndNegativeTests(theory, completeTheory);

        return theory;
    }

    /**
     * Copies the positive, negative, entailed and non entailed tests from the knowledge base of the source theory
     * to the target theory
     * @param targetTheory the target theory
     * @param sourceTheory the source theory
     */
    public void copyPositiveAndNegativeTests(OWLTheory targetTheory, OWLTheory sourceTheory) {
        Iterator<Set<OWLLogicalAxiom>> iterator = sourceTheory.getKnowledgeBase().getPositiveTests().iterator();
        while(iterator.hasNext()) {
            targetTheory.getKnowledgeBase().addPositiveTest(iterator.next());
        }
        iterator = sourceTheory.getKnowledgeBase().getNegativeTests().iterator();
        while(iterator.hasNext()) {
            targetTheory.getKnowledgeBase().addNegativeTest(iterator.next());
        }

        iterator = sourceTheory.getKnowledgeBase().getEntailedTests().iterator();
        while(iterator.hasNext()) {
            targetTheory.getKnowledgeBase().addEntailedTest(iterator.next());
        }
        iterator = sourceTheory.getKnowledgeBase().getNonentailedTests().iterator();
        while(iterator.hasNext()) {
            targetTheory.getKnowledgeBase().addNonEntailedTest(iterator.next());
        }
    }

    /**
     * searches for n conflict and returns them
     * @param MultiQuickXplain<OWLLogicalAxiom> quick
     * @param OWLTheory theory
     * @param int numConflicts maximum number of computed conflicts
     * @return Set<FormulaSet<OWLLogicalAxiom>> conflicts
     * @throws at.ainf.diagnosis.model.InconsistentTheoryException
     * @throws at.ainf.diagnosis.model.SolverException
     * @throws at.ainf.diagnosis.tree.exceptions.NoConflictException
     */
    public Set<FormulaSet<OWLLogicalAxiom>> computeNConflictsAtTime(MultiQuickXplain<OWLLogicalAxiom> quick, OWLTheory theory, int numConflicts) throws InconsistentTheoryException, SolverException, NoConflictException {
        List<OWLLogicalAxiom> list = new LinkedList<OWLLogicalAxiom>();
        list.addAll(theory.getKnowledgeBase().getFaultyFormulas());

        //set maxConflict to 1 to get only one conflict
        quick.setMaxConflictSetCount(numConflicts);
        Set<FormulaSet<OWLLogicalAxiom>> conflicts = quick.search(theory, list);
        return conflicts;
    }

    /**
     * Returns a formula set of the axioms, that are present in the conflict sets
     * @param diagnosis
     * @param conflicts conflict sets
     * @return intersecting FormulaSet<OWLLogicalAxiom>
     */
    private FormulaSet<OWLLogicalAxiom> getIntersectingDiagnosisAxiom(FormulaSet<OWLLogicalAxiom> diagnosis, Set<FormulaSet<OWLLogicalAxiom>> conflicts) {
        Set<OWLLogicalAxiom> axiomSet = new HashSet<OWLLogicalAxiom>();
        if(diagnosis==null || conflicts==null)
            return null;

        for (OWLLogicalAxiom diagAxiom : diagnosis) {
            boolean isPresent = false;
            for (FormulaSet<OWLLogicalAxiom> conflict : conflicts) {
                if (conflict.contains(diagAxiom))
                    axiomSet.add(diagAxiom);
            }
        }
        FormulaSet<OWLLogicalAxiom> reducedDiagnosis = new FormulaSetImpl<OWLLogicalAxiom>(diagnosis.getMeasure(), axiomSet, null);
        return reducedDiagnosis;
    }
}