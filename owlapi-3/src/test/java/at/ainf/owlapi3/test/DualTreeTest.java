package at.ainf.owlapi3.test;

import at.ainf.diagnosis.Searcher;
import at.ainf.diagnosis.quickxplain.DirectDiagnosis;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.tree.*;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.BreadthFirstSearchStrategy;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.base.CalculateDiagnoses;
import at.ainf.owlapi3.base.SimulatedSession;
import at.ainf.owlapi3.base.tools.TableList;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.parser.MyOWLRendererParser;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.AxiomSet;
import junit.framework.Assert;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

import static at.ainf.owlapi3.base.SimulatedSession.QSSType;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 16.02.12
 * Time: 19:23
 * To change this template use File | Settings | File Templates.
 */
public class DualTreeTest {//extends BasePerformanceTests {

    private static Logger logger = LoggerFactory.getLogger(DualTreeTest.class.getName());

    String[] ontologies = {"Univ.owl"}; //, "Univ2.owl"};

    private static final boolean TEST_CACHING = false;

    /*@BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlapi3-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    } */

    @Ignore
    @Test
    public void testDualTreePruning() throws InconsistentTheoryException, OWLOntologyCreationException, SolverException, NoConflictException {
        //String ont = "queryontologies/onediag.owl";
        String ont = "ontologies/empty.owl";
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
        //SimpleStorage<OWLLogicalAxiom> dualStorage = new SimpleStorage<OWLLogicalAxiom>();

        InvHsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> searchDual = new InvHsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        searchDual.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());
        searchDual.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
        //searchDual.setLogic(new DualTreeLogic<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom>());

        ((NewQuickXplain<OWLLogicalAxiom>)dualSearcher).setAxiomRenderer(new MyOWLRendererParser(null));

        computeQueryExample(ont, runs, true, dualSearcher, searchDual, ptestCases, ntestCases);

        logger.info("----- Computing normal case -----");
        Searcher<OWLLogicalAxiom> searcher = new NewQuickXplain<OWLLogicalAxiom>();
        //SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();

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
            logger.info("HS " + LogUtil.renderAxioms(hs));
        for (AxiomSet<OWLLogicalAxiom> confl : searchNormal.getStorage().getConflicts())
            logger.info("cs " + LogUtil.renderAxioms(confl));

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
            logger.info("HS " + CalculateDiagnoses.renderAxioms(hs));
        for (AxiomSet<OWLLogicalAxiom> confl : searchNormal.getConflicts())
            logger.info("cs " + CalculateDiagnoses.renderAxioms(confl));
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
        OWLTheory theoryNormal = createTheory(manager, "ontologies/" + ont, false);
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
        OWLTheory theoryDual = createTheory(manager, "ontologies/" + ont, true);
        searchDual.setTheory(theoryDual);
        searchDual.run();
        Set<? extends AxiomSet<OWLLogicalAxiom>> resultDual = searchDual.getDiagnoses();
        dual = System.currentTimeMillis() - dual;

        logger.info("normal " + CalculateDiagnoses.getStringTime(normal) + " subsets: " + theoryNormal.getCache().size());
        logger.info("dual " + CalculateDiagnoses.getStringTime(dual) + " subsets: " + theoryDual.getCache().size());

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

    protected static boolean userBrk = true;

    protected static BigDecimal SIGMA = new BigDecimal("100");

    protected double avg(List<Double> nqueries) {
        double res = 0;
        for (Double qs : nqueries) {
            res += qs;
        }
        return res / nqueries.size();
    }



    protected long computeDual(TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> searchDual, OWLTheory theoryDual,
                               AxiomSet<OWLLogicalAxiom> diagnosis, List<Double> queries, QSSType type) {
        SimulatedSession session = new SimulatedSession();
        TableList entry2 = new TableList();
        long timeDual = System.currentTimeMillis();
        int diagnosesCalc = 0;
        String daStr = "";
        int conflictsCalc = 0;
        //QSS<OWLLogicalAxiom> qss = null;
        //if (type != null) qss = createQSSWithDefaultParam(type);
        session.simulateQuerySession(searchDual, theoryDual, diagnosis, entry2, type, "", null, null, null);
        timeDual = System.currentTimeMillis() - timeDual;
        AxiomSet<OWLLogicalAxiom> diag2 = getMostProbable(searchDual.getDiagnoses());
        boolean foundCorrectD2 = diag2.equals(diagnosis);
        boolean hasNegativeTestcases = searchDual.getTheory().getNonentailedTests().size() > 0;

        logger.info("dual tree iteration finished: window size "
                + entry2.getMeanWin() + " num of query " + entry2.getMeanQuery() +
                " time " + CalculateDiagnoses.getStringTime(timeDual) + " found correct diag " + foundCorrectD2 +
                " diagnoses: " + diagnosesCalc + " conflict  " + conflictsCalc +
                " has negative tests " + hasNegativeTestcases + " diagnoses in storage " + daStr +
                " cached subsets " + theoryDual.getCache().size()
        );
        Assert.assertTrue(foundCorrectD2);
        theoryDual.clearTestCases();
        searchDual.reset();
        queries.add(entry2.getMeanQuery());
        return timeDual;
    }

