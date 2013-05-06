package at.ainf.owlapi3.test;

import at.ainf.diagnosis.Speed4JMeasurement;
import at.ainf.owlapi3.module.OtfModuleProvider;
import at.ainf.owlapi3.module.iterative.*;
import at.ainf.owlapi3.module.iterative.diag.IterativeModuleDiagnosis;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import java.io.File;
import java.io.InputStream;
import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 06.03.13
 * Time: 10:06
 * To change this template use File | Settings | File Templates.
 */
public class IterativeModuleDiagTests {

    private static Logger logger = LoggerFactory.getLogger(IterativeModuleDiagTests.class.getName());

    protected Set<OWLLogicalAxiom> getAxioms (String filename) {

        InputStream stream = ClassLoader.getSystemResourceAsStream(filename);
        OWLOntology ontology = null;
        try {
            ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(stream);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return ontology.getLogicalAxioms();
    }

    protected OWLOntology createOntology (Set<? extends OWLAxiom> axioms) {
        OWLOntology debuggingOntology = null;
        try {
            debuggingOntology = OWLManager.createOWLOntologyManager().createOntology();
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        debuggingOntology.getOWLOntologyManager().addAxioms(debuggingOntology,axioms);
        return debuggingOntology;
    }

    @Test
    public void snomed2nci() throws OWLOntologyCreationException {

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        File onto1File = new File(System.getenv("bigontosdir") + "/oaei2012_SNOMED_extended_overlapping_fma_nci.owl");
        Set<OWLLogicalAxiom> onto1Ax = man.loadOntologyFromOntologyDocument(onto1File).getLogicalAxioms();
        File onto2File = new File(System.getenv("bigontosdir") + "/oaei2012_NCI_whole_ontology.owl");
        Set<OWLLogicalAxiom> onto2Ax = man.loadOntologyFromOntologyDocument(onto2File).getLogicalAxioms();
        File mappingsFile = new File(System.getenv("bigontosdir") + "/onto_mappings_SNOMED_NCI_cleanDG.txt");
        Set<OWLLogicalAxiom> gs = new HashSet<OWLLogicalAxiom>(new ModuleTargetDiagSearcher(mappingsFile.getPath(),null).getGSMappings());

        ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
        Set<OWLLogicalAxiom> ontoAxioms = new LinkedHashSet<OWLLogicalAxiom>();
        ontoAxioms.addAll(onto1Ax);
        ontoAxioms.addAll(onto2Ax);
        Set<OWLLogicalAxiom> allAxioms = new HashSet<OWLLogicalAxiom>();
        allAxioms.addAll(ontoAxioms);
        allAxioms.addAll(gs);

        IterativeModuleDiagnosis diagnosisFinder = new IterativeModuleDiagnosis(gs, ontoAxioms,
                new Reasoner.ReasonerFactory(), new ModuleMinDiagSearcher(), false);
        Set<OWLLogicalAxiom> diagnosis = diagnosisFinder.calculateTargetDiagnosis();

        for (OWLLogicalAxiom axiom : diagnosis)
            logger.info("" + axiom);

        logger.info("");

    }

    @Test
    public void fma2snomed() throws OWLOntologyCreationException {

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        File onto1File = new File(System.getenv("bigontosdir") + "/oaei2012_FMA_whole_ontology.owl");
        Set<OWLLogicalAxiom> onto1Ax = man.loadOntologyFromOntologyDocument(onto1File).getLogicalAxioms();
        File onto2File = new File(System.getenv("bigontosdir") + "/oaei2012_SNOMED_extended_overlapping_fma_nci.owl");
        Set<OWLLogicalAxiom> onto2Ax = man.loadOntologyFromOntologyDocument(onto2File).getLogicalAxioms();
        File mappingsFile = new File(System.getenv("bigontosdir") + "/onto_mappings_SNOMED_NCI_cleanDG_rmbd.txt");
        Set<OWLLogicalAxiom> gs = new HashSet<OWLLogicalAxiom>(new ModuleTargetDiagSearcher(mappingsFile.getPath()).getGSMappings());

        ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
        Set<OWLLogicalAxiom> ontoAxioms = new LinkedHashSet<OWLLogicalAxiom>();
        ontoAxioms.addAll(onto1Ax);
        ontoAxioms.addAll(onto2Ax);
        Set<OWLLogicalAxiom> allAxioms = new HashSet<OWLLogicalAxiom>();
        allAxioms.addAll(ontoAxioms);
        allAxioms.addAll(gs);

        IterativeModuleDiagnosis diagnosisFinder = new IterativeModuleDiagnosis(gs, ontoAxioms,
                new Reasoner.ReasonerFactory(), new ModuleMinDiagSearcher(), true);
        diagnosisFinder.calculateTargetDiagnosis();

        logger.info("");

    }

    @Test
    public void fma2nci() throws OWLOntologyCreationException {

        String onto = "fma2nci";
        ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
        Set<OWLLogicalAxiom> onto1Axioms = getAxioms("ontologies/" + onto + "genonto1.owl");
        Set<OWLLogicalAxiom> onto2Axioms = getAxioms("ontologies/" + onto + "genonto2.owl");
        String fileMappings = "onto_mappings_FMA_NCI_cleanDG_rmbd";
        String path= ClassLoader.getSystemResource("ontologies/" + fileMappings + ".txt").getPath();
        Set<OWLLogicalAxiom> gs = new HashSet<OWLLogicalAxiom>(new ModuleTargetDiagSearcher(path).getGSMappings());


        Set<OWLLogicalAxiom> ontoAxioms = new LinkedHashSet<OWLLogicalAxiom>();
        ontoAxioms.addAll(onto1Axioms);
        ontoAxioms.addAll(onto2Axioms);
        Set<OWLLogicalAxiom> allAxioms = new HashSet<OWLLogicalAxiom>();
        allAxioms.addAll(ontoAxioms);
        allAxioms.addAll(gs);

        IterativeModuleDiagnosis diagnosisFinder = new IterativeModuleDiagnosis(gs, ontoAxioms,
                new Reasoner.ReasonerFactory(), new ModuleMinDiagSearcher(), true);
        diagnosisFinder.calculateTargetDiagnosis();

        logger.info("");

    }

    @Test
    public void fma2nciModules() throws OWLOntologyCreationException {

        String onto = "fma2nci";
        ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
        Set<OWLLogicalAxiom> onto1Axioms = getAxioms("ontologies/" + onto + "genonto1.owl");
        Set<OWLLogicalAxiom> onto2Axioms = getAxioms("ontologies/" + onto + "genonto2.owl");
        Set<OWLLogicalAxiom> mappingAxioms = getAxioms("ontologies/" + onto + "genmapp.owl");


        Set<OWLLogicalAxiom> ontoAxioms = new LinkedHashSet<OWLLogicalAxiom>();
        ontoAxioms.addAll(onto1Axioms);
        ontoAxioms.addAll(onto2Axioms);
        Set<OWLLogicalAxiom> allAxioms = new HashSet<OWLLogicalAxiom>();
        allAxioms.addAll(ontoAxioms);
        allAxioms.addAll(mappingAxioms);

        OtfModuleProvider provider =
                new OtfModuleProvider(createOntology(allAxioms),new Reasoner.ReasonerFactory(),false);
        provider.getModuleUnsatClass();
        Map<OWLClass, Set<OWLLogicalAxiom>> unsatClasses = provider.getUnsatClasses();

        logger.info("" + unsatClasses);


    }

    @Test
    public void testGS_Reader() {
        String file = "mouse2human_reference_2011";
        String path = ClassLoader.getSystemResource("ontologies/" + file + ".txt").getPath();
        GS_MappingsReader reader = new GS_MappingsReader();
        Set<OWLLogicalAxiom> axioms = reader.loadGSmappings(path);

        logger.info(axioms.toString() + "");
    }

    @Test
    public void testIterativeDiagnosis() throws OWLOntologyCreationException {

        String onto = "mouse2human";
        ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
        Set<OWLLogicalAxiom> onto1Axioms = getAxioms("ontologies/" + onto + "genonto1.owl");
        Set<OWLLogicalAxiom> onto2Axioms = getAxioms("ontologies/" + onto + "genonto2.owl");
        Set<OWLLogicalAxiom> mappingAxioms = getAxioms("ontologies/" + onto + "genmapp.owl");
        //String fileMappings = "mouse2human_reference_2011";
        String fileMappings = "onto_mappings_FMA_NCI_cleanDG_rmbd";

        Set<OWLLogicalAxiom> ontoAxioms = new HashSet<OWLLogicalAxiom>();
        ontoAxioms.addAll(onto1Axioms);
        ontoAxioms.addAll(onto2Axioms);
        String pathMappings = ClassLoader.getSystemResource("ontologies/" + fileMappings + ".txt").getPath();

        Set<OWLLogicalAxiom> correctAxioms = new HashSet<OWLLogicalAxiom>();
        Set<OWLLogicalAxiom> gsMappings = new ModuleTargetDiagSearcher(pathMappings).getGSMappings();
        correctAxioms.addAll(ontoAxioms);
        correctAxioms.addAll(gsMappings);
        Set<OWLLogicalAxiom> falseAxioms = new HashSet<OWLLogicalAxiom>(mappingAxioms);
        falseAxioms.removeAll(correctAxioms);

        ModuleDiagSearcher d = new ModuleMinDiagSearcher();
        //ModuleDiagSearcher d = new ModuleTargetDiagSearcher(pathMappings);
        //ModuleDiagSearcher d = new ModuleQuerDiagSearcher(pathMappings,correctAxioms,falseAxioms, false);

        long time = System.currentTimeMillis();
        IterativeModuleDiagnosis diagnosisFinder = new IterativeModuleDiagnosis(mappingAxioms, ontoAxioms,
                                                         new Reasoner.ReasonerFactory(), d, true);

        Set<OWLLogicalAxiom> targetDiagnosis = diagnosisFinder.calculateTargetDiagnosis();
        time = System.currentTimeMillis() - time;

        Set<OWLLogicalAxiom> repaired = new HashSet<OWLLogicalAxiom>();
        repaired.addAll(ontoAxioms);
        repaired.addAll(mappingAxioms);
        repaired.removeAll(targetDiagnosis);

        OWLReasoner reasoner = new Reasoner.ReasonerFactory().createNonBufferingReasoner(createOntology(repaired));
        Set<OWLLogicalAxiom> ontoAxInDiag = new HashSet<OWLLogicalAxiom>(ontoAxioms);
        ontoAxInDiag.retainAll(targetDiagnosis);
        assertTrue(ontoAxInDiag.isEmpty());
        Set<OWLLogicalAxiom> gsMappingAxInDiag = new HashSet<OWLLogicalAxiom>(ontoAxioms);
        gsMappingAxInDiag.retainAll(targetDiagnosis);
        //assertTrue(gsMappingAxInDiag.isEmpty());
        assertTrue(reasoner.getUnsatisfiableClasses().getEntities().size() == 1);

        logger.info("time needed: " + time);

    }


}
