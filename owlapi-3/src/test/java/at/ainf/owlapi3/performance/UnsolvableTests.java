package at.ainf.owlapi3.performance;

import at.ainf.diagnosis.quickxplain.DirectDiagnosis;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.tree.*;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.model.OWLIncoherencyExtractor;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.costestimation.OWLAxiomCostsEstimator;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.parser.MyOWLRendererParser;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.*;
import at.ainf.owlapi3.utils.creation.*;
import at.ainf.owlapi3.utils.distribution.ExtremeDistribution;
import at.ainf.owlapi3.utils.distribution.ModerateDistribution;
import at.ainf.owlapi3.performance.table.TableList;
import at.ainf.owlapi3.utils.*;
import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.*;
import org.semanticweb.owlapi.model.*;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

import static at.ainf.owlapi3.utils.SimulatedSession.QSSType;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 21.03.12
 * Time: 08:26
 * To change this template use File | Settings | File Templates.
 */
public class UnsolvableTests {

    private static Logger logger = Logger.getLogger(UnsolvableTests.class.getName());

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

    @BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlapi3-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }


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



    public int minCard(Set<AxiomSet<OWLLogicalAxiom>> s) {
        int r = -1;

        try {
            for (AxiomSet<OWLLogicalAxiom> set : s)
                if (r == -1 || set.size() < r)
                    r = set.size();
        } catch (NoSuchElementException e) {

        }

        return r;
    }

    public int maxCard(Set<AxiomSet<OWLLogicalAxiom>> s) {
        int r = -1;

        try {
            for (AxiomSet<OWLLogicalAxiom> set : s)
                if (set.size() > r)
                    r = set.size();
        } catch (NoSuchElementException e) {

        }

        return r;
    }

    public static double meanCard(Set<AxiomSet<OWLLogicalAxiom>> s) {
        double sum = 0;
        int cnt = 0;

        for (AxiomSet<OWLLogicalAxiom> set : s) {
            sum += set.size();
            cnt++;
        }

        if (cnt == 0) return -1;
        return sum / cnt;
    }

    @Test
    public void testDaReadMethods() throws IOException, SolverException, InconsistentTheoryException {
        Properties properties = OAEI08Utils.readProps("alignment.properties");
        Map<String, List<String>> mapOntos = OAEI08Utils.readOntologiesFromFile(properties);

        for (String m : mapOntos.keySet()) {
            for (String o : mapOntos.get(m)) {
                String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
                String[] targetAxioms2 = OAEI08Utils.getDiagnosis(m, o);
                boolean eq = OAEI08Utils.compareDiagnoses(targetAxioms, targetAxioms2);
                if (!eq) {
                    OWLOntology ontology = CreationUtils.createOwlOntology2(m.trim(), o.trim());
                    Set<OWLLogicalAxiom> targetDg = CommonUtils.getDiagnosis(targetAxioms, ontology);
                    System.out.println(targetAxioms.toString());
                    System.out.println(targetAxioms2.toString());
                }
                assertTrue(m + " " + o, eq);
            }
        }
    }


    private class MyFilenameFilter implements FilenameFilter {
        private Set<String> acceptedNames;

        public MyFilenameFilter(File includedNames) {
            acceptedNames = new LinkedHashSet<String>();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(includedNames)));
                String strLine;
                while ((strLine = br.readLine()) != null) {
                    if (!strLine.startsWith("#") || !strLine.endsWith(".rdf"))
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

    private Set<OWLLogicalAxiom> getRandomDiag(File file, String directory) throws SolverException, InconsistentTheoryException {
        String matchingsDir = "oaei11conference/matchings/";
        String mapd = matchingsDir + directory;

        String fileName = file.getName();
        StringTokenizer t = new StringTokenizer(fileName, "-");
        String matcher = t.nextToken();

        String o1 = t.nextToken();
        String o2 = t.nextToken();
        o2 = o2.substring(0, o2.length() - 4);

        String n = file.getName().substring(0, file.getName().length() - 4);
        OWLOntology merged = OAEI11ConferenceUtils.createOntologyWithRdfMappings("oaei11conference/ontology",
                o1, o2, mapd, n + ".rdf");

        OWLOntology ontology = new OWLIncoherencyExtractor(
                new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(merged);
        OWLTheory theory = CreationUtils.createTheoryOAEI(ontology, true, true);
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = CreationUtils.createUniformCostSearch(theory, true);

        LinkedHashSet<OWLLogicalAxiom> bx = new LinkedHashSet<OWLLogicalAxiom>();
        OWLOntology ontology1 = CreationUtils.createOwlOntology("oaei11conference/ontology", o1 + ".owl");
        OWLOntology ontology2 = CreationUtils.createOwlOntology("oaei11conference/ontology", o2 + ".owl");
        bx.addAll(OAEI11ConferenceUtils.getIntersection(ontology.getLogicalAxioms(), ontology1.getLogicalAxioms()));
        bx.addAll(OAEI11ConferenceUtils.getIntersection(ontology.getLogicalAxioms(), ontology2.getLogicalAxioms()));
        theory.addBackgroundFormulas(bx);

        Map<OWLLogicalAxiom, BigDecimal> map1 = OAEI11ConferenceUtils.readRdfMapping(mapd, n + ".rdf");

        OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, map1);


        search.setCostsEstimator(es);

        search.reset();


        Set<OWLLogicalAxiom> targetDg = null;


        OWLTheory th30 = CreationUtils.createTheoryOAEI(ontology, true, true);
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search30 = CreationUtils.createUniformCostSearch(th30, true);
        th30.addBackgroundFormulas(bx);
        OWLAxiomCostsEstimator es30 = new OWLAxiomCostsEstimator(th30, OAEI11ConferenceUtils.readRdfMapping(mapd, n + ".rdf"));
        search30.setCostsEstimator(es30);

        try {
            search30.run(30);
        } catch (NoConflictException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        Set<AxiomSet<OWLLogicalAxiom>> diagnoses = search30.getDiagnoses();
        int rnd = random.nextInt(diagnoses.size());
        randomDiagNr = rnd;
        logger.info(file.getName() + ",diagnosis selected as target," + rnd);
        targetDg = new LinkedHashSet<OWLLogicalAxiom>((AxiomSet<OWLLogicalAxiom>) diagnoses.toArray()[rnd]);
        logger.info(file.getName() + ",target diagnosis axioms," + CommonUtils.renderAxioms(targetDg));

        search30.reset();
        return targetDg;
    }

    int randomDiagNr = -1;

    Random random = new Random(12311);

    @Test
    public void saveOntologiesConference2011()
            throws SolverException, InconsistentTheoryException, IOException, OWLOntologyCreationException {

        String matchingsDir = "oaei11conference/matchings/";
        String directory = "incoherent";
        File[] f = new File(ClassLoader.getSystemResource(matchingsDir + directory)
                .getFile()).listFiles();
        String directory2 = "inconsistent";
        File[] f2 = new File(ClassLoader.getSystemResource(matchingsDir + directory2)
                .getFile()).listFiles();
        Set<File> files = new LinkedHashSet<File>();
        Map<File, String> map = new HashMap<File, String>();
        for (File file : f) {
            files.add(file);
            map.put(file, "incoherent");
        }
        for (File file : f2) {
            files.add(file);
            map.put(file, "inconsistent");
        }

        for (File file : files) {
            String fileName = file.getName();
            StringTokenizer t = new StringTokenizer(fileName, "-");
            t.nextToken();

            String o1 = t.nextToken();
            String o2 = t.nextToken();
            o2 = o2.substring(0, o2.length() - 4);

            String n = file.getName().substring(0, file.getName().length() - 4);
            OWLOntology merged = OAEI11ConferenceUtils.createOntologyWithRdfMappings("oaei11conference/ontology",
                    o1, o2, matchingsDir + map.get(file), n + ".rdf");

            logger.info(merged.getOntologyID().getOntologyIRI().toString());
            File fileSave = new File("C:\\Daten\\Work\\tasks\\Papers\\ISWC12dualRev\\ontologies\\"+n+".owl");
            try {
                ManchesterOWLSyntaxOntologyFormat rdfxmlOntologyFormat = new ManchesterOWLSyntaxOntologyFormat();
                OWLOntologyDocumentTarget documentTarget = new FileDocumentTarget(fileSave);

                OWLManager.createOWLOntologyManager().saveOntology(merged, rdfxmlOntologyFormat, documentTarget);

            } catch (OWLOntologyStorageException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }
    }



    @Test
    public void doTestsOAEIConference()
            throws SolverException, InconsistentTheoryException, IOException, OWLOntologyCreationException {

        SimulatedSession session = new SimulatedSession();

        String matchingsDir = "oaei11conference/matchings/";
        String directory = "incoherent";
        //String mapd = matchingsDir + directory;
        File incl = new File(ClassLoader.getSystemResource(matchingsDir + "includedIncoher.txt").getFile());
        MyFilenameFilter filter = new MyFilenameFilter(incl);
        File[] f = new File(ClassLoader.getSystemResource(matchingsDir + directory)
                .getFile()).listFiles(filter);
        String directory2 = "inconsistent";
        File incl2 = new File(ClassLoader.getSystemResource(matchingsDir + "included.txt").getFile());
        MyFilenameFilter filter2 = new MyFilenameFilter(incl2);
        File[] f2 = new File(ClassLoader.getSystemResource(matchingsDir + directory2)
                .getFile()).listFiles(filter2);
        Set<File> files = new LinkedHashSet<File>();
        Map<File, String> map = new HashMap<File, String>();
        for (File file : f) {
            files.add(file);
            map.put(file, "incoherent");
        }
        for (File file : f2) {
            files.add(file);
            map.put(file, "inconsistent");
        }

        session.setShowElRates(false);

        SimulatedSession.QSSType[] qssTypes = new SimulatedSession.QSSType[]
                {SimulatedSession.QSSType.MINSCORE, SimulatedSession.QSSType.SPLITINHALF};

        for (SimulatedSession.TargetSource targetSource : new SimulatedSession.TargetSource[]{SimulatedSession.TargetSource.FROM_30_DIAGS}) {

            for (File file : files) {
                logger.info("processing " + file.getName());

                String out = "STAT, " + file;


                Set<OWLLogicalAxiom> targetDg = getRandomDiag(file, map.get(file));

                for (QSSType type : qssTypes) {


                    String fileName = file.getName();
                    StringTokenizer t = new StringTokenizer(fileName, "-");
                    String matcher = t.nextToken();

                    String o1 = t.nextToken();
                    String o2 = t.nextToken();
                    o2 = o2.substring(0, o2.length() - 4);

                    String n = file.getName().substring(0, file.getName().length() - 4);
                    OWLOntology merged = OAEI11ConferenceUtils.createOntologyWithRdfMappings("oaei11conference/ontology",
                            o1, o2, matchingsDir + map.get(file), n + ".rdf");

                            long preprocessModulExtract = System.currentTimeMillis();
                            OWLOntology ontology = new OWLIncoherencyExtractor(
                                    new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(merged);
                            preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;
                             OWLTheory theory = CreationUtils.createTheoryOAEI(ontology, true, true);
                    TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = CreationUtils.createUniformCostSearch(theory, true);

                    LinkedHashSet<OWLLogicalAxiom> bx = new LinkedHashSet<OWLLogicalAxiom>();
                    OWLOntology ontology1 = CreationUtils.createOwlOntology("oaei11conference/ontology", o1 + ".owl");
                    OWLOntology ontology2 = CreationUtils.createOwlOntology("oaei11conference/ontology", o2 + ".owl");
                    bx.addAll(OAEI11ConferenceUtils.getIntersection(ontology.getLogicalAxioms(), ontology1.getLogicalAxioms()));
                    bx.addAll(OAEI11ConferenceUtils.getIntersection(ontology.getLogicalAxioms(), ontology2.getLogicalAxioms()));
                    theory.addBackgroundFormulas(bx);

                    Map<OWLLogicalAxiom, BigDecimal> map1 = OAEI11ConferenceUtils.readRdfMapping(matchingsDir + map.get(file), n + ".rdf");

                    OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, map1);


                    search.setCostsEstimator(es);

                    search.reset();


                    TableList e = new TableList();
                    out += "," + type + ",";
                    String message = "act," + file.getName() + "," + map.get(file) + "," + targetSource
                            + "," + type + "," + preprocessModulExtract + "," + randomDiagNr;
                    out += session.simulateQuerySession(search, theory, targetDg, e, type, message, null, null, null);

                }
                logger.info(out);


            }
        }
    }


    @Test
    public void doSimpleQuerySession()
            throws SolverException, InconsistentTheoryException, IOException, OWLOntologyCreationException {

        SimulatedSession session = new SimulatedSession();

        session.setTraceDiagnosesAndQueries(true);
        session.setMinimizeQuery(true);

        session.setNumberOfHittingSets(2);
        QSSType type = QSSType.MINSCORE;
        boolean dual = true;
        String name = "dualISWC2012.owl";
        //String name = "dualpaper.owl";

        OWLOntology ontology = CreationUtils.createOwlOntology("queryontologies", name);

        Set<OWLLogicalAxiom> targetDg = new LinkedHashSet<OWLLogicalAxiom>();
        targetDg.add(new MyOWLRendererParser(ontology).parse("B SubClassOf D and (not (s some C))"));
        targetDg.add(new MyOWLRendererParser(ontology).parse("C SubClassOf not (D or E)"));

        //targetDg.add(new MyOWLRendererParser(ontology).parse("B SubClassOf C"));
        //targetDg.add(new MyOWLRendererParser(ontology).parse("B SubClassOf D"));

        long preprocessModulExtract = System.currentTimeMillis();
        ontology = new OWLIncoherencyExtractor(new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
        preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;

        OWLTheory theory = CreationUtils.createTheoryOAEI(ontology, dual, true);
        theory.addEntailedTest(new MyOWLRendererParser(ontology).parse("w Type B"));
        theory.setIncludeClassAssertionAxioms(true);
        theory.setIncludeTrivialEntailments(false);
        theory.setIncludeSubClassOfAxioms(false);
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = CreationUtils.createUniformCostSearch(theory, dual);
        ((NewQuickXplain<OWLLogicalAxiom>)search.getSearcher()).setAxiomRenderer(new MyOWLRendererParser(null));

        CostsEstimator es = new SimpleCostsEstimator();
        search.setCostsEstimator(es);

        TableList e = new TableList();
        String message = "act," + type + "," + dual + "," + name + "," + preprocessModulExtract;
        session.simulateQuerySession(search, theory, targetDg, e, type, message, null, null, null);

    }


    @Test
    public void doTestAroma()

            throws SolverException, InconsistentTheoryException, IOException, OWLOntologyCreationException {

        SimulatedSession session = new SimulatedSession();

        Properties properties = OAEI08Utils.readProps("alignment.unsolvable.properties");
        Map<String, List<String>> mapOntos = OAEI08Utils.readOntologiesFromFile(properties);
        //boolean background_add = false;
        session.setShowElRates(false);

        String[] files =
                new String[]{"Aroma"};
        //String[] files = new String[]{"Aroma"};

        QSSType[] qssTypes = new QSSType[]
                {QSSType.MINSCORE, QSSType.SPLITINHALF,
                        QSSType.DYNAMICRISK};
        for (boolean dual : new boolean[]{true}) {
            for (boolean background : new boolean[]{true}) {
                for (SimulatedSession.TargetSource targetSource : new SimulatedSession.TargetSource[]{SimulatedSession.TargetSource.FROM_FILE}) {
                    for (String file : files) {

                        String out = "STAT, " + file;
                        for (QSSType type : qssTypes) {

                            //String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");

                            //String[] targetAxioms = AlignmentUtils.getDiagnosis2(m,o);
                            //OWLOntology ontology = createOwlOntology2(m.trim(), o.trim());

                            OWLOntology ontology = OAEI11AnatomyUtils.createOntologyFromTxtOAEI(file);

                            Set<OWLLogicalAxiom> targetDg;
                            long preprocessModulExtract = System.currentTimeMillis();
                            ontology = new OWLIncoherencyExtractor(
                                    new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
                            preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;
                            OWLTheory theory = CreationUtils.createTheoryOAEI(ontology, dual, true);
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = CreationUtils.createUniformCostSearch(theory, dual);

                            LinkedHashSet<OWLLogicalAxiom> bx = new LinkedHashSet<OWLLogicalAxiom>();
                            bx.addAll(OAEI11AnatomyUtils.getLogicalAxiomsOfOntologiesOAEI());
                            bx.retainAll(theory.getOriginalOntology().getLogicalAxioms());
                            theory.addBackgroundFormulas(bx);

                            //ProbabilityTableModel mo = new ProbabilityTableModel();
                            HashMap<ManchesterOWLSyntax, BigDecimal> map = CommonUtils.getProbabMap();

                            String path = ClassLoader.getSystemResource("oaei11/" + file + ".txt").getPath();

                            OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, path);


                            targetDg = null;

                            search.setCostsEstimator(es);

                            Set<AxiomSet<OWLLogicalAxiom>> allD = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>(search.getDiagnoses());
                            search.reset();


                            if (targetSource == SimulatedSession.TargetSource.FROM_FILE)
                                targetDg = OAEI11AnatomyUtils.getTargetDOAEI(ClassLoader.getSystemResource("oaei11").getPath() + "/",
                                        file);

                            TableList e = new TableList();
                            out += "," + type + ",";
                            String message = "act," + file + "," + targetSource + "," + type + "," + dual + "," + background + "," + preprocessModulExtract;
                            //out += simulateQuerySession(search, theory, diags, e, type, message, allD, search2, t3);

                            out += session.simulateQuerySession(search, theory, targetDg, e, type, message, null, null, null);

                        }
                        logger.info(out);

                    }
                }
            }
        }
    }

    @Test
    public void doTestsOAEIAnatomyTrack()

            throws SolverException, InconsistentTheoryException, IOException, OWLOntologyCreationException {

        SimulatedSession session = new SimulatedSession();

        Properties properties = OAEI08Utils.readProps("alignment.unsolvable.properties");
        Map<String, List<String>> mapOntos = OAEI08Utils.readOntologiesFromFile(properties);
        //boolean background_add = false;
        session.setShowElRates(false);

        //String[] files =
                  //new String[]{"AgrMaker", "GOMMA-bk", "GOMMA-nobk", "Lily", "LogMap", "LogMapLt", "MapSSS"};
        String[] files = new String[]{"AgrMaker"};

        //QSSType[] qssTypes = new QSSType[]{DYNAMICRISK};
        QSSType[] qssTypes = new QSSType[]
                { QSSType.MINSCORE, QSSType.SPLITINHALF, QSSType.DYNAMICRISK };
        for (boolean dual : new boolean[] {false}) {
            for (boolean background : new boolean[]{false}) {
                for (SimulatedSession.TargetSource targetSource : new SimulatedSession.TargetSource[]{SimulatedSession.TargetSource.FROM_FILE}) {
                    for (String file : files) {

                        String out = "STAT, " + file;
                        for (QSSType type : qssTypes) {

                            //String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");

                            //String[] targetAxioms = AlignmentUtils.getDiagnosis2(m,o);
                            //OWLOntology ontology = createOwlOntology2(m.trim(), o.trim());

                            OWLOntology ontology = OAEI11AnatomyUtils.createOntologyFromTxtOAEI(file);

                                Set<OWLLogicalAxiom> targetDg;
                                long preprocessModulExtract = System.currentTimeMillis();
                                ontology = new OWLIncoherencyExtractor(
                                        new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
                                preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;
                                OWLTheory theory = CreationUtils.createTheoryOAEI(ontology, dual, true);
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = CreationUtils.createUniformCostSearch(theory, dual);

                            LinkedHashSet<OWLLogicalAxiom> bx = new LinkedHashSet<OWLLogicalAxiom>();
                            bx.addAll(OAEI11AnatomyUtils.getLogicalAxiomsOfOntologiesOAEI());
                            bx.retainAll(theory.getOriginalOntology().getLogicalAxioms());
                            if (background) theory.addBackgroundFormulas(bx);

                            //ProbabilityTableModel mo = new ProbabilityTableModel();
                            HashMap<ManchesterOWLSyntax, BigDecimal> map = CommonUtils.getProbabMap();

                            String path = ClassLoader.getSystemResource("oaei11/" + file + ".txt").getPath();

                            OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, path);


                            targetDg = null;

                            search.setCostsEstimator(es);

                            Set<AxiomSet<OWLLogicalAxiom>> allD = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>(search.getDiagnoses());
                            search.reset();


                            if (targetSource == SimulatedSession.TargetSource.FROM_FILE)
                                targetDg = OAEI11AnatomyUtils.getTargetDOAEI(ClassLoader.getSystemResource("oaei11").getPath() + "/",
                                        file);

                            TableList e = new TableList();
                            out += "," + type + ",";
                            String message = "act," + file + "," + targetSource + "," + type + "," + dual + "," + background + "," + preprocessModulExtract;
                            //out += simulateQuerySession(search, theory, diags, e, type, message, allD, search2, t3);

                            out += session.simulateQuerySession(search, theory, targetDg, e, type, message, null, null, null);

                        }
                        logger.info(out);

                    }
                }
            }
        }
    }

    @Test
    protected void doOverallTreeTestEconomy(boolean dual) throws IOException, SolverException, InconsistentTheoryException, NoConflictException {

        SimulatedSession session = new SimulatedSession();

        session.setShowElRates(false);
        QSSType[] qssTypes = new QSSType[]{QSSType.MINSCORE, QSSType.SPLITINHALF, QSSType.DYNAMICRISK};
        //String[] norm = new String[]{"Transportation-SDA"};
        String[] norm = new String[]{"Transportation-SDA", "Economy-SDA"};


        for (SimulatedSession.TargetSource targetSource : new SimulatedSession.TargetSource[]{SimulatedSession.TargetSource.FROM_30_DIAGS}) {
            for (String o : norm) {
                String out = "STAT, " + o;
                TreeSet<AxiomSet<OWLLogicalAxiom>> diagnoses = OntologyUtils.getAllD(o);
                for (QSSType type : qssTypes) {
                    for (DiagProbab diagProbab : DiagProbab.values()) {
                        for (int i = 0; i < 20; i++) {


                        OWLOntology ontology = CreationUtils.createOwlOntology("queryontologies", o + ".owl");
                        //OWLOntology ontology = createOwlOntology2(m.trim(), o.trim());
                        long preprocessModulExtract = System.currentTimeMillis();
                        ontology = new OWLIncoherencyExtractor(
                                new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
                        preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;
                        Set<OWLLogicalAxiom> targetDg;
                        OWLTheory theory = CreationUtils.createOWLTheory(ontology, dual);
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = CreationUtils.createUniformCostSearch(theory, dual);

                            HashMap<ManchesterOWLSyntax, BigDecimal> map = CommonUtils.getProbabMap();
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


    @Test
    public void hookTest() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("User aborted shutdown hook");
            }
        });

        new Thread() {
            public void run() {
                while (true) {
                    try {
                        while (System.in.available() > 0) {
                            char pressedKey = (char) System.in.read();
                            switch (pressedKey) {
                                case 'c':
                                    System.out.println("statistics");
                                    System.exit(0);
                                case 'i':
                                    System.out.println("statistics");
                                default:
                                    System.out.println("press <i> for statistics or <c> to cancel");
                            }

                        }
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        }.start();

        while (true) ;
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

                TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = CreationUtils.getSearch(CreationUtils.createOwlOntology("queryontologies", name), false);



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

                            TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> search2 = CreationUtils.getSearch(CreationUtils.createOwlOntology("queryontologies", name), dual);
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
        ArrayList<ManchesterOWLSyntax> keywordList = new ArrayList<ManchesterOWLSyntax>(EnumSet.copyOf(CommonUtils.getProbabMap().keySet()));
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

    @Test
    public void doShowMappingAxiomsSizes() throws SolverException, InconsistentTheoryException, IOException {
        Properties properties = OAEI08Utils.readProps("alignment.allFiles.properties");
        Map<String, List<String>> mapOntos = OAEI08Utils.readOntologiesFromFile(properties);

        for (String m : mapOntos.keySet()) {
            for (String o : mapOntos.get(m)) {
                OAEI08Utils.getDiagnosis(m, o);
            }
        }
    }


    @Test
    public void readTest() throws IOException {
        String filename = ClassLoader.getSystemResource("alignment/evaluation/owlctxmatch-incoherent-evaluation/CMT-CONFTOOL.txt").getFile();
        Map<String, Double> axioms = new LinkedHashMap<String, Double>();
        Set<String> targetDiag = new LinkedHashSet<String>();
        OAEI08Utils.readData2(filename, axioms, targetDiag);
        logger.info("Read " + axioms.size() + " " + targetDiag.size());
        assertEquals(axioms.size(), 36);
        assertEquals(targetDiag.size(), 6);

        filename = ClassLoader.getSystemResource("alignment/evaluation/hmatch-incoherent-evaluation/CMT-CRS.txt").getFile();
        axioms.clear();
        targetDiag.clear();
        OAEI08Utils.readData2(filename, axioms, targetDiag);
        logger.info("Read " + axioms.size() + " " + targetDiag.size());
        assertEquals(axioms.size(), 2 * (17 - 5));
        assertEquals(targetDiag.size(), 4);
    }

    @Test
    public void readTest2() throws IOException {
        String filename = ClassLoader.getSystemResource("alignment/evaluation/coma-evaluation/CMT-CONFTOOL.txt").getFile();
        Map<String, Double> axioms = new LinkedHashMap<String, Double>();
        Set<String> targetDiag = new LinkedHashSet<String>();
        OAEI08Utils.readData2(filename, axioms, targetDiag);
        logger.info("Read " + axioms.size() + " " + targetDiag.size());
    }

    @Test
    public void readTest1() throws IOException {
        String filename = ClassLoader.getSystemResource("alignment/evaluation/coma-evaluation/CMT-CONFTOOL.txt").getFile();
        Map<String, Double> axioms = new LinkedHashMap<String, Double>();
        Set<String> targetDiag = new LinkedHashSet<String>();
        OAEI08Utils.readData2(filename, axioms, targetDiag);
        logger.info("Read " + axioms.size() + " " + targetDiag.size());
    }


    @Ignore
    @Test
    public void search() throws SolverException, InconsistentTheoryException {
        Properties properties = OAEI08Utils.readProps2("alignment/alignment.properties");
        Map<String, List<String>> mapOntos = OAEI08Utils.readOntologiesFromFile2(properties);
        for (String m : mapOntos.keySet()) {
            for (String o : mapOntos.get(m)) {
                CommonUtils.BackgroundO[] backgrounds = new CommonUtils.BackgroundO[]{CommonUtils.BackgroundO.O1_O2};
                for (CommonUtils.BackgroundO background : backgrounds) {
                    String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
                    OWLOntology ontology = CreationUtils.createOwlOntology2(m.trim(), o.trim());
                    Set<OWLLogicalAxiom> targetDg = OAEI08Utils.getDiagnosis2(targetAxioms, ontology);
                    OWLOntology ontology1 = CreationUtils.createOwlOntology(o.split("-")[0].trim());
                    OWLOntology ontology2 = CreationUtils.createOwlOntology(o.split("-")[1].trim());
                    OWLTheory theory = CreationUtils.createOWLTheory2(ontology, false);
                    TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = CreationUtils.createUniformCostSearch2(theory, false);
                    //ProbabilityTableModel mo = new ProbabilityTableModel();
                    HashMap<ManchesterOWLSyntax, BigDecimal> map = CommonUtils.getProbabMap();
                    OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theory);
                    es.updateKeywordProb(map);
                    if (background == CommonUtils.BackgroundO.O1 || background == CommonUtils.BackgroundO.O1_O2)
                        theory.addBackgroundFormulas(ontology1.getLogicalAxioms());
                    if (background == CommonUtils.BackgroundO.O2 || background == CommonUtils.BackgroundO.O1_O2)
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
                    String t = CommonUtils.getStringTime(time / 1000000);

                    Set<AxiomSet<OWLLogicalAxiom>> diagnoses =
                            Collections.unmodifiableSet(search.getDiagnoses());
                    //logger.info(m + " " + o + " background: " + background + " diagnoses: " + diagnoses.size());

                    int n = 0;
                    Set<AxiomSet<OWLLogicalAxiom>> set = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>();
                    for (AxiomSet<OWLLogicalAxiom> d : diagnoses)
                        if (targetDg.containsAll(d)) set.add(d);
                    n = set.size();
                    int cs = search.getConflicts().size();
                    search.reset();
                    logger.info(m + " " + o + " background: " + background + " diagnoses: " + diagnoses.size()
                            + " conflicts: " + cs + " time " + t + " target " + n);

                }
            }
        }
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
                " time " + CommonUtils.getStringTime(timeDual) + " found correct diag " + foundCorrectD2 +
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
                CommonUtils.getStringTime(timeNormal) + " found correct diag " + foundCorrectD +
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
        OWLTheory theoryNormal = CreationUtils.createTheory(CreationUtils.createOwlOntology("ontologies", ontology), false);
        searchNormal.setTheory(theoryNormal);
        theoryNormal.useCache(false, 0);
        HashMap<ManchesterOWLSyntax, BigDecimal> map = CommonUtils.getProbabMap();
        OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theoryNormal);
        es.updateKeywordProb(map);
        searchNormal.setCostsEstimator(es);
        searchNormal.run();
        Set<? extends AxiomSet<OWLLogicalAxiom>> resultNormal = searchNormal.getDiagnoses();

        manager = OWLManager.createOWLOntologyManager();
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> searchDual = new InvHsTreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        searchDual.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        searchDual.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
        OWLTheory theoryDual = CreationUtils.createTheory(CreationUtils.createOwlOntology("ontologies", ontology), true);
        theoryDual.useCache(false, 0);
        searchDual.setTheory(theoryDual);
        map = CommonUtils.getProbabMap();
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
        logger.info("needed overall " + CommonUtils.getStringTime(needed));
        for (QSSType type : QSSType.values()) {
            logger.info("needed normal " + type + " " + CommonUtils.getStringTime(ntimes.get(type).getOverall()) +
                    " max " + CommonUtils.getStringTime(ntimes.get(type).getMax()) +
                    " min " + CommonUtils.getStringTime(ntimes.get(type).getMin()) +
                    " avg2 " + CommonUtils.getStringTime(ntimes.get(type).getMean()) +
                    " Queries max " + Collections.max(nqueries1.get(type)) +
                    " min " + Collections.min(nqueries1.get(type)) +
                    " avg2 " + OntologyUtils.avg2(nqueries1.get(type))
            );
            logger.info("needed dual " + type + " " + CommonUtils.getStringTime(dtimes.get(type).getOverall()) +
                    " max " + CommonUtils.getStringTime(dtimes.get(type).getMax()) +
                    " min " + CommonUtils.getStringTime(dtimes.get(type).getMin()) +
                    " avg2 " + CommonUtils.getStringTime(dtimes.get(type).getMean()) +
                    " Queries max " + Collections.max(dqueries1.get(type)) +
                    " min " + Collections.min(dqueries1.get(type)) +
                    " avg2 " + OntologyUtils.avg2(dqueries1.get(type)));
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
        OWLTheory theoryNormal = CreationUtils.createTheory(CreationUtils.createOwlOntology(path, ont), false);
        searchNormal.setTheory(theoryNormal);
        HashMap<ManchesterOWLSyntax, BigDecimal> map = CommonUtils.getProbabMap();
        OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theoryNormal);
        es.updateKeywordProb(map);
        searchNormal.setCostsEstimator(es);
        searchNormal.run();
        Set<? extends AxiomSet<OWLLogicalAxiom>> resultNormal = searchNormal.getDiagnoses();

        manager = OWLManager.createOWLOntologyManager();
        InvHsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> searchDual = new InvHsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        searchNormal.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        searchDual.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
        OWLTheory theoryDual = CreationUtils.createTheory(CreationUtils.createOwlOntology(path, ont), true);
        searchDual.setTheory(theoryDual);
        map = CommonUtils.getProbabMap();
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
    public void calcOneDiagAndMore() throws SolverException, InconsistentTheoryException, IOException {
        Properties properties = OAEI08Utils.readProps("alignment.unsolvable.properties");
        Map<String, List<String>> mapOntos = OAEI08Utils.readOntologiesFromFile(properties);

        for (boolean dual : new boolean[]{true, false}) {

            for (String m : mapOntos.keySet()) {
                for (String o : mapOntos.get(m)) {
                    for (int nd : new int[]{1, 5, 9}) {
                        String out = "STAT, " + m + ", " + o;

                        String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
                        OWLOntology ontology = CreationUtils.createOwlOntology2(m.trim(), o.trim());
                        Set<OWLLogicalAxiom> targetDg;
                        OWLTheory theory = CreationUtils.createOWLTheory(ontology, dual);
                        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = CreationUtils.createUniformCostSearch(theory, dual);
                        //ProbabilityTableModel mo = new ProbabilityTableModel();
                        HashMap<ManchesterOWLSyntax, BigDecimal> map = CommonUtils.getProbabMap();

                        OWLOntology ontology1 = CreationUtils.createOwlOntology(o.split("-")[0].trim());
                        OWLOntology ontology2 = CreationUtils.createOwlOntology(o.split("-")[1].trim());

                        String path = ClassLoader.getSystemResource("alignment/evaluation/"
                                + m.trim()
                                + "-incoherent-evaluation/"
                                + o.trim()
                                + ".txt").getPath();

                        OWLTheory theory2 = CreationUtils.createOWLTheory(ontology, dual);
                        OWLTheory t3 = CreationUtils.createOWLTheory(ontology, dual);
                        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search2 = CreationUtils.createUniformCostSearch(theory2, dual);
                        search2.setCostsEstimator(new OWLAxiomCostsEstimator(theory2, path));

                        //theory.addBackgroundFormulas(ontology1.getLogicalAxioms());
                        //theory.addBackgroundFormulas(ontology2.getLogicalAxioms());

                        OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, path);

                        //es.updateKeywordProb(map);
                        targetDg = null;

                        search.setCostsEstimator(es);
                        //
                        long time = System.nanoTime();
                        try {
                            search.run(nd);
                        } catch (SolverException e) {
                            logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        } catch (NoConflictException e) {
                            logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        } catch (InconsistentTheoryException e) {
                            logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                        time = System.nanoTime() - time;

                        int minDiagnosisC = minCard(search.getDiagnoses());
                        double meanDiagnosisC = meanCard(search.getDiagnoses());
                        int maxDiagnosisC = maxCard(search.getDiagnoses());
                        int minConfC = minCard(search.getConflicts());
                        double meanConfC = meanCard(search.getConflicts());
                        int maxConfC = maxCard(search.getConflicts());

                        int c = search.getConflicts().size();
                        String s = nd + ", " + minDiagnosisC + ", " + meanDiagnosisC + ", " + maxDiagnosisC + ", " +
                                c + ", " + minConfC + ", " + meanConfC + ", " + maxConfC;

                        logger.info("Stat, " + m.trim() + ", " + o.trim() + ", "
                                + s + ", "
                                + theory.getConsistencyCount() + ", " + dual + ", " + CommonUtils.getStringTime(time / 1000000));
                    }
                }
            }

        }
    }

    @Test
    public void calcOnlyDiagnoses() throws IOException, SolverException, InconsistentTheoryException, NoConflictException {

        String m = "coma";
        String o = "CRS-EKAW";
        QSSType type = QSSType.SPLITINHALF;
        Properties properties = OAEI08Utils.readProps2("alignment/alignment.full.properties");
        String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
        OWLOntology ontology = CreationUtils.createOwlOntology2(m.trim(), o.trim());
        Set<OWLLogicalAxiom> targetDg;
        OWLTheory theory = CreationUtils.createOWLTheory2(ontology, false);
        Set<AxiomSet<OWLLogicalAxiom>> allD = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>();
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = CreationUtils.createUniformCostSearch2(theory, false);
        String path = ClassLoader.getSystemResource("alignment/evaluation/"
                + m.trim()
                + "-incoherent-evaluation/"
                + o.trim()
                + ".txt").getPath();
        OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, path);

        targetDg = null;

        search.setCostsEstimator(es);

        search.run();

        allD = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>(search.getDiagnoses());
        search.reset();

        search.run(9);
        TreeSet<OWLLogicalAxiom> testcase = new TreeSet<OWLLogicalAxiom>();
        MyOWLRendererParser parser = new MyOWLRendererParser(ontology);
        testcase.add(parser.parse("conference DisjointWith session"));
        testcase.add(parser.parse("Conference_Session SubClassOf conference"));
        testcase.add(parser.parse("conference SubClassOf Conference_Session"));

        theory.addNonEntailedTest(testcase);
        Set<AxiomSet<OWLLogicalAxiom>> toRemove = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>();
        for (AxiomSet<OWLLogicalAxiom> axiomSet : allD)
            if (!theory.testDiagnosis(axiomSet))
                toRemove.add(axiomSet);
        allD.removeAll(toRemove);
        //deleteDiag(theory,allDiags,false,testcase);

        search.run(9);
        Set<AxiomSet<OWLLogicalAxiom>> diagnoses = search.getDiagnoses();
        /*for (AxiomSet<OWLLogicalAxiom> diagnosis : diagnoses) {
            if(!theory.testDiagnosis(diagnosis))
                logger.info("prob");
        }*/

        for (AxiomSet<OWLLogicalAxiom> diagnosis : diagnoses) {
            Assert.assertTrue(allD.contains(diagnosis));
        }

    }

    @Test
    public void docomparehsdual() throws SolverException, InconsistentTheoryException, IOException {
        SimulatedSession session = new SimulatedSession();

        Properties properties = OAEI08Utils.readProps("alignment.allFiles.properties");
        Map<String, List<String>> mapOntos = OAEI08Utils.readOntologiesFromFile(properties);
        //boolean background_add = false;

        session.setShowElRates(false);

        QSSType[] qssTypes = new QSSType[]{QSSType.MINSCORE, QSSType.SPLITINHALF, QSSType.DYNAMICRISK};
        for (boolean dual : new boolean[]{false}) {
            for (boolean background : new boolean[]{true, false}) {
                for (SimulatedSession.TargetSource targetSource : new SimulatedSession.TargetSource[]{SimulatedSession.TargetSource.FROM_FILE, SimulatedSession.TargetSource.FROM_30_DIAGS}) {
                    for (String m : mapOntos.keySet()) {
                        for (String o : mapOntos.get(m)) {
                            String out = "STAT, " + m + ", " + o;
                            for (QSSType type : qssTypes) {

                                //String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");

                                String[] targetAxioms = OAEI08Utils.getDiagnosis(m, o);
                                OWLOntology ontology = CreationUtils.createOwlOntology2(m.trim(), o.trim());
                                ontology = new OWLIncoherencyExtractor(
                                        new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
                                Set<OWLLogicalAxiom> targetDg;
                                OWLTheory theory = CreationUtils.createOWLTheory(ontology, dual);
                                TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = CreationUtils.createUniformCostSearch(theory, dual);
                                if (background) {
                                    OWLOntology ontology1 = CreationUtils.createOwlOntology(o.split("-")[0].trim());
                                    OWLOntology ontology2 = CreationUtils.createOwlOntology(o.split("-")[1].trim());
                                    theory.addBackgroundFormulas(ontology1.getLogicalAxioms());
                                    theory.addBackgroundFormulas(ontology2.getLogicalAxioms());
                                }
                                //ProbabilityTableModel mo = new ProbabilityTableModel();
                                HashMap<ManchesterOWLSyntax, BigDecimal> map = CommonUtils.getProbabMap();

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
                                //                            try {
                                //                                search.run();
                                //                            } catch (SolverException e) {
                                //                                logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                //                            } catch (NoConflictException e) {
                                //                                logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                //                            } catch (InconsistentTheoryException e) {
                                //                                logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                //                            }

                                Set<AxiomSet<OWLLogicalAxiom>> allD = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>(search.getDiagnoses());
                                search.reset();

                                if (targetSource == SimulatedSession.TargetSource.FROM_30_DIAGS) {
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
                                            Collections.unmodifiableSet(search.getDiagnoses());
                                    search.reset();
                                    AxiomSet<OWLLogicalAxiom> targD = OAEI08Utils.getTargetDiag(diagnoses, es, m);
                                    targetDg = new LinkedHashSet<OWLLogicalAxiom>();
                                    for (OWLLogicalAxiom axiom : targD)
                                        targetDg.add(axiom);
                                }

                                if (targetSource == SimulatedSession.TargetSource.FROM_FILE)
                                    targetDg = CommonUtils.getDiagnosis(targetAxioms, ontology);

                                TableList e = new TableList();
                                out += "," + type + ",";
                                String message = "act," + m.trim() + "," + o.trim() + "," + targetSource + "," + type + "," + dual + "," + background;
                                //out += simulateQuerySession(search, theory, diags, e, type, message, allD, search2, t3);

                                out += session.simulateQuerySession(search, theory, targetDg, e, type, message, null, null, null);

                            }
                            logger.info(out);
                        }
                    }
                }
            }
        }
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
                TreeSet<AxiomSet<OWLLogicalAxiom>> diagnoses = OntologyUtils.getAllD(o);
                for (QSSType type : qssTypes) {
                    for (DiagProbab diagProbab : new DiagProbab[]{DiagProbab.GOOD}) {
                        for (int i = 0; i < 1500; i++) {


                            OWLOntology ontology = CreationUtils.createOwlOntology("queryontologies", o + ".owl");
                            //OWLOntology ontology = createOwlOntology2(m.trim(), o.trim());
                            long preprocessModulExtract = System.currentTimeMillis();
                            ontology = new OWLIncoherencyExtractor(
                                    new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
                            preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;
                            Set<OWLLogicalAxiom> targetDg;
                            OWLTheory theory = CreationUtils.createOWLTheory(ontology, true);
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = CreationUtils.createUniformCostSearch(theory, true);

                            HashMap<ManchesterOWLSyntax, BigDecimal> map = CommonUtils.getProbabMap();
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
                            logger.info("target diagnosis:" + targetDg.size() + " " + CommonUtils.renderAxioms(targetDg));
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
