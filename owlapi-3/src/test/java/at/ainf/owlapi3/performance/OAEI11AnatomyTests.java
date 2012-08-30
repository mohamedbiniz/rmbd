package at.ainf.owlapi3.performance;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.owlapi3.base.OAEI11AnatomySession;
import at.ainf.owlapi3.base.SimulatedSession;
import at.ainf.owlapi3.costestimation.OWLAxiomCostsEstimator;
import at.ainf.owlapi3.model.OWLIncoherencyExtractor;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.base.tools.TableList;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 06.08.12
 * Time: 08:03
 * To change this template use File | Settings | File Templates.
 */
public class OAEI11AnatomyTests extends OAEI11AnatomySession {

    private static Logger logger = LoggerFactory.getLogger(OAEI11AnatomyTests.class.getName());

    @Test
    public void doTestAroma() {

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

                            OWLOntology ontology = getOntology(file);

                            Set<OWLLogicalAxiom> targetDg;
                            long preprocessModulExtract = System.currentTimeMillis();
                            ontology = new OWLIncoherencyExtractor(
                                    new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
                            preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;
                            OWLTheory theory = getExtendTheory(ontology, dual);
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = getUniformCostSearch(theory, dual);

                            LinkedHashSet<OWLLogicalAxiom> bx = new LinkedHashSet<OWLLogicalAxiom>();
                            Set<OWLLogicalAxiom> r = new LinkedHashSet<OWLLogicalAxiom>();

                            OWLOntology mouse = getOntologySimple("oaei11/mouse.owl");
                            OWLOntology human = getOntologySimple("oaei11/human.owl");

                            r.addAll(mouse.getLogicalAxioms());
                            r.addAll(human.getLogicalAxioms());

                            bx.addAll(r);
                            bx.retainAll(theory.getOriginalOntology().getLogicalAxioms());
                            try {
                                theory.addBackgroundFormulas(bx);
                            } catch (InconsistentTheoryException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            } catch (SolverException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }

                            //ProbabilityTableModel mo = new ProbabilityTableModel();


                            String path = ClassLoader.getSystemResource("oaei11/" + file + ".txt").getPath();

                            OWLAxiomCostsEstimator es = null;
                            try {
                                es = new OWLAxiomCostsEstimator(theory, path);
                            } catch (IOException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }


                            targetDg = null;

                            search.setCostsEstimator(es);

                            Set<AxiomSet<OWLLogicalAxiom>> allD = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>(search.getDiagnoses());
                            search.reset();


                            if (targetSource == SimulatedSession.TargetSource.FROM_FILE)
                                targetDg = getDiagnosisTarget(file);

                            TableList e = new TableList();
                            out += "," + type + ",";
                            String message = "act," + file + "," + targetSource + "," + type + "," + dual + "," + background + "," + preprocessModulExtract;
                            //out += simulateQuerySession(search, theory, diags, e, type, message, allD, search2, t3);

                            session.setEntry(e);
                            session.setMessage(message);
                            session.setScoringFunct(type);
                            session.setTargetD(targetDg);
                            session.setTheory(theory);
                            session.setSearch(search);
                            out += session.simulateQuerySession();

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

                            OWLOntology ontology = getOntology(file);

                            Set<OWLLogicalAxiom> targetDg;
                            long preprocessModulExtract = System.currentTimeMillis();
                            ontology = new OWLIncoherencyExtractor(
                                    new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
                            preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;
                            OWLTheory theory = getExtendTheory(ontology, dual);
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = getUniformCostSearch(theory, dual);

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

                            String path = ClassLoader.getSystemResource("oaei11/" + file + ".txt").getPath();

                            OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, path);


                            targetDg = null;

                            search.setCostsEstimator(es);

                            Set<AxiomSet<OWLLogicalAxiom>> allD = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>(search.getDiagnoses());
                            search.reset();


                            if (targetSource == SimulatedSession.TargetSource.FROM_FILE)
                                targetDg = getDiagnosisTarget(file);

                            TableList e = new TableList();
                            out += "," + type + ",";
                            String message = "act," + file + "," + targetSource + "," + type + "," + dual + "," + background + "," + preprocessModulExtract;
                            //out += simulateQuerySession(search, theory, diags, e, type, message, allD, search2, t3);

                            session.setEntry(e);
                            session.setMessage(message);
                            session.setScoringFunct(type);
                            session.setTargetD(targetDg);
                            session.setTheory(theory);
                            session.setSearch(search);
                            out += session.simulateQuerySession();

                        }
                        logger.info(out);

                    }
                }
            }
        }
    }

}
