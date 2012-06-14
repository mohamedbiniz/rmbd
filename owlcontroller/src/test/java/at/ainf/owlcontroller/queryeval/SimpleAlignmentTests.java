package at.ainf.owlcontroller.queryeval;

import at.ainf.diagnosis.partitioning.BruteForce;
import at.ainf.diagnosis.partitioning.QueryMinimizer;
import at.ainf.diagnosis.partitioning.scoring.QSS;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlcontroller.costestimation.OWLAxiomCostsEstimator;
import at.ainf.owlcontroller.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlcontroller.Utils;
import at.ainf.owlcontroller.parser.MyOWLRendererParser;
import at.ainf.owlcontroller.queryeval.result.TableList;
import at.ainf.owlcontroller.queryeval.result.Time;
import at.ainf.theory.model.ITheory;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.Partition;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static junit.framework.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 27.03.12
 * Time: 11:56
 * To change this template use File | Settings | File Templates.
 */
public class SimpleAlignmentTests extends BaseAlignmentTests {

    private static Logger logger = Logger.getLogger(SimpleAlignmentTests.class.getName());

    enum BackgroundO {EMPTY, O1, O2, O1_O2}

    @BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlcontroller-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
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


    @Test
    public void doOnlyOneQuerySession() throws SolverException, InconsistentTheoryException, IOException {
        Properties properties = readProps("alignment/alignment.properties");
        //Map<String, List<String>> mapOntos = readOntologiesFromFile(properties);

        QSSType[] qssTypes = new QSSType[]{QSSType.MINSCORE, QSSType.SPLITINHALF, QSSType.DYNAMICRISK};
        String m = "owlctxmatch";
        String o = "SIGKDD-EKAW";
        TargetSource targetSource = TargetSource.FROM_FILE;
        QSSType type = QSSType.SPLITINHALF;
        String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
        OWLOntology ontology = createOwlOntology(m.trim(), o.trim());
        Set<OWLLogicalAxiom> targetDg;
        OWLTheory theory = createOWLTheory(ontology, false);
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = createUniformCostSearch(theory, false);
        OWLOntology ontology1 = createOwlOntology(o.split("-")[0].trim());
        OWLOntology ontology2 = createOwlOntology(o.split("-")[1].trim());
        //theory.addBackgroundFormulas(ontology1.getLogicalAxioms());
        //theory.addBackgroundFormulas(ontology2.getLogicalAxioms());
        //ProbabilityTableModel mo = new ProbabilityTableModel();
        HashMap<ManchesterOWLSyntax, BigDecimal> map = Utils.getProbabMap();

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
            Set<AxiomSet<OWLLogicalAxiom>> diagnoses = run(search, 30);
            search.reset();
            AxiomSet<OWLLogicalAxiom> targD = getTargetDiag(diagnoses, es, m);
            targetDg = new LinkedHashSet<OWLLogicalAxiom>();
            for (OWLLogicalAxiom axiom : targD)
                targetDg.add(axiom);
        }

        if (targetSource == TargetSource.FROM_FILE) {
            targetDg = getDiagnosis(targetAxioms, ontology);
            Set<AxiomSet<OWLLogicalAxiom>> diagnoses = run(search, -1);
            assertTrue(diagnoses.contains(targetDg));
            search.reset();
        }

        TableList e = new TableList();
        String message = "act " + m + " - " + o + " - " + targetSource + " " + type;
        simulateBruteForceOnl(search, theory, targetDg, e, type, message, null, null, null);

    }

    private Set<AxiomSet<OWLLogicalAxiom>> run(TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search, int diags) {
        try {
            search.run(diags);
        } catch (SolverException e) {
            logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (NoConflictException e) {
            logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InconsistentTheoryException e) {
            logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        
        return Collections.unmodifiableSet(search.getDiagnoses());
    }

    @Ignore
    @Test
    public void doHardTwoTests() throws SolverException, InconsistentTheoryException, IOException {
        Properties properties = readProps("alignment/alignment.properties");
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
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = createUniformCostSearch(theory, false);
                            //ProbabilityTableModel mo = new ProbabilityTableModel();
                            HashMap<ManchesterOWLSyntax, BigDecimal> map = Utils.getProbabMap();

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
                                run(search, 30);

                                Set<AxiomSet<OWLLogicalAxiom>> diagnoses =
                                        Collections.unmodifiableSet(search.getDiagnoses());
                                search.reset();
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
                            simulateBruteForceOnl(search, theory, targetDg, e, type, message, null, null, null);
                        }
                    }
                }
            }
        }
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
        Map<OWLLogicalAxiom, BigDecimal> probMap = new LinkedHashMap<OWLLogicalAxiom, BigDecimal>();

        for (OWLLogicalAxiom axiom : ontology.getLogicalAxioms())
            ontology.getOWLOntologyManager().removeAxiom(ontology, axiom);


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

        //Set<OWLLogicalAxiom> diags = getDiagnosis(targetAxioms, ontology);
        //OWLOntology ontology1 = createOwlOntology(o.split("-")[0].trim());
        //OWLOntology ontology2 = createOwlOntology(o.split("-")[1].trim());
        OWLTheory theory = createOWLTheory(ontology, false);
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = createUniformCostSearch(theory, false);
        //ProbabilityTableModel mo = new ProbabilityTableModel();
        HashMap<ManchesterOWLSyntax, BigDecimal> map = Utils.getProbabMap();

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
        for (OWLLogicalAxiom axiom : ontology.getLogicalAxioms())
            if (MyOWLRendererParser.render(axiom).equals("A SubClassOf B"))
                targetDg.add(axiom);


        //es.updateKeywordProb(map);

        //theory.addBackgroundFormulas(ontology1.getLogicalAxioms());
        //theory.addBackgroundFormulas(ontology2.getLogicalAxioms());
        search.setCostsEstimator(es);

        // brute force statt ckk

