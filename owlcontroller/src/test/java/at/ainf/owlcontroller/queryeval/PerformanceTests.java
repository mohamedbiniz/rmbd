package at.ainf.owlcontroller.queryeval;

import at.ainf.diagnosis.partitioning.*;
import at.ainf.diagnosis.quickxplain.FastDiagnosis;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import at.ainf.owlcontroller.OWLAxiomNodeCostsEstimator;
import at.ainf.owlcontroller.Utils;
//import at.ainf.protegeview.controlpanel.ProbabilityTableModel;
import at.ainf.theory.model.ITheory;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.DualStorage;
import at.ainf.theory.storage.Partition;
import at.ainf.theory.storage.SimpleStorage;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.UniformCostSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlcontroller.distributiongenerators.ExtremeDistribution;
import at.ainf.owlcontroller.distributiongenerators.ModerateDistribution;
import at.ainf.owlcontroller.queryeval.result.TableList;
import at.ainf.owlcontroller.queryeval.result.Time;
import at.ainf.owlcontroller.queryeval.result.UserProbAndQualityTable;
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

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static _dev.TimeLog.printOverallStats;
import static _dev.TimeLog.printStatsAndClear;
import static junit.framework.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 16.05.11
 * Time: 16:36
 * To change this template use File | Settings | File Templates.
 */
public class PerformanceTests {

    private static Logger logger = Logger.getLogger(PerformanceTests.class.getName());
    public static final int NUMBER_OF_HITTING_SETS = 9;


    //private final String[] ontologies = {"CHEM-A.owl", "src/test/resources/koala.owl", "buggy-sweet-jpl.owl", "miniTambis.owl", "Univ.owl",
    //        "Economy-SDA.owl", "Transportation-SDA.owl"};
    // "koala.owl",
    protected String[] ontologies = {"Univ.owl"};
    //private String[] ontologies = {"opengalen-no-propchainsmod.owl"};

    protected final String queryontologies = "queryontologies";

    // chemical koala sweet univ minitambis
    // dice-A chem-A univ koala - NEW
    protected int MAX_RUNS = 1;
    protected static final double SIGMA = 85;
    protected static final boolean BRUTE = false;
    
    protected static boolean userBrk = false;

    protected Random rnd = new Random();


    protected ModerateDistribution moderateDistribution;

    protected ExtremeDistribution extremeDistribution;


    protected enum ScoringFunc {ENTROPY, SPLITINHALF}

    protected ScoringFunc scoringFunc = ScoringFunc.ENTROPY;

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
        if(dual)
            theory = new DualTreeOWLTheory(reasonerFactory, ontology, bax);
        else
            theory = new OWLTheory(reasonerFactory, ontology, bax);
        //assert (theory.verifyRequirements());

