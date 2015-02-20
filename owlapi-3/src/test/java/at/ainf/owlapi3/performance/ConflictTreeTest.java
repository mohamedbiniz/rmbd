package at.ainf.owlapi3.performance;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.SimpleCostsEstimator;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.base.OAEI11ConferenceSession;
import at.ainf.owlapi3.base.SimulatedSession;
import at.ainf.owlapi3.base.tools.TableList;
import at.ainf.owlapi3.costestimation.OWLAxiomCostsEstimator;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.model.OWLIncoherencyExtractor;
import at.ainf.owlapi3.model.OWLTheory;
import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.util.*;

import static at.ainf.owlapi3.util.SetUtils.createIntersection;
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
    private OWLTheory ctTheory;
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
        ctTheory = getSimpleTheory(ontology, false);
        //set query parameters
//        ctTheory.setIncludeSubClassOfAxioms(true);
//        ctTheory.setIncludeClassAssertionAxioms(true);
//        ctTheory.setIncludeEquivalentClassAxioms(true);
//        ctTheory.setIncludePropertyAssertAxioms(true);
//        ctTheory.setIncludeDisjointClassAxioms(true);


        origTheory = (OWLTheory) ctTheory.copy();

        //setup search
        TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        //get target diagnosis
        search.setSearcher(new QuickXplain<OWLLogicalAxiom>());
        search.setSearchable(ctTheory);
        search.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(ctTheory));

        //set for all found diagnoses during search
        foundDiagnoses = new LinkedHashSet<Set<OWLLogicalAxiom>>();

        return search;
    }

    @Test
    public void doOAEIConferenceTest() throws SolverException, InconsistentTheoryException, OWLOntologyCreationException {
        String matchingsDir = "oaei11conference/matchings/";
        String ontologyDir = "oaei11conference/ontology";

        File[] f = getMappingFiles(matchingsDir, "incoherent", "incoherent_2015.txt");
//        File[] f2 = getMappingFiles(matchingsDir, "inconsistent", "inconsistent_2015.txt");

        Set<File> files = new LinkedHashSet<File>();
        Map<File, String> map = new HashMap<File, String>();
        for (File file : f) {
            files.add(file);
            map.put(file, "incoherent");
        }
//        for (File file : f2) {
//            files.add(file);
//            map.put(file, "inconsistent");
//        }

        runOaeiConfereneTests(matchingsDir, ontologyDir, files, map);

    }

    private void runOaeiConfereneTests(String matchingsDir, String ontologyDir, Set<File> files, Map<File, String> map) throws SolverException, InconsistentTheoryException, OWLOntologyCreationException {
        OAEI11ConferenceSession conferenceSession = new OAEI11ConferenceSession();

        QSSType[] qssTypes = new QSSType[]{QSSType.MINSCORE};
        //QSSType[] qssTypes = new QSSType[]{QSSType.MINSCORE, QSSType.SPLITINHALF};

        for (TargetSource targetSource : new TargetSource[]{TargetSource.FROM_30_DIAGS}) {

            for (File file : files) {
                logger.info("processing " + file.getName());

                String out = "STAT, " + file;


                Random random = new Random(12311);
                Set<FormulaSet<OWLLogicalAxiom>> targetDgSet = conferenceSession.getRandomDiagSet(file, map.get(file));
                int randomNr = conferenceSession.chooseRandomNum(targetDgSet, random);
                Set<OWLLogicalAxiom> targetDg = conferenceSession.chooseRandomDiag(targetDgSet,file,randomNr);


                for (QSSType qssType : qssTypes) {


                    String fileName = file.getName();
                    StringTokenizer t = new StringTokenizer(fileName, "-");
                    String matcher = t.nextToken();

                    String o1 = t.nextToken();
                    String o2 = t.nextToken();
                    o2 = o2.substring(0, o2.length() - 4);

                    String n = file.getName().substring(0, file.getName().length() - 4);
                    OWLOntology merged = conferenceSession.getOntology(ontologyDir,
                            o1, o2, matchingsDir + map.get(file), n + ".rdf");

                    long preprocessModulExtract = System.currentTimeMillis();
                    OWLOntology ontology = new OWLIncoherencyExtractor(
                            new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(merged);
                    preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;
                    ctTheory = getExtendTheory(ontology, false);
                    origTheory = (OWLTheory) ctTheory.copy();
                    //Define Treesearch here
                    TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = getUniformCostSearch(ctTheory, false);
                   /* BinaryTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = new BinaryTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
                    search.setCostsEstimator(new SimpleCostsEstimator<OWLLogicalAxiom>());
                    search.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());
                    MultiQuickXplain<OWLLogicalAxiom> searcher = new MultiQuickXplain<OWLLogicalAxiom>(2,10,10);
                    //searcher.setAxiomListener(new QXSingleAxiomListener<OWLLogicalAxiom>(true));
                    searcher.setAxiomListener(new QXAxiomSetListener<OWLLogicalAxiom>(true));
                    // QuickXplain<OWLLogicalAxiom> searcher = new QuickXplain<OWLLogicalAxiom>();
                    search.setSearcher(searcher);
                    search.setSearchable(theory);     */
                    //Copy of Search initialisation ends here


                    LinkedHashSet<OWLLogicalAxiom> bx = new LinkedHashSet<OWLLogicalAxiom>();
                    OWLOntology ontology1 = getOntologySimple(ontologyDir, o1 + ".owl");
                    OWLOntology ontology2 = getOntologySimple(ontologyDir, o2 + ".owl");
                    Set<OWLLogicalAxiom> onto1Axioms = createIntersection(ontology.getLogicalAxioms(), ontology1.getLogicalAxioms());
                    Set<OWLLogicalAxiom> onto2Axioms = createIntersection(ontology.getLogicalAxioms(), ontology2.getLogicalAxioms());
                    int modOnto1Size = onto1Axioms.size();
                    int modOnto2Size = onto2Axioms.size();
                    int modMappingSize = ontology.getLogicalAxioms().size() - modOnto1Size - modOnto2Size;
                    bx.addAll(onto1Axioms);
                    bx.addAll(onto2Axioms);
                    ctTheory.getKnowledgeBase().addBackgroundFormulas(bx);

                    Map<OWLLogicalAxiom, BigDecimal> map1 = conferenceSession.readRdfMapping(matchingsDir + map.get(file), n + ".rdf");

                    OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(ctTheory, map1);


                    search.setCostsEstimator(es);


                    search.reset();

                    //set for all found diagnoses during search
                    foundDiagnoses = new LinkedHashSet<Set<OWLLogicalAxiom>>();

                    Map<QSSType, DurationStat> ctTimes = new HashMap<QSSType, DurationStat>();
                    Map<QSSType, List<Double>> ctQueries = new HashMap<QSSType, List<Double>>();
                    ctTimes.put(qssType, new DurationStat());
                    ctQueries.put(qssType, new LinkedList<Double>());


                    //main part
                    ConflictTreeSession conflictTreeSearch = new ConflictTreeSession(this, ctTheory, search);
                    conflictTreeSearch.setOutputString(out);
                    long completeTime = conflictTreeSearch.search((FormulaSet<OWLLogicalAxiom>) targetDg, ctQueries, qssType);
                    ctTimes.get(qssType).add(completeTime);

                    foundDiagnoses.addAll(conflictTreeSearch.getDiagnosis());
                    assertTrue(ctTheory.verifyConsistency());

                    resetTheoryTests(ctTheory);
                    search.reset();
                    // end main part

                }
                logger.info(out);
            }
        }
    }


    @Ignore //TODO bugfix error at secund run (SPLITINHALF scoring function)
    @Test
    public void testCompareSearchMethods() throws SolverException, InconsistentTheoryException, NoConflictException, OWLOntologyCreationException {
        logger.info("NormalSimulatedSession compared to ConflictTreeSimulatedSession\n");

        TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = testSetup("ontologies/ecai2010.owl");
        Set<? extends FormulaSet<OWLLogicalAxiom>> diagnoses = getDiagnoses(search);

        Map<QSSType, DurationStat> nTimes = new HashMap<QSSType, DurationStat>();
        Map<QSSType, List<Double>> nQueries = new HashMap<QSSType, List<Double>>();
        Map<QSSType, DurationStat> ctTimes = new HashMap<QSSType, DurationStat>();
        Map<QSSType, List<Double>> ctQueries = new HashMap<QSSType, List<Double>>();

        for (QSSType type : QSSType.values()) { //run for each scoring function
            logger.info("\n-------QSSType: " + type);
            search.setSearchable(ctTheory);

            //run normal simulated session
            logger.info("NormalSimulatedSession\n");
            nTimes.put(type, new DurationStat());
            nQueries.put(type, new LinkedList<Double>());
            for (FormulaSet<OWLLogicalAxiom> targetDiagnosis : diagnoses) { //run for each possible target diagnosis
                long completeTime = System.currentTimeMillis();

                computeHS(search, ctTheory, targetDiagnosis, nQueries.get(type), type);
                ctTheory.getKnowledgeBase().removeFormulas(targetDiagnosis);
                completeTime = System.currentTimeMillis() - completeTime;
                nTimes.get(type).add(completeTime);

                foundDiagnoses.add(targetDiagnosis);
                assertTrue(ctTheory.verifyConsistency());

                resetTheoryTests(ctTheory);
                search.reset();
            }
            logger.info("found Diagnoses: " + foundDiagnoses.toString());
            logger.info("\n-----found all target diagnoses: " + (foundDiagnoses.size() > 0 && foundDiagnoses.containsAll(diagnoses)) + "------\n");
            // end (run normal simulated session)

            foundDiagnoses.clear();

            //run conflict tree simulated session
            logger.info("ConflictTreeSimulatedSession\n");
            ctTimes.put(type, new DurationStat());
            ctQueries.put(type, new LinkedList<Double>());
            for (FormulaSet<OWLLogicalAxiom> targetDiagnosis : diagnoses) { //run for each possible target diagnosis
                logger.info("\ntargetD: " + targetDiagnosis.toString() + "\n");

                ConflictTreeSession conflictTreeSearch = new ConflictTreeSession(this, ctTheory, search);
                long completeTime = conflictTreeSearch.search(targetDiagnosis, ctQueries, type);
                ctTimes.get(type).add(completeTime);

                foundDiagnoses.addAll(conflictTreeSearch.getDiagnosis());
                assertTrue(ctTheory.verifyConsistency());

                resetTheoryTests(ctTheory);
                search.reset();
            }
            logger.info("found Diagnoses: " + foundDiagnoses.toString());
            logger.info("\n-----found all target diagnoses: " + (foundDiagnoses.size() > 0 && foundDiagnoses.containsAll(diagnoses)) + "------\n");
            // end (run conflict tree simulated session)

            //print time statistics
            logStatistics(nQueries, nTimes, type, "normal");
            logStatistics(ctQueries, ctTimes, type, "treeSearch");

            foundDiagnoses.clear();
        }
    }


    @Test
    public void testNormalSimulatedSession() throws SolverException, InconsistentTheoryException, NoConflictException, OWLOntologyCreationException {
        logger.info("NormalSimulatedSession\n");

        TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = testSetup("ontologies/ecai2010.owl");
        Set<? extends FormulaSet<OWLLogicalAxiom>> diagnoses = getDiagnoses(search);

        Map<QSSType, DurationStat> nTimes = new HashMap<QSSType, DurationStat>();
        Map<QSSType, List<Double>> nQueries = new HashMap<QSSType, List<Double>>();

        for (QSSType type : QSSType.values()) { //run for each scoring function
            logger.info("QSSType: " + type);

            nTimes.put(type, new DurationStat());
            nQueries.put(type, new LinkedList<Double>());
            for (FormulaSet<OWLLogicalAxiom> targetDiagnosis : diagnoses) { //run for each possible target diagnosis
                logger.info("\ntargetD: " + targetDiagnosis.toString() + "\n");
                long completeTime = System.currentTimeMillis();

                computeHS(search, ctTheory, targetDiagnosis, nQueries.get(type), type);
                ctTheory.getKnowledgeBase().removeFormulas(targetDiagnosis);
                completeTime = System.currentTimeMillis() - completeTime;
                nTimes.get(type).add(completeTime);

                foundDiagnoses.add(targetDiagnosis);
                assertTrue(ctTheory.verifyConsistency());

                resetTheoryTests(ctTheory);
                search.reset();
            }
            logStatistics(nQueries, nTimes, type, "normal");
            logger.info("found Diagnoses: " + foundDiagnoses.toString());
            logger.info("\n-----found all target diagnoses: " + (foundDiagnoses.size() > 0 && foundDiagnoses.containsAll(diagnoses)) + "------\n");
        }
    }


    @Test
    public void testConflictTreeSimulatedSession() throws SolverException, InconsistentTheoryException, OWLOntologyCreationException, NoConflictException {
        logger.info("ConflictTreeSimulatedSession\n");

        TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = testSetup("ontologies/ecai2010.owl");
        Set<? extends FormulaSet<OWLLogicalAxiom>> diagnoses = getDiagnoses(search);

        Map<QSSType, DurationStat> ctTimes = new HashMap<QSSType, DurationStat>();
        Map<QSSType, List<Double>> ctQueries = new HashMap<QSSType, List<Double>>();

        for (QSSType type : QSSType.values()) { //run for each scoring function
            logger.info("QSSType: " + type);

            ctTimes.put(type, new DurationStat());
            ctQueries.put(type, new LinkedList<Double>());
            for (FormulaSet<OWLLogicalAxiom> targetDiagnosis : diagnoses) {
                logger.info("\ntargetD: " + targetDiagnosis.toString() + "\n");

                ConflictTreeSession conflictTreeSearch = new ConflictTreeSession(this, ctTheory, search);
                long completeTime = conflictTreeSearch.search(targetDiagnosis, ctQueries, type);
                ctTimes.get(type).add(completeTime);

                foundDiagnoses.addAll(conflictTreeSearch.getDiagnosis());
                assertTrue(ctTheory.verifyConsistency());

                resetTheoryTests(ctTheory);
                search.reset();
            }
            logStatistics(ctQueries, ctTimes, type, "treeSearch");
            logger.info("found Diagnoses: " + foundDiagnoses.toString());
            logger.info("\n-----found all target diagnoses: " + (foundDiagnoses.size() > 0 && foundDiagnoses.containsAll(diagnoses)) + "------\n");
        }
    }


    private Set<? extends FormulaSet<OWLLogicalAxiom>> getDiagnoses(TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search) throws SolverException, InconsistentTheoryException, NoConflictException {
        //cost estimator
        SimpleCostsEstimator<OWLLogicalAxiom> es = new SimpleCostsEstimator<OWLLogicalAxiom>();
        search.setCostsEstimator(es);

        //searching diagnoses
        search.start();
        Set<? extends FormulaSet<OWLLogicalAxiom>> diagnoses = search.getDiagnoses();

        //resets history
        ctTheory.getKnowledgeBase().clearTestCases();
        ctTheory.getKnowledgeBase().clearTestsList();

        search.reset();

        return diagnoses;
    }

    /**
     * Logs the time and the number of needed queries for a specific QSS type
     * @param queries
     * @param times
     * @param type
     * @param statisticName
     */
    private void logStatistics(Map<QSSType, List<Double>> queries, Map<QSSType, DurationStat> times, QSSType type, String statisticName){
        List<Double> queriesOfType = queries.get(type);
        double res = 0;
        for (Double qs : queriesOfType) {
            res += qs;
        }
        logger.info(statisticName + " needed time " + type + ": " + getStringTime(times.get(type).getOverall()) +
                        " max " + getStringTime(times.get(type).getMax()) +
                        " min " + getStringTime(times.get(type).getMin()) +
                        " avg2 " + getStringTime(times.get(type).getMean()) +
                        " Queries max " + Collections.max(queries.get(type)) +
                        " min " + Collections.min(queries.get(type)) +
                        " avg2 " + res / queriesOfType.size()
        );

    }

    private void resetTheoryTests(OWLTheory theory) {
        theory.getKnowledgeBase().addFormulas(origTheory.getKnowledgeBase().getKnowledgeBase());
        theory.getKnowledgeBase().clearTestCases();
        theory.getKnowledgeBase().clearTestsList();
    }


    protected String computeHSShortLog(TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> searchNormal,
                             OWLTheory theoryNormal, FormulaSet<OWLLogicalAxiom> diagnoses,
                             List<Double> queries, QSSType type, String message) {
        SimulatedSession session = new SimulatedSession();
        session.setShowElRates(false);

        TableList entry = new TableList();
        session.setEntry(entry);
        session.setMessage(message);
        session.setTargetD(diagnoses);
        session.setScoringFunct(type);
        session.setTheory(theoryNormal);
        session.setSearch(searchNormal);
        String out = session.simulateQuerySession();
        FormulaSet<OWLLogicalAxiom> diag = getMostProbable(searchNormal.getDiagnoses());
        logger.info("\nfound Diag" + diag.toString() + "\n");
        boolean foundCorrectD = diag.equals(diagnoses);
        if (this.getClass() != ConflictTreeTest.class)
            theoryNormal.getKnowledgeBase().clearTestCases(); // don't clear if called from ConflictTreeTest
        searchNormal.reset();
        Assert.assertTrue(foundCorrectD);
        queries.add(entry.getMeanQuery());
        return out;
    }




    //copied methods from OAE11ConferenceTests

    protected File[] getMappingFiles(String matchingsDir, String dir, String exclusionFile) {
        URL exclusionFileUrl = ClassLoader.getSystemResource(matchingsDir + exclusionFile);
        File folder = new File(ClassLoader.getSystemResource(matchingsDir + dir).getFile());
        if (exclusionFileUrl == null)
            return folder.listFiles();
        File incl = new File(exclusionFileUrl.getFile());
        MyFilenameFilter filter = new MyFilenameFilter(incl);
        return folder.listFiles(filter);
    }

    private class MyFilenameFilter implements FilenameFilter {
        private Set<String> acceptedNames;

        public MyFilenameFilter(File includedNames) {
            acceptedNames = new LinkedHashSet<String>();
            try{
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(includedNames)));
                String strLine;
                while ((strLine = br.readLine()) != null)   {
                    if (!strLine.startsWith("#") && strLine.endsWith(".rdf"))
                        acceptedNames.add(strLine);
                }
                br.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        public boolean accept(File dir, String name) {
            return acceptedNames.contains(name);
        }
    }


}