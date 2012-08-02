package at.ainf.owlapi3.performance;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.costestimation.OWLAxiomCostsEstimator;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.performance.table.TableList;
import at.ainf.owlapi3.utils.SimulatedSession;
import at.ainf.owlapi3.utils.creation.CommonUtils;
import at.ainf.owlapi3.utils.creation.CreationUtils;
import at.ainf.owlapi3.utils.creation.OAEI08Utils;
import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

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
        Properties properties = OAEI08Utils.readProps2("alignment/alignment.retest.properties");
        Map<String, List<String>> mapOntos = OAEI08Utils.readOntologiesFromFile2(properties);

        SimulatedSession.QSSType[] qssTypes = new SimulatedSession.QSSType[]{SimulatedSession.QSSType.MINSCORE, SimulatedSession.QSSType.SPLITINHALF, SimulatedSession.QSSType.DYNAMICRISK};
        for (boolean dual : new boolean[]{false}) {
            for (SimulatedSession.TargetSource targetSource : SimulatedSession.TargetSource.values()) {
                for (String m : mapOntos.keySet()) {
                    for (String o : mapOntos.get(m)) {
                        String out = "STAT, " + m + ", " + o;
                        for (SimulatedSession.QSSType type : qssTypes) {
                            SimulatedSession  a = new SimulatedSession();
                            String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
                            OWLOntology ontology = CreationUtils.createOwlOntology2(m.trim(), o.trim());
                            Set<OWLLogicalAxiom> targetDg;
                            OWLTheory theory = CreationUtils.createOWLTheory2(ontology, dual);
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = CreationUtils.createUniformCostSearch2(theory, dual);
                            //ProbabilityTableModel mo = new ProbabilityTableModel();
                            HashMap<ManchesterOWLSyntax, BigDecimal> map = CommonUtils.getProbabMap();

                            String path = ClassLoader.getSystemResource("alignment/evaluation/"
                                    + m.trim()
                                    + "-incoherent-evaluation/"
                                    + o.trim()
                                    + ".txt").getPath();

                            OWLTheory theory2 = CreationUtils.createOWLTheory2(ontology, dual);
                            OWLTheory t3 = CreationUtils.createOWLTheory2(ontology, dual);
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search2 = CreationUtils.createUniformCostSearch2(theory2, dual);
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
                                    logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                } catch (NoConflictException e) {
                                    logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                } catch (InconsistentTheoryException e) {
                                    logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                }

                                Set<AxiomSet<OWLLogicalAxiom>> diagnoses =
                                        Collections.unmodifiableSet(search.getDiagnoses());
                                search.reset();
                                AxiomSet<OWLLogicalAxiom> targD = OAEI08Utils.getTargetDiag2(diagnoses, es, m);
                                targetDg = new LinkedHashSet<OWLLogicalAxiom>();
                                for (OWLLogicalAxiom axiom : targD)
                                    targetDg.add(axiom);
                            }

                            if (targetSource == SimulatedSession.TargetSource.FROM_FILE)
                                targetDg = OAEI08Utils.getDiagnosis2(targetAxioms, ontology);

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

        Properties properties = OAEI08Utils.readProps();
        Map<String, List<String>> mapOntos = OAEI08Utils.readOntologiesFromFile(properties);
        boolean background_add = false;
        session.setShowElRates(false);
        SimulatedSession.QSSType[] qssTypes = new SimulatedSession.QSSType[]{SimulatedSession.QSSType.MINSCORE, SimulatedSession.QSSType.SPLITINHALF, SimulatedSession.QSSType.DYNAMICRISK};
        for (boolean dual : new boolean[]{false}) {
            for (SimulatedSession.TargetSource targetSource : new SimulatedSession.TargetSource[]{SimulatedSession.TargetSource.FROM_FILE}) {
                for (String m : mapOntos.keySet()) {
                    for (String o : mapOntos.get(m)) {
                        String out = "STAT, " + m + ", " + o;
                        for (SimulatedSession.QSSType type : qssTypes) {
                            //String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
                            String[] targetAxioms = OAEI08Utils.getDiagnosis(m, o);
                            OWLOntology ontology = CreationUtils.createOwlOntology2(m.trim(), o.trim());
                            Set<OWLLogicalAxiom> targetDg;
                            OWLTheory theory = CreationUtils.createOWLTheory(ontology, dual);
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = CreationUtils.createUniformCostSearch(theory, dual);
                            //ProbabilityTableModel mo = new ProbabilityTableModel();
                            if (background_add) {
                                OWLOntology ontology1 = CreationUtils.createOwlOntology(o.split("-")[0].trim());
                                OWLOntology ontology2 = CreationUtils.createOwlOntology(o.split("-")[1].trim());
                                theory.addBackgroundFormulas(ontology1.getLogicalAxioms());
                                theory.addBackgroundFormulas(ontology2.getLogicalAxioms());
                            }
                            HashMap<ManchesterOWLSyntax, BigDecimal> map = CommonUtils.getProbabMap();

                            String path = ClassLoader.getSystemResource("alignment/evaluation/"
                                    + m.trim()
                                    + "-incoherent-evaluation/"
                                    + o.trim()
                                    + ".txt").getPath();

                            OWLTheory theory2 = CreationUtils.createOWLTheory(ontology, dual);
                            OWLTheory t3 = CreationUtils.createOWLTheory(ontology, dual);
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search2 = CreationUtils.createUniformCostSearch(theory2, dual);
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
        Properties properties = OAEI08Utils.readProps2("alignment/alignment.properties");
        Map<String, List<String>> mapOntos = OAEI08Utils.readOntologiesFromFile2(properties);

        SimulatedSession.QSSType[] qssTypes = new SimulatedSession.QSSType[]{SimulatedSession.QSSType.MINSCORE, SimulatedSession.QSSType.SPLITINHALF, SimulatedSession.QSSType.DYNAMICRISK};
        SimulatedSession.TargetSource[] targetSources = new SimulatedSession.TargetSource[]{SimulatedSession.TargetSource.FROM_FILE};


        for (String m : mapOntos.keySet()) {
            for (String o : mapOntos.get(m)) {
                for (SimulatedSession.TargetSource targetSource : targetSources) {
                    for (SimulatedSession.QSSType type : qssTypes) {
                        CommonUtils.BackgroundO[] backgr = new CommonUtils.BackgroundO[]{CommonUtils.BackgroundO.EMPTY, CommonUtils.BackgroundO.O1_O2};
                        for (CommonUtils.BackgroundO background : backgr) {

                            SimulatedSession s = new SimulatedSession();
                            s.setNumberOfHittingSets(4);
                            String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
                            OWLOntology ontology = CreationUtils.createOwlOntology2(m.trim(), o.trim());
                            Set<OWLLogicalAxiom> targetDg;
                            OWLTheory theory = CreationUtils.createOWLTheory2(ontology, false);
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = CreationUtils.createUniformCostSearch2(theory, false);
                            //ProbabilityTableModel mo = new ProbabilityTableModel();
                            HashMap<ManchesterOWLSyntax, BigDecimal> map = CommonUtils.getProbabMap();

                            String path = ClassLoader.getSystemResource("alignment/evaluation/"
                                    + m.trim()
                                    + "-incoherent-evaluation/"
                                    + o.trim()
                                    + ".txt").getPath();

                            OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, path);
                            OWLOntology ontology1 = CreationUtils.createOwlOntology(o.split("-")[0].trim());
                            OWLOntology ontology2 = CreationUtils.createOwlOntology(o.split("-")[1].trim());
                            if (background == CommonUtils.BackgroundO.O1_O2) {
                                theory.addBackgroundFormulas(ontology1.getLogicalAxioms());
                                theory.addBackgroundFormulas(ontology2.getLogicalAxioms());
                            }
                            //es.updateKeywordProb(map);
                            targetDg = null;

                            search.setCostsEstimator(es);
                            if (targetSource == SimulatedSession.TargetSource.FROM_30_DIAGS) {
                                CommonUtils.run(search, 30);

                                Set<AxiomSet<OWLLogicalAxiom>> diagnoses =
                                        Collections.unmodifiableSet(search.getDiagnoses());
                                search.reset();
                                AxiomSet<OWLLogicalAxiom> targD = OAEI08Utils.getTargetDiag2(diagnoses, es, m);
                                targetDg = new LinkedHashSet<OWLLogicalAxiom>();
                                for (OWLLogicalAxiom axiom : targD)
                                    targetDg.add(axiom);
                            }

                            if (targetSource == SimulatedSession.TargetSource.FROM_FILE)
                                targetDg = OAEI08Utils.getDiagnosis2(targetAxioms, ontology);

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

        Properties properties = OAEI08Utils.readProps2("alignment/alignment.properties");
        SimulatedSession.QSSType[] qssTypes = new SimulatedSession.QSSType[]{SimulatedSession.QSSType.MINSCORE, SimulatedSession.QSSType.SPLITINHALF, SimulatedSession.QSSType.DYNAMICRISK};
        String m = "owlctxmatch";
        String o = "SIGKDD-EKAW";
        SimulatedSession.TargetSource targetSource = SimulatedSession.TargetSource.FROM_FILE;
        SimulatedSession.QSSType type = SimulatedSession.QSSType.SPLITINHALF;
        String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
        OWLOntology ontology = CreationUtils.createOwlOntology2(m.trim(), o.trim());
        Set<OWLLogicalAxiom> targetDg;
        OWLTheory theory = CreationUtils.createOWLTheory2(ontology, false);
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = CreationUtils.createUniformCostSearch2(theory, false);
        OWLOntology ontology1 = CreationUtils.createOwlOntology(o.split("-")[0].trim());
        OWLOntology ontology2 = CreationUtils.createOwlOntology(o.split("-")[1].trim());
        //theory.addBackgroundFormulas(ontology1.getLogicalAxioms());
        //theory.addBackgroundFormulas(ontology2.getLogicalAxioms());
        //ProbabilityTableModel mo = new ProbabilityTableModel();
        HashMap<ManchesterOWLSyntax, BigDecimal> map = CommonUtils.getProbabMap();

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
            Set<AxiomSet<OWLLogicalAxiom>> diagnoses = CommonUtils.run(search, 30);
            search.reset();
            AxiomSet<OWLLogicalAxiom> targD = OAEI08Utils.getTargetDiag2(diagnoses, es, m);
            targetDg = new LinkedHashSet<OWLLogicalAxiom>();
            for (OWLLogicalAxiom axiom : targD)
                targetDg.add(axiom);
        }

        if (targetSource == SimulatedSession.TargetSource.FROM_FILE) {
            targetDg = OAEI08Utils.getDiagnosis2(targetAxioms, ontology);
            Set<AxiomSet<OWLLogicalAxiom>> diagnoses = CommonUtils.run(search, -1);
            Assert.assertTrue(diagnoses.contains(targetDg));
            search.reset();
        }

        TableList e = new TableList();
        String message = "act " + m + " - " + o + " - " + targetSource + " " + type;
        s.simulateQuerySession(search, theory, targetDg, e, type, message, null, null, null);
    }

    @Test
    public void doQueryEliminationRateTest() throws SolverException, InconsistentTheoryException, IOException {
        Properties properties = OAEI08Utils.readProps2("alignment/alignment.properties");
        Map<String, List<String>> mapOntos = OAEI08Utils.readOntologiesFromFile2(properties);

        SimulatedSession.QSSType[] qssTypes = new SimulatedSession.QSSType[]{SimulatedSession.QSSType.SPLITINHALF,};
        for (boolean dual : new boolean[]{false}) {
            for (SimulatedSession.TargetSource targetSource : new SimulatedSession.TargetSource[]{SimulatedSession.TargetSource.FROM_FILE}) {
                for (String m : new String[]{"coma"}) {
                    for (String o : new String[]{"CRS-EKAW"}) {
                        String out = "STAT, " + m + ", " + o;
                        for (SimulatedSession.QSSType type : qssTypes) {
                            SimulatedSession s = new SimulatedSession();
                            String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
                            OWLOntology ontology = CreationUtils.createOwlOntology2(m.trim(), o.trim());
                            Set<OWLLogicalAxiom> targetDg;
                            OWLTheory theory = CreationUtils.createOWLTheory2(ontology, dual);
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = CreationUtils.createUniformCostSearch2(theory, dual);
                            //ProbabilityTableModel mo = new ProbabilityTableModel();
                            HashMap<ManchesterOWLSyntax, BigDecimal> map = CommonUtils.getProbabMap();

                            String path = ClassLoader.getSystemResource("alignment/evaluation/"
                                    + m.trim()
                                    + "-incoherent-evaluation/"
                                    + o.trim()
                                    + ".txt").getPath();

                            OWLTheory theory2 = CreationUtils.createOWLTheory2(ontology, dual);
                            OWLTheory t3 = CreationUtils.createOWLTheory2(ontology, dual);
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search2 = CreationUtils.createUniformCostSearch2(theory2, dual);
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
                                AxiomSet<OWLLogicalAxiom> targD = OAEI08Utils.getTargetDiag2(diagnoses, es, m);
                                targetDg = new LinkedHashSet<OWLLogicalAxiom>();
                                for (OWLLogicalAxiom axiom : targD)
                                    targetDg.add(axiom);
                            }

                            if (targetSource == SimulatedSession.TargetSource.FROM_FILE)
                                targetDg = OAEI08Utils.getDiagnosis2(targetAxioms, ontology);

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

}
