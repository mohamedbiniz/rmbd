package at.ainf.owlcontroller.queryeval;

import at.ainf.diagnosis.partitioning.BruteForce;
import at.ainf.diagnosis.partitioning.CKK;
import at.ainf.diagnosis.partitioning.Partitioning;
import at.ainf.diagnosis.partitioning.QueryMinimizer;
import at.ainf.diagnosis.partitioning.scoring.QSS;
import at.ainf.diagnosis.quickxplain.FastDiagnosis;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.tree.CostsEstimator;
import at.ainf.diagnosis.tree.UniformCostSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlcontroller.OWLAxiomCostsEstimator;
import at.ainf.owlcontroller.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlcontroller.Utils;
import at.ainf.owlcontroller.distributiongenerators.ModerateDistribution;
import at.ainf.owlcontroller.parser.MyOWLRendererParser;
import at.ainf.owlcontroller.queryeval.result.TableList;
import at.ainf.owlcontroller.queryeval.result.Time;
import at.ainf.theory.model.ITheory;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.*;
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
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import java.io.*;
import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 28.02.12
 * Time: 11:32
 * To change this template use File | Settings | File Templates.
 */
public class AlignmentTests extends BasePerformanceTests {

    private static Logger logger = Logger.getLogger(AlignmentTests.class.getName());

    private Properties p = null;

