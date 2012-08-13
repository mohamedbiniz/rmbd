package at.ainf.owlapi3.performance;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.costestimation.OWLAxiomCostsEstimator;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.model.OWLIncoherencyExtractor;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.parser.MyOWLRendererParser;
import at.ainf.owlapi3.performance.table.TableList;
import at.ainf.owlapi3.utils.ProbabMapCreator;
import at.ainf.owlapi3.utils.creation.target.OAEI08TargetChooser;
import at.ainf.owlapi3.utils.creation.target.OAEI08TargetProvider;
import at.ainf.owlapi3.utils.session.SimulatedSession;
import at.ainf.owlapi3.utils.LogUtil;
import at.ainf.owlapi3.utils.creation.ontology.OAEI08OntologyCreator;
import at.ainf.owlapi3.utils.creation.search.UniformCostSearchCreator;
import at.ainf.owlapi3.utils.creation.theory.BackgroundExtendedTheoryCreator;
import at.ainf.owlapi3.utils.StatisticsTools;
import junit.framework.Assert;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 02.08.12
 * Time: 14:41
 * To change this template use File | Settings | File Templates.
 */
public class
         OAEI08Tests {

    /*@BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlapi3-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }*/

    private static Logger logger = LoggerFactory.getLogger(OAEI08Tests.class.getName());

    public static Map<String, List<String>> readOntologiesFromFile(String str) {
        Properties properties = new Properties();
        String config = ClassLoader.getSystemResource(str).getFile();
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

        String[] testsuites = properties.getProperty("alignment.testsuites").split(",");

        Map<String, List<String>> ontologies = new HashMap<String, List<String>>();

        for (String testsuite : testsuites) {
            List<String> ontologie = Arrays.asList(properties.getProperty(testsuite.trim()).split(","));
            ontologies.put(testsuite, ontologie);
        }
        return ontologies;
    }


    @Test
    public void doTwoTests() throws SolverException, InconsistentTheoryException, IOException {

        Map<String, List<String>> mapOntos = readOntologiesFromFile("alignment/alignment.retest.properties");

        SimulatedSession.QSSType[] qssTypes = new SimulatedSession.QSSType[]{SimulatedSession.QSSType.MINSCORE, SimulatedSession.QSSType.SPLITINHALF, SimulatedSession.QSSType.DYNAMICRISK};
        for (boolean dual : new boolean[]{false}) {
            for (SimulatedSession.TargetSource targetSource : SimulatedSession.TargetSource.values()) {
                for (String m : mapOntos.keySet()) {
                    for (String o : mapOntos.get(m)) {
                        String out = "STAT, " + m + ", " + o;
                        for (SimulatedSession.QSSType type : qssTypes) {
                            SimulatedSession  a = new SimulatedSession();

                            OWLOntology ontology = new OAEI08OntologyCreator(m.trim(), o.trim()).getOntology();
                            Set<OWLLogicalAxiom> targetDg;
                            OWLTheory theory = new BackgroundExtendedTheoryCreator(ontology, dual).getTheory();
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new UniformCostSearchCreator(theory, dual).getSearch();
                            //ProbabilityTableModel mo = new ProbabilityTableModel();
                            HashMap<ManchesterOWLSyntax, BigDecimal> map = ProbabMapCreator.getProbabMap();

                            String path = ClassLoader.getSystemResource("alignment/evaluation/"
                                    + m.trim()
                                    + "-incoherent-evaluation/"
                                    + o.trim()
                                    + ".txt").getPath();

                            OWLTheory theory2 = new BackgroundExtendedTheoryCreator(ontology, dual).getTheory();
                            OWLTheory t3 = new BackgroundExtendedTheoryCreator(ontology, dual).getTheory();
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search2 = new UniformCostSearchCreator(theory2, dual).getSearch();
                            search2.setCostsEstimator(new OWLAxiomCostsEstimator(theory2, path));


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
                            /*
                            try {
                                search.run();
                            } catch (SolverException e) {
                                logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            } catch (NoConflictException e) {
                                logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            } catch (InconsistentTheoryException e) {
                                logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }

                            Set<AxiomSet<OWLLogicalAxiom>> allD = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>(search.getStorage().getDiagnoses());
                            search.reset();
                            */
                            if (targetSource == SimulatedSession.TargetSource.FROM_30_DIAGS) {
                                try {
                                    search.run(30);
                                } catch (SolverException e) {
                                    logger.error(e.toString());//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                } catch (NoConflictException e) {
                                    logger.error(e.toString());//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                } catch (InconsistentTheoryException e) {
                                    logger.error(e.toString());//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                }

                                Set<AxiomSet<OWLLogicalAxiom>> diagnoses =
                                        Collections.unmodifiableSet(search.getDiagnoses());
                                search.reset();
                                AxiomSet<OWLLogicalAxiom> targD = new OAEI08TargetChooser(diagnoses,es).getDgTarget();
                                targetDg = new LinkedHashSet<OWLLogicalAxiom>();
                                for (OWLLogicalAxiom axiom : targD)
                                    targetDg.add(axiom);
                            }

                            if (targetSource == SimulatedSession.TargetSource.FROM_FILE)
                                targetDg = new OAEI08TargetProvider(m,o,ontology).getDiagnosisTarget();

                            TableList e = new TableList();
                            out += "," + type + ",";
                            String message = "act " + m + " - " + o + " - " + targetSource + " " + type + " d " + dual;
                            out +=  a.simulateQuerySession(search, theory, targetDg, e, type, message, null, null, null);

                            //out += simulateQuerySession(search, theory, diags, e, type, message, allD, null, null);
                        }
                        logger.info(out);
                    }
                }
            }
        }
    }

    @Test
    public void doUnsolvableTest() throws SolverException, InconsistentTheoryException, IOException {

        SimulatedSession session = new SimulatedSession();


        Map<String, List<String>> mapOntos = readOntologiesFromFile("alignment/" + "alignment.unsolvable.properties");
        boolean background_add = false;
        session.setShowElRates(false);
        SimulatedSession.QSSType[] qssTypes = new SimulatedSession.QSSType[]{SimulatedSession.QSSType.MINSCORE, SimulatedSession.QSSType.SPLITINHALF, SimulatedSession.QSSType.DYNAMICRISK};
        for (boolean dual : new boolean[]{false}) {
            for (SimulatedSession.TargetSource targetSource : new SimulatedSession.TargetSource[]{SimulatedSession.TargetSource.FROM_FILE}) {
                for (String m : mapOntos.keySet()) {
                    for (String o : mapOntos.get(m)) {
                        String out = "STAT, " + m + ", " + o;
                        for (SimulatedSession.QSSType type : qssTypes) {

                            OWLOntology ontology = new OAEI08OntologyCreator(m.trim(), o.trim()).getOntology();
                            Set<OWLLogicalAxiom> targetDg;
                            OWLTheory theory = new BackgroundExtendedTheoryCreator(ontology, dual).getTheory();
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new UniformCostSearchCreator(theory, dual).getSearch();
                            //ProbabilityTableModel mo = new ProbabilityTableModel();
                            if (background_add) {
                                OWLOntology ontology1 = new OAEI08OntologyCreator(o.split("-")[0].trim()).getOntology();
                                OWLOntology ontology2 = new OAEI08OntologyCreator(o.split("-")[1].trim()).getOntology();
                                theory.addBackgroundFormulas(ontology1.getLogicalAxioms());
                                theory.addBackgroundFormulas(ontology2.getLogicalAxioms());
                            }
                            HashMap<ManchesterOWLSyntax, BigDecimal> map = ProbabMapCreator.getProbabMap();

                            String path = ClassLoader.getSystemResource("alignment/evaluation/"
                                    + m.trim()
                                    + "-incoherent-evaluation/"
                                    + o.trim()
                                    + ".txt").getPath();

                            OWLTheory theory2 = new BackgroundExtendedTheoryCreator(ontology, dual).getTheory();
                            OWLTheory t3 = new BackgroundExtendedTheoryCreator(ontology, dual).getTheory();
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search2 = new UniformCostSearchCreator(theory2, dual).getSearch();
                            search2.setCostsEstimator(new OWLAxiomCostsEstimator(theory2, path));


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
                                    logger.error(e.toString());//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                } catch (NoConflictException e) {
                                    logger.error(e.toString());//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                } catch (InconsistentTheoryException e) {
                                    logger.error(e.toString());//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                }

                                Set<AxiomSet<OWLLogicalAxiom>> diagnoses =
                                        Collections.unmodifiableSet(search.getDiagnoses());
                                search.reset();
                                AxiomSet<OWLLogicalAxiom> targD = new OAEI08TargetChooser(diagnoses,es).getDgTarget();
                                targetDg = new LinkedHashSet<OWLLogicalAxiom>();
                                for (OWLLogicalAxiom axiom : targD)
                                    targetDg.add(axiom);
                            }

                            if (targetSource == SimulatedSession.TargetSource.FROM_FILE)
                                targetDg = new OAEI08TargetProvider(m,o,ontology).getDiagnosisTarget();

                            TableList e = new TableList();
                            out += "," + type + ",";
                            String message = "act " + m + " - " + o + " - " + targetSource + " " + type + " d " + dual;
                            //out += simulateQuerySession(search, theory, diags, e, type, message, allD, search2, t3);

                            out += session.simulateQuerySession(search, theory, targetDg, e, type, message, null, null, null);

                        }
                        logger.info(out);
                    }
                }
            }
        }
    }


    @Ignore
    @Test
    public void doHardTwoTests() throws SolverException, InconsistentTheoryException, IOException {

        Map<String, List<String>> mapOntos = readOntologiesFromFile("alignment/alignment.properties");

        SimulatedSession.QSSType[] qssTypes = new SimulatedSession.QSSType[]{SimulatedSession.QSSType.MINSCORE, SimulatedSession.QSSType.SPLITINHALF, SimulatedSession.QSSType.DYNAMICRISK};
        SimulatedSession.TargetSource[] targetSources = new SimulatedSession.TargetSource[]{SimulatedSession.TargetSource.FROM_FILE};


        for (String m : mapOntos.keySet()) {
            for (String o : mapOntos.get(m)) {
                for (SimulatedSession.TargetSource targetSource : targetSources) {
                    for (SimulatedSession.QSSType type : qssTypes) {
                        BackgroundO[] backgr = new BackgroundO[]{BackgroundO.EMPTY, BackgroundO.O1_O2};
                        for (BackgroundO background : backgr) {

                            SimulatedSession s = new SimulatedSession();
                            s.setNumberOfHittingSets(4);

                            OWLOntology ontology = new OAEI08OntologyCreator(m.trim(), o.trim()).getOntology();
                            Set<OWLLogicalAxiom> targetDg;
                            OWLTheory theory = new BackgroundExtendedTheoryCreator(ontology, false).getTheory();
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new UniformCostSearchCreator(theory, false).getSearch();
                            //ProbabilityTableModel mo = new ProbabilityTableModel();
                            HashMap<ManchesterOWLSyntax, BigDecimal> map = ProbabMapCreator.getProbabMap();

                            String path = ClassLoader.getSystemResource("alignment/evaluation/"
                                    + m.trim()
                                    + "-incoherent-evaluation/"
                                    + o.trim()
                                    + ".txt").getPath();

                            OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, path);
                            OWLOntology ontology1 = new OAEI08OntologyCreator(o.split("-")[0].trim()).getOntology();
                            OWLOntology ontology2 = new OAEI08OntologyCreator(o.split("-")[1].trim()).getOntology();
                            if (background == BackgroundO.O1_O2) {
                                theory.addBackgroundFormulas(ontology1.getLogicalAxioms());
                                theory.addBackgroundFormulas(ontology2.getLogicalAxioms());
                            }
                            //es.updateKeywordProb(map);
                            targetDg = null;

                            search.setCostsEstimator(es);
                            if (targetSource == SimulatedSession.TargetSource.FROM_30_DIAGS) {
                                try {
                                    search.run(30);
                                } catch (SolverException e) {
                                    //logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                } catch (NoConflictException e) {
                                    //logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                } catch (InconsistentTheoryException e) {
                                    //logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                }

                                Collections.unmodifiableSet(search.getDiagnoses());

                                Set<AxiomSet<OWLLogicalAxiom>> diagnoses =
                                        Collections.unmodifiableSet(search.getDiagnoses());
                                search.reset();
                                AxiomSet<OWLLogicalAxiom> targD = new OAEI08TargetChooser(diagnoses,es).getDgTarget();
                                targetDg = new LinkedHashSet<OWLLogicalAxiom>();
                                for (OWLLogicalAxiom axiom : targD)
                                    targetDg.add(axiom);
                            }

                            if (targetSource == SimulatedSession.TargetSource.FROM_FILE)
                                targetDg = new OAEI08TargetProvider(m,o,ontology).getDiagnosisTarget();

                            TableList e = new TableList();
                            String message = "running "
                                    + "matcher " + m
                                    + ", ontology " + o
                                    + ", source " + targetSource
                                    + ", qss " + type
                                    + ", background " + background;
                            s.simulateQuerySession(search, theory, targetDg, e, type, message, null, null, null);
                        }
                    }
                }
            }
        }
    }

    @Test
    public void doOnlyOneQuerySession() throws SolverException, InconsistentTheoryException, IOException {

        SimulatedSession s = new SimulatedSession();


        SimulatedSession.QSSType[] qssTypes = new SimulatedSession.QSSType[]{SimulatedSession.QSSType.MINSCORE, SimulatedSession.QSSType.SPLITINHALF, SimulatedSession.QSSType.DYNAMICRISK};
        String m = "owlctxmatch";
        String o = "SIGKDD-EKAW";
        SimulatedSession.TargetSource targetSource = SimulatedSession.TargetSource.FROM_FILE;
        SimulatedSession.QSSType type = SimulatedSession.QSSType.SPLITINHALF;

        OWLOntology ontology = new OAEI08OntologyCreator(m.trim(), o.trim()).getOntology();
        Set<OWLLogicalAxiom> targetDg;
        OWLTheory theory = new BackgroundExtendedTheoryCreator(ontology, false).getTheory();
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new UniformCostSearchCreator(theory, false).getSearch();
        OWLOntology ontology1 = new OAEI08OntologyCreator(o.split("-")[0].trim()).getOntology();
        OWLOntology ontology2 = new OAEI08OntologyCreator(o.split("-")[1].trim()).getOntology();
        //theory.addBackgroundFormulas(ontology1.getLogicalAxioms());
        //theory.addBackgroundFormulas(ontology2.getLogicalAxioms());
        //ProbabilityTableModel mo = new ProbabilityTableModel();
        HashMap<ManchesterOWLSyntax, BigDecimal> map = ProbabMapCreator.getProbabMap();

        String path = ClassLoader.getSystemResource("alignment/evaluation/"
                + m.trim()
                + "-incoherent-evaluation/"
                + o.trim()
                + ".txt").getPath();

        OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, path);
        //es.updateKeywordProb(map);
        targetDg = null;

        search.setCostsEstimator(es);
        if (targetSource == SimulatedSession.TargetSource.FROM_30_DIAGS) {
            try {
                search.run(30);
            } catch (SolverException e) {
                //logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (NoConflictException e) {
                //logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InconsistentTheoryException e) {
                //logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            Set<AxiomSet<OWLLogicalAxiom>> diagnoses = Collections.unmodifiableSet(search.getDiagnoses());
            search.reset();
            AxiomSet<OWLLogicalAxiom> targD = new OAEI08TargetChooser(diagnoses,es).getDgTarget();
            targetDg = new LinkedHashSet<OWLLogicalAxiom>();
            for (OWLLogicalAxiom axiom : targD)
                targetDg.add(axiom);
        }

        if (targetSource == SimulatedSession.TargetSource.FROM_FILE) {
            targetDg = new OAEI08TargetProvider(m,o,ontology).getDiagnosisTarget();
            int diags = -1;
            try {
                search.run(diags);
            } catch (SolverException e) {
                //logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (NoConflictException e) {
                //logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InconsistentTheoryException e) {
                //logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            Set<AxiomSet<OWLLogicalAxiom>> diagnoses = Collections.unmodifiableSet(search.getDiagnoses());
            Assert.assertTrue(diagnoses.contains(targetDg));
            search.reset();
        }

        TableList e = new TableList();
        String message = "act " + m + " - " + o + " - " + targetSource + " " + type;
        s.simulateQuerySession(search, theory, targetDg, e, type, message, null, null, null);
    }

    @Test
    public void doQueryEliminationRateTest() throws SolverException, InconsistentTheoryException, IOException {

        SimulatedSession.QSSType[] qssTypes = new SimulatedSession.QSSType[]{SimulatedSession.QSSType.SPLITINHALF,};
        for (boolean dual : new boolean[]{false}) {
            for (SimulatedSession.TargetSource targetSource : new SimulatedSession.TargetSource[]{SimulatedSession.TargetSource.FROM_FILE}) {
                for (String m : new String[]{"coma"}) {
                    for (String o : new String[]{"CRS-EKAW"}) {
                        String out = "STAT, " + m + ", " + o;
                        for (SimulatedSession.QSSType type : qssTypes) {
                            SimulatedSession s = new SimulatedSession();

                            OWLOntology ontology = new OAEI08OntologyCreator(m.trim(), o.trim()).getOntology();
                            Set<OWLLogicalAxiom> targetDg;
                            OWLTheory theory = new BackgroundExtendedTheoryCreator(ontology, dual).getTheory();
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new UniformCostSearchCreator(theory, dual).getSearch();
                            //ProbabilityTableModel mo = new ProbabilityTableModel();
                            HashMap<ManchesterOWLSyntax, BigDecimal> map = ProbabMapCreator.getProbabMap();

                            String path = ClassLoader.getSystemResource("alignment/evaluation/"
                                    + m.trim()
                                    + "-incoherent-evaluation/"
                                    + o.trim()
                                    + ".txt").getPath();

                            OWLTheory theory2 = new BackgroundExtendedTheoryCreator(ontology, dual).getTheory();
                            OWLTheory t3 = new BackgroundExtendedTheoryCreator(ontology, dual).getTheory();
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search2 = new UniformCostSearchCreator(theory2, dual).getSearch();
                            search2.setCostsEstimator(new OWLAxiomCostsEstimator(theory2, path));


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
                                logger.error(e.toString());//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            } catch (NoConflictException e) {
                                logger.error(e.toString());//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            } catch (InconsistentTheoryException e) {
                                logger.error(e.toString());//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }

                            Set<AxiomSet<OWLLogicalAxiom>> allD = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>(search.getDiagnoses());
                            search.reset();

                            if (targetSource == SimulatedSession.TargetSource.FROM_30_DIAGS) {
                                try {
                                    search.run(30);
                                } catch (SolverException e) {
                                    logger.error(e.toString());//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                } catch (NoConflictException e) {
                                    logger.error(e.toString());//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                } catch (InconsistentTheoryException e) {
                                    logger.error(e.toString());//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                }

                                Set<AxiomSet<OWLLogicalAxiom>> diagnoses =
                                        Collections.unmodifiableSet(search.getDiagnoses());
                                search.reset();
                                AxiomSet<OWLLogicalAxiom> targD = new OAEI08TargetChooser(diagnoses,es).getDgTarget();
                                targetDg = new LinkedHashSet<OWLLogicalAxiom>();
                                for (OWLLogicalAxiom axiom : targD)
                                    targetDg.add(axiom);
                            }

                            if (targetSource == SimulatedSession.TargetSource.FROM_FILE)
                                targetDg = new OAEI08TargetProvider(m,o,ontology).getDiagnosisTarget();

                            TableList e = new TableList();
                            out += "," + type + ",";
                            String message = "act " + m + " - " + o + " - " + targetSource + " " + type + " d " + dual;
                            out += s.simulateQuerySession(search, theory, targetDg, e, type, message, allD, search2, t3);
                        }
                        logger.info(out);
                    }
                }
            }
        }
    }

    @Ignore
    @Test
    public void search() throws SolverException, InconsistentTheoryException {

        Map<String, List<String>> mapOntos = readOntologiesFromFile("alignment/alignment.properties");
        for (String m : mapOntos.keySet()) {
            for (String o : mapOntos.get(m)) {
                BackgroundO[] backgrounds = new BackgroundO[]{BackgroundO.O1_O2};
                for (BackgroundO background : backgrounds) {
                    OWLOntology ontology = new OAEI08OntologyCreator(m.trim(), o.trim()).getOntology();
                    Set<OWLLogicalAxiom> targetDg = new OAEI08TargetProvider(m,o,ontology).getDiagnosisTarget();
                    OWLOntology ontology1 = new OAEI08OntologyCreator(o.split("-")[0].trim()).getOntology();
                    OWLOntology ontology2 = new OAEI08OntologyCreator(o.split("-")[1].trim()).getOntology();
                    OWLTheory theory = new BackgroundExtendedTheoryCreator(ontology, false).getTheory();
                    TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new UniformCostSearchCreator(theory, false).getSearch();
                    //ProbabilityTableModel mo = new ProbabilityTableModel();
                    HashMap<ManchesterOWLSyntax, BigDecimal> map = ProbabMapCreator.getProbabMap();
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
                        logger.error(e.toString());//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (NoConflictException e) {
                        logger.error(e.toString());//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (InconsistentTheoryException e) {
                        logger.error(e.toString());//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    time = System.nanoTime() - time;
                    String t = LogUtil.getStringTime(time / 1000000);

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

    @Test
    public void calcOneDiagAndMore() throws SolverException, InconsistentTheoryException, IOException {

        Map<String, List<String>> mapOntos = readOntologiesFromFile("alignment/" + "alignment.unsolvable.properties");

        for (boolean dual : new boolean[]{true, false}) {

            for (String m : mapOntos.keySet()) {
                for (String o : mapOntos.get(m)) {
                    for (int nd : new int[]{1, 5, 9}) {
                        String out = "STAT, " + m + ", " + o;


                        OWLOntology ontology = new OAEI08OntologyCreator(m.trim(), o.trim()).getOntology();
                        Set<OWLLogicalAxiom> targetDg;
                        OWLTheory theory = new BackgroundExtendedTheoryCreator(ontology, dual).getTheory();
                        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new UniformCostSearchCreator(theory, dual).getSearch();
                        //ProbabilityTableModel mo = new ProbabilityTableModel();
                        HashMap<ManchesterOWLSyntax, BigDecimal> map = ProbabMapCreator.getProbabMap();

                        OWLOntology ontology1 = new OAEI08OntologyCreator(o.split("-")[0].trim()).getOntology();
                        OWLOntology ontology2 = new OAEI08OntologyCreator(o.split("-")[1].trim()).getOntology();

                        String path = ClassLoader.getSystemResource("alignment/evaluation/"
                                + m.trim()
                                + "-incoherent-evaluation/"
                                + o.trim()
                                + ".txt").getPath();

                        OWLTheory theory2 = new BackgroundExtendedTheoryCreator(ontology, dual).getTheory();
                        OWLTheory t3 = new BackgroundExtendedTheoryCreator(ontology, dual).getTheory();
                        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search2 = new UniformCostSearchCreator(theory2, dual).getSearch();
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
                            logger.error(e.toString());//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        } catch (NoConflictException e) {
                            logger.error(e.toString());//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        } catch (InconsistentTheoryException e) {
                            logger.error(e.toString());//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                        time = System.nanoTime() - time;

                        int minDiagnosisC = StatisticsTools.minCard(search.getDiagnoses());
                        double meanDiagnosisC = StatisticsTools.meanCard(search.getDiagnoses());
                        int maxDiagnosisC = StatisticsTools.maxCard(search.getDiagnoses());
                        int minConfC = StatisticsTools.minCard(search.getConflicts());
                        double meanConfC = StatisticsTools.meanCard(search.getConflicts());
                        int maxConfC = StatisticsTools.maxCard(search.getConflicts());

                        int c = search.getConflicts().size();
                        String s = nd + ", " + minDiagnosisC + ", " + meanDiagnosisC + ", " + maxDiagnosisC + ", " +
                                c + ", " + minConfC + ", " + meanConfC + ", " + maxConfC;

                        logger.info("Stat, " + m.trim() + ", " + o.trim() + ", "
                                + s + ", "
                                + theory.getConsistencyCount() + ", " + dual + ", " + LogUtil.getStringTime(time / 1000000));
                    }
                }
            }

        }
    }

    @Test
    public void calcOnlyDiagnoses() throws IOException, SolverException, InconsistentTheoryException, NoConflictException {
        String m = "coma";
        String o = "CRS-EKAW";



        OWLOntology ontology = new OAEI08OntologyCreator(m.trim(), o.trim()).getOntology();
        Set<OWLLogicalAxiom> targetDg;
        OWLTheory theory = new BackgroundExtendedTheoryCreator(ontology, false).getTheory();
        Set<AxiomSet<OWLLogicalAxiom>> allD = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>();
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new UniformCostSearchCreator(theory, false).getSearch();
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


        Map<String, List<String>> mapOntos = readOntologiesFromFile("alignment/" + "alignment.allFiles.properties");
        //boolean background_add = false;

        session.setShowElRates(false);

        SimulatedSession.QSSType[] qssTypes = new SimulatedSession.QSSType[]{SimulatedSession.QSSType.MINSCORE, SimulatedSession.QSSType.SPLITINHALF, SimulatedSession.QSSType.DYNAMICRISK};
        for (boolean dual : new boolean[]{false}) {
            for (boolean background : new boolean[]{true, false}) {
                for (SimulatedSession.TargetSource targetSource : new SimulatedSession.TargetSource[]{SimulatedSession.TargetSource.FROM_FILE, SimulatedSession.TargetSource.FROM_30_DIAGS}) {
                    for (String m : mapOntos.keySet()) {
                        for (String o : mapOntos.get(m)) {
                            String out = "STAT, " + m + ", " + o;
                            for (SimulatedSession.QSSType type : qssTypes) {

                                //String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");


                                OWLOntology ontology = new OAEI08OntologyCreator(m.trim(), o.trim()).getOntology();
                                ontology = new OWLIncoherencyExtractor(
                                        new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
                                Set<OWLLogicalAxiom> targetDg;
                                OWLTheory theory = new BackgroundExtendedTheoryCreator(ontology, dual).getTheory();
                                TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new UniformCostSearchCreator(theory, dual).getSearch();
                                if (background) {
                                    OWLOntology ontology1 = new OAEI08OntologyCreator(o.split("-")[0].trim()).getOntology();
                                    OWLOntology ontology2 = new OAEI08OntologyCreator(o.split("-")[1].trim()).getOntology();
                                    theory.addBackgroundFormulas(ontology1.getLogicalAxioms());
                                    theory.addBackgroundFormulas(ontology2.getLogicalAxioms());
                                }
                                //ProbabilityTableModel mo = new ProbabilityTableModel();
                                HashMap<ManchesterOWLSyntax, BigDecimal> map = ProbabMapCreator.getProbabMap();

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
                                        logger.error(e.toString());//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                    } catch (NoConflictException e) {
                                        logger.error(e.toString());//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                    } catch (InconsistentTheoryException e) {
                                        logger.error(e.toString());//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                    }

                                    Set<AxiomSet<OWLLogicalAxiom>> diagnoses =
                                            Collections.unmodifiableSet(search.getDiagnoses());
                                    search.reset();
                                    AxiomSet<OWLLogicalAxiom> targD = new OAEI08TargetChooser(diagnoses,es).getDgTarget();
                                    targetDg = new LinkedHashSet<OWLLogicalAxiom>();
                                    for (OWLLogicalAxiom axiom : targD)
                                        targetDg.add(axiom);
                                }

                                if (targetSource == SimulatedSession.TargetSource.FROM_FILE)
                                    targetDg = new OAEI08TargetProvider(m,o,ontology).getDiagnosisTarget();

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
    public void readTest() throws IOException {
        String filename = ClassLoader.getSystemResource("alignment/evaluation/owlctxmatch-incoherent-evaluation/CMT-CONFTOOL.txt").getFile();
        Map<String, Double> axioms = new LinkedHashMap<String, Double>();
        Set<String> targetDiag = new LinkedHashSet<String>();
        OAEI08TargetProvider.readData(filename, axioms, targetDiag);
        logger.info("Read " + axioms.size() + " " + targetDiag.size());
        assertEquals(axioms.size(), 36);
        assertEquals(targetDiag.size(), 6);

        filename = ClassLoader.getSystemResource("alignment/evaluation/hmatch-incoherent-evaluation/CMT-CRS.txt").getFile();
        axioms.clear();
        targetDiag.clear();
        OAEI08TargetProvider.readData(filename, axioms, targetDiag);
        logger.info("Read " + axioms.size() + " " + targetDiag.size());
        assertEquals(axioms.size(), 2 * (17 - 5));
        assertEquals(targetDiag.size(), 4);
    }

    @Test
    public void readTest2() throws IOException {
        String filename = ClassLoader.getSystemResource("alignment/evaluation/coma-evaluation/CMT-CONFTOOL.txt").getFile();
        Map<String, Double> axioms = new LinkedHashMap<String, Double>();
        Set<String> targetDiag = new LinkedHashSet<String>();
        OAEI08TargetProvider.readData(filename, axioms, targetDiag);
        logger.info("Read " + axioms.size() + " " + targetDiag.size());
    }

    @Test
    public void readTest1() throws IOException {
        String filename = ClassLoader.getSystemResource("alignment/evaluation/coma-evaluation/CMT-CONFTOOL.txt").getFile();
        Map<String, Double> axioms = new LinkedHashMap<String, Double>();
        Set<String> targetDiag = new LinkedHashSet<String>();
        OAEI08TargetProvider.readData(filename, axioms, targetDiag);
        logger.info("Read " + axioms.size() + " " + targetDiag.size());
    }


    public enum BackgroundO {EMPTY, O1, O2, O1_O2}
}
