package at.ainf.owlapi3.performance;

import at.ainf.diagnosis.quickxplain.DirectDiagnosis;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.tree.*;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.CostsEstimator;
import at.ainf.diagnosis.tree.SimpleCostsEstimator;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.logging.SimulatedCalculationTest;
import at.ainf.logging.aop.ProfVarLogWatch;
import at.ainf.logging.aop.ProfiledVar;
import at.ainf.owlapi3.base.CalculateDiagnoses;
import at.ainf.owlapi3.base.OntologySession;
import at.ainf.owlapi3.base.SimulatedSession;
import at.ainf.owlapi3.model.OWLIncoherencyExtractor;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.parser.MyOWLRendererParser;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.*;
import at.ainf.owlapi3.base.distribution.ExtremeDistribution;
import at.ainf.owlapi3.base.distribution.ModerateDistribution;
import at.ainf.owlapi3.base.tools.TableList;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import junit.framework.Assert;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.junit.Ignore;
import org.junit.Test;
import org.perf4j.aop.Profiled;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 21.03.12
 * Time: 08:26
 * To change this template use File | Settings | File Templates.
 */
public class OntologyTests extends OntologySession {

    private static Logger logger = LoggerFactory.getLogger(OntologyTests.class.getName());

    //private boolean showElRates = true;


    //public static int NUMBER_OF_HITTING_SETS = 9;
    //protected static BigDecimal SIGMA = new BigDecimal("100");
    //protected static boolean userBrk = true;

    //protected int diagnosesCalc = 0;
    //protected int conflictsCalc = 0;
    //protected String daStr = "";

    //public enum QSSType {MINSCORE, SPLITINHALF, STATICRISK, DYNAMICRISK, PENALTY, NO_QSS};


    //private boolean traceDiagnosesAndQueries = false;
    //private boolean minimizeQuery = false;


    /*protected BreadthFirstSearch<OWLLogicalAxiom> createBreathFirstSearch(OWLTheory th, boolean dual) {

       SimpleStorage<OWLLogicalAxiom> storage;
       if (dual)
           storage = new DualStorage<OWLLogicalAxiom>();
       else
           storage = new SimpleStorage<OWLLogicalAxiom>();
       BreadthFirstSearch<OWLLogicalAxiom> start = new BreadthFirstSearch<OWLLogicalAxiom>(storage);
       if (dual) {
           start.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
           start.setLogic(new DualTreeLogic<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>());
       } else
           start.setSearcher(new QuickXplain<OWLLogicalAxiom>());
       start.setSearchable(th);

       return start;
   } */

















