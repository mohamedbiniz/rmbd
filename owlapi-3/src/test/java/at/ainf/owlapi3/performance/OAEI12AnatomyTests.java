package at.ainf.owlapi3.performance;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.owlapi3.base.OAEI11AnatomySession;
import at.ainf.owlapi3.base.OAEI11ConferenceSession;
import at.ainf.owlapi3.base.SimulatedSession;
import at.ainf.owlapi3.base.mappings.XmlFormatMappingsReader;
import at.ainf.owlapi3.base.tools.TableList;
import at.ainf.owlapi3.costestimation.OWLAxiomCostsEstimator;
import at.ainf.owlapi3.model.OWLIncoherencyExtractor;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.test.XmlMappingsReaderTest;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 17.07.13
 * Time: 10:20
 * To change this template use File | Settings | File Templates.
 */
public class OAEI12AnatomyTests extends OAEI11AnatomySession {

    private static Logger logger = LoggerFactory.getLogger(OAEI11AnatomyTests.class.getName());

    @Test
    public void doTestsOAEI12AnatomyTrack()

            throws SolverException, InconsistentTheoryException, IOException, OWLOntologyCreationException {

        SimulatedSession session = new SimulatedSession();
        session.setShowElRates(false);

        //String[] files =
        //        new String[]{ "AROMA", "CODI", "GO2A", "GOMMA", "GOMMA-bk", "Hertuda", "HotMatch",
        //                     "LogMap", "LogMapLt", "MaasMatch", "MapSSS", "Optima", "ServOMap", "ServOMapL",
        //                     "TOAST", "WeSeE", "Wmatch", "YAM++"};

        // "GO2A"

        String[] files = new String[]{"Hertuda", "HotMatch",
                        "LogMap", "LogMapLt", "MaasMatch", "MapSSS", "Optima", "ServOMap", "ServOMapL",
                        "TOAST", "WeSeE", "Wmatch", "YAM++"};

        // SimulatedSession.QSSType[] qssTypes = new SimulatedSession.QSSType[]
        //         { SimulatedSession.QSSType.MINSCORE, SimulatedSession.QSSType.SPLITINHALF, SimulatedSession.QSSType.DYNAMICRISK };
        SimulatedSession.QSSType[] qssTypes = new SimulatedSession.QSSType[] { SimulatedSession.QSSType.MINSCORE };
        for (boolean dual : new boolean[] {false}) {
            for (boolean background : new boolean[]{false}) {
                for (SimulatedSession.TargetSource targetSource : new SimulatedSession.TargetSource[]{SimulatedSession.TargetSource.FROM_FILE}) {
                    for (String file : files) {

                        String out = "STAT, " + file;
                        for (SimulatedSession.QSSType type : qssTypes) {

                            //String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");

                            //String[] targetAxioms = AlignmentUtils.getDiagnosis2(m,o);
                            //OWLOntology ontology = createOwlOntology2(m.trim(), o.trim());

                            String matcherFileSuffix = "-anatomy-track1.rdf";
                            OWLOntology ontology = getOntology(file + matcherFileSuffix, "oaei12anatomy", "");

                            Set<OWLLogicalAxiom> targetDg;
                            long preprocessModulExtract = System.currentTimeMillis();
                            ontology = new OWLIncoherencyExtractor(
                                    new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
                            preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;
                            OWLTheory theory = getExtendTheory(ontology, dual);
                            TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = getUniformCostSearch(theory, dual);

                            LinkedHashSet<OWLLogicalAxiom> bx = new LinkedHashSet<OWLLogicalAxiom>();
                            Set<OWLLogicalAxiom> r = new LinkedHashSet<OWLLogicalAxiom>();

                            OWLOntologyManager man = OWLManager.createOWLOntologyManager();
                            InputStream st = ClassLoader.getSystemResourceAsStream("oaei12anatomy/mouse.owl");
                            OWLOntology mouse = man.loadOntologyFromOntologyDocument(st);
                            st = ClassLoader.getSystemResourceAsStream("oaei12anatomy/human.owl");
                            OWLOntology human = man.loadOntologyFromOntologyDocument(st);

                            r.addAll(mouse.getLogicalAxioms());
                            r.addAll(human.getLogicalAxioms());

                            bx.addAll(r);
                            bx.retainAll(theory.getOriginalOntology().getLogicalAxioms());
                            if (background) theory.getKnowledgeBase().addBackgroundFormulas(bx);

                            //ProbabilityTableModel mo = new ProbabilityTableModel();

                            String path = ClassLoader.getSystemResource("oaei12anatomy/"
                                    + file + matcherFileSuffix).getPath();

                            OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, path, true);


                            targetDg = null;

                            search.setCostsEstimator(es);

                            Set<FormulaSet<OWLLogicalAxiom>> allD = new LinkedHashSet<FormulaSet<OWLLogicalAxiom>>(search.getDiagnoses());
                            search.reset();


                            if (targetSource == SimulatedSession.TargetSource.FROM_FILE) {
                                String mappings = "oaei12anatomy/" + file + "-anatomy-track1.rdf";
                                String reference = "oaei12anatomy/reference.rdf";
                                XmlFormatMappingsReader mappingsReader = new XmlFormatMappingsReader(mappings, reference);
                                targetDg = new HashSet<OWLLogicalAxiom>(mappingsReader.getIncorrectMappings().keySet());
                                //targetDg = getDiagnosisTarget(file, "oaei12anatomy", "-anatomy-track1.rdf");
                            }

                            TableList e = new TableList();
                            out += "," + type + ",";
                            String message = "act," + file + "," + targetSource + "," + type + "," + dual + "," + background + "," + preprocessModulExtract;
                            //out += simulateQuerySession(start, theory, diags, e, type, message, allD, search2, t3);

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

    @Override
    public void readDataOAEI(String filename, Map<OWLLogicalAxiom, Double> axioms, Set<OWLLogicalAxiom> targetDiag, OWLOntologyManager man) throws IOException {
        String path = new File(filename).getParentFile().getName();
        String file = new File(filename).getName();
        Map<OWLLogicalAxiom,BigDecimal> mappings = new OAEI11ConferenceSession().readRdfMapping(path, file);
        for (Map.Entry<OWLLogicalAxiom,BigDecimal> entry : mappings.entrySet())
            axioms.put(entry.getKey(),entry.getValue().doubleValue());
    }

}