    @BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlcontroller-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }

    @Ignore
    @Test
    public void readTest() throws IOException {
        String filename = "/home/kostya/java/files/CMT-CONFTOOL.txt";
        Map<String, Double> axioms = new LinkedHashMap<String, Double>();
        Set<String> targetDiag = new LinkedHashSet<String>();
        readData(filename, axioms, targetDiag);
        System.out.println("Read " + axioms.size() + " " + targetDiag.size());
        assertEquals(axioms.size(), 36);
        assertEquals(targetDiag.size(), 13);

        filename = "/home/kostya/java/files/CMT-CRS.txt";
        axioms.clear();
        targetDiag.clear();
        readData(filename, axioms, targetDiag);
        System.out.println("Read " + axioms.size() + " " + targetDiag.size());
        assertEquals(axioms.size(), 2 * (17 - 5));
        assertEquals(targetDiag.size(), 7);
    }

    @Ignore
    @Test
    public void readTest2() throws IOException {
        String filename = "C:\\axiome\\ontologies\\coma-evaluation/CMT-CONFTOOL.txt";
        Map<String, Double> axioms = new LinkedHashMap<String, Double>();
        Set<String> targetDiag = new LinkedHashSet<String>();
        readData(filename, axioms, targetDiag);
        System.out.println("Read " + axioms.size() + " " + targetDiag.size());
    }

    @Ignore
    @Test
    public void readTest1() throws IOException {
        String filename = "C:\\axiome\\ontologies\\coma-evaluation/CMT-CONFTOOL.txt";
        Map<String, Double> axioms = new LinkedHashMap<String, Double>();
        Set<String> targetDiag = new LinkedHashSet<String>();
        readData(filename, axioms, targetDiag);
        System.out.println("Read " + axioms.size() + " " + targetDiag.size());
    }

    public void readData(String filename, Map<String, Double> axioms, Set<String> targetDiag) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        String sourceNamespace = "";
        String targetNamespace = "";
        while ((line = br.readLine()) != null) {
            if (line.startsWith("sourceNamespace"))
                sourceNamespace = line.substring(line.indexOf("=") + 1).trim();
            if (line.startsWith("targetNamespace"))
                targetNamespace = line.substring(line.indexOf("=") + 1).trim();
            if (line.startsWith(">") || line.startsWith("<") || line.startsWith("+") || line.startsWith("-")) {
                String status = line.substring(0, 2).trim();
                String sub = line.substring(2);
                String source = "";
                String target = "";
                if (sub.contains("=")) {
                    source = sub.substring(0, sub.indexOf("=")).trim();
                    target = sub.substring(sub.indexOf("=") + 1, sub.indexOf("|")).trim();
                    axioms.put(createAxiom(sourceNamespace, source, targetNamespace, target),
                            Double.parseDouble(sub.substring(sub.indexOf("|") + 1)));
                    axioms.put(createAxiom(targetNamespace, target, sourceNamespace, source),
                            Double.parseDouble(sub.substring(sub.indexOf("|") + 1)));
                }
                if (sub.contains(">")) {
                    source = sub.substring(0, sub.indexOf(">")).trim();
                    target = sub.substring(sub.indexOf(">") + 1, sub.indexOf("|")).trim();
                    axioms.put(createAxiom(sourceNamespace, source, targetNamespace, target),
                            Double.parseDouble(sub.substring(sub.indexOf("|") + 1)));
                }
                if (sub.contains("<")) {
                    source = sub.substring(0, sub.indexOf("<")).trim();
                    target = sub.substring(sub.indexOf("<") + 1, sub.indexOf("|")).trim();
                    axioms.put(createAxiom(targetNamespace, target, sourceNamespace, source),
                            Double.parseDouble(sub.substring(sub.indexOf("|") + 1)));
                }
                if (status.equals("-")) {
                    targetDiag.add(createAxiom(sourceNamespace, source, targetNamespace, target));
                    if (sub.contains("="))
                        targetDiag.add(createAxiom(targetNamespace, target, sourceNamespace, source));
                }
                if (status.equals(">")) {
                    targetDiag.add(createAxiom(sourceNamespace, source, targetNamespace, target));
                }
                if (status.equals("<")) {
                    targetDiag.add(createAxiom(targetNamespace, target, sourceNamespace, source));
                }

            }
        }
    }

    private String createAxiom(String sourceNamespace, String source, String targetNamespace, String target) {
        return "<" + sourceNamespace + "#" + source + "> <" + targetNamespace + "#" + target + ">";
    }

    protected Properties readProps() {
        Properties properties = new Properties();
        String config = ClassLoader.getSystemResource("alignment/alignment.full.properties").getFile();
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

        return properties;

    }

    protected Map<String, List<String>> readOntologiesFromFile(Properties properties) {

        String[] testsuites = properties.getProperty("alignment.testsuites").split(",");

        Map<String, List<String>> ontologies = new HashMap<String, List<String>>();

        for (String testsuite : testsuites) {
            List<String> ontologie = Arrays.asList(properties.getProperty(testsuite.trim()).split(","));
            ontologies.put(testsuite, ontologie);
        }
        return ontologies;
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

            result.setIncludeTrivialEntailments(false);
            // QueryDebuggerPreference.getInstance().setTestIncoherencyToInconsistency(true);

            result.setIncludeSubClassOfAxioms(false);
            result.setIncludeClassAssertionAxioms(false);
            result.setIncludeEquivalentClassAxioms(false);
            result.setIncludeDisjointClassAxioms(false);
            result.setIncludePropertyAssertAxioms(false);
            result.setIncludeReferencingThingAxioms(false);
            result.setIncludeOntologyAxioms(true);
            //  result.setIncludeTrivialEntailments(true);
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return result;
    }

    protected OWLOntology createOwlOntology(String matcher, String name) {
        String path = ClassLoader.getSystemResource("alignment/" + matcher + "_incoherent_matched_ontologies").getPath();
        File ontF = new File(path + "/" + name + ".owl");
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        OWLOntology ontology = null;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(ontF);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return ontology;
    }

    protected OWLOntology createOwlOntologyFromP(String name, String pn) {
        String path = ClassLoader.getSystemResource(pn).getPath();
        File ontF = new File(path + "/" + name + ".owl");
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        OWLOntology ontology = null;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(ontF);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return ontology;
    }
    
    protected OWLOntology createOwlOntology(String name) {
        String path = ClassLoader.getSystemResource("alignment").getPath();
        File ontF = new File(path + "/" + name + ".owl");
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        OWLOntology ontology = null;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(ontF);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return ontology;
    }

    protected Set<OWLLogicalAxiom> getDiagnosis(String[] targetAxioms, OWLOntology ontology) {

        Set<OWLLogicalAxiom> res = new LinkedHashSet<OWLLogicalAxiom>();
        for (String targetAxiom : targetAxioms) {
            for (OWLLogicalAxiom axiom : ontology.getLogicalAxioms()) {
                if (axiom.toString().contains(targetAxiom.trim()))
                    res.add(axiom);
            }
        }
        return res;
    }

    protected UniformCostSearch<OWLLogicalAxiom> createUniformCostSearch(OWLTheory th, boolean dual) {

        SimpleStorage<OWLLogicalAxiom> storage;
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

    protected String simulateBruteForceOnl(UniformCostSearch<OWLLogicalAxiom> search,
                                           OWLTheory theory, Set<OWLLogicalAxiom> targetDiag,
                                           TableList entry, QSSType scoringFunc, String message) {
        //DiagProvider diagProvider = new DiagProvider(search, false, 9);

        QSS<OWLLogicalAxiom> qss = createQSSWithDefaultParam(scoringFunc);
        //userBrk=false;

        Partition<OWLLogicalAxiom> actPa = null;

        Set<AxiomSet<OWLLogicalAxiom>> diagnoses = null;
        int num_of_queries = 0;

        boolean userBreak = false;
        boolean systemBreak = false;

        boolean querySessionEnd = false;
        long time = System.currentTimeMillis();
        boolean hasQueryWithNoDecisionPossible = false;
        Time queryTime = new Time();
        Time diagTime = new Time();
        int queryCardinality = 0;
        long reactionTime = 0;
        Partitioning<OWLLogicalAxiom> queryGenerator = new CKK<OWLLogicalAxiom>(theory, qss);
        Set<AxiomSet<OWLLogicalAxiom>> remainingAllDiags = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>(allDiags);
        while (!querySessionEnd) {
            try {
                Collection<AxiomSet<OWLLogicalAxiom>> lastD = diagnoses;
                logger.trace("numOfQueries: " + num_of_queries + " search for diagnoses");

                userBreak = false;
                systemBreak = false;

                if (actPa != null && actPa.dx.size() == 1 && actPa.dz.size() == 1 && actPa.dnx.isEmpty()) {
                    logger.error("Help!");
                    printc(theory.getEntailedTests());
                    printc(theory.getNonentailedTests());
                    print(actPa.partition);
                    prinths(actPa.dx);
                    prinths(actPa.dz);
                }

                try {
                    long diag = System.currentTimeMillis();
                    search.run(NUMBER_OF_HITTING_SETS);

                    daStr += search.getStorage().getDiagnoses().size() + "/";
                    diagnosesCalc += search.getStorage().getDiagnoses().size();
                    conflictsCalc += search.getStorage().getConflicts().size();

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
                    double diagProbabilities = 0;
                    for (AxiomSet<OWLLogicalAxiom> tempd : diagnoses)
                        diagProbabilities += tempd.getMeasure();
                    logger.trace("diagnoses: " + diagnoses.size() +
                            " (" + diagProbabilities + ") first diagnosis: " + d +
                            " is target: " + isTargetDiagFirst + " is in window: " +
                            ((o == null) ? false : o.toString()));
                }

                if (d1 != null) {// && scoringFunc != QSSType.SPLITINHALF) {
                    double d1p = d1.getMeasure();
                    double diff = 100 - (d1p * 100) / dp;
                    logger.trace("difference : " + (dp - d1p) + " - " + diff + " %");
                    if (userBrk && diff > SIGMA && isTargetDiagFirst && num_of_queries > 0) {
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
                //actPa = getBestQuery(search, diagnoses);

                actPa = queryGenerator.generatePartition(diagnoses);

                if (actPa == null || actPa.partition == null || (last != null && actPa.partition.equals(last.partition))) {
                    // system brake
                    querySessionEnd = true;
                    break;
                }
                queryCardinality = actPa.partition.size();


                long querytime = System.currentTimeMillis() - query;
                queryTime.setTime(querytime);
                reactionTime += querytime;
                num_of_queries++;

                logger.trace("numOfQueries: " + num_of_queries + " generate answer");
                boolean answer = true;
                boolean hasAn = false;
                while (!hasAn) {
                    try {
                        answer = generateQueryAnswer(search, actPa, targetDiag);
                        hasAn = true;
                    } catch (NoDecisionPossibleException e) {
                        hasQueryWithNoDecisionPossible = true;
                        actPa = queryGenerator.nextPartition(actPa);
                        if (actPa == null) {
                            logger.error("All partitions were tested and none provided an answer to the target diagnosis!");
                            break;
                        }
                    }
                }

                if (qss != null) qss.updateParameters(answer);

                
                // fine all dz diagnoses
                // TODO do we need this fine?
                for (AxiomSet<OWLLogicalAxiom> ph : actPa.dz) {
                    ph.setMeasure(0.5d * ph.getMeasure());
                }

                int eliminatedInLeading = getEliminationRate(search.getTheory(),diagnoses,answer,actPa);
                int eliminatedInRemaining = getEliminationRate(search.getTheory(),remainingAllDiags,answer,actPa);
                int eliminatedInRemainingSize = remainingAllDiags.size();
                int eliminatedInfull = getEliminationRate(search.getTheory(),allDiags,answer,actPa);
                for (AxiomSet<OWLLogicalAxiom> diagnosis : diagnoses) {
                    if (!remainingAllDiags.contains(diagnosis))
                        logger.info ("");
                }
                    
                deleteDiag(search.getTheory(),remainingAllDiags,answer,actPa.partition);
                AxiomSet<OWLLogicalAxiom> foundTarget;
                foundTarget=null;
                for (AxiomSet<OWLLogicalAxiom> axiom : allDiags)
                    if (targetDiag.containsAll(axiom)) {
                        if(foundTarget!=null)
                            logger.info("");
                        foundTarget = axiom;
                    }
                logger.info("elimination rates: in all diags ;" + eliminatedInfull + "/" + allDiags.size() +
                        "; in all remaining diags ;" + eliminatedInRemaining + "/" + eliminatedInRemainingSize +
                        "; in leading ;" + eliminatedInLeading + "/" + diagnoses.size()+" "+foundTarget);
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
            targetDiagnosisIsInWind = isInWindow(targetDiag, diagnoses);
            if (diagnoses.size() >= 1 && targetDiag.
                    containsAll((new TreeSet<AxiomSet<OWLLogicalAxiom>>(diagnoses)).last())) {
                targetDiagnosisIsMostProbable = true;
                targetDiagnosisIsInWind = true;
            }
        }
        int diagWinSize = 0;
        if (diagnoses != null)
            diagWinSize = diagnoses.size();

        int consistencyCount = 0;
        if (num_of_queries != 0) consistencyCount = theory.getConsistencyCount() / num_of_queries;
        if (num_of_queries != 0) reactionTime = reactionTime / num_of_queries;

        message += " , Iteration finished within " + time + " ms, required " + num_of_queries + " queries, most probable "
                + targetDiagnosisIsMostProbable + ", is in window " + targetDiagnosisIsInWind + ", size of window  " + diagWinSize
                + ", reaction " + reactionTime + ", user " + userBreak +
                ", systemBrake " + systemBreak + ", nd " + hasQueryWithNoDecisionPossible +
                ", consistency checks " + consistencyCount;
        logger.info(message);

        String msg = time + ", " + num_of_queries + ", " + targetDiagnosisIsMostProbable + ", " + targetDiagnosisIsInWind + ", " + diagWinSize
                + ", " + reactionTime + ", " + userBreak +
                ", " + systemBreak + ", " + hasQueryWithNoDecisionPossible +
                ", " + consistencyCount;
        entry.addEntr(num_of_queries, queryCardinality, targetDiagnosisIsInWind, targetDiagnosisIsMostProbable,
                diagWinSize, userBreak, systemBreak, time, queryTime, diagTime, reactionTime, consistencyCount);
        return msg;
    }

    @Test
    public void calcOnlyDiagnoses() throws IOException, SolverException, InconsistentTheoryException, NoConflictException {

        String m = "coma";
        String o = "CRS-EKAW";
        QSSType type = QSSType.SPLITINHALF;
        Properties properties = readProps();
        String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
        OWLOntology ontology = createOwlOntology(m.trim(), o.trim());
        Set<OWLLogicalAxiom> targetDg;
        OWLTheory theory = createOWLTheory(ontology, false);
        UniformCostSearch<OWLLogicalAxiom> search = createUniformCostSearch(theory, false);
            String path = ClassLoader.getSystemResource("alignment/evaluation/"
                    + m.trim()
                    + "-incoherent-evaluation/"
                    + o.trim()
                    + ".txt").getPath();
            OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, path);
    
            targetDg = null;
    
            search.setCostsEstimator(es);

            search.run();
    
            allDiags = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>(search.getStorage().getDiagnoses());
            search.clearSearch();

            search.run(9);
            TreeSet<OWLLogicalAxiom> testcase = new TreeSet<OWLLogicalAxiom>();
        MyOWLRendererParser parser = new MyOWLRendererParser(ontology);
        testcase.add(parser.parse("conference DisjointWith session"));
        testcase.add(parser.parse("Conference_Session SubClassOf conference"));
        testcase.add(parser.parse("conference SubClassOf Conference_Session"));

        theory.addNonEntailedTest(testcase);
        Set<AxiomSet<OWLLogicalAxiom>> toRemove = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>();
        for (AxiomSet<OWLLogicalAxiom> axiomSet : allDiags)
            if(!theory.testDiagnosis(axiomSet))
                toRemove.add(axiomSet);
        allDiags.removeAll(toRemove);
        //deleteDiag(theory,allDiags,false,testcase);

        search.run(9);
        Set<AxiomSet<OWLLogicalAxiom>> diagnoses = search.getStorage().getDiagnoses();
        /*for (AxiomSet<OWLLogicalAxiom> diagnosis : diagnoses) {
            if(!theory.testDiagnosis(diagnosis))
                logger.info("prob");
        }*/
        
        for (AxiomSet<OWLLogicalAxiom> diagnosis : diagnoses) {
            assertTrue(allDiags.contains(diagnosis));
        }

    }


    
    private int getEliminationRate(ITheory<OWLLogicalAxiom> theory, Set<AxiomSet<OWLLogicalAxiom>> d,
                                     boolean a, Partition<OWLLogicalAxiom> partition) 
            throws SolverException {
        int deleted = 0;
        for (AxiomSet<OWLLogicalAxiom> diagnosis : d) {
            if (a && !((OWLTheory)theory).diagnosisConsistentWithoutEntailedTc(diagnosis, partition.partition))
                deleted++;
            else if (!a && ((OWLTheory)theory).diagnosisEntailsWithoutEntailedTC(diagnosis, partition.partition))
                deleted++;
        }
        return deleted;

    }

    private void deleteDiag(ITheory<OWLLogicalAxiom> theory, Set<AxiomSet<OWLLogicalAxiom>> d,
                            boolean answer, Set<OWLLogicalAxiom> partition)
            throws SolverException {
        Set<AxiomSet<OWLLogicalAxiom>> t = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>();
        for (AxiomSet<OWLLogicalAxiom> diagnosis : d) {
            if (answer && !((OWLTheory)theory).diagnosisConsistentWithoutEntailedTc(diagnosis, partition)) {
                t.add(diagnosis);
            }
            else if (!answer && ((OWLTheory)theory).diagnosisEntailsWithoutEntailedTC(diagnosis, partition)) {
                t.add(diagnosis);
            }
        }
        d.removeAll(t);

    }

    protected String simulateBruteForcePaperTest(UniformCostSearch<OWLLogicalAxiom> search,
                                           OWLTheory theory, Set<OWLLogicalAxiom> targetDiag,
                                           TableList entry, QSSType scoringFunc, String message) {
        //DiagProvider diagProvider = new DiagProvider(search, false, 9);

        QSS<OWLLogicalAxiom> qss = createQSSWithDefaultParam(scoringFunc);
        //userBrk=false;

        Partition<OWLLogicalAxiom> actPa = null;

        Set<AxiomSet<OWLLogicalAxiom>> diagnoses = null;
        int num_of_queries = 0;

        boolean userBreak = false;
        boolean systemBreak = false;

        boolean querySessionEnd = false;
        long time = System.currentTimeMillis();
        boolean hasQueryWithNoDecisionPossible = false;
        Time queryTime = new Time();
        Time diagTime = new Time();
        int queryCardinality = 0;
        long reactionTime = 0;
        BruteForce<OWLLogicalAxiom> queryGenerator = new BruteForce<OWLLogicalAxiom>(theory, qss);
        while (!querySessionEnd) {
            try {
                Collection<AxiomSet<OWLLogicalAxiom>> lastD = diagnoses;
                logger.trace("numOfQueries: " + num_of_queries + " search for diagnoses");

                userBreak = false;
                systemBreak = false;

                if (actPa != null && actPa.dx.size() == 1 && actPa.dz.size() == 1 && actPa.dnx.isEmpty()) {
                    logger.error("Help!");
                    printc(theory.getEntailedTests());
                    printc(theory.getNonentailedTests());
                    print(actPa.partition);
                    prinths(actPa.dx);
                    prinths(actPa.dz);
                }

                try {
                    long diag = System.currentTimeMillis();
                    search.run(NUMBER_OF_HITTING_SETS);

                    daStr += search.getStorage().getDiagnoses().size() + "/";
                    diagnosesCalc += search.getStorage().getDiagnoses().size();
                    conflictsCalc += search.getStorage().getConflicts().size();

                    diagnoses = search.getStorage().getDiagnoses();
                    diagTime.setTime(System.currentTimeMillis() - diag);
                } catch (SolverException e) {
                    diagnoses = new TreeSet<AxiomSet<OWLLogicalAxiom>>();

                } catch (NoConflictException e) {
                    diagnoses = new TreeSet<AxiomSet<OWLLogicalAxiom>>(search.getStorage().getDiagnoses());

                }
                
                //logger.info("diagnoses: ");
                for (AxiomSet<OWLLogicalAxiom> diagnosis : diagnoses)
                    logger.info("diagnosis: " + Utils.renderManyAxioms(diagnosis));

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
                    double diagProbabilities = 0;
                    for (AxiomSet<OWLLogicalAxiom> tempd : diagnoses)
                        diagProbabilities += tempd.getMeasure();
                    logger.trace("diagnoses: " + diagnoses.size() +
                            " (" + diagProbabilities + ") first diagnosis: " + d +
                            " is target: " + isTargetDiagFirst + " is in window: " +
                            ((o == null) ? false : o.toString()));
                }

                if (d1 != null) {// && scoringFunc != QSSType.SPLITINHALF) {
                    double d1p = d1.getMeasure();
                    double diff = 100 - (d1p * 100) / dp;
                    logger.trace("difference : " + (dp - d1p) + " - " + diff + " %");
                    if (userBrk && diff > SIGMA && isTargetDiagFirst && num_of_queries > 0) {
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
                //actPa = getBestQuery(search, diagnoses);

                actPa = queryGenerator.generatePartition(diagnoses);
                minimizePartition(actPa,theory);
                logger.info("queried partition: ");
                logger.info("score: " + actPa.score);
                logger.info("actual partition axioms: " + Utils.renderAxioms(actPa.partition));


                //logger.info("actual partition dx: " + actPa.dx.size());
                for (AxiomSet<OWLLogicalAxiom> diagnosis : actPa.dx)
                    logger.info("actual partition dx diag: " + Utils.renderAxioms(diagnosis) + " p: " + diagnosis.getMeasure());
                for (AxiomSet<OWLLogicalAxiom> diagnosis : actPa.dnx)
                    logger.info("actual partition dnx diag: " + Utils.renderAxioms(diagnosis) + " p: " + diagnosis.getMeasure());
                for (AxiomSet<OWLLogicalAxiom> diagnosis : actPa.dz)
                    logger.info("actual partition dz diag: " + Utils.renderAxioms(diagnosis) + " p: " + diagnosis.getMeasure());

                for (Partition<OWLLogicalAxiom> partition : queryGenerator.getPartitions()) {
                    minimizePartition(partition,theory);
                    logger.info("partition: " + queryGenerator.getPartitions().size());
                    logger.info("score: " + partition.score);
                    logger.info("actual partition axioms: " + Utils.renderAxioms(partition.partition));
                    //logger.info("actual partition dx: " + actPa.dx.size());
                    for (AxiomSet<OWLLogicalAxiom> diagnosis : partition.dx)
                        logger.info("actual partition dx diag: " + Utils.renderAxioms(diagnosis) + " p: " + diagnosis.getMeasure());
                    for (AxiomSet<OWLLogicalAxiom> diagnosis : partition.dnx)
                        logger.info("actual partition dnx diag: " + Utils.renderAxioms(diagnosis) + " p: " + diagnosis.getMeasure());
                    for (AxiomSet<OWLLogicalAxiom> diagnosis : partition.dz)
                        logger.info("actual partition dz diag: " + Utils.renderAxioms(diagnosis) + " p: " + diagnosis.getMeasure());
                }
                
                if (actPa == null || actPa.partition == null || (last != null && actPa.partition.equals(last.partition))) {
                    // system brake
                    querySessionEnd = true;
                    break;
                }
                queryCardinality = actPa.partition.size();


                long querytime = System.currentTimeMillis() - query;
                queryTime.setTime(querytime);
                reactionTime += querytime;
                num_of_queries++;

                logger.trace("numOfQueries: " + num_of_queries + " generate answer");
                boolean answer = true;
                boolean hasAn = false;
                while (!hasAn) {
                    try {
                        answer = generateQueryAnswer(search, actPa, targetDiag);
                        hasAn = true;
                    } catch (NoDecisionPossibleException e) {
                        hasQueryWithNoDecisionPossible = true;
                        actPa = queryGenerator.nextPartition(actPa);
                        if (actPa == null) {
                            logger.error("All partitions were tested and none provided an answer to the target diagnosis!");
                            break;
                        }
                    }
                }
                logger.info("answer " + answer);

                if (qss != null) qss.updateParameters(answer);


                // fine all dz diagnoses
                // TODO do we need this fine?
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
            targetDiagnosisIsInWind = isInWindow(targetDiag, diagnoses);
            if (diagnoses.size() >= 1 && targetDiag.
                    containsAll((new TreeSet<AxiomSet<OWLLogicalAxiom>>(diagnoses)).last())) {
                targetDiagnosisIsMostProbable = true;
                targetDiagnosisIsInWind = true;
            }
        }
        int diagWinSize = 0;
        if (diagnoses != null)
            diagWinSize = diagnoses.size();

        int consistencyCount = 0;
        if (num_of_queries != 0) consistencyCount = theory.getConsistencyCount() / num_of_queries;
        if (num_of_queries != 0) reactionTime = reactionTime / num_of_queries;

        message += " , Iteration finished within " + time + " ms, required " + num_of_queries + " queries, most probable "
                + targetDiagnosisIsMostProbable + ", is in window " + targetDiagnosisIsInWind + ", size of window  " + diagWinSize
                + ", reaction " + reactionTime + ", user " + userBreak +
                ", systemBrake " + systemBreak + ", nd " + hasQueryWithNoDecisionPossible +
                ", consistency checks " + consistencyCount;
        logger.info(message);

        String msg = time + ", " + num_of_queries + ", " + targetDiagnosisIsMostProbable + ", " + targetDiagnosisIsInWind + ", " + diagWinSize
                + ", " + reactionTime + ", " + userBreak +
                ", " + systemBreak + ", " + hasQueryWithNoDecisionPossible +
                ", " + consistencyCount;
        entry.addEntr(num_of_queries, queryCardinality, targetDiagnosisIsInWind, targetDiagnosisIsMostProbable,
                diagWinSize, userBreak, systemBreak, time, queryTime, diagTime, reactionTime, consistencyCount);
        return msg;
    }

    protected <E extends AxiomSet<OWLLogicalAxiom>> E containsItem(Collection<E> col, Set<OWLLogicalAxiom> item) {
        for (E o : col) {
            if (item.containsAll(o)) {
                if (logger.isTraceEnabled())
                    logger.trace("Target dianosis " + o + "is in the window");
                return o;
            }
        }
        return null;
    }

    protected boolean isInWindow(Set<OWLLogicalAxiom> targetDiag, Set<AxiomSet<OWLLogicalAxiom>> diagnoses) {
        for (AxiomSet<OWLLogicalAxiom> ps : diagnoses)
            if (targetDiag.containsAll(ps)) {
                if (logger.isDebugEnabled())
                    logger.debug("Target diagnosis is in window " + ps.getName());
                return true;
            }
        return false;
    }

    public boolean generateQueryAnswer
            (UniformCostSearch<OWLLogicalAxiom> search,
             Partition<OWLLogicalAxiom> actualQuery, Set<OWLLogicalAxiom> t) throws NoDecisionPossibleException {
        boolean answer;
        ITheory<OWLLogicalAxiom> theory = search.getTheory();

        AxiomSet<OWLLogicalAxiom> target = AxiomSetFactory.createHittingSet(0.5, t, new LinkedHashSet<OWLLogicalAxiom>());
        if (theory.diagnosisEntails(target, actualQuery.partition)) {
            answer = true;
            assertTrue(!actualQuery.dnx.contains(target));
        } else if (!theory.diagnosisConsistent(target, actualQuery.partition)) {
            answer = false;
            assertTrue(!actualQuery.dx.contains(target));
        } else {
            throw new NoDecisionPossibleException();
        }

        return answer;

    }

    class NoDecisionPossibleException extends Exception {

    }


    enum BackgroundO {EMPTY, O1, O2, O1_O2}

    @Ignore
    @Test
    public void search() throws SolverException, InconsistentTheoryException {
        Properties properties = readProps();
        Map<String, List<String>> mapOntos = readOntologiesFromFile(properties);
        for (String m : mapOntos.keySet()) {
            for (String o : mapOntos.get(m)) {
                BackgroundO[] backgrounds = new BackgroundO[] { BackgroundO.O1_O2 };
                for (BackgroundO background : backgrounds ) {
                    String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
                    OWLOntology ontology = createOwlOntology(m.trim(), o.trim());
                    Set<OWLLogicalAxiom> targetDg = getDiagnosis(targetAxioms, ontology);
                    OWLOntology ontology1 = createOwlOntology(o.split("-")[0].trim());
                    OWLOntology ontology2 = createOwlOntology(o.split("-")[1].trim());
                    OWLTheory theory = createOWLTheory(ontology, false);
                    UniformCostSearch<OWLLogicalAxiom> search = createUniformCostSearch(theory, false);
                    //ProbabilityTableModel mo = new ProbabilityTableModel();
                    HashMap<ManchesterOWLSyntax, Double> map = Utils.getProbabMap();
                    OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theory);
                    es.updateKeywordProb(map);
                    if (background == BackgroundO.O1 || background == BackgroundO.O1_O2)
                        theory.addBackgroundFormulas(ontology1.getLogicalAxioms());
                    if (background == BackgroundO.O2 || background == BackgroundO.O1_O2)
                        theory.addBackgroundFormulas(ontology2.getLogicalAxioms());
                    search.setCostsEstimator(es);


                    long time = System.nanoTime();
                    try {
                        search.run();
                    } catch (SolverException e) {
                        logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (NoConflictException e) {
                        logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (InconsistentTheoryException e) {
                        logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    time = System.nanoTime() - time;
                    String t = Utils.getStringTime(time / 1000000);

                    Set<AxiomSet<OWLLogicalAxiom>> diagnoses =
                            Collections.unmodifiableSet(search.getStorage().getDiagnoses());
                    //logger.info(m + " " + o + " background: " + background + " diagnoses: " + diagnoses.size());

                    int n = 0;
                    Set<AxiomSet<OWLLogicalAxiom>> set = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>();
                    for (AxiomSet<OWLLogicalAxiom> d : diagnoses)
                        if (targetDg.containsAll(d)) set.add(d);
                    n = set.size();
                    int cs = search.getStorage().getConflicts().size();
                    search.clearSearch();
                    logger.info(m + " " + o + " background: " + background + " diagnoses: " + diagnoses.size()
                            + " conflicts: " + cs + " time " + t + " target " + n);

                }
            }
        }
    }

    public AxiomSet<OWLLogicalAxiom> getTargetDiag(Set<AxiomSet<OWLLogicalAxiom>> diagnoses, final CostsEstimator<OWLLogicalAxiom> e, String m) {
        Comparator<AxiomSet<OWLLogicalAxiom>> c = new Comparator<AxiomSet<OWLLogicalAxiom>>() {
            public int compare(AxiomSet<OWLLogicalAxiom> o1, AxiomSet<OWLLogicalAxiom> o2) {
                int numOfOntologyAxiomsO1 = 0;
                int numOfMatchingAxiomO1 = 0;
                for (OWLLogicalAxiom axiom : o1) {
                    if (e.getAxiomCosts(axiom) != 0.001)
                        numOfMatchingAxiomO1++;
                    else
                        numOfOntologyAxiomsO1++;
                }
                double percAxiomFromOntO1 = (double) numOfOntologyAxiomsO1;// / (numOfOntologyAxiomsO1 + numOfMatchingAxiomO1);

                int numOfOntologyAxiomsO2 = 0;
                int numOfMatchingAxiomO2 = 0;
                for (OWLLogicalAxiom axiom : o2) {
                    if (e.getAxiomCosts(axiom) != 0.001)
                        numOfMatchingAxiomO2++;
                    else
                        numOfOntologyAxiomsO2++;
                }
                double percAxiomFromOntO2 = (double) numOfOntologyAxiomsO2;// / (numOfOntologyAxiomsO2 + numOfMatchingAxiomO2);


                if (percAxiomFromOntO1 < percAxiomFromOntO2)
                    return -1;
                else if (percAxiomFromOntO1 == percAxiomFromOntO2)
                    return 0;
                else
                    return 1;
            }
        };

        return Collections.max(diagnoses, c);

    }

    enum TargetSource {FROM_FILE, FROM_30_DIAGS}

    @Ignore
    @Test
    public void doOnlyOneQuerySession() throws SolverException, InconsistentTheoryException, IOException {
        Properties properties = readProps();
        Map<String, List<String>> mapOntos = readOntologiesFromFile(properties);

        QSSType[] qssTypes = new QSSType[]{QSSType.MINSCORE, QSSType.SPLITINHALF, QSSType.DYNAMICRISK};
        String m = "owlctxmatch";
        String o = "SIGKDD-EKAW";
        TargetSource targetSource = TargetSource.FROM_30_DIAGS; //TargetSource.FROM_FILE;
        QSSType type = QSSType.SPLITINHALF;
        String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
        OWLOntology ontology = createOwlOntology(m.trim(), o.trim());
        Set<OWLLogicalAxiom> targetDg;
        OWLTheory theory = createOWLTheory(ontology, false);
        UniformCostSearch<OWLLogicalAxiom> search = createUniformCostSearch(theory, false);
        OWLOntology ontology1 = createOwlOntology(o.split("-")[0].trim());
        OWLOntology ontology2 = createOwlOntology(o.split("-")[1].trim());
        //theory.addBackgroundFormulas(ontology1.getLogicalAxioms());
        //theory.addBackgroundFormulas(ontology2.getLogicalAxioms());
        //ProbabilityTableModel mo = new ProbabilityTableModel();
        HashMap<ManchesterOWLSyntax, Double> map = Utils.getProbabMap();

        String path = ClassLoader.getSystemResource("alignment/evaluation/"
                + m.trim()
                + "-incoherent-evaluation/"
                + o.trim()
                + ".txt").getPath();

        OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, path);
        //es.updateKeywordProb(map);
        targetDg = null;

        search.setCostsEstimator(es);
        if (targetSource == TargetSource.FROM_30_DIAGS) {
            try {
                search.run(30);
            } catch (SolverException e) {
                logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (NoConflictException e) {
                logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InconsistentTheoryException e) {
                logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            Set<AxiomSet<OWLLogicalAxiom>> diagnoses =
                    Collections.unmodifiableSet(search.getStorage().getDiagnoses());
            search.clearSearch();
            AxiomSet<OWLLogicalAxiom> targD = getTargetDiag(diagnoses, es, m);
            targetDg = new LinkedHashSet<OWLLogicalAxiom>();
            for (OWLLogicalAxiom axiom : targD)
                targetDg.add(axiom);
        }

        if (targetSource == TargetSource.FROM_FILE)
            targetDg = getDiagnosis(targetAxioms, ontology);

        TableList e = new TableList();
        String message = "act " + m + " - " + o + " - " + targetSource + " " + type;
        simulateBruteForceOnl(search, theory, targetDg, e, type, message);

    }

    @Ignore
    @Test
    public void doHardTwoTests() throws SolverException, InconsistentTheoryException, IOException {
        Properties properties = readProps();
        Map<String, List<String>> mapOntos = readOntologiesFromFile(properties);

        QSSType[] qssTypes = new QSSType[]{QSSType.MINSCORE, QSSType.SPLITINHALF, QSSType.DYNAMICRISK};
        TargetSource[] targetSources = new TargetSource[]{TargetSource.FROM_FILE};
        NUMBER_OF_HITTING_SETS = 4;


        for (String m : mapOntos.keySet()) {
            for (String o : mapOntos.get(m)) {
                for (TargetSource targetSource : targetSources) {
                    for (QSSType type : qssTypes) {
                        BackgroundO[] backgr = new BackgroundO[]{BackgroundO.EMPTY, BackgroundO.O1_O2};
                        for (BackgroundO background : backgr) {
                            String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
                            OWLOntology ontology = createOwlOntology(m.trim(), o.trim());
                            Set<OWLLogicalAxiom> targetDg;
                            OWLTheory theory = createOWLTheory(ontology, false);
                            UniformCostSearch<OWLLogicalAxiom> search = createUniformCostSearch(theory, false);
                            //ProbabilityTableModel mo = new ProbabilityTableModel();
                            HashMap<ManchesterOWLSyntax, Double> map = Utils.getProbabMap();

                            String path = ClassLoader.getSystemResource("alignment/evaluation/"
                                    + m.trim()
                                    + "-incoherent-evaluation/"
                                    + o.trim()
                                    + ".txt").getPath();

                            OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, path);
                            OWLOntology ontology1 = createOwlOntology(o.split("-")[0].trim());
                            OWLOntology ontology2 = createOwlOntology(o.split("-")[1].trim());
                            if (background == BackgroundO.O1_O2) {
                                theory.addBackgroundFormulas(ontology1.getLogicalAxioms());
                                theory.addBackgroundFormulas(ontology2.getLogicalAxioms());
                            }
                            //es.updateKeywordProb(map);
                            targetDg = null;

                            search.setCostsEstimator(es);
                            if (targetSource == TargetSource.FROM_30_DIAGS) {
                                try {
                                    search.run(30);
                                } catch (SolverException e) {
                                    logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                } catch (NoConflictException e) {
                                    logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                } catch (InconsistentTheoryException e) {
                                    logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                }

                                Set<AxiomSet<OWLLogicalAxiom>> diagnoses =
                                        Collections.unmodifiableSet(search.getStorage().getDiagnoses());
                                search.clearSearch();
                                AxiomSet<OWLLogicalAxiom> targD = getTargetDiag(diagnoses, es, m);
                                targetDg = new LinkedHashSet<OWLLogicalAxiom>();
                                for (OWLLogicalAxiom axiom : targD)
                                    targetDg.add(axiom);
                            }

                            if (targetSource == TargetSource.FROM_FILE)
                                targetDg = getDiagnosis(targetAxioms, ontology);

                            TableList e = new TableList();
                            String message = "running "
                                    + "matcher " + m
                                    + ", ontology " + o
                                    + ", source " + targetSource
                                    + ", qss " + type
                                    + ", background " + background;
                            simulateBruteForceOnl(search, theory, targetDg, e, type, message);
                        }
                    }
                }
            }
        }
    }
    
    private void addAxiomPaperTest(OWLOntology ontology, String axiom, Double probab,Map<OWLLogicalAxiom,Double> m) {
        MyOWLRendererParser parser = new MyOWLRendererParser(ontology);
        OWLLogicalAxiom ax1 = parser.parse(axiom);
        ontology.getOWLOntologyManager().addAxiom(ontology, ax1);
        m.put(ax1,probab);
    }

    @Ignore
    @Test
    public void doPaperTestOld() throws SolverException, InconsistentTheoryException, IOException, OWLOntologyCreationException {
        //Properties properties = readProps();
        //Map<String, List<String>> mapOntos = readOntologiesFromFile(properties);
        //for (String m : mapOntos.keySet()) {
        // for (String o : mapOntos.get(m)) {

        //String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
        OWLOntology ontology = createOwlOntologyFromP("test1", "ontologies");


//                OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
//                IRI ontologyIRI = IRI.create("http://testont.owl");
//                IRI documentIRI = IRI.create("file:/tmp/MyOnt.owl");
//                SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, documentIRI);
//                manager.addIRIMapper(mapper);
//                OWLOntology ontology = manager.createOntology(ontologyIRI);
//                OWLDataFactory factory = manager.getOWLDataFactory();
//                OWLClass clsA = factory.getOWLClass(IRI.create(ontologyIRI + "#A"));
//                OWLClass clsB = factory.getOWLClass(IRI.create(ontologyIRI + "#B"));
//                OWLAxiom axiom2 = factory.getOWLSubClassOfAxiom(clsA, clsB);
//                ontology.getOWLOntologyManager().addAxiom(ontology, axiom2);

        //Set<OWLLogicalAxiom> targetDg = getDiagnosis(targetAxioms, ontology);
        //OWLOntology ontology1 = createOwlOntology(o.split("-")[0].trim());
        //OWLOntology ontology2 = createOwlOntology(o.split("-")[1].trim());
        OWLTheory theory = createOWLTheory(ontology, false);
        UniformCostSearch<OWLLogicalAxiom> search = createUniformCostSearch(theory, false);
        //ProbabilityTableModel mo = new ProbabilityTableModel();
        HashMap<ManchesterOWLSyntax, Double> map = Utils.getProbabMap();

        /*String path = ClassLoader.getSystemResource("alignment/evaluation/"
       + m.trim()
       + "-incoherent-evaluation/"
       + o.trim()
       + ".txt").getPath();*/



        MyOWLRendererParser parser = new MyOWLRendererParser(ontology);
        Map<OWLLogicalAxiom,Double> probMap = new LinkedHashMap<OWLLogicalAxiom, Double>();

        for (OWLLogicalAxiom axiom : ontology.getLogicalAxioms())
            ontology.getOWLOntologyManager().removeAxiom(ontology,axiom);

        /*OWLLogicalAxiom ax1 = parser.parse("t1 Type Student");
        ontology.getOWLOntologyManager().addAxiom(ontology, ax1);
        probMap.put(ax1,0.2);*/




        //OWLOntology ontology = ontology1.getOWLOntologyManager().createOntology();

        addAxiomPaperTest(ontology, "InstChair_1 SubClassOf Researcher1", 0.1, probMap);
        addAxiomPaperTest(ontology, "Prof_1 SubClassOf InstChair_1", 0.1, probMap);
        addAxiomPaperTest(ontology, "InstChair_1 SubClassOf DeptChair_2", 0.1, probMap);
        addAxiomPaperTest(ontology, "DeptChair_2 SubClassOf Manager_2 and (not (Researcher2))", 0.1, probMap);
        addAxiomPaperTest(ontology, "Researcher1 SubClassOf Researcher2", 0.1, probMap);
        addAxiomPaperTest(ontology, "s1 Type Student_1", 0.1, probMap);
        addAxiomPaperTest(ontology, "s1 hasSupervisor t1", 0.1, probMap);
        addAxiomPaperTest(ontology, "Student_1 SubClassOf hasSupervisor only Prof_1", 0.1, probMap);

        //Properties properties = readTestProps();
        //Map<OWLLogicalAxiom,Double> probMap = new LinkedHashMap<OWLLogicalAxiom, Double>();
        /*for (OWLLogicalAxiom axiom : ontology.getLogicalAxioms()) {
            String axiomStr = MyOWLRendererParser.render(axiom).trim();
            String p = properties.getProperty(axiomStr);
            probMap.put(axiom,Double.parseDouble(p));
            logger.info("axiom: " + axiomStr + " p: " + Double.parseDouble(p));
        }*/


        OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, probMap);

        Set<OWLLogicalAxiom> targetDg = new LinkedHashSet<OWLLogicalAxiom>();
        for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms())
            if (MyOWLRendererParser.render(axiom).equals("Teacher SubClassOf hasBoss only HeadProf"))
                targetDg.add(axiom);


        //es.updateKeywordProb(map);

        //theory.addBackgroundFormulas(ontology1.getLogicalAxioms());
        //theory.addBackgroundFormulas(ontology2.getLogicalAxioms());
        search.setCostsEstimator(es);

        // brute force statt ckk

                try {
                    search.run();
                } catch (SolverException e) {
                    logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (NoConflictException e) {
                    logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (InconsistentTheoryException e) {
                    logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

        Set<AxiomSet<OWLLogicalAxiom>> diagnoses =
                Collections.unmodifiableSet(search.getStorage().getDiagnoses());
        search.clearSearch();

        TableList e = new TableList();
        //logger.info(m + " - " + o);
        simulateBruteForcePaperTest(search, theory, targetDg, e, QSSType.MINSCORE, "");

//                boolean target = false;
//                for (AxiomSet<OWLLogicalAxiom> d : diagnoses)
//                    if (targetDg.containsAll(d)) target = true;
//                if (!target) logger.info("target notf "+m+o);


        //  }
        //}


    }

    @Ignore
    @Test
    public void doPaperSm() throws SolverException, InconsistentTheoryException, IOException, OWLOntologyCreationException {
        //Properties properties = readProps();
        //Map<String, List<String>> mapOntos = readOntologiesFromFile(properties);
        //for (String m : mapOntos.keySet()) {
        // for (String o : mapOntos.get(m)) {

        //String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
        OWLOntology ontology = createOwlOntologyFromP("testPaperSm", "ontologies");


//                OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
//                IRI ontologyIRI = IRI.create("http://testont.owl");
//                IRI documentIRI = IRI.create("file:/tmp/MyOnt.owl");
//                SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, documentIRI);
//                manager.addIRIMapper(mapper);
//                OWLOntology ontology = manager.createOntology(ontologyIRI);
//                OWLDataFactory factory = manager.getOWLDataFactory();
//                OWLClass clsA = factory.getOWLClass(IRI.create(ontologyIRI + "#A"));
//                OWLClass clsB = factory.getOWLClass(IRI.create(ontologyIRI + "#B"));
//                OWLAxiom axiom2 = factory.getOWLSubClassOfAxiom(clsA, clsB);
//                ontology.getOWLOntologyManager().addAxiom(ontology, axiom2);

        MyOWLRendererParser parser = new MyOWLRendererParser(ontology);
        Map<OWLLogicalAxiom,Double> probMap = new LinkedHashMap<OWLLogicalAxiom, Double>();

        for (OWLLogicalAxiom axiom : ontology.getLogicalAxioms())
            ontology.getOWLOntologyManager().removeAxiom(ontology,axiom);


        /*OWLLogicalAxiom ax1 = parser.parse("t1 Type Student");
        ontology.getOWLOntologyManager().addAxiom(ontology, ax1);
        probMap.put(ax1,0.2);*/

        addAxiomPaperTest(ontology, "w Type A", 0.1, probMap);
        addAxiomPaperTest(ontology, "B SubClassOf C", 0.1, probMap);
        addAxiomPaperTest(ontology, "C SubClassOf Q", 0.1, probMap);
        addAxiomPaperTest(ontology, "Q SubClassOf R", 0.1, probMap);
        addAxiomPaperTest(ontology, "w Type not (R)", 0.1, probMap);
        addAxiomPaperTest(ontology, "R SubClassOf Thing", 0.1, probMap);
        addAxiomPaperTest(ontology, "A SubClassOf B", 0.1, probMap);

        //Set<OWLLogicalAxiom> targetDg = getDiagnosis(targetAxioms, ontology);
        //OWLOntology ontology1 = createOwlOntology(o.split("-")[0].trim());
        //OWLOntology ontology2 = createOwlOntology(o.split("-")[1].trim());
        OWLTheory theory = createOWLTheory(ontology, false);
        UniformCostSearch<OWLLogicalAxiom> search = createUniformCostSearch(theory, false);
        //ProbabilityTableModel mo = new ProbabilityTableModel();
        HashMap<ManchesterOWLSyntax, Double> map = Utils.getProbabMap();

        /*String path = ClassLoader.getSystemResource("alignment/evaluation/"
       + m.trim()
       + "-incoherent-evaluation/"
       + o.trim()
       + ".txt").getPath();*/






        //OWLOntology ontology = ontology1.getOWLOntologyManager().createOntology();



        //Properties properties = readTestProps();
        //Map<OWLLogicalAxiom,Double> probMap = new LinkedHashMap<OWLLogicalAxiom, Double>();
        /*for (OWLLogicalAxiom axiom : ontology.getLogicalAxioms()) {
            String axiomStr = MyOWLRendererParser.render(axiom).trim();
            String p = properties.getProperty(axiomStr);
            probMap.put(axiom,Double.parseDouble(p));
            logger.info("axiom: " + axiomStr + " p: " + Double.parseDouble(p));
        }*/


        OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, probMap);

        Set<OWLLogicalAxiom> targetDg = new LinkedHashSet<OWLLogicalAxiom>();
        for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms())
            if (MyOWLRendererParser.render(axiom).equals("A SubClassOf B"))
                targetDg.add(axiom);


        //es.updateKeywordProb(map);

        //theory.addBackgroundFormulas(ontology1.getLogicalAxioms());
        //theory.addBackgroundFormulas(ontology2.getLogicalAxioms());
        search.setCostsEstimator(es);

        // brute force statt ckk

//                try {
//                    search.run();
//                } catch (SolverException e) {
//                    logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                } catch (NoConflictException e) {
//                    logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                } catch (InconsistentTheoryException e) {
//                    logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                }

        Set<AxiomSet<OWLLogicalAxiom>> diagnoses =
                Collections.unmodifiableSet(search.getStorage().getDiagnoses());
        search.clearSearch();

        TableList e = new TableList();
        //logger.info(m + " - " + o);
        simulateBruteForcePaperTest(search, theory, targetDg, e, QSSType.MINSCORE, "");

//                boolean target = false;
//                for (AxiomSet<OWLLogicalAxiom> d : diagnoses)
//                    if (targetDg.containsAll(d)) target = true;
//                if (!target) logger.info("target notf "+m+o);


        //  }
        //}


    }

    public void minimizePartition(Partition<OWLLogicalAxiom> partition, ITheory<OWLLogicalAxiom> theory) {
        QueryMinimizer<OWLLogicalAxiom> mnz = new QueryMinimizer<OWLLogicalAxiom>(partition, theory);
        NewQuickXplain<OWLLogicalAxiom> q = new NewQuickXplain<OWLLogicalAxiom>();
        try {
            partition.partition = q.search(mnz, partition.partition, null);
        } catch (NoConflictException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    @Ignore
    @Test
    public void doPaperTest() throws SolverException, InconsistentTheoryException, IOException, OWLOntologyCreationException {

        QSSType[] types = new QSSType[] {QSSType.MINSCORE, QSSType.SPLITINHALF, QSSType.DYNAMICRISK};
        for (QSSType type : types ) {
            //Properties properties = readProps();
            //Map<String, List<String>> mapOntos = readOntologiesFromFile(properties);
            //for (String m : mapOntos.keySet()) {
               // for (String o : mapOntos.get(m)) {

                //String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
                    OWLOntology ontology = createOwlOntologyFromP("test", "ontologies");


    //                OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    //                IRI ontologyIRI = IRI.create("http://testont.owl");
    //                IRI documentIRI = IRI.create("file:/tmp/MyOnt.owl");
    //                SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, documentIRI);
    //                manager.addIRIMapper(mapper);
    //                OWLOntology ontology = manager.createOntology(ontologyIRI);
    //                OWLDataFactory factory = manager.getOWLDataFactory();
    //                OWLClass clsA = factory.getOWLClass(IRI.create(ontologyIRI + "#A"));
    //                OWLClass clsB = factory.getOWLClass(IRI.create(ontologyIRI + "#B"));
    //                OWLAxiom axiom2 = factory.getOWLSubClassOfAxiom(clsA, clsB);
    //                ontology.getOWLOntologyManager().addAxiom(ontology, axiom2);
            
            logger.info("--    " + type + "   --");
            MyOWLRendererParser parser = new MyOWLRendererParser(ontology);
            Map<OWLLogicalAxiom,Double> probMap = new LinkedHashMap<OWLLogicalAxiom, Double>();

            for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms()) {
                if (new ManchesterOWLSyntaxOWLObjectRendererImpl().render(axiom).equals("PHD_2 SubClassOf PHD_1")) {
                    probMap.put(axiom,1-0.1);
                }
                else if (new ManchesterOWLSyntaxOWLObjectRendererImpl().render(axiom).equals("DeptMember_1 SubClassOf DeptMember_2")) {
                    probMap.put(axiom,1-0.45);
                }
                else {
                    probMap.put(axiom,1-0.001);
                }
            }

            /*for (OWLLogicalAxiom axiom : ontology.getLogicalAxioms())
                ontology.getOWLOntologyManager().removeAxiom(ontology,axiom);*/


            /*OWLLogicalAxiom ax1 = parser.parse("t1 Type Student");
            ontology.getOWLOntologyManager().addAxiom(ontology, ax1);
            probMap.put(ax1,0.2);*/

            /*addAxiomPaperTest(ontology, "PHD_2 SubClassOf PHD_1", 0.1, probMap);
            addAxiomPaperTest(ontology, "PHD_1 SubClassOf Researcher1", 0.1, probMap);
            addAxiomPaperTest(ontology, "Student_2 SubClassOf not (ResearchStuff_2)", 0.1, probMap);
            //addAxiomPaperTest(ontology, "s1 hasStudent t1", 0.1, probMap);
            addAxiomPaperTest(ontology, "Researcher1 SubClassOf ResearchStuff_1", 0.1, probMap);
            addAxiomPaperTest(ontology, "PHD_2 SubClassOf Student_2", 0.1, probMap);
            //addAxiomPaperTest(ontology, "t1 Type Researcher1", 0.1, probMap);
            addAxiomPaperTest(ontology, "ResearchStuff_1 SubClassOf ResearchStuff_2", 0.1, probMap); */
            //addAxiomPaperTest(ontology, "ResearchStuff_2 SubClassOf hasStudent only PHD_2", 0.1, probMap);


                    //Set<OWLLogicalAxiom> targetDg = getDiagnosis(targetAxioms, ontology);
                    //OWLOntology ontology1 = createOwlOntology(o.split("-")[0].trim());
                    //OWLOntology ontology2 = createOwlOntology(o.split("-")[1].trim());
                    OWLTheory theory = createOWLTheory(ontology, false);
                    UniformCostSearch<OWLLogicalAxiom> search = createUniformCostSearch(theory, false);
                    //ProbabilityTableModel mo = new ProbabilityTableModel();
                    HashMap<ManchesterOWLSyntax, Double> map = Utils.getProbabMap();

                    /*String path = ClassLoader.getSystemResource("alignment/evaluation/"
                            + m.trim()
                            + "-incoherent-evaluation/"
                            + o.trim()
                            + ".txt").getPath();*/






                    //OWLOntology ontology = ontology1.getOWLOntologyManager().createOntology();



                    //Properties properties = readTestProps();
                    //Map<OWLLogicalAxiom,Double> probMap = new LinkedHashMap<OWLLogicalAxiom, Double>();
                    /*for (OWLLogicalAxiom axiom : ontology.getLogicalAxioms()) {
                        String axiomStr = MyOWLRendererParser.render(axiom).trim();
                        String p = properties.getProperty(axiomStr);
                        probMap.put(axiom,Double.parseDouble(p));
                        logger.info("axiom: " + axiomStr + " p: " + Double.parseDouble(p));
                    }*/


                    OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, probMap);

                    Set<OWLLogicalAxiom> targetDg = new LinkedHashSet<OWLLogicalAxiom>();
                    for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms())
                            if (MyOWLRendererParser.render(axiom).equals("Researcher_1 SubClassOf DeptMember_1"))
                                targetDg.add(axiom);


                    //es.updateKeywordProb(map);

                    //theory.addBackgroundFormulas(ontology1.getLogicalAxioms());
                    //theory.addBackgroundFormulas(ontology2.getLogicalAxioms());
                    search.setCostsEstimator(es);

                    // brute force statt ckk

    //                try {
    //                    search.run();
    //                } catch (SolverException e) {
    //                    logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    //                } catch (NoConflictException e) {
    //                    logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    //                } catch (InconsistentTheoryException e) {
    //                    logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    //                }

                    Set<AxiomSet<OWLLogicalAxiom>> diagnoses =
                            Collections.unmodifiableSet(search.getStorage().getDiagnoses());
                    search.clearSearch();

                    TableList e = new TableList();
                    //logger.info(m + " - " + o);
                    simulateBruteForcePaperTest(search, theory, targetDg, e, type, "");

    //                boolean target = false;
    //                for (AxiomSet<OWLLogicalAxiom> d : diagnoses)
    //                    if (targetDg.containsAll(d)) target = true;
    //                if (!target) logger.info("target notf "+m+o);


              //  }
            //}
        }

    }
    
    Set<AxiomSet<OWLLogicalAxiom>> allDiags;

    @Test
    public void doQueryEliminationRateTest() throws SolverException, InconsistentTheoryException, IOException {
        Properties properties = readProps();
        Map<String, List<String>> mapOntos = readOntologiesFromFile(properties);

        QSSType[] qssTypes = new QSSType[]{QSSType.SPLITINHALF, };
        for (boolean dual : new boolean[] {false}) {
            for (TargetSource targetSource : new TargetSource[]{TargetSource.FROM_FILE}) {
                for (String m : new String[]{"coma"}) {
                    for (String o : new String[]{"CRS-EKAW"}) {
                        String out ="STAT, " + m +  ", " + o;
                        for (QSSType type : qssTypes) {
                            String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
                            OWLOntology ontology = createOwlOntology(m.trim(), o.trim());
                            Set<OWLLogicalAxiom> targetDg;
                            OWLTheory theory = createOWLTheory(ontology, dual);
                            UniformCostSearch<OWLLogicalAxiom> search = createUniformCostSearch(theory, dual);
                            //ProbabilityTableModel mo = new ProbabilityTableModel();
                            HashMap<ManchesterOWLSyntax, Double> map = Utils.getProbabMap();

                            String path = ClassLoader.getSystemResource("alignment/evaluation/"
                                    + m.trim()
                                    + "-incoherent-evaluation/"
                                    + o.trim()
                                    + ".txt").getPath();

                            OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, path);

                            //                        double[] p = new ModerateDistribution().getProbabilities(ontology.getLogicalAxioms().size());
                            //                        Map<OWLLogicalAxiom,Double> axmap = new LinkedHashMap<OWLLogicalAxiom, Double>();
                            //                        int i = 0;
                            //                        for (OWLLogicalAxiom axiom : ontology.getLogicalAxioms()) {
                            //                            axmap.put(axiom,p[i]);
                            //                            i++;
                            //                        }
                            //                        OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, axmap);


                            //es.updateKeywordProb(map);
                            targetDg = null;

                            search.setCostsEstimator(es);
                            //
                            try {
                                search.run();
                            } catch (SolverException e) {
                                logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            } catch (NoConflictException e) {
                                logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            } catch (InconsistentTheoryException e) {
                                logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }

                            allDiags = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>(search.getStorage().getDiagnoses());
                            search.clearSearch();

                            if (targetSource == TargetSource.FROM_30_DIAGS) {
                                try {
                                    search.run(30);
                                } catch (SolverException e) {
                                    logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                } catch (NoConflictException e) {
                                    logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                } catch (InconsistentTheoryException e) {
                                    logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                }

                                Set<AxiomSet<OWLLogicalAxiom>> diagnoses =
                                        Collections.unmodifiableSet(search.getStorage().getDiagnoses());
                                search.clearSearch();
                                AxiomSet<OWLLogicalAxiom> targD = getTargetDiag(diagnoses, es, m);
                                targetDg = new LinkedHashSet<OWLLogicalAxiom>();
                                for (OWLLogicalAxiom axiom : targD)
                                    targetDg.add(axiom);
                            }

                            if (targetSource == TargetSource.FROM_FILE)
                                targetDg = getDiagnosis(targetAxioms, ontology);

                            TableList e = new TableList();
                            out += "," + type + ",";
                            String message = "act " + m + " - " + o + " - " + targetSource + " " + type + " d " + dual;
                            out += simulateBruteForceOnl(search, theory, targetDg, e, type, message);
                        }
                        logger.info(out);
                    }
                }
            }
        }
    }

    @Test
    public void doTwoTests() throws SolverException, InconsistentTheoryException, IOException {
        Properties properties = readProps();
        Map<String, List<String>> mapOntos = readOntologiesFromFile(properties);

        QSSType[] qssTypes = new QSSType[]{QSSType.MINSCORE, QSSType.SPLITINHALF, QSSType.DYNAMICRISK};
        for (boolean dual : new boolean[] {false, true}) {
           for (TargetSource targetSource : TargetSource.values()) {
                for (String m : mapOntos.keySet()) {
                    for (String o : mapOntos.get(m)) {
                        String out ="STAT, " + m +  ", " + o;
                        for (QSSType type : qssTypes) {
                            String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
                            OWLOntology ontology = createOwlOntology(m.trim(), o.trim());
                            Set<OWLLogicalAxiom> targetDg;
                            OWLTheory theory = createOWLTheory(ontology, dual);
                            UniformCostSearch<OWLLogicalAxiom> search = createUniformCostSearch(theory, dual);
                            //ProbabilityTableModel mo = new ProbabilityTableModel();
                            HashMap<ManchesterOWLSyntax, Double> map = Utils.getProbabMap();

                            String path = ClassLoader.getSystemResource("alignment/evaluation/"
                                    + m.trim()
                                    + "-incoherent-evaluation/"
                                    + o.trim()
                                    + ".txt").getPath();

                            OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, path);

    //                        double[] p = new ModerateDistribution().getProbabilities(ontology.getLogicalAxioms().size());
    //                        Map<OWLLogicalAxiom,Double> axmap = new LinkedHashMap<OWLLogicalAxiom, Double>();
    //                        int i = 0;
    //                        for (OWLLogicalAxiom axiom : ontology.getLogicalAxioms()) {
    //                            axmap.put(axiom,p[i]);
    //                            i++;
    //                        }
    //                        OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, axmap);


                            //es.updateKeywordProb(map);
                            targetDg = null;

                            search.setCostsEstimator(es);
                            // 
                            try {
                                search.run();
                            } catch (SolverException e) {
                                logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            } catch (NoConflictException e) {
                                logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            } catch (InconsistentTheoryException e) {
                                logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }

                            allDiags = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>(search.getStorage().getDiagnoses());
                            search.clearSearch();
                            
                            if (targetSource == TargetSource.FROM_30_DIAGS) {
                                try {
                                    search.run(30);
                                } catch (SolverException e) {
                                    logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                } catch (NoConflictException e) {
                                    logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                } catch (InconsistentTheoryException e) {
                                    logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                }

                                Set<AxiomSet<OWLLogicalAxiom>> diagnoses =
                                        Collections.unmodifiableSet(search.getStorage().getDiagnoses());
                                search.clearSearch();
                                AxiomSet<OWLLogicalAxiom> targD = getTargetDiag(diagnoses, es, m);
                                targetDg = new LinkedHashSet<OWLLogicalAxiom>();
                                for (OWLLogicalAxiom axiom : targD)
                                    targetDg.add(axiom);
                            }

                            if (targetSource == TargetSource.FROM_FILE)
                                targetDg = getDiagnosis(targetAxioms, ontology);

                            TableList e = new TableList();
                            out += "," + type + ",";
                            String message = "act " + m + " - " + o + " - " + targetSource + " " + type + " d " + dual;
                            out += simulateBruteForceOnl(search, theory, targetDg, e, type, message);
                        }
                        logger.info(out);
                    }
                }
            }
        }
    }

    @Ignore
    @Test
    public void doAlignmentTest() throws SolverException, InconsistentTheoryException, IOException {
        Properties properties = readProps();
        Map<String, List<String>> mapOntos = readOntologiesFromFile(properties);
        for (String m : mapOntos.keySet()) {
            for (String o : mapOntos.get(m)) {
                String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
                OWLOntology ontology = createOwlOntology(m.trim(), o.trim());
                Set<OWLLogicalAxiom> targetDg = getDiagnosis(targetAxioms, ontology);
                OWLOntology ontology1 = createOwlOntology(o.split("-")[0].trim());
                OWLOntology ontology2 = createOwlOntology(o.split("-")[1].trim());
                OWLTheory theory = createOWLTheory(ontology, false);
                UniformCostSearch<OWLLogicalAxiom> search = createUniformCostSearch(theory, false);
                //ProbabilityTableModel mo = new ProbabilityTableModel();
                HashMap<ManchesterOWLSyntax, Double> map = Utils.getProbabMap();

                String path = ClassLoader.getSystemResource("alignment/evaluation/"
                        + m.trim()
                        + "-incoherent-evaluation/"
                        + o.trim()
                        + ".txt").getPath();

                OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, path);
                //es.updateKeywordProb(map);

                //theory.addBackgroundFormulas(ontology1.getLogicalAxioms());
                //theory.addBackgroundFormulas(ontology2.getLogicalAxioms());
                search.setCostsEstimator(es);

//                try {
//                    search.runPostprocessor();
//                } catch (SolverException e) {
//                    logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                } catch (NoConflictException e) {
//                    logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                } catch (InconsistentTheoryException e) {
//                    logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                }

                Set<AxiomSet<OWLLogicalAxiom>> diagnoses =
                        Collections.unmodifiableSet(search.getStorage().getDiagnoses());
                search.clearSearch();

                TableList e = new TableList();
                logger.info(m + " - " + o);
                simulateBruteForceOnl(search, theory, targetDg, e, QSSType.MINSCORE, "");

//                boolean target = false;
//                for (AxiomSet<OWLLogicalAxiom> d : diagnoses)
//                    if (targetDg.containsAll(d)) target = true;
//                if (!target) logger.info("target notf "+m+o);


            }
        }


    }


}