    @Test
    public void doSimpleQuerySession()
            throws SolverException, InconsistentTheoryException, IOException, OWLOntologyCreationException {

        SimulatedSession session = new SimulatedSession();

        session.setTraceDiagnosesAndQueries(true);
        session.setMinimizeQuery(true);

        session.setNumberOfHittingSets(2);
        QSSType type = QSSType.MINSCORE;
        boolean dual = true;
        String name = "koala.owl";
        //String name = "dualpaper.owl";

        OWLOntology ontology = getOntologySimple("ontologies", name);

        Set<OWLLogicalAxiom> targetDg = new LinkedHashSet<OWLLogicalAxiom>();
        targetDg.add(new MyOWLRendererParser(ontology).parse("Marsupials DisjointWith Person"));
        //targetDg.add(new MyOWLRendererParser(ontology).parse("C SubClassOf not (D or E)"));

        //targetDg.add(new MyOWLRendererParser(ontology).parse("B SubClassOf C"));
        //targetDg.add(new MyOWLRendererParser(ontology).parse("B SubClassOf D"));

        long preprocessModulExtract = System.currentTimeMillis();
        ontology = new OWLIncoherencyExtractor(new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
        preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;

        OWLTheory theory = getExtendTheory(ontology, dual);
        //theory.addEntailedTest(new MyOWLRendererParser(ontology).parse("w Type B"));
        theory.setIncludeClassAssertionAxioms(true);
        theory.setIncludeTrivialEntailments(false);
        theory.setIncludeSubClassOfAxioms(false);
        TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = getUniformCostSearch(theory, dual);
        //((QuickXplain<OWLLogicalAxiom>)start.getSearcher()).setAxiomRenderer(new MyOWLRendererParser(null));

        CostsEstimator es = new SimpleCostsEstimator();
        search.setCostsEstimator(es);

        TableList e = new TableList();
        String message = "act," + type + "," + dual + "," + name + "," + preprocessModulExtract;

        logger.info("start session");
        loggingTest();

        session.setEntry(e);
        session.setScoringFunct(type);
        session.setTargetD(targetDg);
        session.setMessage(message);
        session.setTheory(theory);
        session.setSearch(search);
        session.simulateQuerySession();
        logger.info("stop session ");
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Appender<ILoggingEvent> coalescingStatistics = loggerContext.getLogger(ProfVarLogWatch.DEFAULT_LOGGER_NAME).getAppender("CoalescingStatistics");

        coalescingStatistics.stop();
    }

    @Profiled(tag="time_loggingTest")
    @ProfiledVar(tag = "loggingTest")
    public long loggingTest() {
        logger.info("loggingTest does work");
        new SimulatedCalculationTest().doSimulation();
        return 7;
    }


    protected void doOverallTreeTestEconomy(boolean dual) throws IOException, SolverException, InconsistentTheoryException, NoConflictException {

        SimulatedSession session = new SimulatedSession();

        session.setShowElRates(false);
        QSSType[] qssTypes = new QSSType[]{QSSType.MINSCORE, QSSType.SPLITINHALF, QSSType.DYNAMICRISK};
        //String[] norm = new String[]{"Transportation-SDA"};
        String[] norm = new String[]{"Transportation-SDA", "Economy-SDA"};


        for (SimulatedSession.TargetSource targetSource : new SimulatedSession.TargetSource[]{SimulatedSession.TargetSource.FROM_30_DIAGS}) {
            for (String o : norm) {
                String out = "STAT, " + o;
                TreeSet<FormulaSet<OWLLogicalAxiom>> diagnoses = new CalculateDiagnoses().getDiagnoses("ontologies/"+o+".owl", -1);
                for (QSSType type : qssTypes) {
                    for (DiagProbab diagProbab : DiagProbab.values()) {
                        for (int i = 0; i < 20; i++) {


                        OWLOntology ontology = getOntologySimple("queryontologies", o + ".owl");
                        //OWLOntology ontology = createOwlOntology2(m.trim(), o.trim());
                        long preprocessModulExtract = System.currentTimeMillis();
                        ontology = new OWLIncoherencyExtractor(
                                new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
                        preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;
                        Set<OWLLogicalAxiom> targetDg;
                        OWLTheory theory = getExtendTheory(ontology, dual);
                            TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = getUniformCostSearch(theory, dual);

                            HashMap<ManchesterOWLSyntax, BigDecimal> map = getProbabMap();
                            OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theory);
                            es.updateKeywordProb(map);
                            search.setCostsEstimator(es);

                            targetDg = null;

                            search.setCostsEstimator(es);

                            search.reset();

                            targetDg = chooseTargetDiagnosis(diagProbab, diagnoses);


                            TableList e = new TableList();
                            out += "," + type + ",";
                            String message = "act," + "," + o.trim() + "," + targetSource + "," + dual
                                    + "," + type + "," + preprocessModulExtract + "," + diagProbab + "," + i;
                            //out += simulateQuerySession(start, theory, diags, e, type, message, allD, search2, t3);

                            session.setEntry(e);
                            session.setMessage(message);
                            session.setScoringFunct(type);
                            session.setTargetD(targetDg);
                            session.setTheory(theory);
                            session.setSearch(search);
                            out += session.simulateQuerySession();

                            //logger.info(out);
                        }
                    }
                }
            }
        }
    }


    @Test
    public void testNormalCasesDual() throws SolverException, InconsistentTheoryException, IOException {

        SimulatedSession session = new SimulatedSession();

        session.setShowElRates(false);
        int MAX_RUNS = 7+1;
        getRandom().setSeed(121);

        for (String name : new String[]{"Economy-SDA.owl"}) {
            for (boolean dual : new boolean[] {true}) {

                OWLOntology extracted1 = new OWLIncoherencyExtractor(
                        new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(getOntologySimple("queryontologies", name));
                OWLTheory theory1 = getExtendTheory(extracted1, false);
                TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search3 = getUniformCostSearch(theory1, false);

                OWLAxiomKeywordCostsEstimator es1 = new OWLAxiomKeywordCostsEstimator(theory1);

                search3.setCostsEstimator(es1);
                search3.reset();

                TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = search3;



                try {
                    search.start();
                } catch (NoConflictException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                Set<FormulaSet<OWLLogicalAxiom>> diagnoses = Collections.unmodifiableSet(search.getDiagnoses());
                ModerateDistribution moderateDistribution = new ModerateDistribution();
                ExtremeDistribution extremeDistribution = new ExtremeDistribution();
                search.reset();

                String out = "";

                for (UsersProbab usersProbab : new UsersProbab[]{UsersProbab.MODERATE}) {
                    for (DiagProbab diagProbab : new DiagProbab[]{DiagProbab.BAD}) {
                        for (int run = 7; run < MAX_RUNS; run++) {

                            OWLOntology extracted = new OWLIncoherencyExtractor(
                                    new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(getOntologySimple("queryontologies", name));
                            OWLTheory theory = getExtendTheory(extracted, dual);
                            TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search1 = getUniformCostSearch(theory, dual);

                            OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theory);

                            search1.setCostsEstimator(es);
                            search1.reset();

                            TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search2 = search1;
                            diagnoses = chooseUserProbab(usersProbab, search2, diagnoses,extremeDistribution,moderateDistribution);
                            FormulaSet<OWLLogicalAxiom> targetDiag = chooseTargetDiagnosis(diagProbab, new TreeSet<FormulaSet<OWLLogicalAxiom>>(diagnoses));





                            TableList e = new TableList();
                            String message = "act," + name + "," + dual + "," + usersProbab + ","
                                    + diagProbab + "," + run  ;

                            session.setEntry(e);
                            session.setScoringFunct(QSSType.MINSCORE);
                            session.setTargetD(targetDiag);
                            session.setMessage(message);
                            session.setTheory((OWLTheory) search2.getSearchable());
                            session.setSearch(search2);
                            out += session.simulateQuerySession();

                        }
                    }
                }
            }
        }





    }


    protected long computeDual(TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> searchDual, OWLTheory theoryDual,
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
                " time " + getStringTime(timeDual) + " found correct diag " + foundCorrectD2 +
                " diagnoses: " + diagnosesCalc + " conflict  " + conflictsCalc +
                " has negative tests " + hasNegativeTestcases + " diagnoses in storage " + daStr
        );
        Assert.assertTrue(foundCorrectD2);
        theoryDual.getKnowledgeBase().clearTestCases();
        searchDual.reset();
        queries.add(entry2.getMeanQuery());
        return timeDual;
    }

    protected long computeHS(TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> searchNormal,
                             OWLTheory theoryNormal, FormulaSet<OWLLogicalAxiom> diagnoses,
                             List<Double> queries, QSSType type) {
        SimulatedSession session = new SimulatedSession();
        TableList entry = new TableList();
        long timeNormal = System.currentTimeMillis();
        int diagnosesCalc = 0;
        String daStr = "";
        int conflictsCalc = 0;
        session.setEntry(entry);
        session.setMessage("");
        session.setTargetD(diagnoses);
        session.setScoringFunct(type);
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
                getStringTime(timeNormal) + " found correct diag " + foundCorrectD +
                " diagnoses: " + diagnosesCalc + " conflict  " + conflictsCalc +
                " has negative testst " + hasNegativeTestcases + " diagnoses in storage " + daStr
        );
        Assert.assertTrue(foundCorrectD);
        queries.add(entry.getMeanQuery());
        return timeNormal;
    }


    @Test
    public void testCompareDiagnosisMethods() throws SolverException, InconsistentTheoryException, OWLOntologyCreationException, NoConflictException {
        long t = System.currentTimeMillis();

        TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> searchNormal = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        searchNormal.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        searchNormal.setSearcher(new QuickXplain<OWLLogicalAxiom>());
        OWLTheory theoryNormal = getSimpleTheory(getOntologySimple("ontologies", "koala.owl"), false);
        searchNormal.setSearchable(theoryNormal);
        HashMap<ManchesterOWLSyntax, BigDecimal> map = getProbabMap();
        OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theoryNormal);
        es.updateKeywordProb(map);
        searchNormal.setCostsEstimator(es);
        searchNormal.start();
        Set<? extends FormulaSet<OWLLogicalAxiom>> resultNormal = searchNormal.getDiagnoses();

        TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> searchDual = new InvHsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        searchDual.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        searchDual.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
        OWLTheory theoryDual = getSimpleTheory(getOntologySimple("ontologies", "koala.owl"), true);
        searchDual.setSearchable(theoryDual);
        map = getProbabMap();
        es = new OWLAxiomKeywordCostsEstimator(theoryDual);
        es.updateKeywordProb(map);
        searchDual.setCostsEstimator(es);

        theoryNormal.getKnowledgeBase().clearTestCases();
        searchNormal.reset();

        Map<QSSType, DurationStat> ntimes = new HashMap<QSSType, DurationStat>();
        Map<QSSType, DurationStat> dtimes = new HashMap<QSSType, DurationStat>();
        Map<QSSType, List<Double>> nqueries1 = new HashMap<QSSType, List<Double>>();
        Map<QSSType, List<Double>> dqueries1 = new HashMap<QSSType, List<Double>>();
        //DurationStat timeNormalStat = new DurationStat();
        //DurationStat timeDualStat = new DurationStat();


        //List<Double> nqueries = new LinkedList<Double>();
        //List<Double> dqueries = new LinkedList<Double>();

        for (QSSType type : QSSType.values()) {
            logger.info("QSSType: " + type);
            int count = 0;
            ntimes.put(type, new DurationStat());
            dtimes.put(type, new DurationStat());
            nqueries1.put(type, new LinkedList<Double>());
            dqueries1.put(type, new LinkedList<Double>());
            for (FormulaSet<OWLLogicalAxiom> diagnosis : resultNormal) {
                logger.info("iteration " + ++count + " from " + resultNormal.size());
                long timeNormal, timeDual;
                if (count % 2 != 0) {
                    timeNormal = computeHS(searchNormal, theoryNormal, diagnosis, nqueries1.get(type), type);
                    timeDual = computeDual(searchDual, theoryDual, diagnosis, dqueries1.get(type), type);
                } else {
                    timeDual = computeDual(searchDual, theoryDual, diagnosis, dqueries1.get(type), type);
                    timeNormal = computeHS(searchNormal, theoryNormal, diagnosis, nqueries1.get(type), type);
                }
                ntimes.get(type).add(timeNormal);
                dtimes.get(type).add(timeDual);
            }
        }

        long needed = System.currentTimeMillis() - t;
        logger.info("needed overall " + getStringTime(needed));
        for (QSSType type : QSSType.values()) {
            List<Double> nqueries2 = nqueries1.get(type);
            double res1 = 0;
            for (Double qs1 : nqueries2) {
                res1 += qs1;
            }
            logger.info("needed normal " + type + " " + getStringTime(ntimes.get(type).getOverall()) +
                    " max " + getStringTime(ntimes.get(type).getMax()) +
                    " min " + getStringTime(ntimes.get(type).getMin()) +
                    " avg2 " + getStringTime(ntimes.get(type).getMean()) +
                    " Queries max " + Collections.max(nqueries1.get(type)) +
                    " min " + Collections.min(nqueries1.get(type)) +
                    " avg2 " + res1 / nqueries2.size()
            );
            List<Double> nqueries = dqueries1.get(type);
            double res = 0;
            for (Double qs : nqueries) {
                res += qs;
            }
            logger.info("needed dual " + type + " " + getStringTime(dtimes.get(type).getOverall()) +
                    " max " + getStringTime(dtimes.get(type).getMax()) +
                    " min " + getStringTime(dtimes.get(type).getMin()) +
                    " avg2 " + getStringTime(dtimes.get(type).getMean()) +
                    " Queries max " + Collections.max(dqueries1.get(type)) +
                    " min " + Collections.min(dqueries1.get(type)) +
                    " avg2 " + res / nqueries.size());
        }
    }


