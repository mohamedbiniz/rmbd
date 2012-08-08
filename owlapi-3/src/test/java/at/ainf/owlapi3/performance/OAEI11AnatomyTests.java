package at.ainf.owlapi3.performance;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.owlapi3.costestimation.OWLAxiomCostsEstimator;
import at.ainf.owlapi3.model.OWLIncoherencyExtractor;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.performance.table.TableList;
import at.ainf.owlapi3.utils.ProbabMapCreator;
import at.ainf.owlapi3.utils.creation.target.OAEI11AnatomyTargetProvider;
import at.ainf.owlapi3.utils.session.SimulatedSession;
import at.ainf.owlapi3.utils.creation.ontology.OAEI11AnatomyOntologyCreator;
import at.ainf.owlapi3.utils.creation.search.UniformCostSearchCreator;
import at.ainf.owlapi3.utils.creation.theory.BackgroundExtendedTheoryCreator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 06.08.12
 * Time: 08:03
 * To change this template use File | Settings | File Templates.
 */
public class OAEI11AnatomyTests {

    private static Logger logger = Logger.getLogger(OAEI11AnatomyTests.class.getName());

    @BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlapi3-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }

    @Test
    public void doTestAroma()

            throws SolverException, InconsistentTheoryException, IOException, OWLOntologyCreationException {

        SimulatedSession session = new SimulatedSession();


        session.setShowElRates(false);

        String[] files =
                new String[]{"Aroma"};
        //String[] files = new String[]{"Aroma"};

        SimulatedSession.QSSType[] qssTypes = new SimulatedSession.QSSType[]
                {SimulatedSession.QSSType.MINSCORE, SimulatedSession.QSSType.SPLITINHALF,
                        SimulatedSession.QSSType.DYNAMICRISK};
        for (boolean dual : new boolean[]{true}) {
            for (boolean background : new boolean[]{true}) {
                for (SimulatedSession.TargetSource targetSource : new SimulatedSession.TargetSource[]{SimulatedSession.TargetSource.FROM_FILE}) {
                    for (String file : files) {

                        String out = "STAT, " + file;
                        for (SimulatedSession.QSSType type : qssTypes) {

                            //String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");

                            //String[] targetAxioms = AlignmentUtils.getDiagnosis2(m,o);
                            //OWLOntology ontology = createOwlOntology2(m.trim(), o.trim());

                            OWLOntology ontology = new OAEI11AnatomyOntologyCreator(file).getOntology();

                            Set<OWLLogicalAxiom> targetDg;
                            long preprocessModulExtract = System.currentTimeMillis();
                            ontology = new OWLIncoherencyExtractor(
                                    new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
                            preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;
                            OWLTheory theory = new BackgroundExtendedTheoryCreator(ontology, dual).getTheory();
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new UniformCostSearchCreator(theory, dual).getSearch();

                            LinkedHashSet<OWLLogicalAxiom> bx = new LinkedHashSet<OWLLogicalAxiom>();
                            Set<OWLLogicalAxiom> r = new LinkedHashSet<OWLLogicalAxiom>();

                            OWLOntologyManager man = OWLManager.createOWLOntologyManager();
                            InputStream st = ClassLoader.getSystemResourceAsStream("oaei11/mouse.owl");
                            OWLOntology mouse = man.loadOntologyFromOntologyDocument(st);
                            st = ClassLoader.getSystemResourceAsStream("oaei11/human.owl");
                            OWLOntology human = man.loadOntologyFromOntologyDocument(st);

                            r.addAll(mouse.getLogicalAxioms());
                            r.addAll(human.getLogicalAxioms());

                            bx.addAll(r);
                            bx.retainAll(theory.getOriginalOntology().getLogicalAxioms());
                            theory.addBackgroundFormulas(bx);

                            //ProbabilityTableModel mo = new ProbabilityTableModel();
                            HashMap<ManchesterOWLSyntax, BigDecimal> map = ProbabMapCreator.getProbabMap();

                            String path = ClassLoader.getSystemResource("oaei11/" + file + ".txt").getPath();

                            OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, path);


                            targetDg = null;

                            search.setCostsEstimator(es);

                            Set<AxiomSet<OWLLogicalAxiom>> allD = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>(search.getDiagnoses());
                            search.reset();


                            if (targetSource == SimulatedSession.TargetSource.FROM_FILE)
                                targetDg = new OAEI11AnatomyTargetProvider(file).getDiagnosisTarget();

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
        session.setShowElRates(false);

        String[] files =
        new String[]{"AgrMaker", "GOMMA-bk", "GOMMA-nobk", "Lily", "LogMap", "LogMapLt", "MapSSS"};

        SimulatedSession.QSSType[] qssTypes = new SimulatedSession.QSSType[]
                { SimulatedSession.QSSType.MINSCORE, SimulatedSession.QSSType.SPLITINHALF, SimulatedSession.QSSType.DYNAMICRISK };
        for (boolean dual : new boolean[] {false}) {
            for (boolean background : new boolean[]{false}) {
                for (SimulatedSession.TargetSource targetSource : new SimulatedSession.TargetSource[]{SimulatedSession.TargetSource.FROM_FILE}) {
                    for (String file : files) {

                        String out = "STAT, " + file;
                        for (SimulatedSession.QSSType type : qssTypes) {

                            //String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");

                            //String[] targetAxioms = AlignmentUtils.getDiagnosis2(m,o);
                            //OWLOntology ontology = createOwlOntology2(m.trim(), o.trim());

                            OWLOntology ontology = new OAEI11AnatomyOntologyCreator(file).getOntology();

                            Set<OWLLogicalAxiom> targetDg;
                            long preprocessModulExtract = System.currentTimeMillis();
                            ontology = new OWLIncoherencyExtractor(
                                    new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
                            preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;
                            OWLTheory theory = new BackgroundExtendedTheoryCreator(ontology, dual).getTheory();
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new UniformCostSearchCreator(theory, dual).getSearch();

                            LinkedHashSet<OWLLogicalAxiom> bx = new LinkedHashSet<OWLLogicalAxiom>();
                            Set<OWLLogicalAxiom> r = new LinkedHashSet<OWLLogicalAxiom>();

                            OWLOntologyManager man = OWLManager.createOWLOntologyManager();
                            InputStream st = ClassLoader.getSystemResourceAsStream("oaei11/mouse.owl");
                            OWLOntology mouse = man.loadOntologyFromOntologyDocument(st);
                            st = ClassLoader.getSystemResourceAsStream("oaei11/human.owl");
                            OWLOntology human = man.loadOntologyFromOntologyDocument(st);

                            r.addAll(mouse.getLogicalAxioms());
                            r.addAll(human.getLogicalAxioms());

                            bx.addAll(r);
                            bx.retainAll(theory.getOriginalOntology().getLogicalAxioms());
                            if (background) theory.addBackgroundFormulas(bx);

                            //ProbabilityTableModel mo = new ProbabilityTableModel();
                            HashMap<ManchesterOWLSyntax, BigDecimal> map = ProbabMapCreator.getProbabMap();

                            String path = ClassLoader.getSystemResource("oaei11/" + file + ".txt").getPath();

                            OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, path);


                            targetDg = null;

                            search.setCostsEstimator(es);

                            Set<AxiomSet<OWLLogicalAxiom>> allD = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>(search.getDiagnoses());
                            search.reset();


                            if (targetSource == SimulatedSession.TargetSource.FROM_FILE)
                                targetDg = new OAEI11AnatomyTargetProvider(file).getDiagnosisTarget();

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

}
