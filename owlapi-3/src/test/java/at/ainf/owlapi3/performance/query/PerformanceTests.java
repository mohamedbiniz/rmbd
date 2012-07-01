package at.ainf.owlapi3.performance.query;

import at.ainf.diagnosis.model.ITheory;
import at.ainf.diagnosis.partitioning.CKK;
import at.ainf.diagnosis.partitioning.Partitioning;
import at.ainf.diagnosis.partitioning.scoring.QSS;
import at.ainf.diagnosis.partitioning.scoring.QSSFactory;
import at.ainf.diagnosis.quickxplain.DirectDiagnosis;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.storage.Partition;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.InvHsTreeSearch;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.utils.CreationUtils;
import at.ainf.owlapi3.utils.SimulatedSession;
import at.ainf.owlapi3.utils.Utils;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.owlapi3.utils.distribution.ExtremeDistribution;
import at.ainf.owlapi3.utils.distribution.ModerateDistribution;
import at.ainf.owlapi3.performance.query.table.TableList;
import at.ainf.owlapi3.performance.query.table.Time;
import at.ainf.owlapi3.performance.query.table.UserProbAndQualityTable;
import junit.framework.Assert;
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
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import static at.ainf.diagnosis.tree.Rounding.PRECISION;
import static at.ainf.diagnosis.tree.Rounding.ROUNDING_MODE;
import static at.ainf.owlapi3.utils.Constants.DiagProbab;
import static at.ainf.owlapi3.utils.Constants.UsersProbab;

import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

import static at.ainf.owlapi3.utils.SimulatedSession.QSSType;

import static junit.framework.Assert.assertTrue;

//import at.ainf.protegeview.controlpanel.ProbabilityTableModel;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 16.05.11
 * Time: 16:36
 * To change this template use File | Settings | File Templates.
 */
public class PerformanceTests {//extends BasePerformanceTests {

    private static Logger logger = Logger.getLogger(PerformanceTests.class.getName());


    //private final String[] ontologies = {"CHEM-A.owl", "src/test/resources/koala2.owl", "buggy-sweet-jpl.owl", "miniTambis.owl", "Univ2.owl",
    //        "Economy-SDA.owl", "Transportation-SDA.owl"};
    // "koala2.owl",
    protected String[] ontologies = {"ecai.owl"};
    //private String[] ontologies = {"opengalen-no-propchainsmod.owl"};

    protected final String queryontologies = "ontologies";

    // chemical koala sweet univ minitambis
    // dice-A chem-A univ koala - NEW
    protected int MAX_RUNS = 1;


    protected ModerateDistribution moderateDistribution;

    protected ExtremeDistribution extremeDistribution;

    protected QSSType scoringFunc = QSSType.MINSCORE;

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