    class DurationStat {
        long min = Long.MAX_VALUE;
        long max = 0;
        long overall = 0;

        int cnt = 0;

        public long getMin() {
            return min;
        }

        public long getMax() {
            return max;
        }

        public long getMean() {
            return overall / cnt;
        }

        public long getOverall() {
            return overall;
        }

        public void add(long time) {
            if (min > time)
                min = time;
            else if (max < time)
                max = time;
            overall += time;
            cnt++;
        }
    }


    @Ignore
    @Test
    public void queryToDiags()
            throws NoConflictException, SolverException, InconsistentTheoryException, OWLOntologyCreationException {
        String ont = "koala.owl";
        String path = "ontologies";
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        SimulatedSession session = new SimulatedSession();

        HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> searchNormal = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        searchNormal.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        searchNormal.setSearcher(new QuickXplain<OWLLogicalAxiom>());
        OWLTheory theoryNormal = getSimpleTheory(getOntologySimple(path, ont), false);
        searchNormal.setSearchable(theoryNormal);
        HashMap<ManchesterOWLSyntax, BigDecimal> map = getProbabMap();
        OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theoryNormal);
        es.updateKeywordProb(map);
        searchNormal.setCostsEstimator(es);
        searchNormal.start();
        Set<? extends FormulaSet<OWLLogicalAxiom>> resultNormal = searchNormal.getDiagnoses();