    protected AxiomSet<OWLLogicalAxiom> getMostProbable(Set<AxiomSet<OWLLogicalAxiom>> diagnoses) {
        TreeSet<AxiomSet<OWLLogicalAxiom>> ts = new TreeSet<AxiomSet<OWLLogicalAxiom>>();
        ts.addAll(diagnoses);
        return ts.last();
    }

    protected long computeHS(TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> searchNormal,
                             OWLTheory theoryNormal, AxiomSet<OWLLogicalAxiom> diagnoses,
                             List<Double> queries, QSSType type) {
        SimulatedSession session = new SimulatedSession();
        TableList entry = new TableList();
        long timeNormal = System.currentTimeMillis();
        int diagnosesCalc = 0;
        String daStr = "";
        int conflictsCalc = 0;
        //QSS<OWLLogicalAxiom> qss = null;
        //if (type != null) qss = createQSSWithDefaultParam(type);
        session.simulateQuerySession(searchNormal, theoryNormal, diagnoses, entry, type, "", null, null, null);
        timeNormal = System.currentTimeMillis() - timeNormal;
        AxiomSet<OWLLogicalAxiom> diag = getMostProbable(searchNormal.getDiagnoses());
        boolean foundCorrectD = diag.equals(diagnoses);
        boolean hasNegativeTestcases = searchNormal.getTheory().getNonentailedTests().size() > 0;
        theoryNormal.clearTestCases();
        searchNormal.reset();
        logger.info("hstree iteration finished: window size "
                + entry.getMeanWin() + " num of query " + entry.getMeanQuery() + " time " +
                CalculateDiagnoses.getStringTime(timeNormal) + " found correct diag " + foundCorrectD +
                " diagnoses: " + diagnosesCalc + " conflict  " + conflictsCalc +
                " has negative testst " + hasNegativeTestcases + " diagnoses in storage " + daStr +
                " cached subsets " + theoryNormal.getCache().size()
        );
        Assert.assertTrue(foundCorrectD);
        queries.add(entry.getMeanQuery());
        return timeNormal;
    }

    private void compareAllDiagnoses(String ontology, boolean useSubsets, int threshold) throws SolverException, InconsistentTheoryException, OWLOntologyCreationException, NoConflictException {
        long t = System.currentTimeMillis();
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        userBrk = true;
        SIGMA = new BigDecimal("85");
        HsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> searchNormal = new HsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        searchNormal.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        searchNormal.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
        OWLTheory theoryNormal = createTheory(manager, "ontologies/" + ontology, false);
        searchNormal.setTheory(theoryNormal);
        theoryNormal.useCache(useSubsets, threshold);
        HashMap<ManchesterOWLSyntax, BigDecimal> map = CalculateDiagnoses.getProbabMap();
        OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theoryNormal);
        es.updateKeywordProb(map);
        searchNormal.setCostsEstimator(es);
        searchNormal.run();
        Set<? extends AxiomSet<OWLLogicalAxiom>> resultNormal = searchNormal.getDiagnoses();

        manager = OWLManager.createOWLOntologyManager();
        InvHsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> searchDual = new InvHsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        searchDual.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        searchDual.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
        OWLTheory theoryDual = createTheory(manager, "ontologies/" + ontology, true);
        theoryDual.useCache(useSubsets, threshold);
        searchDual.setTheory(theoryDual);
        //searchDual.setLogic(new DualTreeLogic<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>());
        map = CalculateDiagnoses.getProbabMap();
        es = new OWLAxiomKeywordCostsEstimator(theoryDual);
        es.updateKeywordProb(map);
        searchDual.setCostsEstimator(es);

        theoryNormal.clearTestCases();
        searchNormal.reset();

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
        logger.info("needed overall " + CalculateDiagnoses.getStringTime(needed));
        logger.info("needed normal " + CalculateDiagnoses.getStringTime(timeNormalOverall) +
                " max " + CalculateDiagnoses.getStringTime(timeNormalMax) +
                " min " + CalculateDiagnoses.getStringTime(timeNormalMin) +
                " avg2 " + CalculateDiagnoses.getStringTime(timeNormalOverall / count) +
                " Queries max " + Collections.max(nqueries) +
                " min " + Collections.min(nqueries) +
                " avg2 " + avg(nqueries)
        );
        logger.info("needed dual " + CalculateDiagnoses.getStringTime(timeDualOverall) +
                " max " + CalculateDiagnoses.getStringTime(timeDualMax) +
                " min " + CalculateDiagnoses.getStringTime(timeDualMin) +
                " avg2 " + CalculateDiagnoses.getStringTime(timeDualOverall / count) +
                " Queries max " + Collections.max(dqueries) +
                " min " + Collections.min(dqueries) +
                " avg2 " + avg(dqueries));
    }

}