        return theory;
    }

    @Test
    public void queryToDiags2()
            throws NoConflictException, SolverException, InconsistentTheoryException, OWLOntologyCreationException {
        String ont = "Univ.owl";
        long t = System.currentTimeMillis();
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        UniformCostSearch<OWLLogicalAxiom> searchNormal = new UniformCostSearch<OWLLogicalAxiom>(new SimpleStorage<OWLLogicalAxiom>());
        searchNormal.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
        OWLTheory theoryNormal = createTheory(manager, "queryontologies/" + ont, false);
        searchNormal.setTheory(theoryNormal);
        HashMap<ManchesterOWLSyntax, Double> map = Utils.getProbabMap();
        OWLAxiomNodeCostsEstimator es = new OWLAxiomNodeCostsEstimator(theoryNormal);
        es.updateKeywordProb(map);
        searchNormal.setNodeCostsEstimator(es);
        searchNormal.run();
        Set<? extends AxiomSet<OWLLogicalAxiom>> resultNormal = searchNormal.getStorage().getDiagnoses();

        manager = OWLManager.createOWLOntologyManager();
        UniformCostSearch<OWLLogicalAxiom> searchDual = new UniformCostSearch<OWLLogicalAxiom>(new DualStorage<OWLLogicalAxiom>());
        searchDual.setSearcher(new FastDiagnosis<OWLLogicalAxiom>());
        OWLTheory theoryDual = createTheory(manager, "queryontologies/" + ont, true);
        searchDual.setTheory(theoryDual);
        map = Utils.getProbabMap();
        es = new OWLAxiomNodeCostsEstimator(theoryDual);
        es.updateKeywordProb(map);
        searchDual.setNodeCostsEstimator(es);

        theoryNormal.clearTestCases();
        searchNormal.clearSearch();

        long timeNormal = System.currentTimeMillis();
        for (AxiomSet<OWLLogicalAxiom> diagnoses : resultNormal) {
            TableList entry = new TableList();
            simulateBruteForceOnl(searchNormal,theoryNormal,diagnoses,entry);
            theoryNormal.clearTestCases();
            searchNormal.clearSearch();
            logger.info("using hstree remaining queries " + entry.getMeanWin() + " " + Utils.renderAxioms(diagnoses));
        }
        timeNormal = System.currentTimeMillis() - timeNormal;

        long timeDual = System.currentTimeMillis();
        for (AxiomSet<OWLLogicalAxiom> diagnoses : resultNormal) {
            TableList entry = new TableList();
            simulateBruteForceOnl(searchDual,theoryDual,diagnoses,entry);
            theoryDual.clearTestCases();
            searchDual.clearSearch();
            logger.info("using dual tree remaining queries " + entry.getMeanWin() + " " + Utils.renderAxioms(diagnoses));
        }
        timeDual = System.currentTimeMillis() - timeDual;

        long needed = System.currentTimeMillis() - t;
        logger.info("needed overall " + needed);
        logger.info("needed normal " + timeNormal);
        logger.info("needed dual " + timeDual);
    }

    @BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlcontroller-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }

    public Partition<OWLLogicalAxiom> getBestQuery(TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> search, Set<AxiomSet<OWLLogicalAxiom>> diags)
            throws SolverException, InconsistentTheoryException {

        ScoringFunction f = null;
        if (scoringFunc.equals(ScoringFunc.ENTROPY))
            f = new EntropyScoringFunction();
        else if (scoringFunc.equals(ScoringFunc.SPLITINHALF))
            f = new SplitScoringFunction();

        Partitioning<OWLLogicalAxiom> algo;
        String name;
        if (BRUTE) {
            algo = new BruteForce<OWLLogicalAxiom>(search.getTheory(), f);
            name = "BRUTE";
        } else {
            algo = new CKK<OWLLogicalAxiom>(search.getTheory(), f);
            name = "GREEDY";
        }
        Partition<OWLLogicalAxiom> partition = algo.generatePartition(diags);
        /*
        if ((partition == null || partition.score > 0.95d) && !BRUTE) {
            name = "BRUTE";
            partition = new BruteForce<OWLLogicalAxiom>(search.getTheory(), f).generatePartition(diags);
        }
        */
        if (partition != null)
            logger.info(name + ": " + partition.score + " dx:" + partition.dx.size()
                    + " dnx:" + partition.dnx.size() + " dz:" + partition.dz.size());
        else
            logger.error("No partition is found for diagnoses " + diags);
        return partition;
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

            if(dual)
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

    protected UniformCostSearch<OWLLogicalAxiom> createUniformCostSearch(OWLTheory th, boolean dual) {

        SimpleStorage<OWLLogicalAxiom> storage ;
        if (dual)
            storage = new DualStorage<OWLLogicalAxiom>();
        else
            storage = new SimpleStorage<OWLLogicalAxiom>();
        UniformCostSearch<OWLLogicalAxiom> search = new UniformCostSearch<OWLLogicalAxiom>(storage);
        if (dual)
            search.setSearcher(new FastDiagnosis<OWLLogicalAxiom>());
        else
            search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
        search.setTheory(th);

        return search;
    }

    boolean NOOUTPUT = false;

    @Test
    public void doPerformanceTest() {


        readParametersFromFile();
        HashMap<String, UserProbAndQualityTable> map_entropy = new HashMap<String, UserProbAndQualityTable>();
        HashMap<String, UserProbAndQualityTable> map_split = new HashMap<String, UserProbAndQualityTable>();
        moderateDistribution = new ModerateDistribution();
        extremeDistribution = new ExtremeDistribution();

        for (String ontologyFileString : ontologies) {

            UserProbAndQualityTable split_result = new UserProbAndQualityTable();
            UserProbAndQualityTable entropy_result = new UserProbAndQualityTable();

            rnd.setSeed(1641998975);

            logger.info("creating objects for " + ontologyFileString);

            OWLOntology ont = createOwlOntology(ontologyFileString);


            boolean dual = false;
            OWLTheory theory = createOWLTheory(ont,dual);
            UniformCostSearch<OWLLogicalAxiom> search = createUniformCostSearch(theory,dual);
            //ProbabilityTableModel mo = new ProbabilityTableModel();
            HashMap<ManchesterOWLSyntax, Double> map = Utils.getProbabMap();
            OWLAxiomNodeCostsEstimator es = new OWLAxiomNodeCostsEstimator(theory);
            es.updateKeywordProb(map);
            search.setNodeCostsEstimator(es);

            logger.info("searching diagnoses for " + ontologyFileString);
            try {
                search.run();
            } catch (SolverException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (NoConflictException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InconsistentTheoryException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            Set<AxiomSet<OWLLogicalAxiom>> diagnoses =
                    Collections.unmodifiableSet(search.getStorage().getDiagnoses());

            theory.clearTestCases();
            search.clearSearch();

            logger.info("found all diagnoses for " + ontologyFileString);
            for (UsersProbab usersProbab : UsersProbab.values()) {
                for (DiagProbab diagProbab : DiagProbab.values()) {
                    //if (diagProbab == DiagProbab.GOOD && usersProbab == UsersProbab.EXTREME)

                    for (int i = 0; i < MAX_RUNS; i++) {
                        for (AxiomSet<OWLLogicalAxiom> d : diagnoses) {
                            TableList entry = null;
    
                            logger.info(ontologyFileString + " " + usersProbab + " " + diagProbab + " " // + i
                                    + " choosing target diagnoses and user probabilities  ");
    
                            diagnoses = chooseUserProbab(usersProbab, search, diagnoses);
                            testOrder(diagnoses);
                            AxiomSet<OWLLogicalAxiom> targetDiag = chooseTargetDiagnosis(diagProbab, new TreeSet<AxiomSet<OWLLogicalAxiom>>(diagnoses));
                            targetDiag = d;
    
                            for (int j = 0; j < 2; j++) {
                                scoringFunc = ScoringFunc.values()[j];
                                try {
                                    theory.clearTestCases();
                                    search.clearSearch();
    
    
                                    if (entry == null && scoringFunc.equals(ScoringFunc.ENTROPY))
                                        entry = entropy_result.getEntry(usersProbab, diagProbab);
                                    else if (entry == null && scoringFunc.equals(ScoringFunc.SPLITINHALF)) {
                                        entry = split_result.getEntry(usersProbab, diagProbab);
                                    }
                                    logger.info(ontologyFileString + " " + usersProbab + " " + diagProbab + " " // + i
                                            + " starting simulation with " + scoringFunc);
                                    //if (ontologyFileString.equals("buggy-sweet-jpl.owl")
                                    //        && diagProbab == DiagProbab.GOOD && usersProbab == UsersProbab.EXTREME && i > 9)
                                    simulateBruteForceOnl(search, theory, targetDiag, entry);
                                    entry = null;
    
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                    j--;
                                }
    
                                printStatsAndClear("");
    
    
                            }
                        }
                    }

                }
                map_entropy.put(ontologyFileString, entropy_result);
                map_split.put(ontologyFileString, split_result);

            }
            if (!NOOUTPUT) {
                for (String ontologyStr : map_entropy.keySet()) {
                    String message = "Entropy : " + ontologyStr;
                    System.out.println(message);
                    logger.info(message);
                    print(map_entropy.get(ontologyStr));
                }
                for (String ontologyStr : map_split.keySet()) {
                    String message = "Split : " + ontologyStr;
                    System.out.println(message);
                    logger.info(message);
                    print(map_split.get(ontologyStr));
                }
            }
        }
    }

    protected void readParametersFromFile() {
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
        ontologies = properties.getProperty("ontologies").split(",");
        for (int i = 0; i < ontologies.length; i++)
            ontologies[i] = ontologies[i].trim() + ".owl";
        MAX_RUNS= Integer.parseInt(properties.getProperty("runs") );

    }

    protected void testOrder(Set<AxiomSet<OWLLogicalAxiom>> diagnoses) {
        double prob = 0;
        for (AxiomSet<OWLLogicalAxiom> diag : diagnoses) {
            if (diag.getMeasure() < prob)
                throw new IllegalStateException("Set of the diagnoses is not ordered!");
            prob = diag.getMeasure();
        }
    }

    //    public TreeSet<ProbabilisticHittingSet<OWLLogicalAxiom>> updateHittingSetProb(TreeSet<ProbabilisticHittingSet<OWLLogicalAxiom>> hittingSets,
    //                                                                                  UniformCostSearch<OWLLogicalAxiom> search) {
    //        for (ProbabilisticHittingSet<OWLLogicalAxiom> hittingSet : hittingSets) {
    //            Set<OWLLogicalAxiom> labels = new TreeSet<OWLLogicalAxiom>();
//            Iterator<OWLLogicalAxiom> iterator = hittingSet.iterator();
//            while (iterator.hasNext())
//                labels.add(iterator.next());
//
//            double probability = ((ConfEntailmentOwlTheory)search.getTheory()).getFailureProbabilityDiagnosis(labels);
//
//            hittingSet.setMeasure(probability);
//            //hittingSet.setUserAssignedProbability(probability);
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

    private double sum(Set<? extends AxiomSet> dx) {
        double sum = 0;
        for (AxiomSet hs : dx)
            sum += hs.getMeasure();
        return sum;
    }

    public <E extends AxiomSet<OWLLogicalAxiom>> TreeSet<E> normalize(Set<E> hittingSets) {
        TreeSet<E> set = new TreeSet<E>();
        double sum = sum(hittingSets);
        for (E hs : hittingSets) {
            double value = hs.getMeasure() / sum;
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
            logger.info(usersProbab + " \t");
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
                logger.info(min + "/" + mean + "/" + max + "/" + meanTargetDiagInWin + "/" + meanTargetDiagInWinCounter +
                        "/" + meanTargetDiagIsMostProbable + "/" + targetDiagIsMostProbableCounter
                        + "/" + minWinSize + "/" + meanWinSize + "/" + maxWinSize + "\t  ");
            }
        }

    }
    */
    protected void print(UserProbAndQualityTable table) {
        if (table == null)
            return;
        for (UsersProbab usersProbab : UsersProbab.values()) {
            System.out.print(usersProbab + " \t");
            logger.info(usersProbab + " \t");
            for (DiagProbab diagProbab : DiagProbab.values()) {
                TableList entry = table.getEntry(usersProbab, diagProbab);
                if (entry == null)
                    continue;
                double meanQuery = entry.getMeanQuery();
                double meanTargetDiagInWin = entry.getMeanTargetDiagInWin();
                double maxQuery = entry.getMaxQuery();
                int targetDiagIsMostProbableCounter = entry.getCouTargetDiagIsMostProbable();
                double minQuery = entry.getMinQuery();
                int meanTargetDiagInWinCounter = entry.getTargetDiagInWindowCount();
                double meanTargetDiagIsMostProbable = entry.getMeanTargetDiagIsMostProbable();
                double meanWinSize = entry.getMeanWin();
                double minWinSize = entry.getMinWin();
                double maxWinSize = entry.getMaxWin();
                LinkedList<Time> queryTime = entry.getQueryTime();
                LinkedList<Time> diagTime = entry.getDiagTime();
                int userBreakCount = entry.getUserBreakCount();
                int systemBreakCount = entry.getSystemBreakCount();

                String message = formD(minQuery) + "\t" + formD(meanQuery) + "\t" + formD(maxQuery)
                        + "\t" + formD(entry.getMinTime()) + "\t" + formD(entry.getMeanTime()) + "\t" + formD(entry.getMaxTime())
                        + "\t" + formD(entry.getMinTime(queryTime)) + "\t" + formD(entry.getAvgTime(queryTime)) + "\t" + formD(entry.getMaxTime(queryTime))
                        + "\t" + formD(entry.getMinTime(diagTime)) + "\t" + formD(entry.getAvgTime(diagTime)) + "\t" + formD(entry.getMaxTime(diagTime))
                        + "\t" + formD(entry.getAvgQueryCardinality())
                        + "\t"
                        //+ "\t" + formD(meanTargetDiagInWin) + "\t" + formD(meanTargetDiagInWinCounter)
                        //+ "\t" + formD(meanTargetDiagIsMostProbable) + "\t" + formD(targetDiagIsMostProbableCounter)
                        //+ "\t" + formD(minWinSize) + "\t" + formD(meanWinSize) + "\t" + formD(maxWinSize)
                        //+ "\t" + formD(userBreakCount) + "\t" + formD(systemBreakCount) + "\t  "
                        ;
                logger.info(message);
                System.out.print(message);
            }
            System.out.println("");
        }

    }

    private String formD(double d) {
        DecimalFormat fo = new DecimalFormat("##.####");

        if (!new Double(d).equals(Double.NaN))
            return fo.format(d);
        else
            return "NaN";
    }

    public <E extends AxiomSet<OWLLogicalAxiom>> E containsItem(Collection<E> col, E item) {
        for (E o : col) {
            if (o.equals(item)) {
                if (logger.isInfoEnabled())
                    logger.info("Target dianosis " + o + "is in the window");
                return o;
            }
        }
        return null;
    }

    protected void simulateBruteForceOnl
            (UniformCostSearch<OWLLogicalAxiom> search, OWLTheory
                    theory, AxiomSet<OWLLogicalAxiom> targetDiag, TableList
                    entry) {
        //DiagProvider diagProvider = new DiagProvider(search, false, 9);

        Partition<OWLLogicalAxiom> actPa = null;

        Set<AxiomSet<OWLLogicalAxiom>> diagnoses = null;
        int num_of_queries = 0;

        boolean userBreak = false;
        boolean systemBreak = false;

        boolean querySessionEnd = false;
        long time = System.currentTimeMillis();
        Time queryTime = new Time();
        Time diagTime = new Time();
        int queryCardinality = 0;
        while (!querySessionEnd) {
            try {
                Collection<AxiomSet<OWLLogicalAxiom>> lastD = diagnoses;
                logger.trace("numOfQueries: " + num_of_queries + " search for diagnoses");

                userBreak = false;
                systemBreak = false;

                if (actPa != null && actPa.dx.size() == 1 && actPa.dz.size() == 1 && actPa.dnx.isEmpty()) {
                    logger.info("Help!");
                    printc(theory.getEntailedTests());
                    printc(theory.getNonentailedTests());
                    print(actPa.partition);
                    prinths(actPa.dx);
                    prinths(actPa.dz);
                }

                try {
                    long diag = System.currentTimeMillis();
                    search.run(NUMBER_OF_HITTING_SETS);
                    diagnoses = search.getStorage().getDiagnoses();
                    diagTime.setTime(System.currentTimeMillis() - diag);
                } catch (SolverException e) {
                    diagnoses = new TreeSet<AxiomSet<OWLLogicalAxiom>>();

                } catch (NoConflictException e) {
                    diagnoses = new TreeSet<AxiomSet<OWLLogicalAxiom>>(search.getStorage().getDiagnoses());

                }

                if (diagnoses.isEmpty())
                    logger.error("No diagnoses found!");

                // cast should be corrected
                Iterator<AxiomSet<OWLLogicalAxiom>> descendSet = (new TreeSet<AxiomSet<OWLLogicalAxiom>>(diagnoses)).descendingIterator();
                AxiomSet<OWLLogicalAxiom> d = descendSet.next();
                AxiomSet<OWLLogicalAxiom> d1 = (descendSet.hasNext()) ? descendSet.next() : null;

                boolean isTargetDiagFirst = d.equals(targetDiag);
                double dp = d.getMeasure();
                if (logger.isInfoEnabled()) {
                    AxiomSet<OWLLogicalAxiom> o = containsItem(diagnoses, targetDiag);
                    logger.info("diagnoses: " + diagnoses.size() +
                            " first diagnosis: " + d + " is target: " + isTargetDiagFirst + " is in window: " + ((o == null) ? false : o.toString()));
                }

                if (d1 != null) {
                    double d1p = d1.getMeasure();
                    double diff = 100 - (d1p * 100) / dp;
                    logger.trace("difference : " + (dp - d1p) + " - " + diff + " %");
                    if ( userBrk && diff > SIGMA && isTargetDiagFirst && num_of_queries > 0) {
                        // user brake
                        querySessionEnd = true;
                        userBreak = true;
                        break;
                    }
                }

                if (diagnoses.equals(lastD) || diagnoses.size() < 2) {
                    // system brake
                    querySessionEnd = true;
                    systemBreak = true;
                    break;
                }
                Partition<OWLLogicalAxiom> last = actPa;

                logger.trace("numOfQueries: " + num_of_queries + " search for  query");

                long query = System.currentTimeMillis();
                actPa = getBestQuery(search, diagnoses);
                queryTime.setTime(System.currentTimeMillis() - query);

                if (actPa == null || actPa.partition == null || (last != null && actPa.partition.equals(last.partition))) {
                    // system brake
                    querySessionEnd = true;
                    break;
                }
                queryCardinality = actPa.partition.size();

                logger.trace("numOfQueries: " + num_of_queries + " generate answer");
                boolean answer = generateQueryAnswer(search, actPa, targetDiag);
                num_of_queries++;
                // fine all dz diagnoses
                for (AxiomSet<OWLLogicalAxiom> ph : actPa.dz) {
                    ph.setMeasure(0.5d * ph.getMeasure());
                }
                if (answer) {
                    try {
                        search.getTheory().addEntailedTest(new TreeSet<OWLLogicalAxiom>(actPa.partition));
                        if (actPa.dnx.isEmpty() && diagnoses.size() < NUMBER_OF_HITTING_SETS)
                            querySessionEnd = true;
                    } catch (InconsistentTheoryException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                } else {
                    try {
                        search.getTheory().addNonEntailedTest(new TreeSet<OWLLogicalAxiom>(actPa.partition));
                        if (actPa.dx.isEmpty() && diagnoses.size() < NUMBER_OF_HITTING_SETS)
                            querySessionEnd = true;
                    } catch (InconsistentTheoryException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            } catch (SolverException e) {
                querySessionEnd = true;
                logger.error(e);

            } catch (InconsistentTheoryException e) {
                querySessionEnd = true;
                logger.error(e);

            }
        }
        time = System.currentTimeMillis() - time;
        boolean targetDiagnosisIsInWind = false;
        boolean targetDiagnosisIsMostProbable = false;
        if (diagnoses != null) {
            //TreeSet<ProbabilisticHittingSet> diags = new TreeSet<ProbabilisticHittingSet>(diagnoses);
            for (AxiomSet<OWLLogicalAxiom> ps : diagnoses)
                if (ps.equals(targetDiag))
                    targetDiagnosisIsInWind = true;
            if (diagnoses.size() >= 1 && ( new TreeSet<AxiomSet<OWLLogicalAxiom>>(diagnoses)).last().equals(targetDiag)) {
                targetDiagnosisIsMostProbable = true;
                targetDiagnosisIsInWind = true;
            }
        }
        int diagWinSize = 0;
        if (diagnoses != null)
            diagWinSize = diagnoses.size();
        logger.info("Iteration finished within " + time + " ms, required " + num_of_queries + " queries, most probable "
                + targetDiagnosisIsMostProbable + " is in window " + targetDiagnosisIsInWind + " size of window  " + diagWinSize);
        entry.addEntr(num_of_queries, queryCardinality, targetDiagnosisIsInWind, targetDiagnosisIsMostProbable, diagWinSize, userBreak, systemBreak, time, queryTime, diagTime);

    }


    private <E extends OWLObject> void prinths
            (Collection<AxiomSet<E>> c) {
        for (AxiomSet<E> hs : c) {
            System.out.println(hs);
            print(hs);
        }

    }

    private <E extends OWLObject> void printc
            (Collection<? extends Collection<E>> c) {
        for (Collection<E> hs : c) {
            System.out.println("Test case:");
            print(hs);
        }
    }

    private <E extends OWLObject> void print
            (Collection<E> c) {
        ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
        for (E el : c) {
            System.out.println(renderer.render(el));
        }
    }

    private void print(TreeSet<AxiomSet<OWLLogicalAxiom>> diagnoses) {
        for (AxiomSet<OWLLogicalAxiom> hs : diagnoses.descendingSet()) {
            System.out.println(hs.toString());
        }
    }

    /*
private void simulateQuerySession
        (UniformCostSearch<OWLLogicalAxiom> search, OWLTheory
                theory, AxiomSet<OWLLogicalAxiom> targetDiag, QuerySelStrat
                strat, TableList
                entry) {
    /*theory.clearTestCases();
    search.clearSearch();*/
    /*
      IQueryProvider queryProvider = createQueryProvider(strat, search);


      Query actualQuery = null;
      int num_of_queries = 0;
      QInfo qinfo = null;

      boolean querySessionEnd = false;
      while (!querySessionEnd) {
          try {
              Query last = actualQuery;
              actualQuery = queryProvider.getQuery(false);
              if (actualQuery.equals(last)) {
                  querySessionEnd = true;
                  break;
              }

              boolean answer = generateQueryAnswer(search, actualQuery, targetDiag);
              num_of_queries++;
              if (answer) {
                  try {
                      search.getTheory().addEntailedTest(actualQuery.getQueryAxioms());
                  } catch (InconsistentTheoryException e) {
                      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                  }
              } else {
                  try {
                      search.getTheory().addNonEntailedTest(actualQuery.getQueryAxioms());
                  } catch (InconsistentTheoryException e) {
                      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                  }
              }
              qinfo = queryProvider.setQueryAnswer(answer);
          } catch (NoFurtherQueryException e) {
              querySessionEnd = true;
          } catch (SolverException e) {
              querySessionEnd = true;
          } catch (SingleDiagnosisLeftException e) {
              querySessionEnd = true;
          }
      }
      boolean w = false;
      if (qinfo == null || !qinfo.getCurrentDiags().contains(targetDiag))
          w = false;
      else
          w = true;
      boolean wd = false;
      if (qinfo != null) {

          TreeSet<QueryModuleDiagnosis> diags = new TreeSet<QueryModuleDiagnosis>(qinfo.getCurrentDiags());
          if (diags.size() >= 1 && diags.last().equals(targetDiag)) {
              wd = true;
          }
      }
      //entry.addEntr(num_of_queries, w, wd, 0, false, false, 1, queryTime, diagTime);

  }

  private boolean generateQueryAnswer
          (UniformCostSearch<OWLLogicalAxiom> search, Query
                  actualQuery, AxiomSet<OWLLogicalAxiom> targetDiag) {
      boolean answer;
      ITheory<OWLLogicalAxiom> theory = search.getTheory();

      if (theory.diagnosisEntails(targetDiag, actualQuery.getQueryAxioms())) {
          answer = true;
      } else if (!theory.diagnosisConsistent(targetDiag, actualQuery.getQueryAxioms())) {
          answer = false;
      } else {
          answer = rnd.nextBoolean();
      }

      return answer;

  }
    */
    private boolean generateQueryAnswer
    (UniformCostSearch<OWLLogicalAxiom> search, Partition<OWLLogicalAxiom> actualQuery, AxiomSet<OWLLogicalAxiom> targetDiag) {
        boolean answer;
        ITheory<OWLLogicalAxiom> theory = search.getTheory();

        if (theory.diagnosisEntails(targetDiag, actualQuery.partition)) {
            answer = true;
            assertTrue(!actualQuery.dnx.contains(targetDiag));
        } else if (!theory.diagnosisConsistent(targetDiag, actualQuery.partition)) {
            answer = false;
            assertTrue(!actualQuery.dx.contains(targetDiag));
        } else {
            answer = rnd.nextBoolean();
        }

        return answer;

    }

    /*private IQueryProvider createQueryProvider
            (QuerySelStrat
                     split, UniformCostSearch<OWLLogicalAxiom> search) {
        DiagProvider diagProvider = new DiagProvider(search, false, 9);
        IQueryProvider queryGenerator = null;
        if (split == QuerySelStrat.SPLIT) {
            queryGenerator = new SplitInHalfQSS();
        } else if (split == QuerySelStrat.ENTROPY) {
            queryGenerator = new MinScoreQSS();
        }
        queryGenerator.setNumOfLeadingDiagnoses(9);
        //queryGenerator.setDiagnosisProvider(diagProvider);
        return queryGenerator;
    }   */

    protected AxiomSet<OWLLogicalAxiom> chooseTargetDiagnosis
            (DiagProbab
                     diagProbab, TreeSet<AxiomSet<OWLLogicalAxiom>> diagnoses) {


        double sum = 0;
        TreeSet<AxiomSet<OWLLogicalAxiom>> res;
        TreeSet<AxiomSet<OWLLogicalAxiom>> good = new TreeSet<AxiomSet<OWLLogicalAxiom>>();
        TreeSet<AxiomSet<OWLLogicalAxiom>> avg = new TreeSet<AxiomSet<OWLLogicalAxiom>>();
        TreeSet<AxiomSet<OWLLogicalAxiom>> bad = new TreeSet<AxiomSet<OWLLogicalAxiom>>();

        for (AxiomSet<OWLLogicalAxiom> hs : diagnoses.descendingSet()) {
            if (sum <= 0.33) {
                good.add(hs);
            } else if (sum >= 0.33 && sum <= 0.66) {
                avg.add(hs);
            } else if (sum >= 0.66) {
                bad.add(hs);
            }
            sum += hs.getMeasure();
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
        /*
        int bad_size = diagnoses.size() / 3;
        int average_size = diagnoses.size() / 3;
        int good_size = diagnoses.size() / 3;
        if (diagnoses.size() % 3 == 1) {
            bad_size++;
        } else if (diagnoses.size() % 3 == 2) {
            bad_size++;
            average_size++;
        } else {

        }
        LinkedList<ProbabilisticHittingSet<OWLLogicalAxiom>> diagsList = new LinkedList<ProbabilisticHittingSet<OWLLogicalAxiom>>(diagnoses);

        int num_of_diag = 0;
        switch (diagProbab) {
            case BAD:
                num_of_diag = rnd.nextInt(bad_size);
                break;
            case AVERAGE:
                num_of_diag = bad_size + rnd.nextInt(average_size);
                break;
            case GOOD:
                num_of_diag = bad_size + average_size + rnd.nextInt(good_size);
                break;
        }

        return diagsList.get(num_of_diag);*/
    }

    private double getProbabBetween
            (
                    double min,
                    double max) {
        return min + rnd.nextDouble() * (max - min);
    }

    protected Set<AxiomSet<OWLLogicalAxiom>> chooseUserProbab
            (UsersProbab
                     usersProbab, UniformCostSearch<OWLLogicalAxiom> search, Set<AxiomSet<OWLLogicalAxiom>> diagnoses) {
        Map<ManchesterOWLSyntax, Double> keywordProbs = new HashMap<ManchesterOWLSyntax, Double>();
        //ProbabilityTableModel m = new ProbabilityTableModel();
        ArrayList<ManchesterOWLSyntax> keywordList = new ArrayList<ManchesterOWLSyntax>(EnumSet.copyOf(Utils.getProbabMap().keySet()));
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
                    keywordProbs.put(keyword, probabilities[keywordList.indexOf(keyword)]);
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
                    keywordProbs.put(keyword, probabilities[keywordList.indexOf(keyword)]);
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
                    keywordProbs.put(keyword, (1.0 / n));
                }
                break;
        }
        ((OWLAxiomNodeCostsEstimator)search.getNodeCostsEstimator()).setKeywordProbabilities(keywordProbs, diagnoses);
        return sortDiagnoses(diagnoses);


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

}
