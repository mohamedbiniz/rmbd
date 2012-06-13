package at.ainf.owlcontroller.queryeval;

import at.ainf.diagnosis.Searcher;
import at.ainf.diagnosis.quickxplain.DirectDiagnosis;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.tree.*;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.BreadthFirstSearchStrategy;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlcontroller.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlcontroller.Utils;
import at.ainf.owlcontroller.parser.MyOWLRendererParser;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.SimpleStorage;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.InputStream;
import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 16.02.12
 * Time: 19:23
 * To change this template use File | Settings | File Templates.
 */
public class DualTreeTest extends BasePerformanceTests {

    private static Logger logger = Logger.getLogger(DualTreeTest.class.getName());

    String[] ontologies = {"Univ.owl"}; //, "Univ.owl"};

    private static final boolean TEST_CACHING = false;

    @BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlcontroller-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }

    @Ignore
    @Test
    public void testDualTreePruning() throws InconsistentTheoryException, OWLOntologyCreationException, SolverException, NoConflictException {
        //String ont = "queryontologies/onediag.owl";
        String ont = "queryontologies/empty.owl";
        List<String> ptestCases = new LinkedList<String>();
        List<String> ntestCases = new LinkedList<String>();
        ntestCases.add("w Type (not C)");
        ptestCases.add("w Type B");
       // ptestCases.add("w Type E");

        runComparison(ont, 2, ptestCases, ntestCases);

        //ntestCases.add("v Type D");
        //runComparison(ont, 2, ptestCases, ntestCases);
        /*testCases.add("w Type not D");
        runComparison(ont, 2, testCases);
        */
    }

    private void runComparison(String ont, int runs, List<String> ptestCases, List<String> ntestCases)
            throws SolverException, InconsistentTheoryException, OWLOntologyCreationException, NoConflictException {


        logger.info("----- Computing dual case -----");
        Searcher<OWLLogicalAxiom> dualSearcher = new DirectDiagnosis<OWLLogicalAxiom>();
        SimpleStorage<OWLLogicalAxiom> dualStorage = new SimpleStorage<OWLLogicalAxiom>();

        InvHsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> searchDual = new InvHsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        searchDual.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());
        searchDual.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
        //searchDual.setLogic(new DualTreeLogic<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom>());

        ((NewQuickXplain<OWLLogicalAxiom>)dualSearcher).setAxiomRenderer(new MyOWLRendererParser(null));

        computeQueryExample(ont, runs, true, dualSearcher, searchDual, ptestCases, ntestCases);

        logger.info("----- Computing normal case -----");
        Searcher<OWLLogicalAxiom> searcher = new NewQuickXplain<OWLLogicalAxiom>();
        SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();

        HsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> searchNormal = new HsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        searchNormal.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());
        searchNormal.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());

        computeQueryExample(ont, runs, false, searcher, searchNormal, ptestCases, ntestCases);

        //prinths(storage.getDiagnoses());
        //prinths(dualStorage.getDiagnoses());
        if (runs < 0) {
            assertTrue(compare(searchNormal.getDiagnoses(), searchDual.getDiagnoses()));
            assertTrue(compare(searchNormal.getConflicts(), searchDual.getConflicts()));
        }
    }


    private boolean compare(Set<AxiomSet<OWLLogicalAxiom>> diagnoses, Set<AxiomSet<OWLLogicalAxiom>> diagnoses1) {
        if (diagnoses.size() != diagnoses1.size()) return false;
        for (AxiomSet<OWLLogicalAxiom> diagnose : diagnoses) {
            if (!findDiagnosis(diagnoses1, diagnose)) return false;
        }
        return true;
    }

    private boolean findDiagnosis(Set<AxiomSet<OWLLogicalAxiom>> diagnoses1, AxiomSet<OWLLogicalAxiom> diagnose) {
        for (AxiomSet<OWLLogicalAxiom> owlLogicalAxioms : diagnoses1) {
            if (diagnose.equals(owlLogicalAxioms)) {
                return true;
            }
        }
        return false;
    }


    private void computeQueryExample(String ont, int runs, boolean dual, Searcher<OWLLogicalAxiom> searcher,
                                     TreeSearch<? extends AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> searchNormal,
                                     List<String> ptestCases,  List<String> ntestCases)
            throws SolverException, InconsistentTheoryException, OWLOntologyCreationException, NoConflictException {

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        searchNormal.setSearcher(searcher);
        OWLTheory theoryNormal = createTheory(manager, ont, dual);
        searchNormal.setTheory(theoryNormal);
        /*
        searchNormal.run(runs);

        logger.info("First " + runs + " Diagnoses and corresponding conflicts before test case");
        for (AxiomSet<OWLLogicalAxiom> hs : searchNormal.getStorage().getDiagnoses())
            logger.info("HS " + Utils.renderAxioms(hs));
        for (AxiomSet<OWLLogicalAxiom> confl : searchNormal.getStorage().getConflicts())
            logger.info("cs " + Utils.renderAxioms(confl));

        if (runs < 1) return;

        */
        HashSet<OWLLogicalAxiom> positiveTestcase = new HashSet<OWLLogicalAxiom>();
        HashSet<OWLLogicalAxiom> negativeTestcase = new HashSet<OWLLogicalAxiom>();

        MyOWLRendererParser parser = new MyOWLRendererParser(theoryNormal.getOriginalOntology());
        for (String testcase : ptestCases)
            positiveTestcase.add(parser.parse(testcase));
        for (String testcase : ntestCases)
            negativeTestcase.add(parser.parse(testcase));

        logger.info("All diagnoses and conflicts with test cases");
        theoryNormal.addEntailedTest(positiveTestcase);
        theoryNormal.addNonEntailedTest(negativeTestcase);
        searchNormal.run(runs);
        for (AxiomSet<OWLLogicalAxiom> hs : searchNormal.getDiagnoses())
            logger.info("HS " + Utils.renderAxioms(hs));
        for (AxiomSet<OWLLogicalAxiom> confl : searchNormal.getConflicts())
            logger.info("cs " + Utils.renderAxioms(confl));
    }


    @Test
    public void testResultsEqualTime() throws InconsistentTheoryException, OWLOntologyCreationException, SolverException, NoConflictException {

        String ont = "koala.owl";

        long normal = System.currentTimeMillis();
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        TreeSearch<? extends AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> searchNormal = new HsTreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        searchNormal.setCostsEstimator(new SimpleCostsEstimator<OWLLogicalAxiom>());
        searchNormal.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());
        searchNormal.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
        OWLTheory theoryNormal = createTheory(manager, "queryontologies/" + ont, false);
        searchNormal.setTheory(theoryNormal);
        searchNormal.run();
        Set<? extends AxiomSet<OWLLogicalAxiom>> resultNormal = searchNormal.getDiagnoses();
        normal = System.currentTimeMillis() - normal;

        long dual = System.currentTimeMillis();
        manager = OWLManager.createOWLOntologyManager();
        TreeSearch<? extends AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> searchDual = new InvHsTreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        searchDual.setCostsEstimator(new SimpleCostsEstimator<OWLLogicalAxiom>());
        searchDual.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());
        searchDual.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
        OWLTheory theoryDual = createTheory(manager, "queryontologies/" + ont, true);
        searchDual.setTheory(theoryDual);
        searchDual.run();
        Set<? extends AxiomSet<OWLLogicalAxiom>> resultDual = searchDual.getDiagnoses();
        dual = System.currentTimeMillis() - dual;

        logger.info("normal " + Utils.getStringTime(normal) + " subsets: " + theoryNormal.getCache().size());
        logger.info("dual " + Utils.getStringTime(dual) + " subsets: " + theoryDual.getCache().size());

        assert (resultNormal.equals(resultDual));

    }

    public OWLTheory createTheory(OWLOntologyManager manager, String path, boolean dual) throws SolverException, InconsistentTheoryException, OWLOntologyCreationException {
        InputStream st = ClassLoader.getSystemResourceAsStream(path);
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(st);
        Set<OWLLogicalAxiom> bax = new HashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        OWLTheory theory = null;
        if (dual)
            theory = new DualTreeOWLTheory(reasonerFactory, ontology, bax);
        else
            theory = new OWLTheory(reasonerFactory, ontology, bax);
        //assert (theory.verifyRequirements());

        return theory;
    }


    @Test
    public void computeAllDiagnoses()
            throws NoConflictException, SolverException, InconsistentTheoryException, OWLOntologyCreationException {

            String ont = "koala.owl";
            if (TEST_CACHING) {
                for (int i = 10; i <= 10; i = i + 5) {
                    logger.info("Running diagnosis compare " + ont + " (" + i + ")");
                    compareAllDiagnoses(ont, true, i);
                }
            }
            logger.info("Running diagnosis compare " + ont + " without caching");
            compareAllDiagnoses(ont, false, 0);
        
    }


    private void compareAllDiagnoses(String ontology, boolean useSubsets, int threshold) throws SolverException, InconsistentTheoryException, OWLOntologyCreationException, NoConflictException {
        long t = System.currentTimeMillis();
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        userBrk = true;
        SIGMA = 85;
        HsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> searchNormal = new HsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        searchNormal.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        searchNormal.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
        OWLTheory theoryNormal = createTheory(manager, "queryontologies/" + ontology, false);
        searchNormal.setTheory(theoryNormal);
        theoryNormal.useCache(useSubsets, threshold);
        HashMap<ManchesterOWLSyntax, Double> map = Utils.getProbabMap();
        OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theoryNormal);
        es.updateKeywordProb(map);
        searchNormal.setCostsEstimator(es);
        searchNormal.run();
        Set<? extends AxiomSet<OWLLogicalAxiom>> resultNormal = searchNormal.getDiagnoses();

        manager = OWLManager.createOWLOntologyManager();
        InvHsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> searchDual = new InvHsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        searchDual.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        searchDual.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
        OWLTheory theoryDual = createTheory(manager, "queryontologies/" + ontology, true);
        theoryDual.useCache(useSubsets, threshold);
        searchDual.setTheory(theoryDual);
        //searchDual.setLogic(new DualTreeLogic<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>());
        map = Utils.getProbabMap();
        es = new OWLAxiomKeywordCostsEstimator(theoryDual);
        es.updateKeywordProb(map);
        searchDual.setCostsEstimator(es);

        theoryNormal.clearTestCases();
        searchNormal.clearSearch();

        long timeNormalOverall = 0;
        long timeDualOverall = 0;
        long timeNormalMax = 0;
        long timeNormalMin = Long.MAX_VALUE;
        long timeDualMax = 0;
        long timeDualMin = Long.MAX_VALUE;
        int count = 0;
        List<Double> nqueries = new LinkedList<Double>();
        List<Double> dqueries = new LinkedList<Double>();

        for (AxiomSet<OWLLogicalAxiom> diagnosis : resultNormal) {
            logger.info("iteration " + ++count);
            long timeNormal, timeDual;
            if (count % 2 != 0) {
                timeNormal = computeHS(searchNormal, theoryNormal, diagnosis, nqueries, QSSType.MINSCORE);
                timeDual = computeDual(searchDual, theoryDual, diagnosis, dqueries, QSSType.MINSCORE);
            } else {
                timeDual = computeDual(searchDual, theoryDual, diagnosis, dqueries, QSSType.MINSCORE);
                timeNormal = computeHS(searchNormal, theoryNormal, diagnosis, nqueries, QSSType.MINSCORE);
            }
            timeNormalOverall += timeNormal;
            timeDualOverall += timeDual;
            if (timeNormalMax < timeNormal) timeNormalMax = timeNormal;
            if (timeDualMax < timeDual) timeDualMax = timeDual;
            if (timeNormalMin > timeNormal) timeNormalMin = timeNormal;
            if (timeDualMin > timeNormal) timeDualMin = timeDual;
        }

        long needed = System.currentTimeMillis() - t;
        logger.info("needed overall " + Utils.getStringTime(needed));
        logger.info("needed normal " + Utils.getStringTime(timeNormalOverall) +
                " max " + Utils.getStringTime(timeNormalMax) +
                " min " + Utils.getStringTime(timeNormalMin) +
                " avg " + Utils.getStringTime(timeNormalOverall / count) +
                " Queries max " + Collections.max(nqueries) +
                " min " + Collections.min(nqueries) +
                " avg " + avg(nqueries)
        );
        logger.info("needed dual " + Utils.getStringTime(timeDualOverall) +
                " max " + Utils.getStringTime(timeDualMax) +
                " min " + Utils.getStringTime(timeDualMin) +
                " avg " + Utils.getStringTime(timeDualOverall / count) +
                " Queries max " + Collections.max(dqueries) +
                " min " + Collections.min(dqueries) +
                " avg " + avg(dqueries));
    }


}
