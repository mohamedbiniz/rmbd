package at.ainf.owlapi3.test;

import at.ainf.diagnosis.Searcher;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.DirectDiagnosis;
import at.ainf.diagnosis.quickxplain.MultiQuickXplain;
import at.ainf.diagnosis.quickxplain.QXAxiomSetListener;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.*;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.BreadthFirstSearchStrategy;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.diagnosis.tree.splitstrategy.*;
import at.ainf.owlapi3.base.CalculateDiagnoses;
import at.ainf.owlapi3.base.SimulatedSession;
import at.ainf.owlapi3.base.tools.TableList;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.parser.MyOWLRendererParser;
import junit.framework.Assert;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.junit.BeforeClass;
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
import static at.ainf.owlapi3.base.SimulatedSession.QSSType.MINSCORE;
import static org.junit.Assert.assertEquals;
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
    String[] jwsOntologies = {"CHEM-A.owl", "koala.owl", "buggy-sweet-jpl.owl", "miniTambis.owl", "univ.owl", "Economy-SDA.owl", "Transportation-SDA.owl"};
    static Set<SplitStrategy<OWLLogicalAxiom>> splitStrategies = new LinkedHashSet<SplitStrategy<OWLLogicalAxiom>>();
    static Set<Searcher<OWLLogicalAxiom>> searchers = new LinkedHashSet<Searcher<OWLLogicalAxiom>>();


    private static final boolean TEST_CACHING = false;

    @BeforeClass
    public static void setUp() {
        /* String conf = ClassLoader.getSystemResource("owlapi3-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);*/
        splitStrategies.add(new SimpleSplitStrategy<OWLLogicalAxiom>());
        splitStrategies.add(new MostFrequentSplitStrategy<OWLLogicalAxiom>());
        splitStrategies.add(new GreatestConflictSplitStrategy<OWLLogicalAxiom>());
        splitStrategies.add(new MostProbableSplitStrategy<OWLLogicalAxiom>());

        searchers.add(new QuickXplain<OWLLogicalAxiom>());

        MultiQuickXplain<OWLLogicalAxiom> mult = new MultiQuickXplain<OWLLogicalAxiom>();
        mult.setAxiomListener(new QXAxiomSetListener<OWLLogicalAxiom>(true));
        searchers.add(mult);


    }


    @Test
    public void testAllVariants() throws NoConflictException, OWLOntologyCreationException, SolverException, InconsistentTheoryException {

        Set<Set<? extends FormulaSet<OWLLogicalAxiom>>> resultsBinary = new LinkedHashSet<Set<? extends FormulaSet<OWLLogicalAxiom>>>();

        BinaryTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> binary = new BinaryTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        binary.setCostsEstimator(new SimpleCostsEstimator<OWLLogicalAxiom>());


        for (Searcher<OWLLogicalAxiom> searcher : searchers) {

            binary.setSearcher(searcher);
            binary.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());

            for (SplitStrategy<OWLLogicalAxiom> split : splitStrategies) {
                binary.setSplitStrategy(split);

                for (String ont : ontologies) {
                    boolean success = false;

                    while (!success) {
                        success = true;
                        success = getResults(resultsBinary, binary, ont);

                    }
                }
            }

        }

        Set<Set<? extends FormulaSet<OWLLogicalAxiom>>> resultsNormal = new LinkedHashSet<Set<? extends FormulaSet<OWLLogicalAxiom>>>();

        for (String ont : ontologies) {
            resultsNormal.add(testHSTree(ont));
        }

    }

    private boolean getResults(Set<Set<? extends FormulaSet<OWLLogicalAxiom>>> resultsBinary, BinaryTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> binary, String ont) throws OWLOntologyCreationException, SolverException, InconsistentTheoryException, NoConflictException {
        try {
            resultsBinary.add(testBHSTree(binary, false, ont));
        } catch (Exception cm) {
            return false;
        }
        return true;
    }


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
        BinaryTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> binary = new BinaryTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        InvHsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> searchDual = new InvHsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();

        runComparison(ont, 2, ptestCases, ntestCases, binary);

        // runComparison(ont, 2, ptestCases, ntestCases, searchDual);

        //ntestCases.add("v Type D");
        //runComparison(ont, 2, ptestCases, ntestCases);
        /*testCases.add("w Type not D");
        runComparison(ont, 2, testCases);
        */
    }

    private void runComparison(String ont, int runs, List<String> ptestCases, List<String> ntestCases, AbstractTreeSearch searchDual)
            throws SolverException, InconsistentTheoryException, OWLOntologyCreationException, NoConflictException {


        logger.info("----- Computing dual case -----");
        Searcher<OWLLogicalAxiom> dualSearcher = new DirectDiagnosis<OWLLogicalAxiom>();
        //SimpleStorage<OWLLogicalAxiom> dualStorage = new SimpleStorage<OWLLogicalAxiom>();


        searchDual.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());
        searchDual.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
        //searchDual.setLogic(new DualTreeLogic<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom>());

        ((QuickXplain<OWLLogicalAxiom>) dualSearcher).setFormulaRenderer(new MyOWLRendererParser(null));

        computeQueryExample(ont, runs, true, dualSearcher, searchDual, ptestCases, ntestCases);

        logger.info("----- Computing normal case -----");
        Searcher<OWLLogicalAxiom> searcher = new QuickXplain<OWLLogicalAxiom>();
        //SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();

        HsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> searchNormal = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        searchNormal.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());
        searchNormal.setSearcher(new QuickXplain<OWLLogicalAxiom>());

        computeQueryExample(ont, runs, false, searcher, searchNormal, ptestCases, ntestCases);

        //prinths(storage.getDiagnoses());
        //prinths(dualStorage.getDiagnoses());
        if (runs < 0) {
            assertTrue(compare(searchNormal.getDiagnoses(), searchDual.getDiagnoses()));
            assertTrue(compare(searchNormal.getConflicts(), searchDual.getConflicts()));
        }
    }


    private boolean compare(Set<FormulaSet<OWLLogicalAxiom>> diagnoses, Set<FormulaSet<OWLLogicalAxiom>> diagnoses1) {
        if (diagnoses.size() != diagnoses1.size()) return false;
        for (FormulaSet<OWLLogicalAxiom> diagnose : diagnoses) {
            if (!findDiagnosis(diagnoses1, diagnose)) return false;
        }
        return true;
    }

    private boolean findDiagnosis(Set<FormulaSet<OWLLogicalAxiom>> diagnoses1, FormulaSet<OWLLogicalAxiom> diagnose) {
        for (FormulaSet<OWLLogicalAxiom> owlLogicalAxioms : diagnoses1) {
            if (diagnose.equals(owlLogicalAxioms)) {
                return true;
            }
        }
        return false;
    }


    private void computeQueryExample(String ont, int runs, boolean dual, Searcher<OWLLogicalAxiom> searcher,
                                     TreeSearch<? extends FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> searchNormal,
                                     List<String> ptestCases, List<String> ntestCases)
            throws SolverException, InconsistentTheoryException, OWLOntologyCreationException, NoConflictException {

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        searchNormal.setSearcher(searcher);
        OWLTheory theoryNormal = createTheory(manager, ont, dual);
        searchNormal.setSearchable(theoryNormal);
        /*
        searchNormal.start(runs);

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
        for (String testcase : ptestCases) {
            OWLLogicalAxiom parse = parser.parse(testcase);
            if (parse == null)
                throw new NullPointerException("Parser returned null!");
            positiveTestcase.add(parse);
        }
        for (String testcase : ntestCases) {
            OWLLogicalAxiom parse = parser.parse(testcase);
            if (parse == null)
                throw new NullPointerException("Parser returned null!");
            negativeTestcase.add(parse);
        }

        logger.info("All diagnoses and conflicts with test cases");
        theoryNormal.getKnowledgeBase().addEntailedTest(positiveTestcase);
        theoryNormal.getKnowledgeBase().addNonEntailedTest(negativeTestcase);
        searchNormal.setMaxDiagnosesNumber(runs);
        searchNormal.start();
        for (FormulaSet<OWLLogicalAxiom> hs : searchNormal.getDiagnoses())
            logger.info("HS " + new CalculateDiagnoses().renderAxioms(hs));
        for (FormulaSet<OWLLogicalAxiom> confl : searchNormal.getConflicts())
            logger.info("cs " + new CalculateDiagnoses().renderAxioms(confl));
    }


    @Ignore
    @Test
    public void testResultsEqualTime() throws NoConflictException, OWLOntologyCreationException, SolverException, InconsistentTheoryException {

        BinaryTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> binary = new BinaryTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        binary.setCostsEstimator(new SimpleCostsEstimator<OWLLogicalAxiom>());
        binary.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        // MultiQuickXplain<OWLLogicalAxiom> searcher = new MultiQuickXplain<OWLLogicalAxiom>();
        // searcher.setAxiomListener(new QXAxiomSetListener<OWLLogicalAxiom>(true));
        QuickXplain<OWLLogicalAxiom> searcher = new QuickXplain<OWLLogicalAxiom>();
        binary.setSearcher(searcher);


        runTimeComparison(binary, false, "Economy-SDA.owl");

    }


    @Ignore
    @Test
    public void testResultsMultiThreadedLocks() throws NoConflictException, OWLOntologyCreationException, SolverException, InconsistentTheoryException {
        // test 40 times to check if concurrent modification exception occurs
        for (int i = 0; i < 40; i++) {
            BinaryTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> binary = new BinaryTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
            binary.setCostsEstimator(new SimpleCostsEstimator<OWLLogicalAxiom>());
            binary.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());
            MultiQuickXplain<OWLLogicalAxiom> searcher = new MultiQuickXplain<OWLLogicalAxiom>(4,10,10);
            searcher.setAxiomListener(new QXAxiomSetListener<OWLLogicalAxiom>(true));
            binary.setSearcher(searcher);

            runTimeComparison(binary, false, "Economy-SDA.owl");
        }

    }


    public void runTimeComparison(BinaryTreeSearch referenceSearch, boolean dualMode, String ont)
            throws InconsistentTheoryException, OWLOntologyCreationException, SolverException, NoConflictException {

         try {
         Thread.sleep(10000);
         } catch (InterruptedException e) {
         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }


        Set<? extends FormulaSet<OWLLogicalAxiom>> resultNormal = Collections.emptySet();

        Set<? extends FormulaSet<OWLLogicalAxiom>> resultDual = Collections.emptySet();


        resultDual = testBHSTree(referenceSearch, dualMode, ont);
        resultNormal = testHSTree(ont);

        assertEquals(resultNormal, resultDual);

    }


    public Set testHSTree(String ont) throws OWLOntologyCreationException, SolverException, InconsistentTheoryException, NoConflictException {

        logger.info("Normal HS Tree Search");
        logger.info("Ontology: " + ont);

        long normal = System.currentTimeMillis();
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        TreeSearch<? extends FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> searchNormal = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        searchNormal.setCostsEstimator(new SimpleCostsEstimator<OWLLogicalAxiom>());
        searchNormal.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        searchNormal.setSearcher(new QuickXplain<OWLLogicalAxiom>());
        OWLTheory theoryNormal = createTheory(manager, "ontologies/" + ont, false);
        searchNormal.setSearchable(theoryNormal);

        searchNormal.start();

        Set<? extends FormulaSet<OWLLogicalAxiom>> resultNormal = searchNormal.getDiagnoses();
        normal = System.currentTimeMillis() - normal;

        System.out.println("normal " + new CalculateDiagnoses().getStringTime(normal));
        logger.info("Time needed for first nine Diagnosis: " + ((AbstractTreeSearch) searchNormal).getNinthDiagnosisTime());
        logger.info("Total time: " + new CalculateDiagnoses().getStringTime(normal));
        return resultNormal;
    }

    public Set testBHSTree(BinaryTreeSearch referenceSearch, boolean dualMode, String ont) throws OWLOntologyCreationException, SolverException, InconsistentTheoryException, NoConflictException {

        logger.info("Binary Tree Search");

        if (referenceSearch.getSearcher() instanceof MultiQuickXplain)
            logger.info("Multi-threaded");

        else logger.info("Single-threaded");

        logger.info(((BinaryTreeSearch) referenceSearch).getSplitStrategy().getClass().getName());
        logger.info("Ontology: " + ont);

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        long dual = System.currentTimeMillis();
        manager = OWLManager.createOWLOntologyManager();
        //TreeSearch<? extends FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> referenceSearch = new InvHsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        OWLTheory theoryDual = createTheory(manager, "ontologies/" + ont, dualMode);
        // MostProbableSplitStrategy<OWLLogicalAxiom> split = new MostProbableSplitStrategy<OWLLogicalAxiom>();
        //split.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theoryDual));


        referenceSearch.setSearchable(theoryDual);
        //((BinaryTreeSearch)referenceSearch).setSplitStrategy(split);

        if (referenceSearch.getSplitStrategy() instanceof MostProbableSplitStrategy) {
            ((MostProbableSplitStrategy) referenceSearch.getSplitStrategy()).setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theoryDual));
        }

        referenceSearch.start();
        Set<? extends FormulaSet<OWLLogicalAxiom>> resultDual = referenceSearch.getDiagnoses();
        dual = System.currentTimeMillis() - dual;


        logger.info("Total time " + new CalculateDiagnoses().getStringTime(dual));
        logger.info("Time needed for first nine Diagnosis: " + referenceSearch.getNinthDiagnosisTime());
        System.out.println("Binary " + new CalculateDiagnoses().getStringTime(dual));

        return resultDual;
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

    @Ignore
    @Test
    public void testKoalaQuerySession() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException, NoConflictException {
        HsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        search.setCostsEstimator(new SimpleCostsEstimator<OWLLogicalAxiom>());
        search.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());
        search.setSearcher(new QuickXplain<OWLLogicalAxiom>());
        OWLTheory th = createTheory(OWLManager.createOWLOntologyManager(), "ontologies/koala.owl", false);
        search.setSearchable(th);
        search.setFormulaRenderer(new MyOWLRendererParser(null));

        MyOWLRendererParser parser = new MyOWLRendererParser(th.getOriginalOntology());
        th.getKnowledgeBase().addEntailedTest(Collections.singleton(parser.parse("Marsupials DisjointWith Person")));
        th.getKnowledgeBase().addEntailedTest(Collections.singleton(parser.parse("Koala SubClassOf Marsupials")));
        th.getKnowledgeBase().addEntailedTest(Collections.singleton(parser.parse("hasDegree Domain Person")));

        search.setMaxDiagnosesNumber(9);
        search.start();


        InvHsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> searchDual = new InvHsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        searchDual.setCostsEstimator(new SimpleCostsEstimator<OWLLogicalAxiom>());
        searchDual.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());
        searchDual.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
        OWLTheory thDual = createTheory(OWLManager.createOWLOntologyManager(), "ontologies/koala.owl", true);
        searchDual.setSearchable(thDual);
        searchDual.setFormulaRenderer(new MyOWLRendererParser(null));

        thDual.getKnowledgeBase().addEntailedTest(Collections.singleton(parser.parse("Marsupials DisjointWith Person")));
        thDual.getKnowledgeBase().addEntailedTest(Collections.singleton(parser.parse("Koala SubClassOf Marsupials")));
        thDual.getKnowledgeBase().addEntailedTest(Collections.singleton(parser.parse("hasDegree Domain Person")));

        searchDual.setMaxDiagnosesNumber(9);
        searchDual.start();

        assertTrue(search.getDiagnoses().size() == searchDual.getDiagnoses().size());
        assertTrue(search.getDiagnoses().containsAll(searchDual.getDiagnoses()));

    }

    @Ignore
    @Test
    public void computeAllDiagnoses()
            throws NoConflictException, SolverException, InconsistentTheoryException, OWLOntologyCreationException {

        String ont = "example2005.owl";
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


    protected long computeDual(TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> searchDual, OWLTheory theoryDual,
                               FormulaSet<OWLLogicalAxiom> diagnosis, List<Double> queries, QSSType type) {
        SimulatedSession session = new SimulatedSession();
        TableList entry2 = new TableList();
        long timeDual = System.currentTimeMillis();
        int diagnosesCalc = 0;
        String daStr = "";
        int conflictsCalc = 0;
        //QSS<OWLLogicalAxiom> qss = null;
        //if (type != null) qss = createQSSWithDefaultParam(type);

        session.setEntry(entry2);
        session.setMessage("");
        session.setScoringFunct(type);
        session.setTargetD(diagnosis);
        session.setTheory(theoryDual);
        session.setSearch(searchDual);

        session.simulateQuerySession();
        timeDual = System.currentTimeMillis() - timeDual;
        FormulaSet<OWLLogicalAxiom> diag2 = getMostProbable(searchDual.getDiagnoses());
        boolean foundCorrectD2 = diag2.equals(diagnosis);
        boolean hasNegativeTestcases = searchDual.getSearchable().getKnowledgeBase().getNonentailedTests().size() > 0;

        logger.info("dual tree iteration finished: window size "
                + entry2.getMeanWin() + " num of query " + entry2.getMeanQuery() +
                " time " + new CalculateDiagnoses().getStringTime(timeDual) + " found correct diag " + foundCorrectD2 +
                " diagnoses: " + diagnosesCalc + " conflict  " + conflictsCalc +
                " has negative tests " + hasNegativeTestcases + " diagnoses in storage " + daStr
        );
        Assert.assertTrue(foundCorrectD2);
        theoryDual.getKnowledgeBase().clearTestCases();
        searchDual.reset();
        queries.add(entry2.getMeanQuery());
        return timeDual;
    }

    protected FormulaSet<OWLLogicalAxiom> getMostProbable(Set<FormulaSet<OWLLogicalAxiom>> diagnoses) {
        TreeSet<FormulaSet<OWLLogicalAxiom>> ts = new TreeSet<FormulaSet<OWLLogicalAxiom>>();
        ts.addAll(diagnoses);
        return ts.last();
    }

    protected long computeHS(TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> searchNormal,
                             OWLTheory theoryNormal, FormulaSet<OWLLogicalAxiom> diagnoses,
                             List<Double> queries, QSSType type) {
        SimulatedSession session = new SimulatedSession();
        TableList entry = new TableList();
        long timeNormal = System.currentTimeMillis();
        int diagnosesCalc = 0;
        String daStr = "";
        int conflictsCalc = 0;
        //QSS<OWLLogicalAxiom> qss = null;
        //if (type != null) qss = createQSSWithDefaultParam(type);
        session.setEntry(entry);
        session.setMessage("");
        session.setScoringFunct(type);
        session.setTargetD(diagnoses);
        session.setTheory(theoryNormal);
        session.setSearch(searchNormal);
        session.simulateQuerySession();
        timeNormal = System.currentTimeMillis() - timeNormal;
        FormulaSet<OWLLogicalAxiom> diag = getMostProbable(searchNormal.getDiagnoses());
        boolean foundCorrectD = diag.equals(diagnoses);
        boolean hasNegativeTestcases = searchNormal.getSearchable().getKnowledgeBase().getNonentailedTests().size() > 0;
        theoryNormal.getKnowledgeBase().clearTestCases();
        searchNormal.reset();
        logger.info("hstree iteration finished: window size "
                + entry.getMeanWin() + " num of query " + entry.getMeanQuery() + " time " +
                new CalculateDiagnoses().getStringTime(timeNormal) + " found correct diag " + foundCorrectD +
                " diagnoses: " + diagnosesCalc + " conflict  " + conflictsCalc +
                " has negative testst " + hasNegativeTestcases + " diagnoses in storage " + daStr
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
        HsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> searchNormal = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        searchNormal.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        searchNormal.setSearcher(new QuickXplain<OWLLogicalAxiom>());
        OWLTheory theoryNormal = createTheory(manager, "ontologies/" + ontology, false);
        searchNormal.setSearchable(theoryNormal);
        HashMap<ManchesterOWLSyntax, BigDecimal> map = new CalculateDiagnoses().getProbabMap();
        OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theoryNormal);
        es.updateKeywordProb(map);
        searchNormal.setCostsEstimator(es);
        searchNormal.start();
        Set<? extends FormulaSet<OWLLogicalAxiom>> resultNormal = searchNormal.getDiagnoses();

        manager = OWLManager.createOWLOntologyManager();
        InvHsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> searchDual = new InvHsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        searchDual.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        searchDual.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
        OWLTheory theoryDual = createTheory(manager, "ontologies/" + ontology, true);
        searchDual.setSearchable(theoryDual);
        //searchDual.setLogic(new DualTreeLogic<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>());
        map = new CalculateDiagnoses().getProbabMap();
        es = new OWLAxiomKeywordCostsEstimator(theoryDual);
        es.updateKeywordProb(map);
        searchDual.setCostsEstimator(es);

        theoryNormal.getKnowledgeBase().clearTestCases();
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

        for (FormulaSet<OWLLogicalAxiom> diagnosis : resultNormal) {
            logger.info("iteration " + ++count);
            long timeNormal, timeDual;
            if (count % 2 != 0) {
                timeNormal = computeHS(searchNormal, theoryNormal, diagnosis, nqueries, MINSCORE);
                timeDual = computeDual(searchDual, theoryDual, diagnosis, dqueries, MINSCORE);
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
        CalculateDiagnoses d = new CalculateDiagnoses();
        logger.info("needed overall " + d.getStringTime(needed));
        logger.info("needed normal " + d.getStringTime(timeNormalOverall) +
                " max " + d.getStringTime(timeNormalMax) +
                " min " + d.getStringTime(timeNormalMin) +
                " avg2 " + d.getStringTime(timeNormalOverall / count) +
                " Queries max " + Collections.max(nqueries) +
                " min " + Collections.min(nqueries) +
                " avg2 " + avg(nqueries)
        );
        logger.info("needed dual " + d.getStringTime(timeDualOverall) +
                " max " + d.getStringTime(timeDualMax) +
                " min " + d.getStringTime(timeDualMin) +
                " avg2 " + d.getStringTime(timeDualOverall / count) +
                " Queries max " + Collections.max(dqueries) +
                " min " + Collections.min(dqueries) +
                " avg2 " + avg(dqueries));
    }


    public void printData(OWLTheory theory) {
        logger.info("Consistency count: "+theory.getConsistencyCount());
        logger.info("Consistency time: "+theory.getConsistencyTime());
    }

}
