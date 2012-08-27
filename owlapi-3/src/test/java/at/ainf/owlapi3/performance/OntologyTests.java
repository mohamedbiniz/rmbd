package at.ainf.owlapi3.performance;

import at.ainf.diagnosis.quickxplain.DirectDiagnosis;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.tree.*;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
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

    protected Random rnd = new Random();


    //private boolean traceDiagnosesAndQueries = false;
    //private boolean minimizeQuery = false;


    /*protected BreadthFirstSearch<OWLLogicalAxiom> createBreathFirstSearch(OWLTheory th, boolean dual) {

       SimpleStorage<OWLLogicalAxiom> storage;
       if (dual)
           storage = new DualStorage<OWLLogicalAxiom>();
       else
           storage = new SimpleStorage<OWLLogicalAxiom>();
       BreadthFirstSearch<OWLLogicalAxiom> search = new BreadthFirstSearch<OWLLogicalAxiom>(storage);
       if (dual) {
           search.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
           search.setLogic(new DualTreeLogic<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>());
       } else
           search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
       search.setTheory(th);

       return search;
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
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = getSearch(theory, dual);
        //((NewQuickXplain<OWLLogicalAxiom>)search.getSearcher()).setAxiomRenderer(new MyOWLRendererParser(null));

        CostsEstimator es = new SimpleCostsEstimator();
        search.setCostsEstimator(es);

        TableList e = new TableList();
        String message = "act," + type + "," + dual + "," + name + "," + preprocessModulExtract;

        logger.info("start session");
        loggingTest();
        session.simulateQuerySession(search, theory, targetDg, e, type, message, null, null, null);
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
                TreeSet<AxiomSet<OWLLogicalAxiom>> diagnoses = new CalculateDiagnoses().getDiagnoses("ontologies/"+o+".owl");
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
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = getSearch(theory, dual);

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
                            //out += simulateQuerySession(search, theory, diags, e, type, message, allD, search2, t3);

                            out += session.simulateQuerySession(search, theory, targetDg, e, type, message, null, null, null);

                            //logger.info(out);
                        }
                    }
                }
            }
        }
    }





    public AxiomSet<OWLLogicalAxiom> chooseTargetDiagnosis
            (DiagProbab
                     diagProbab, TreeSet<AxiomSet<OWLLogicalAxiom>> diagnoses) {


        BigDecimal sum = new BigDecimal("0");
        TreeSet<AxiomSet<OWLLogicalAxiom>> res;
        TreeSet<AxiomSet<OWLLogicalAxiom>> good = new TreeSet<AxiomSet<OWLLogicalAxiom>>();
        TreeSet<AxiomSet<OWLLogicalAxiom>> avg = new TreeSet<AxiomSet<OWLLogicalAxiom>>();
        TreeSet<AxiomSet<OWLLogicalAxiom>> bad = new TreeSet<AxiomSet<OWLLogicalAxiom>>();

        for (AxiomSet<OWLLogicalAxiom> hs : diagnoses.descendingSet()) {
            if (sum.compareTo(BigDecimal.valueOf(0.33)) <= 0) {
                good.add(hs);
            } else if (sum.compareTo(BigDecimal.valueOf(0.33)) >= 0 && sum.compareTo(BigDecimal.valueOf(0.66)) <= 0) {
                avg.add(hs);
            } else if (sum.compareTo(BigDecimal.valueOf(0.66)) >= 0) {
                bad.add(hs);
            }
            sum = sum.add(hs.getMeasure());
        }
        switch (diagProbab) {
            case GOOD:
                while (good.size() < 3) {
                    if (!avg.isEmpty()) {
                        good.add(avg.pollLast());
                    } else if (!bad.isEmpty())
                        good.add(bad.pollLast());
                    else
                        break;
                }
                res = good;
                break;
            case AVERAGE:
                if (avg.size() < 3 && !good.isEmpty())
                    avg.add(good.pollFirst());
                while (avg.size() < 3) {
                    if (!bad.isEmpty())
                        avg.add(bad.pollLast());
                    else break;
                }
                res = avg;
                break;
            default: {
                if (bad.size() < 3)
                    logger.error("No diagnoses in bad! " + diagnoses);
                while (bad.size() < 3) {
                    if (!avg.isEmpty()) {
                        bad.add(avg.pollFirst());
                    } else if (!good.isEmpty())
                        bad.add(good.pollFirst());
                    else
                        break;
                }
                res = bad;
            }
        }

        int number = rnd.nextInt(res.size());


        int i = 1;
        AxiomSet<OWLLogicalAxiom> next = null;
        for (Iterator<AxiomSet<OWLLogicalAxiom>> it = res.descendingIterator(); it.hasNext(); i++) {
            next = it.next();
            if (i == number)
                break;
        }
        logger.info(diagProbab + ": selected target diagnosis " + next + " positioned " + number + " out of " + res.size());
        return next;
    }


    @Test
    public void testNormalCasesDual() throws SolverException, InconsistentTheoryException, IOException {

        SimulatedSession session = new SimulatedSession();

        session.setShowElRates(false);
        int MAX_RUNS = 7+1;
        rnd = new Random(121);

        for (String name : new String[]{"Economy-SDA.owl"}) {
            for (boolean dual : new boolean[] {true}) {

                OWLOntology extracted1 = new OWLIncoherencyExtractor(
                        new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(getOntologySimple("queryontologies", name));
                OWLTheory theory1 = getExtendTheory(extracted1, false);
                TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> search3 = getSearch(theory1, false);

                OWLAxiomKeywordCostsEstimator es1 = new OWLAxiomKeywordCostsEstimator(theory1);

                search3.setCostsEstimator(es1);
                search3.reset();

                TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = search3;



                try {
                    search.run();
                } catch (NoConflictException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                Set<AxiomSet<OWLLogicalAxiom>> diagnoses = Collections.unmodifiableSet(search.getDiagnoses());
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
                            TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> search1 = getSearch(theory, dual);

                            OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theory);

                            search1.setCostsEstimator(es);
                            search1.reset();

                            TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> search2 = search1;
                            diagnoses = chooseUserProbab(usersProbab, search2, diagnoses,extremeDistribution,moderateDistribution);
                            AxiomSet<OWLLogicalAxiom> targetDiag = chooseTargetDiagnosis(diagProbab, new TreeSet<AxiomSet<OWLLogicalAxiom>>(diagnoses));





                            TableList e = new TableList();
                            String message = "act," + name + "," + dual + "," + usersProbab + ","
                                    + diagProbab + "," + run  ;

                            out += session.simulateQuerySession(search2, (OWLTheory) search2.getTheory(), targetDiag, e, QSSType.MINSCORE, message, null, null, null);

                        }
                    }
                }
            }
        }





    }

    private Set<AxiomSet<OWLLogicalAxiom>> sortDiagnoses(Set<AxiomSet<OWLLogicalAxiom>> axiomSets) {
        TreeSet<AxiomSet<OWLLogicalAxiom>> phs = new TreeSet<AxiomSet<OWLLogicalAxiom>>();
        for (AxiomSet<OWLLogicalAxiom> hs : axiomSets)
            phs.add(hs);
        return Collections.unmodifiableSet(phs);
    }

    private void shuffleKeyword(ArrayList<ManchesterOWLSyntax> keywordList) {
        ArrayList<ManchesterOWLSyntax> cp = new ArrayList<ManchesterOWLSyntax>(keywordList.size());
        cp.addAll(keywordList);
        keywordList.clear();
        for (int i = 0; cp.size() > 0; i++) {
            int j = rnd.nextInt(cp.size());
            keywordList.add(i, cp.remove(j));
        }
        keywordList.addAll(cp);
    }

    private Set<AxiomSet<OWLLogicalAxiom>> chooseUserProbab
            (UsersProbab
                     usersProbab, TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> search, Set<AxiomSet<OWLLogicalAxiom>> diagnoses, ExtremeDistribution extremeDistribution, ModerateDistribution moderateDistribution) {
        Map<ManchesterOWLSyntax, BigDecimal> keywordProbs = new HashMap<ManchesterOWLSyntax, BigDecimal>();
        //ProbabilityTableModel m = new ProbabilityTableModel();
        ArrayList<ManchesterOWLSyntax> keywordList = new ArrayList<ManchesterOWLSyntax>(EnumSet.copyOf(getProbabMap().keySet()));
        ManchesterOWLSyntax[] selectedKeywords = new ManchesterOWLSyntax[]{ManchesterOWLSyntax.SOME, ManchesterOWLSyntax.ONLY,
                ManchesterOWLSyntax.DISJOINT_CLASSES, ManchesterOWLSyntax.DISJOINT_WITH, ManchesterOWLSyntax.SUBCLASS_OF,
                ManchesterOWLSyntax.EQUIVALENT_CLASSES, ManchesterOWLSyntax.NOT, ManchesterOWLSyntax.AND};

        /*
        keywordList = new ArrayList<ManchesterOWLSyntax>(Arrays.asList(selectedKeywords));
        shuffleKeyword(keywordList);
        */
        //Set<Integer> highKeywordPos = new HashSet<Integer>();

        List<ManchesterOWLSyntax> c = new ArrayList<ManchesterOWLSyntax>(Arrays.asList(selectedKeywords));
        for (int i = 0; i < c.size() / 2; i++) {
            int j = rnd.nextInt(c.size());
            keywordList.remove(c.get(j));
        }
        c.removeAll(keywordList);
        shuffleKeyword(keywordList);
        for (int i = 0; c.size() > 0; i++) {
            int j = rnd.nextInt(c.size());
            keywordList.add(i, c.remove(j));
        }
        //keywordList.addAll(c);

        int n = keywordList.size();
        int k = n / 4;
        double[] probabilities;

        switch (usersProbab) {
            case EXTREME:
                probabilities = extremeDistribution.getProbabilities(n);
                for (ManchesterOWLSyntax keyword : keywordList) {
                    keywordProbs.put(keyword, BigDecimal.valueOf(probabilities[keywordList.indexOf(keyword)]));
                }
                /*highKeywordPos.add(rnd.nextInt(n));
                for(ManchesterOWLSyntax keyword : keywordList) {
                    if (highKeywordPos.contains(keywordList.indexOf(keyword)))
                        keywordProbs.put(keyword,getProbabBetween(LOWER_BOUND_EXTREME_HIGH,HIGHER_BOUND_EXTREME_HIGH));
                    else
                        keywordProbs.put(keyword, getProbabBetween(LOWER_BOUND_EXTREME_LOW,HIGHER_BOUND_EXTREME_LOW));
                }*/
                break;
            case MODERATE:
                probabilities = moderateDistribution.getProbabilities(n);
                for (ManchesterOWLSyntax keyword : keywordList) {
                    keywordProbs.put(keyword, BigDecimal.valueOf(probabilities[keywordList.indexOf(keyword)]));
                }
                /*for (int i = 0; i < k; i++) {
                    int num = rnd.nextInt(n);
                    while (!highKeywordPos.add(num))
                        num = rnd.nextInt();
                }
                for(ManchesterOWLSyntax keyword : keywordList) {
                    if (highKeywordPos.contains(keywordList.indexOf(keyword)))
                        keywordProbs.put(keyword,getProbabBetween(LOWER_BOUND_MODERATE_HIGH,HIGHER_BOUND_MODERATE_HIGH));
                    else
                        keywordProbs.put(keyword, getProbabBetween(LOWER_BOUND_MODERATE_LOW,HIGHER_BOUND_MODERATE_LOW));
                }*/
                break;
            case UNIFORM:
                for (ManchesterOWLSyntax keyword : keywordList) {
                    keywordProbs.put(keyword, BigDecimal.valueOf(1.0 / n));
                }
                break;
        }
        ((OWLAxiomKeywordCostsEstimator)search.getCostsEstimator()).setKeywordProbabilities(keywordProbs, diagnoses);
        return sortDiagnoses(diagnoses);


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
                " time " + getStringTime(timeDual) + " found correct diag " + foundCorrectD2 +
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

        session.simulateQuerySession(searchNormal, theoryNormal, diagnoses, entry, type, "", null, null, null);
        timeNormal = System.currentTimeMillis() - timeNormal;
        AxiomSet<OWLLogicalAxiom> diag = getMostProbable(searchNormal.getDiagnoses());
        boolean foundCorrectD = diag.equals(diagnoses);
        boolean hasNegativeTestcases = searchNormal.getTheory().getNonentailedTests().size() > 0;
        theoryNormal.clearTestCases();
        searchNormal.reset();
        logger.info("hstree iteration finished: window size "
                + entry.getMeanWin() + " num of query " + entry.getMeanQuery() + " time " +
                getStringTime(timeNormal) + " found correct diag " + foundCorrectD +
                " diagnoses: " + diagnosesCalc + " conflict  " + conflictsCalc +
                " has negative testst " + hasNegativeTestcases + " diagnoses in storage " + daStr +
                " cached subsets " + theoryNormal.getCache().size()
        );
        Assert.assertTrue(foundCorrectD);
        queries.add(entry.getMeanQuery());
        return timeNormal;
    }

    private void compareDualWithHS(String ontology) throws SolverException, InconsistentTheoryException, OWLOntologyCreationException, NoConflictException {
        long t = System.currentTimeMillis();
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> searchNormal = new HsTreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        searchNormal.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        searchNormal.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
        OWLTheory theoryNormal = getSimpleTheory(getOntologySimple("ontologies", ontology), false);
        searchNormal.setTheory(theoryNormal);
        theoryNormal.useCache(false, 0);
        HashMap<ManchesterOWLSyntax, BigDecimal> map = getProbabMap();
        OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theoryNormal);
        es.updateKeywordProb(map);
        searchNormal.setCostsEstimator(es);
        searchNormal.run();
        Set<? extends AxiomSet<OWLLogicalAxiom>> resultNormal = searchNormal.getDiagnoses();

        manager = OWLManager.createOWLOntologyManager();
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> searchDual = new InvHsTreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        searchDual.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        searchDual.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
        OWLTheory theoryDual = getSimpleTheory(getOntologySimple("ontologies", ontology), true);
        theoryDual.useCache(false, 0);
        searchDual.setTheory(theoryDual);
        map = getProbabMap();
        es = new OWLAxiomKeywordCostsEstimator(theoryDual);
        es.updateKeywordProb(map);
        searchDual.setCostsEstimator(es);

        theoryNormal.clearTestCases();
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
            for (AxiomSet<OWLLogicalAxiom> diagnosis : resultNormal) {
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


    @Test
    public void testCompareDiagnosisMethods() throws SolverException, InconsistentTheoryException, OWLOntologyCreationException, NoConflictException {
        compareDualWithHS("koala.owl");
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

        HsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> searchNormal = new HsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        searchNormal.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        searchNormal.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
        OWLTheory theoryNormal = getSimpleTheory(getOntologySimple(path, ont), false);
        searchNormal.setTheory(theoryNormal);
        HashMap<ManchesterOWLSyntax, BigDecimal> map = getProbabMap();
        OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theoryNormal);
        es.updateKeywordProb(map);
        searchNormal.setCostsEstimator(es);
        searchNormal.run();
        Set<? extends AxiomSet<OWLLogicalAxiom>> resultNormal = searchNormal.getDiagnoses();

        manager = OWLManager.createOWLOntologyManager();
        InvHsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> searchDual = new InvHsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        searchNormal.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        searchDual.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
        OWLTheory theoryDual = getSimpleTheory(getOntologySimple(path, ont), true);
        searchDual.setTheory(theoryDual);
        map = getProbabMap();
        es = new OWLAxiomKeywordCostsEstimator(theoryDual);
        es.updateKeywordProb(map);
        searchDual.setCostsEstimator(es);

        theoryNormal.clearTestCases();
        searchNormal.reset();

        for (AxiomSet<OWLLogicalAxiom> diagnoses : resultNormal) {
            TableList entry = new TableList();
            session.simulateQuerySession(searchNormal, theoryNormal, diagnoses, entry, null, "", null, null, null);
            theoryNormal.clearTestCases();
            searchNormal.reset();
            assert(entry.getMeanWin() == 1);
        }

        for (AxiomSet<OWLLogicalAxiom> diagnoses : resultNormal) {
            TableList entry = new TableList();
            session.simulateQuerySession(searchDual, theoryDual, diagnoses, entry, null, "", null, null, null);
            theoryDual.clearTestCases();
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
                TreeSet<AxiomSet<OWLLogicalAxiom>> diagnoses = new CalculateDiagnoses().getDiagnoses("ontologies/"+o+".owl");
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
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = getSearch(theory, true);

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
                            //out += simulateQuerySession(search, theory, diags, e, type, message, allD, search2, t3);

                            out += session.simulateQuerySession(search, theory, targetDg, e, type, message, null, null, null);

                            //logger.info(out);
                        }
                    }
                }
            }
        }
    }


}