        manager = OWLManager.createOWLOntologyManager();
        InvHsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> searchDual = new InvHsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        searchNormal.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        searchDual.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
        OWLTheory theoryDual = getSimpleTheory(getOntologySimple(path, ont), true);
        searchDual.setSearchable(theoryDual);
        map = getProbabMap();
        es = new OWLAxiomKeywordCostsEstimator(theoryDual);
        es.updateKeywordProb(map);
        searchDual.setCostsEstimator(es);

        theoryNormal.getKnowledgeBase().clearTestCases();
        searchNormal.reset();

        for (FormulaSet<OWLLogicalAxiom> diagnoses : resultNormal) {
            TableList entry = new TableList();
            session.setEntry(entry);
            session.setScoringFunct(null);
            session.setTargetD(diagnoses);
            session.setMessage("");
            session.setTheory(theoryNormal);
            session.setSearch(searchNormal);
            session.simulateQuerySession();
            theoryNormal.getKnowledgeBase().clearTestCases();
            searchNormal.reset();
            assert(entry.getMeanWin() == 1);
        }

        for (FormulaSet<OWLLogicalAxiom> diagnoses : resultNormal) {
            TableList entry = new TableList();

            session.setEntry(entry);
            session.setScoringFunct(null);
            session.setMessage("");
            session.setTargetD(diagnoses);
            session.setTheory(theoryDual);
            session.setSearch(searchDual);
            session.simulateQuerySession();
            theoryDual.getKnowledgeBase().clearTestCases();
            searchDual.reset();
            assert (entry.getMeanWin() == 1);
        }

    }

    public enum DiagProbab {
        GOOD, AVERAGE, BAD
    }

    public enum UsersProbab {
        EXTREME, MODERATE, UNIFORM
    }









    @Test
    public void doOverallDualTreeTestEconomy()
            throws SolverException, InconsistentTheoryException, IOException, NoConflictException {
        doOverallTreeTestEconomy(true);
    }

    @Test
    public void doOverallHsTreeTestEconomy()
            throws SolverException, InconsistentTheoryException, IOException, NoConflictException {
        doOverallTreeTestEconomy(false);
    }



    @Test
    public void doSearchNoDiagFound() throws IOException, SolverException, InconsistentTheoryException, NoConflictException {

        SimulatedSession session = new SimulatedSession();

        session.setShowElRates(false);
        QSSType[] qssTypes =
                new QSSType[]{QSSType.MINSCORE};
        String[] norm = new String[]{"Transportation-SDA"};


        for (SimulatedSession.TargetSource targetSource : new SimulatedSession.TargetSource[]{SimulatedSession.TargetSource.FROM_30_DIAGS}) {
            for (String o : norm) {
                String out = "STAT, " + o;
                TreeSet<FormulaSet<OWLLogicalAxiom>> diagnoses = new CalculateDiagnoses().getDiagnoses("ontologies/"+o+".owl", -1);
                for (QSSType type : qssTypes) {
                    for (DiagProbab diagProbab : new DiagProbab[]{DiagProbab.GOOD}) {
                        for (int i = 0; i < 1500; i++) {


                            OWLOntology ontology = getOntologySimple("queryontologies", o + ".owl");
                            //OWLOntology ontology = createOwlOntology2(m.trim(), o.trim());
                            long preprocessModulExtract = System.currentTimeMillis();
                            ontology = new OWLIncoherencyExtractor(
                                    new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
                            preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;
                            Set<OWLLogicalAxiom> targetDg;
                            OWLTheory theory = getExtendTheory(ontology, true);
                            TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = getUniformCostSearch(theory, true);

                            HashMap<ManchesterOWLSyntax, BigDecimal> map = getProbabMap();
                            OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theory);
                            es.updateKeywordProb(map);
                            search.setCostsEstimator(es);

                            targetDg = null;

                            search.setCostsEstimator(es);

                            search.reset();

                            //diags = getDualTreeTranspErrDiag();
                            targetDg = chooseTargetDiagnosis(diagProbab, diagnoses);


                            TableList e = new TableList();
                            out += "," + type + ",";
                            String message = "act," + "," + o.trim() + "," + targetSource + "," +
                                    "," + type + "," + preprocessModulExtract + "," + diagProbab + "," + i;
                            logger.info("target diagnosis:" + targetDg.size() + " " + renderAxioms(targetDg));
                            //out += simulateQuerySession(start, theory, diags, e, type, message, allD, search2, t3);

                            session.setEntry(e);
                            session.setMessage(message);
                            session.setScoringFunct(type);
                            session.setTargetD(targetDg);
                            session.setTheory(theory);
                            session.setSearch(search);
                            out += session.simulateQuerySession();

                            //logger.info(out);
                        }
                    }
                }
            }
        }
    }


}
