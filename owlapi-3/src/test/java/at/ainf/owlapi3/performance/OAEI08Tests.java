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
import at.ainf.owlapi3.utils.session.OAEI08Session;
import at.ainf.owlapi3.utils.LogUtil;
import at.ainf.owlapi3.utils.creation.ontology.OAEI08OntologyCreator;
import at.ainf.owlapi3.utils.creation.search.UniformCostSearchCreator;
import at.ainf.owlapi3.utils.creation.theory.BackgroundExtendedTheoryCreator;
import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

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
public class OAEI08Tests {
    @BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlapi3-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }

    private static Logger logger = Logger.getLogger(OAEI08Tests.class.getName());

    @Test
    public void doTwoTests() throws SolverException, InconsistentTheoryException, IOException {
        Properties properties = OAEI08Session.readProps2("alignment/alignment.retest.properties");
        Map<String, List<String>> mapOntos = OAEI08Session.readOntologiesFromFile2(properties);

        OAEI08Session.QSSType[] qssTypes = new OAEI08Session.QSSType[]{OAEI08Session.QSSType.MINSCORE, OAEI08Session.QSSType.SPLITINHALF, OAEI08Session.QSSType.DYNAMICRISK};
        for (boolean dual : new boolean[]{false}) {
            for (OAEI08Session.TargetSource targetSource : OAEI08Session.TargetSource.values()) {
                for (String m : mapOntos.keySet()) {
                    for (String o : mapOntos.get(m)) {
                        String out = "STAT, " + m + ", " + o;
                        for (OAEI08Session.QSSType type : qssTypes) {
                            OAEI08Session  a = new OAEI08Session();
                            String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
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
                            if (targetSource == OAEI08Session.TargetSource.FROM_30_DIAGS) {
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
                                AxiomSet<OWLLogicalAxiom> targD = OAEI08Session.getTargetDiag2(diagnoses, es, m);
                                targetDg = new LinkedHashSet<OWLLogicalAxiom>();
                                for (OWLLogicalAxiom axiom : targD)
                                    targetDg.add(axiom);
                            }

                            if (targetSource == OAEI08Session.TargetSource.FROM_FILE)
                                targetDg = OAEI08Session.getDiagnosis2(targetAxioms, ontology);

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

        OAEI08Session session = new OAEI08Session();

        Properties properties = OAEI08Session.readProps();
        Map<String, List<String>> mapOntos = OAEI08Session.readOntologiesFromFile(properties);
        boolean background_add = false;
        session.setShowElRates(false);
        OAEI08Session.QSSType[] qssTypes = new OAEI08Session.QSSType[]{OAEI08Session.QSSType.MINSCORE, OAEI08Session.QSSType.SPLITINHALF, OAEI08Session.QSSType.DYNAMICRISK};
        for (boolean dual : new boolean[]{false}) {
            for (OAEI08Session.TargetSource targetSource : new OAEI08Session.TargetSource[]{OAEI08Session.TargetSource.FROM_FILE}) {
                for (String m : mapOntos.keySet()) {
                    for (String o : mapOntos.get(m)) {
                        String out = "STAT, " + m + ", " + o;
                        for (OAEI08Session.QSSType type : qssTypes) {
                            //String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
                            String[] targetAxioms = OAEI08Session.getDiagnosis(m, o);
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

                            if (targetSource == OAEI08Session.TargetSource.FROM_30_DIAGS) {
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
                                AxiomSet<OWLLogicalAxiom> targD = OAEI08Session.getTargetDiag(diagnoses, es, m);
                                targetDg = new LinkedHashSet<OWLLogicalAxiom>();
                                for (OWLLogicalAxiom axiom : targD)
                                    targetDg.add(axiom);
                            }

                            if (targetSource == OAEI08Session.TargetSource.FROM_FILE)
                                targetDg = OAEI08Session.getDiagnosis(targetAxioms, ontology);

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
        Properties properties = OAEI08Session.readProps2("alignment/alignment.properties");
        Map<String, List<String>> mapOntos = OAEI08Session.readOntologiesFromFile2(properties);

        OAEI08Session.QSSType[] qssTypes = new OAEI08Session.QSSType[]{OAEI08Session.QSSType.MINSCORE, OAEI08Session.QSSType.SPLITINHALF, OAEI08Session.QSSType.DYNAMICRISK};
        OAEI08Session.TargetSource[] targetSources = new OAEI08Session.TargetSource[]{OAEI08Session.TargetSource.FROM_FILE};


        for (String m : mapOntos.keySet()) {
            for (String o : mapOntos.get(m)) {
                for (OAEI08Session.TargetSource targetSource : targetSources) {
                    for (OAEI08Session.QSSType type : qssTypes) {
                        OAEI08Session.BackgroundO[] backgr = new OAEI08Session.BackgroundO[]{OAEI08Session.BackgroundO.EMPTY, OAEI08Session.BackgroundO.O1_O2};
                        for (OAEI08Session.BackgroundO background : backgr) {

                            OAEI08Session s = new OAEI08Session();
                            s.setNumberOfHittingSets(4);
                            String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
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
                            if (background == OAEI08Session.BackgroundO.O1_O2) {
                                theory.addBackgroundFormulas(ontology1.getLogicalAxioms());
                                theory.addBackgroundFormulas(ontology2.getLogicalAxioms());
                            }
                            //es.updateKeywordProb(map);
                            targetDg = null;

                            search.setCostsEstimator(es);
                            if (targetSource == OAEI08Session.TargetSource.FROM_30_DIAGS) {
                                OAEI08Session.run(search, 30);

                                Set<AxiomSet<OWLLogicalAxiom>> diagnoses =
                                        Collections.unmodifiableSet(search.getDiagnoses());
                                search.reset();
                                AxiomSet<OWLLogicalAxiom> targD = OAEI08Session.getTargetDiag2(diagnoses, es, m);
                                targetDg = new LinkedHashSet<OWLLogicalAxiom>();
                                for (OWLLogicalAxiom axiom : targD)
                                    targetDg.add(axiom);
                            }

                            if (targetSource == OAEI08Session.TargetSource.FROM_FILE)
                                targetDg = OAEI08Session.getDiagnosis2(targetAxioms, ontology);

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

        OAEI08Session s = new OAEI08Session();

        Properties properties = OAEI08Session.readProps2("alignment/alignment.properties");
        OAEI08Session.QSSType[] qssTypes = new OAEI08Session.QSSType[]{OAEI08Session.QSSType.MINSCORE, OAEI08Session.QSSType.SPLITINHALF, OAEI08Session.QSSType.DYNAMICRISK};
        String m = "owlctxmatch";
        String o = "SIGKDD-EKAW";
        OAEI08Session.TargetSource targetSource = OAEI08Session.TargetSource.FROM_FILE;
        OAEI08Session.QSSType type = OAEI08Session.QSSType.SPLITINHALF;
        String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
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
        if (targetSource == OAEI08Session.TargetSource.FROM_30_DIAGS) {
            Set<AxiomSet<OWLLogicalAxiom>> diagnoses = OAEI08Session.run(search, 30);
            search.reset();
            AxiomSet<OWLLogicalAxiom> targD = OAEI08Session.getTargetDiag2(diagnoses, es, m);
            targetDg = new LinkedHashSet<OWLLogicalAxiom>();
            for (OWLLogicalAxiom axiom : targD)
                targetDg.add(axiom);
        }

        if (targetSource == OAEI08Session.TargetSource.FROM_FILE) {
            targetDg = OAEI08Session.getDiagnosis2(targetAxioms, ontology);
            Set<AxiomSet<OWLLogicalAxiom>> diagnoses = OAEI08Session.run(search, -1);
            Assert.assertTrue(diagnoses.contains(targetDg));
            search.reset();
        }

        TableList e = new TableList();
        String message = "act " + m + " - " + o + " - " + targetSource + " " + type;
        s.simulateQuerySession(search, theory, targetDg, e, type, message, null, null, null);
    }

    @Test
    public void doQueryEliminationRateTest() throws SolverException, InconsistentTheoryException, IOException {
        Properties properties = OAEI08Session.readProps2("alignment/alignment.properties");
        Map<String, List<String>> mapOntos = OAEI08Session.readOntologiesFromFile2(properties);

        OAEI08Session.QSSType[] qssTypes = new OAEI08Session.QSSType[]{OAEI08Session.QSSType.SPLITINHALF,};
        for (boolean dual : new boolean[]{false}) {
            for (OAEI08Session.TargetSource targetSource : new OAEI08Session.TargetSource[]{OAEI08Session.TargetSource.FROM_FILE}) {
                for (String m : new String[]{"coma"}) {
                    for (String o : new String[]{"CRS-EKAW"}) {
                        String out = "STAT, " + m + ", " + o;
                        for (OAEI08Session.QSSType type : qssTypes) {
                            OAEI08Session s = new OAEI08Session();
                            String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
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
                                logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            } catch (NoConflictException e) {
                                logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            } catch (InconsistentTheoryException e) {
                                logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }

                            Set<AxiomSet<OWLLogicalAxiom>> allD = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>(search.getDiagnoses());
                            search.reset();

                            if (targetSource == OAEI08Session.TargetSource.FROM_30_DIAGS) {
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
                                AxiomSet<OWLLogicalAxiom> targD = OAEI08Session.getTargetDiag2(diagnoses, es, m);
                                targetDg = new LinkedHashSet<OWLLogicalAxiom>();
                                for (OWLLogicalAxiom axiom : targD)
                                    targetDg.add(axiom);
                            }

                            if (targetSource == OAEI08Session.TargetSource.FROM_FILE)
                                targetDg = OAEI08Session.getDiagnosis2(targetAxioms, ontology);

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
        Properties properties = OAEI08Session.readProps2("alignment/alignment.properties");
        Map<String, List<String>> mapOntos = OAEI08Session.readOntologiesFromFile2(properties);
        for (String m : mapOntos.keySet()) {
            for (String o : mapOntos.get(m)) {
                OAEI08Session.BackgroundO[] backgrounds = new OAEI08Session.BackgroundO[]{OAEI08Session.BackgroundO.O1_O2};
                for (OAEI08Session.BackgroundO background : backgrounds) {
                    String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
                    OWLOntology ontology = new OAEI08OntologyCreator(m.trim(), o.trim()).getOntology();
                    Set<OWLLogicalAxiom> targetDg = OAEI08Session.getDiagnosis2(targetAxioms, ontology);
                    OWLOntology ontology1 = new OAEI08OntologyCreator(o.split("-")[0].trim()).getOntology();
                    OWLOntology ontology2 = new OAEI08OntologyCreator(o.split("-")[1].trim()).getOntology();
                    OWLTheory theory = new BackgroundExtendedTheoryCreator(ontology, false).getTheory();
                    TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new UniformCostSearchCreator(theory, false).getSearch();
                    //ProbabilityTableModel mo = new ProbabilityTableModel();
                    HashMap<ManchesterOWLSyntax, BigDecimal> map = ProbabMapCreator.getProbabMap();
                    OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theory);
                    es.updateKeywordProb(map);
                    if (background == OAEI08Session.BackgroundO.O1 || background == OAEI08Session.BackgroundO.O1_O2)
                        theory.addBackgroundFormulas(ontology1.getLogicalAxioms());
                    if (background == OAEI08Session.BackgroundO.O2 || background == OAEI08Session.BackgroundO.O1_O2)
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
        Properties properties = OAEI08Session.readProps("alignment.unsolvable.properties");
        Map<String, List<String>> mapOntos = OAEI08Session.readOntologiesFromFile(properties);

        for (boolean dual : new boolean[]{true, false}) {

            for (String m : mapOntos.keySet()) {
                for (String o : mapOntos.get(m)) {
                    for (int nd : new int[]{1, 5, 9}) {
                        String out = "STAT, " + m + ", " + o;

                        String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
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
                            logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        } catch (NoConflictException e) {
                            logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        } catch (InconsistentTheoryException e) {
                            logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                        time = System.nanoTime() - time;

                        int minDiagnosisC = OAEI08Session.minCard(search.getDiagnoses());
                        double meanDiagnosisC = OAEI08Session.meanCard(search.getDiagnoses());
                        int maxDiagnosisC = OAEI08Session.maxCard(search.getDiagnoses());
                        int minConfC = OAEI08Session.minCard(search.getConflicts());
                        double meanConfC = OAEI08Session.meanCard(search.getConflicts());
                        int maxConfC = OAEI08Session.maxCard(search.getConflicts());

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
        OAEI08Session.QSSType type = OAEI08Session.QSSType.SPLITINHALF;
        Properties properties = OAEI08Session.readProps2("alignment/alignment.full.properties");
        String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
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
        OAEI08Session session = new OAEI08Session();

        Properties properties = OAEI08Session.readProps("alignment.allFiles.properties");
        Map<String, List<String>> mapOntos = OAEI08Session.readOntologiesFromFile(properties);
        //boolean background_add = false;

        session.setShowElRates(false);

        OAEI08Session.QSSType[] qssTypes = new OAEI08Session.QSSType[]{OAEI08Session.QSSType.MINSCORE, OAEI08Session.QSSType.SPLITINHALF, OAEI08Session.QSSType.DYNAMICRISK};
        for (boolean dual : new boolean[]{false}) {
            for (boolean background : new boolean[]{true, false}) {
                for (OAEI08Session.TargetSource targetSource : new OAEI08Session.TargetSource[]{OAEI08Session.TargetSource.FROM_FILE, OAEI08Session.TargetSource.FROM_30_DIAGS}) {
                    for (String m : mapOntos.keySet()) {
                        for (String o : mapOntos.get(m)) {
                            String out = "STAT, " + m + ", " + o;
                            for (OAEI08Session.QSSType type : qssTypes) {

                                //String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");

                                String[] targetAxioms = OAEI08Session.getDiagnosis(m, o);
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

                                if (targetSource == OAEI08Session.TargetSource.FROM_30_DIAGS) {
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
                                    AxiomSet<OWLLogicalAxiom> targD = OAEI08Session.getTargetDiag(diagnoses, es, m);
                                    targetDg = new LinkedHashSet<OWLLogicalAxiom>();
                                    for (OWLLogicalAxiom axiom : targD)
                                        targetDg.add(axiom);
                                }

                                if (targetSource == OAEI08Session.TargetSource.FROM_FILE)
                                    targetDg = OAEI08Session.getDiagnosis(targetAxioms, ontology);

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
    public void doShowMappingAxiomsSizes() throws SolverException, InconsistentTheoryException, IOException {
        Properties properties = OAEI08Session.readProps("alignment.allFiles.properties");
        Map<String, List<String>> mapOntos = OAEI08Session.readOntologiesFromFile(properties);

        for (String m : mapOntos.keySet()) {
            for (String o : mapOntos.get(m)) {
                OAEI08Session.getDiagnosis(m, o);
            }
        }
    }

    @Test
    public void readTest() throws IOException {
        String filename = ClassLoader.getSystemResource("alignment/evaluation/owlctxmatch-incoherent-evaluation/CMT-CONFTOOL.txt").getFile();
        Map<String, Double> axioms = new LinkedHashMap<String, Double>();
        Set<String> targetDiag = new LinkedHashSet<String>();
        OAEI08Session.readData2(filename, axioms, targetDiag);
        logger.info("Read " + axioms.size() + " " + targetDiag.size());
        assertEquals(axioms.size(), 36);
        assertEquals(targetDiag.size(), 6);

        filename = ClassLoader.getSystemResource("alignment/evaluation/hmatch-incoherent-evaluation/CMT-CRS.txt").getFile();
        axioms.clear();
        targetDiag.clear();
        OAEI08Session.readData2(filename, axioms, targetDiag);
        logger.info("Read " + axioms.size() + " " + targetDiag.size());
        assertEquals(axioms.size(), 2 * (17 - 5));
        assertEquals(targetDiag.size(), 4);
    }

    @Test
    public void readTest2() throws IOException {
        String filename = ClassLoader.getSystemResource("alignment/evaluation/coma-evaluation/CMT-CONFTOOL.txt").getFile();
        Map<String, Double> axioms = new LinkedHashMap<String, Double>();
        Set<String> targetDiag = new LinkedHashSet<String>();
        OAEI08Session.readData2(filename, axioms, targetDiag);
        logger.info("Read " + axioms.size() + " " + targetDiag.size());
    }

    @Test
    public void readTest1() throws IOException {
        String filename = ClassLoader.getSystemResource("alignment/evaluation/coma-evaluation/CMT-CONFTOOL.txt").getFile();
        Map<String, Double> axioms = new LinkedHashMap<String, Double>();
        Set<String> targetDiag = new LinkedHashSet<String>();
        OAEI08Session.readData2(filename, axioms, targetDiag);
        logger.info("Read " + axioms.size() + " " + targetDiag.size());
    }

    @Test
    public void testDaReadMethods() throws IOException, SolverException, InconsistentTheoryException {
        Properties properties = OAEI08Session.readProps("alignment.properties");
        Map<String, List<String>> mapOntos = OAEI08Session.readOntologiesFromFile(properties);

        for (String m : mapOntos.keySet()) {
            for (String o : mapOntos.get(m)) {
                String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
                String[] targetAxioms2 = OAEI08Session.getDiagnosis(m, o);
                boolean eq = OAEI08Session.compareDiagnoses(targetAxioms, targetAxioms2);
                if (!eq) {
                    OWLOntology ontology = new OAEI08OntologyCreator(m.trim(), o.trim()).getOntology();
                    Set<OWLLogicalAxiom> targetDg = OAEI08Session.getDiagnosis(targetAxioms, ontology);
                    System.out.println(targetAxioms.toString());
                    System.out.println(targetAxioms2.toString());
                }
                assertTrue(m + " " + o, eq);
            }
        }
    }

}