//                try {
//                    run.run();
//                } catch (SolverException e) {
//                    logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                } catch (NoConflictException e) {
//                    logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                } catch (InconsistentTheoryException e) {
//                    logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                }

        Set<AxiomSet<OWLLogicalAxiom>> diagnoses =
                Collections.unmodifiableSet(search.getDiagnoses());
        search.reset();

        TableList e = new TableList();
        //logger.info(m + " - " + o);
        simulateBruteForcePaperTest(search, theory, targetDg, e, QSSType.MINSCORE, "");

//                boolean target = false;
//                for (AxiomSet<OWLLogicalAxiom> d : diagnoses)
//                    if (diags.containsAll(d)) target = true;
//                if (!target) logger.info("target notf "+m+o);


        //  }
        //}


    }


    private void addAxiomPaperTest(OWLOntology ontology, String axiom, Double probab, Map<OWLLogicalAxiom, BigDecimal> m) {
        MyOWLRendererParser parser = new MyOWLRendererParser(ontology);
        OWLLogicalAxiom ax1 = parser.parse(axiom);
        ontology.getOWLOntologyManager().addAxiom(ontology, ax1);
        m.put(ax1, BigDecimal.valueOf(probab));

    }


    @Ignore
    @Test
    public void doPaperTest() throws SolverException, InconsistentTheoryException, IOException, OWLOntologyCreationException {

        QSSType[] types = new QSSType[]{QSSType.MINSCORE, QSSType.SPLITINHALF, QSSType.DYNAMICRISK};
        for (QSSType type : types) {
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
            Map<OWLLogicalAxiom, BigDecimal> probMap = new LinkedHashMap<OWLLogicalAxiom, BigDecimal>();

            for (OWLLogicalAxiom axiom : ontology.getLogicalAxioms()) {
                if (new ManchesterOWLSyntaxOWLObjectRendererImpl().render(axiom).equals("PHD_2 SubClassOf PHD_1")) {
                    probMap.put(axiom, new BigDecimal("1").subtract(new BigDecimal("0.1")));
                } else if (new ManchesterOWLSyntaxOWLObjectRendererImpl().render(axiom).equals("DeptMember_1 SubClassOf DeptMember_2")) {
                    probMap.put(axiom, new BigDecimal("1").subtract(new BigDecimal("0.45")));
                } else {
                    probMap.put(axiom, new BigDecimal("1").subtract(new BigDecimal("0.001")));
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


            //Set<OWLLogicalAxiom> diags = getDiagnosis(targetAxioms, ontology);
            //OWLOntology ontology1 = createOwlOntology(o.split("-")[0].trim());
            //OWLOntology ontology2 = createOwlOntology(o.split("-")[1].trim());
            OWLTheory theory = createOWLTheory(ontology, false);
            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = createUniformCostSearch(theory, false);
            //ProbabilityTableModel mo = new ProbabilityTableModel();
            HashMap<ManchesterOWLSyntax, BigDecimal> map = Utils.getProbabMap();

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
            for (OWLLogicalAxiom axiom : ontology.getLogicalAxioms())
                if (MyOWLRendererParser.render(axiom).equals("Researcher_1 SubClassOf DeptMember_1"))
                    targetDg.add(axiom);


            //es.updateKeywordProb(map);

            //theory.addBackgroundFormulas(ontology1.getLogicalAxioms());
            //theory.addBackgroundFormulas(ontology2.getLogicalAxioms());
            search.setCostsEstimator(es);

            // brute force statt ckk

            //                try {
            //                    run.run();
            //                } catch (SolverException e) {
            //                    logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            //                } catch (NoConflictException e) {
            //                    logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            //                } catch (InconsistentTheoryException e) {
            //                    logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            //                }

            Set<AxiomSet<OWLLogicalAxiom>> diagnoses =
                    Collections.unmodifiableSet(search.getDiagnoses());
            search.reset();

            TableList e = new TableList();
            //logger.info(m + " - " + o);
            simulateBruteForcePaperTest(search, theory, targetDg, e, type, "");

            //                boolean target = false;
            //                for (AxiomSet<OWLLogicalAxiom> d : diagnoses)
            //                    if (diags.containsAll(d)) target = true;
            //                if (!target) logger.info("target notf "+m+o);


            //  }
            //}
        }

    }

    @Ignore
    @Test
    public void search() throws SolverException, InconsistentTheoryException {
        Properties properties = readProps("alignment/alignment.properties");
        Map<String, List<String>> mapOntos = readOntologiesFromFile(properties);
        for (String m : mapOntos.keySet()) {
            for (String o : mapOntos.get(m)) {
                BackgroundO[] backgrounds = new BackgroundO[]{BackgroundO.O1_O2};
                for (BackgroundO background : backgrounds) {
                    String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
                    OWLOntology ontology = createOwlOntology(m.trim(), o.trim());
                    Set<OWLLogicalAxiom> targetDg = getDiagnosis(targetAxioms, ontology);
                    OWLOntology ontology1 = createOwlOntology(o.split("-")[0].trim());
                    OWLOntology ontology2 = createOwlOntology(o.split("-")[1].trim());
                    OWLTheory theory = createOWLTheory(ontology, false);
                    TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = createUniformCostSearch(theory, false);
                    //ProbabilityTableModel mo = new ProbabilityTableModel();
                    HashMap<ManchesterOWLSyntax, BigDecimal> map = Utils.getProbabMap();
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

    private String simulateBruteForcePaperTest(TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search,
                                               OWLTheory theory, Set<OWLLogicalAxiom> targetDiag,
                                               TableList entry, QSSType scoringFunc, String message) {
        //DiagProvider diagProvider = new DiagProvider(run, false, 9);

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
                logger.trace("numOfQueries: " + num_of_queries + " run for diagnoses");

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

                    daStr += search.getDiagnoses().size() + "/";
                    diagnosesCalc += search.getDiagnoses().size();
                    conflictsCalc += search.getConflicts().size();

                    diagnoses = search.getDiagnoses();
                    diagTime.setTime(System.currentTimeMillis() - diag);
                } catch (SolverException e) {
                    diagnoses = new TreeSet<AxiomSet<OWLLogicalAxiom>>();

                } catch (NoConflictException e) {
                    diagnoses = new TreeSet<AxiomSet<OWLLogicalAxiom>>(search.getDiagnoses());

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
                BigDecimal dp = d.getMeasure();
                if (logger.isInfoEnabled()) {
                    AxiomSet<OWLLogicalAxiom> o = containsItem(diagnoses, targetDiag);
                    BigDecimal diagProbabilities = new BigDecimal("0");
                    for (AxiomSet<OWLLogicalAxiom> tempd : diagnoses)
                        diagProbabilities = diagProbabilities.add(tempd.getMeasure());
                    logger.trace("diagnoses: " + diagnoses.size() +
                            " (" + diagProbabilities + ") first diagnosis: " + d +
                            " is target: " + isTargetDiagFirst + " is in window: " +
                            ((o == null) ? false : o.toString()));
                }

                if (d1 != null) {// && scoringFunc != QSSType.SPLITINHALF) {
                    BigDecimal d1p = d1.getMeasure();
                    BigDecimal temp = d1p.multiply(new BigDecimal("100"));
                    temp = temp.divide(dp);
                    BigDecimal diff = new BigDecimal("100").subtract(temp);
                    logger.trace("difference : " + (dp.subtract(d1p)) + " - " + diff + " %");
                    if (userBrk && diff.compareTo(SIGMA) > 0 && isTargetDiagFirst && num_of_queries > 0) {
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

                logger.trace("numOfQueries: " + num_of_queries + " run for  query");

                long query = System.currentTimeMillis();
                //actPa = getBestQuery(run, diagnoses);

                actPa = queryGenerator.generatePartition(diagnoses);
                minimizePartition(actPa, theory);
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
                    minimizePartition(partition, theory);
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
                    ph.setMeasure(new BigDecimal("0.5").multiply(ph.getMeasure()));
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

}