    @BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlapi3-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }


    protected OWLOntology createOwlOntology(String fileName) {
        String path = ClassLoader.getSystemResource(queryontologies).getPath();
        File ontF = new File(path + "/" + fileName);
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        OWLOntology ontology = null;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(ontF);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return ontology;
    }

    protected OWLTheory createOWLTheory(OWLOntology ontology, boolean dual) {
        OWLTheory result = null;

        Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();

        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
            bax.addAll(ontology.getDataPropertyAssertionAxioms(ind));
        }

        /*String iri = "http://ainf.at/testiri#";

        for (OWLClass ind : ontology.getClassesInSignature()) {
            OWLDataFactory fac = OWLManager.getOWLDataFactory();
            OWLIndividual test_individual = fac.getOWLNamedIndividual(IRI.create(iri + "{"+ind.getIRI().getFragment()+"}"));

            bax.add(fac.getOWLClassAssertionAxiom (ind,test_individual));
        }*/

        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        try {
            //ProbabilityTableModel mo = new ProbabilityTableModel();
            //HashMap<ManchesterOWLSyntax, Double> map = mo.getProbMap();

            if (dual)
                result = new DualTreeOWLTheory(reasonerFactory, ontology, bax);
            else
                result = new OWLTheory(reasonerFactory, ontology, bax);
            result.activateReduceToUns();

            result.setIncludeTrivialEntailments(true);
            // QueryDebuggerPreference.getInstance().setTestIncoherencyToInconsistency(true);

            result.setIncludeSubClassOfAxioms(false);
            result.setIncludeClassAssertionAxioms(false);
            result.setIncludeEquivalentClassAxioms(false);
            result.setIncludeDisjointClassAxioms(false);
            result.setIncludePropertyAssertAxioms(false);
            result.setIncludeReferencingThingAxioms(true);
            result.setIncludeOntologyAxioms(true);
            //  result.setIncludeTrivialEntailments(true);
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return result;
    }

    protected TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> createUniformCostSearch(OWLTheory th, boolean dual) {

        /*SimpleStorage<OWLLogicalAxiom> storage;
        if (dual)
            storage = new SimpleStorage<OWLLogicalAxiom>();
        else
            storage = new SimpleStorage<OWLLogicalAxiom>();*/
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search;
        if (dual) {
            search = new InvHsTreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
            search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        }
        else {
            search = new HsTreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
            search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        }
        if (dual)
            search.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
        else
            search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
        search.setTheory(th);

        return search;
    }

    protected Random rnd = new Random();

    boolean NOOUTPUT = false;


    class ParametersFromFile {
        public String[] ontologies;
        public int MAX_RUNS;
        public boolean dual;
    }

    protected ParametersFromFile readParametersFromFile() {

        ParametersFromFile result = new ParametersFromFile();
        Properties properties = new Properties();
        String config = ClassLoader.getSystemResource("config.properties").getFile();
        BufferedInputStream stream = null;
        try {
            stream = new BufferedInputStream(new FileInputStream(config));
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            properties.load(stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        result.ontologies = properties.getProperty("ontologies").split(",");
        for (int i = 0; i < result.ontologies.length; i++)
            result.ontologies[i] = result.ontologies[i].trim() + ".owl";
        result.MAX_RUNS = Integer.parseInt(properties.getProperty("runs"));
        result.dual = Boolean.parseBoolean(properties.getProperty("method"));
        return result;

    }

    protected void testOrder(Set<AxiomSet<OWLLogicalAxiom>> diagnoses) {
        BigDecimal prob = new BigDecimal("0");
        for (AxiomSet<OWLLogicalAxiom> diag : diagnoses) {
            if (diag.getMeasure().compareTo(prob) < 0)
                throw new IllegalStateException("Set of the diagnoses is not ordered!");
            prob = diag.getMeasure();
        }
    }

    //    public TreeSet<ProbabilisticHittingSet<OWLLogicalAxiom>> updateHittingSetProb(TreeSet<ProbabilisticHittingSet<OWLLogicalAxiom>> hittingSets,
    //                                                                                  UniformCostSearch<OWLLogicalAxiom> search) {
    //        for (ProbabilisticHittingSet<OWLLogicalAxiom> axioms : hittingSets) {
    //            Set<OWLLogicalAxiom> labels = new TreeSet<OWLLogicalAxiom>();
//            Iterator<OWLLogicalAxiom> iterator = axioms.iterator();
//            while (iterator.hasNext())
//                labels.add(iterator.next());
//
//            double probability = ((ConfEntailmentOwlTheory)search.getTheory()).getFailureProbabilityDiagnosis(labels);
//
//            axioms.setMeasure(probability);
//            //axioms.setUserAssignedProbability(probability);
//        }
//        search.normalizeDiagnoses(hittingSets);
//        return sortDiagnoses(hittingSets);
//    }

    private Set<AxiomSet<OWLLogicalAxiom>> sortDiagnoses(Set<AxiomSet<OWLLogicalAxiom>> axiomSets) {
        TreeSet<AxiomSet<OWLLogicalAxiom>> phs = new TreeSet<AxiomSet<OWLLogicalAxiom>>();
        for (AxiomSet<OWLLogicalAxiom> hs : axiomSets)
            phs.add(hs);
        return Collections.unmodifiableSet(phs);
    }

    private BigDecimal sum(Set<? extends AxiomSet> dx) {
        BigDecimal sum = new BigDecimal("0");
        for (AxiomSet hs : dx)
            sum = sum.add(hs.getMeasure());
        return sum;
    }

    public <E extends AxiomSet<OWLLogicalAxiom>> TreeSet<E> normalize(Set<E> hittingSets) {
        TreeSet<E> set = new TreeSet<E>();
        BigDecimal sum = sum(hittingSets);
        for (E hs : hittingSets) {
            BigDecimal value = hs.getMeasure().divide(sum);
            hs.setMeasure(value);
            set.add(hs);
        }
        return set;
    }

    /*
    private void printOl
            (UserProbAndQualityTable
                     table) {
        for (UsersProbab usersProbab : UsersProbab.values()) {
            logger.trace(usersProbab + " \t");
            for (DiagProbab diagProbab : DiagProbab.values()) {
                TableList entry = table.getEntry(usersProbab, diagProbab);
                double mean = entry.getMeanQuery();
                double meanTargetDiagInWin = entry.getMeanTargetDiagInWin();
                double max = entry.getMaxQuery();
                int targetDiagIsMostProbableCounter = entry.getCouTargetDiagIsMostProbable();
                double min = entry.getMinQuery();
                int meanTargetDiagInWinCounter = entry.getTargetDiagInWindowCount();
                double meanTargetDiagIsMostProbable = entry.getMeanTargetDiagIsMostProbable();
                double meanWinSize = entry.getMeanWin();
                double minWinSize = entry.getMinWin();
                double maxWinSize = entry.getMaxWin();
                logger.trace(min + "/" + mean + "/" + max + "/" + meanTargetDiagInWin + "/" + meanTargetDiagInWinCounter +
                        "/" + meanTargetDiagIsMostProbable + "/" + targetDiagIsMostProbableCounter
                        + "/" + minWinSize + "/" + meanWinSize + "/" + maxWinSize + "\t  ");
            }
        }

    }
    */

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
        session.simulateBruteForceOnl(searchDual, theoryDual, diagnosis, entry2, type,"",null,null,null);
        timeDual = System.currentTimeMillis() - timeDual;
        AxiomSet<OWLLogicalAxiom> diag2 = getMostProbable(searchDual.getDiagnoses());
        boolean foundCorrectD2 = diag2.equals(diagnosis);
        boolean hasNegativeTestcases = searchDual.getTheory().getNonentailedTests().size() > 0;

        logger.info("dual tree iteration finished: window size "
                + entry2.getMeanWin() + " num of query " + entry2.getMeanQuery() +
                " time " + Utils.getStringTime(timeDual) + " found correct diag " + foundCorrectD2 +
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


        session.simulateBruteForceOnl(searchNormal, theoryNormal, diagnoses, entry, type,"",null,null,null);
        timeNormal = System.currentTimeMillis() - timeNormal;
        AxiomSet<OWLLogicalAxiom> diag = getMostProbable(searchNormal.getDiagnoses());
        boolean foundCorrectD = diag.equals(diagnoses);
        boolean hasNegativeTestcases = searchNormal.getTheory().getNonentailedTests().size() > 0;
        theoryNormal.clearTestCases();
        searchNormal.reset();
        logger.info("hstree iteration finished: window size "
                + entry.getMeanWin() + " num of query " + entry.getMeanQuery() + " time " +
                Utils.getStringTime(timeNormal) + " found correct diag " + foundCorrectD +
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
        OWLTheory theoryNormal = createTheory(manager, "queryontologies/" + ontology, false);
        searchNormal.setTheory(theoryNormal);
        theoryNormal.useCache(false, 0);
        HashMap<ManchesterOWLSyntax, BigDecimal> map = Utils.getProbabMap();
        OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theoryNormal);
        es.updateKeywordProb(map);
        searchNormal.setCostsEstimator(es);
        searchNormal.run();
        Set<? extends AxiomSet<OWLLogicalAxiom>> resultNormal = searchNormal.getDiagnoses();

        manager = OWLManager.createOWLOntologyManager();
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> searchDual = new InvHsTreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        searchDual.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        searchDual.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
        OWLTheory theoryDual = createTheory(manager, "queryontologies/" + ontology, true);
        theoryDual.useCache(false, 0);
        searchDual.setTheory(theoryDual);
        map = Utils.getProbabMap();
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
        logger.info("needed overall " + Utils.getStringTime(needed));
        for (QSSType type : QSSType.values()) {
            logger.info("needed normal " + type + " " + Utils.getStringTime(ntimes.get(type).getOverall()) +
                    " max " + Utils.getStringTime(ntimes.get(type).getMax()) +
                    " min " + Utils.getStringTime(ntimes.get(type).getMin()) +
                    " avg " + Utils.getStringTime(ntimes.get(type).getMean()) +
                    " Queries max " + Collections.max(nqueries1.get(type)) +
                    " min " + Collections.min(nqueries1.get(type)) +
                    " avg " + avg(nqueries1.get(type))
            );
            logger.info("needed dual " + type + " " + Utils.getStringTime(dtimes.get(type).getOverall()) +
                    " max " + Utils.getStringTime(dtimes.get(type).getMax()) +
                    " min " + Utils.getStringTime(dtimes.get(type).getMin()) +
                    " avg " + Utils.getStringTime(dtimes.get(type).getMean()) +
                    " Queries max " + Collections.max(dqueries1.get(type)) +
                    " min " + Collections.min(dqueries1.get(type)) +
                    " avg " + avg(dqueries1.get(type)));
        }
    }


    @Test
    public void testCompareDiagnosisMethods() throws SolverException, InconsistentTheoryException, OWLOntologyCreationException, NoConflictException {
        compareDualWithHS("ontologies/koala.owl");
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


    private String formD(double d) {
        DecimalFormat fo = new DecimalFormat("##.####");

        if (!new Double(d).equals(Double.NaN))
            return fo.format(d);
        else
            return "NaN";
    }







    protected double avg(List<Double> nqueries) {
        double res = 0;
        for (Double qs : nqueries) {
            res += qs;
        }
        return res / nqueries.size();
    }



    public <E extends OWLObject> void print
            (Collection<E> c) {
        ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
        for (E el : c) {
            System.out.print(renderer.render(el) + ",");
        }
    }






    public OWLTheory createTheory(OWLOntologyManager manager, String path, String file, boolean dual) throws SolverException, InconsistentTheoryException, OWLOntologyCreationException {
        InputStream st = ClassLoader.getSystemResourceAsStream(path);
        OWLOntology ontology = CreationUtils.createOwlOntology(path, file);
        Set<OWLLogicalAxiom> bax = new HashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        OWLTheory theory = null;
        if(dual)
            theory = new DualTreeOWLTheory(reasonerFactory, ontology, bax);
        else
            theory = new OWLTheory(reasonerFactory, ontology, bax);
        //assert (theory.verifyRequirements());

        return theory;
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
        OWLTheory theoryNormal = createTheory(manager, path, ont, false);
        searchNormal.setTheory(theoryNormal);
        HashMap<ManchesterOWLSyntax, BigDecimal> map = Utils.getProbabMap();
        OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theoryNormal);
        es.updateKeywordProb(map);
        searchNormal.setCostsEstimator(es);
        searchNormal.run();
        Set<? extends AxiomSet<OWLLogicalAxiom>> resultNormal = searchNormal.getDiagnoses();

        manager = OWLManager.createOWLOntologyManager();
        InvHsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> searchDual = new InvHsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        searchNormal.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        searchDual.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
        OWLTheory theoryDual = createTheory(manager,path,ont,true);
        searchDual.setTheory(theoryDual);
        map = Utils.getProbabMap();
        es = new OWLAxiomKeywordCostsEstimator(theoryDual);
        es.updateKeywordProb(map);
        searchDual.setCostsEstimator(es);

        theoryNormal.clearTestCases();
        searchNormal.reset();

        for (AxiomSet<OWLLogicalAxiom> diagnoses : resultNormal) {
            TableList entry = new TableList();
            session.simulateBruteForceOnl(searchNormal,theoryNormal,diagnoses,entry, null,"",null,null,null);
            theoryNormal.clearTestCases();
            searchNormal.reset();
            assert(entry.getMeanWin() == 1);
        }

        for (AxiomSet<OWLLogicalAxiom> diagnoses : resultNormal) {
            TableList entry = new TableList();
            session.simulateBruteForceOnl(searchDual,theoryDual,diagnoses,entry, null,"",null,null,null);
            theoryDual.clearTestCases();
            searchDual.reset();
            assert (entry.getMeanWin() == 1);
        }

    }




}
