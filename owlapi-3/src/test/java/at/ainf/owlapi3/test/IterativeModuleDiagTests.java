package at.ainf.owlapi3.test;

import at.ainf.diagnosis.Debugger;
import at.ainf.diagnosis.logging.MetricsLogger;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.model.OWLModuleExtractor;
import at.ainf.owlapi3.model.intersection.OWLEqualIntersectionExtractor;
import at.ainf.owlapi3.model.intersection.OWLPerecentConceptIntersectionExtractor;
import at.ainf.owlapi3.module.iterative.diagsearcher.*;
import at.ainf.owlapi3.module.iterative.modulediagnosis.multhread.InvTreeDiagSearcher;
import at.ainf.owlapi3.module.modprovider.OtfModuleProvider;
import at.ainf.owlapi3.module.iterative.*;
import at.ainf.owlapi3.module.iterative.modulediagnosis.IterativeModuleDiagnosis;
import at.ainf.owlapi3.reasoner.HornSatReasoner;
import at.ainf.owlapi3.reasoner.HornSatReasonerFactory;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.io.File;
import java.io.InputStream;
import java.util.*;

import static at.ainf.owlapi3.util.OWLUtils.calculateExpressivity;
import static at.ainf.owlapi3.util.OWLUtils.calculateSignature;
import static at.ainf.owlapi3.util.OWLUtils.createOntology;
import static at.ainf.owlapi3.util.SetUtils.createIntersection;
import static at.ainf.owlapi3.util.SetUtils.createUnion;
import static com.codahale.metrics.MetricRegistry.name;
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
        Set<OWLLogicalAxiom> diagnosis = diagnosisFinder.start().iterator().next();

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
        diagnosisFinder.start();

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
        diagnosisFinder.start();

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

    protected Set<OWLClass> getClassesInModuleSignature(Set<OWLLogicalAxiom> module) {
        Set<OWLClass> classesInModule = new LinkedHashSet<OWLClass>();
        for (OWLLogicalAxiom axiom : module)
            classesInModule.addAll (axiom.getClassesInSignature());
        return classesInModule;
    }

    @Test
    public void testSplittingModule() throws OWLOntologyCreationException {

        String onto = "fma2nci";
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

        Set<OWLLogicalAxiom> fullOnto = new LinkedHashSet<OWLLogicalAxiom>(ontoAxioms);
        fullOnto.addAll(mappingAxioms);
        logger.info("onto size: " + fullOnto.size());
        List<OWLClass> signature = new LinkedList<OWLClass>(getClassesInModuleSignature(fullOnto));
        logger.info("signature size: " + signature.size());
        int n = 1;
        List<OWLClass> subsignature = signature.subList(signature.size()/10 * n,signature.size()/10 * (n+1));
        logger.info("subsignature size: " + subsignature.size());

        metricsLogger.startTimer("calculationsubsignaturemodule");
        metricsLogger.startTimer("calculationsubsignaturemodule");
        Set<OWLLogicalAxiom> submodul = extractModule(fullOnto, subsignature);
        metricsLogger.stopTimer("calculationsubsignaturemodule");
        metricsLogger.stopTimer("calculationsubsignaturemodule");

        List<OWLClass> submodulsignature = new LinkedList<OWLClass>(getClassesInModuleSignature(submodul));
        logger.info("submodulsignature size: " + submodulsignature.size());

        metricsLogger.startTimer("cc");
        //boolean consistent = new Reasoner.ReasonerFactory().createNonBufferingReasoner(createOntology(submodul)).getUnsatisfiableClasses().getEntitiesMinusBottom().isEmpty();
        metricsLogger.stopTimer("cc");
        //logger.info("submodul consistent: " + consistent);
        logger.info("size of submodule: " + submodul.size());


    }

    protected List<List<OWLClass>> calculateSubSignatureInterleaved(List<OWLClass> signature, int n) {
        List<List<OWLClass>> subsignatures = new LinkedList<List<OWLClass>>();
        for (int i = 0; i < n; i++) {
            LinkedList<OWLClass> s = new LinkedList<OWLClass>();
            for (int j = 0; j*n+i < signature.size(); j++) {
                OWLClass cls = signature.get(i+j*n);
                s.add(cls);
            }
            subsignatures.add(s);
        }
        return  subsignatures;
    }

    protected List<List<OWLClass>> calculateSubSignaturePartitioned(List<OWLClass> signature, int n) {
        int size = signature.size() / n;
        List<List<OWLClass>> subsignatures = new LinkedList<List<OWLClass>>();
        for (int i = 0; i < n; i++) {
            LinkedList<OWLClass> s = new LinkedList<OWLClass>(signature.subList(size * i, size * (i + 1)));
            subsignatures.add(s);
            //logger.info("generated subsignature with size: " + s.size());
        }
        return  subsignatures;
    }

    protected List<List<OWLClass>> calculateSubSignatureOfSize (List<OWLClass> signature, int size, int number) {
        Random rand = new Random(234234);
        List<List<OWLClass>> subsignatures = new LinkedList<List<OWLClass>>();

        while (subsignatures.size() < number) {
            List<OWLClass> subsignature = new LinkedList<OWLClass>();
            while (subsignature.size() < size) {
                OWLClass cls = signature.get(rand.nextInt(signature.size()));
                if (!subsignature.contains(cls))
                    subsignature.add(cls);
            }
            subsignatures.add(subsignature);
        }

        return subsignatures;
    }

    enum OntoName { SNOMED2NCI, MOUSE2HUMAN }

    protected Set<OWLLogicalAxiom> loadOntologyWithMappings(OntoName name) {
        if (name.equals(OntoName.SNOMED2NCI)) {
            String onto = "snomed2nci";
            Set<OWLLogicalAxiom> onto1Axioms = getAxioms("ontologies/" + onto + "_gen1_onto1.owl");
            Set<OWLLogicalAxiom> onto2Axioms = getAxioms("ontologies/" + onto + "_gen1_onto2.owl");
            Set<OWLLogicalAxiom> mappingAxioms = getAxioms("ontologies/" + onto + "_gen1_mappings.owl");
            Set<OWLLogicalAxiom> ontoAxioms = new HashSet<OWLLogicalAxiom>();
            ontoAxioms.addAll(onto1Axioms);
            ontoAxioms.addAll(onto2Axioms);
            Set<OWLLogicalAxiom> fullOnto = new LinkedHashSet<OWLLogicalAxiom>(ontoAxioms);
            fullOnto.addAll(mappingAxioms);

            return fullOnto;
        }
        else if (name.equals(OntoName.MOUSE2HUMAN)) {
            String onto = "mouse2human";
            Set<OWLLogicalAxiom> onto1Axioms = getAxioms("ontologies/" + onto + "genonto1.owl");
            Set<OWLLogicalAxiom> onto2Axioms = getAxioms("ontologies/" + onto + "genonto2.owl");
            Set<OWLLogicalAxiom> mappingAxioms = getAxioms("ontologies/" + onto + "genmapp.owl");
            Set<OWLLogicalAxiom> ontoAxioms = new HashSet<OWLLogicalAxiom>();
            ontoAxioms.addAll(onto1Axioms);
            ontoAxioms.addAll(onto2Axioms);
            Set<OWLLogicalAxiom> fullOnto = new LinkedHashSet<OWLLogicalAxiom>(ontoAxioms);
            fullOnto.addAll(mappingAxioms);

            return fullOnto;
        }
        else
            throw new IllegalArgumentException("unknown ontology to load");
    }

    protected Set<OWLLogicalAxiom> loadMappings(OntoName name) {
        if (name.equals(OntoName.SNOMED2NCI)) {
            String onto = "snomed2nci";
            return getAxioms("ontologies/" + onto + "_gen1_mappings.owl");
        }
        else if (name.equals(OntoName.MOUSE2HUMAN)) {
            String onto = "mouse2human";
            return getAxioms("ontologies/" + onto + "genmapp.owl");
        }
        else
            throw new IllegalArgumentException("unknown ontology to load");
    }

    @Test
    public void testModuleInconsistency() throws OWLOntologyCreationException {

        ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
        Set<OWLLogicalAxiom> fullOnto = loadOntologyWithMappings(OntoName.MOUSE2HUMAN);


        logger.info("onto size / expressivity: " + fullOnto.size() + ", " + calculateExpressivity(fullOnto));

        List<OWLClass> signature = new LinkedList<OWLClass>(getClassesInModuleSignature(fullOnto));
        logger.info("signature size: " + signature.size());

        List<Set<OWLLogicalAxiom>> modules = recursiveModuleExtract(fullOnto, signature, 40);
        OWLReasonerFactory factory = new Reasoner.ReasonerFactory();

        for (Set<OWLLogicalAxiom> module : modules) {
            OWLReasoner reasoner = factory.createNonBufferingReasoner(createOntology(module));
            Set<OWLClass> unsatClasses = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
            Set<OWLClass> moduleSignature = getClassesInModuleSignature(module);
            logger.info ("module size / module signature size / unsat classes in module / expressiveness module: " +
                        module.size() + ", " + moduleSignature.size() + ", " + unsatClasses.size() +
                        ", " + calculateExpressivity(module));
        }

    }

    @Test
    public void testMinimalModuleDebug() throws OWLOntologyCreationException {

        ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
        Set<OWLLogicalAxiom> fullOnto = loadOntologyWithMappings(OntoName.SNOMED2NCI);

        logger.info("onto size / expressivity: " + fullOnto.size() + ", " + calculateExpressivity(fullOnto));

        List<OWLClass> signature = new LinkedList<OWLClass>(getClassesInModuleSignature(fullOnto));
        logger.info("signature size: " + signature.size());

        //Set<OWLLogicalAxiom> minimalModule = new OWLEqualIntersectionExtractor(40).calculateMinModule(fullOnto);
        //Set<OWLLogicalAxiom> minimalModule = new OWLRestConceptIntersectionExtractor(4107).calculateMinModule(fullOnto);
        Set<OWLLogicalAxiom> minimalModule = new OWLPerecentConceptIntersectionExtractor(0.05).calculateMinModule(fullOnto);

        Set<OWLClass> unsatClasses = new Reasoner.ReasonerFactory().createNonBufferingReasoner(createOntology(minimalModule)).getUnsatisfiableClasses().getEntitiesMinusBottom();
        logger.info ("module size / unsat classes in minimal module / expressiveness minimal module: " +
                minimalModule.size() + ", " + unsatClasses.size() + ", " + calculateExpressivity(minimalModule));

        Set<OWLLogicalAxiom> minimalModuleMappings = new LinkedHashSet<OWLLogicalAxiom>(loadMappings(OntoName.SNOMED2NCI));
        minimalModuleMappings.retainAll(minimalModule);
        Set<OWLLogicalAxiom> minimalModuleOntologies = new LinkedHashSet<OWLLogicalAxiom>(minimalModule);
        minimalModuleOntologies.removeAll(minimalModuleMappings);

        IterativeModuleDiagnosis diagSe = new IterativeModuleDiagnosis(minimalModuleMappings, minimalModuleOntologies, new Reasoner.ReasonerFactory(), new ModuleMinDiagSearcher(), true);
        Set<OWLLogicalAxiom> diagnosis = diagSe.start().iterator().next();

        Set<OWLLogicalAxiom> repairedOnto = new LinkedHashSet<OWLLogicalAxiom>(fullOnto);
        repairedOnto.removeAll(diagnosis);

        Set<OWLLogicalAxiom> minimalModuleRepaired = new OWLPerecentConceptIntersectionExtractor(0.05).calculateMinModule(repairedOnto);
        Set<OWLClass> unsatClassesRepaired = new Reasoner.ReasonerFactory().createNonBufferingReasoner(createOntology(minimalModuleRepaired)).getUnsatisfiableClasses().getEntitiesMinusBottom();
        logger.info ("module size / unsat classes in minimal module / expressiveness minimal module: " +
                minimalModuleRepaired.size() + ", " + unsatClassesRepaired.size() + ", " + calculateExpressivity(minimalModuleRepaired));

        //Set<OWLClass> unsatClassesFull = new Reasoner.ReasonerFactory().createNonBufferingReasoner(createOntology(fullOnto)).getUnsatisfiableClasses().getEntitiesMinusBottom();
        //logger.info("unsat classes in full stage1 ont: " + unsatClassesFull.size());

        //Set<OWLClass> unsatClassesRepaired = new Reasoner.ReasonerFactory().createNonBufferingReasoner(createOntology(repairedOnto)).getUnsatisfiableClasses().getEntitiesMinusBottom();
        //logger.info("unsat classes in repaired stage1 ont: " + unsatClassesRepaired.size());

    }

    @Test
    public void testModuleIntersection() throws OWLOntologyCreationException {

        ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
        Set<OWLLogicalAxiom> fullOnto = loadOntologyWithMappings(OntoName.SNOMED2NCI);

        logger.info("onto size / expressivity: " + fullOnto.size() + ", " + calculateExpressivity(fullOnto));

        List<OWLClass> signature = new LinkedList<OWLClass>(getClassesInModuleSignature(fullOnto));
        logger.info("signature size: " + signature.size());

        //Set<OWLLogicalAxiom> minimalModule = new OWLEqualIntersectionExtractor(40).calculateMinModule(fullOnto);
        //Set<OWLLogicalAxiom> minimalModule = new OWLRestConceptIntersectionExtractor(4107).calculateMinModule(fullOnto);
        Set<OWLLogicalAxiom> minimalModule = new OWLPerecentConceptIntersectionExtractor(0.05).calculateMinModule(fullOnto);
        Set<OWLClass> unsatClasses = new Reasoner.ReasonerFactory().createNonBufferingReasoner(createOntology(minimalModule)).getUnsatisfiableClasses().getEntitiesMinusBottom();

        logger.info ("module size / unsat classes in minimal module / expressiveness minimal module: " +
                minimalModule.size() + ", " + unsatClasses.size() + ", " + calculateExpressivity(minimalModule));

    }

    @Test
    public void testInvTreeDiagSearcher() throws SolverException, InconsistentTheoryException, NoConflictException {
        String onto = "snomed2nci";
        ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());

        Set<OWLLogicalAxiom> onto1Axioms = getAxioms("ontologies/" + onto + "_gen1_onto1.owl");
        Set<OWLLogicalAxiom> onto2Axioms = getAxioms("ontologies/" + onto + "_gen1_onto2.owl");
        Set<OWLLogicalAxiom> mappingAxioms = getAxioms("ontologies/" + onto + "_gen1_mappings.owl");
        Set<OWLLogicalAxiom> ontoAxioms = new HashSet<OWLLogicalAxiom>();
        ontoAxioms.addAll(onto1Axioms);
        ontoAxioms.addAll(onto2Axioms);

        Set<OWLLogicalAxiom> fullOnto = new LinkedHashSet<OWLLogicalAxiom>(ontoAxioms);
        fullOnto.addAll(mappingAxioms);

        Debugger<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> diagSearcher = new InvTreeDiagSearcher(mappingAxioms, ontoAxioms, new Reasoner.ReasonerFactory());
        Set<OWLLogicalAxiom> targetDiagnosis = diagSearcher.start().iterator().next();
        logger.info("found target diagnosis with size: " + targetDiagnosis.size());

    }

    protected MetricsLogger metricsLogger = MetricsLogger.getInstance();
    
    //public MetricsManager metricsManager = MetricsManager.getInstance();

    @Test
    public void testMultThreads() throws OWLOntologyCreationException {

        String onto = "snomed2nci";
        ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());

        Set<OWLLogicalAxiom> onto1Axioms = getAxioms("ontologies/" + onto + "_gen1_onto1.owl");
        Set<OWLLogicalAxiom> onto2Axioms = getAxioms("ontologies/" + onto + "_gen1_onto2.owl");
        Set<OWLLogicalAxiom> mappingAxioms = getAxioms("ontologies/" + onto + "_gen1_mappings.owl");
        Set<OWLLogicalAxiom> ontoAxioms = new HashSet<OWLLogicalAxiom>();
        ontoAxioms.addAll(onto1Axioms);
        ontoAxioms.addAll(onto2Axioms);

        Set<OWLLogicalAxiom> fullOnto = new LinkedHashSet<OWLLogicalAxiom>(ontoAxioms);
        fullOnto.addAll(mappingAxioms);
        logger.info("onto size: " + fullOnto.size());

        List<OWLClass> signatureMappings = new LinkedList<OWLClass>(calculateSignature(mappingAxioms));
        logger.info("signature mappings size: " + signatureMappings.size());

        List<List<OWLClass>> subSignatures =
                new OWLEqualIntersectionExtractor(10).calculateSubSignatures(signatureMappings);

        OWLModuleExtractor owlModuleExtractor = new OWLModuleExtractor(fullOnto);
        List<Set<OWLLogicalAxiom>> subModules = new LinkedList<Set<OWLLogicalAxiom>>();

        //Histogram moduleSizes = metricsManager.getMetrics().histogram(name(IterativeModuleDiagTests.class, "module-sizes"));
        //Timer moduleCalcTime = metricsManager.getMetrics().timer(name(IterativeModuleDiagTests.class, "module-calc"));

        for (List<OWLClass> subsignature : subSignatures) {
            //Timer.Context time = moduleCalcTime.time();
            metricsLogger.startTimer("modulecalcnew");
            Set<OWLLogicalAxiom> subModule = owlModuleExtractor.calculateModule(subsignature);
            metricsLogger.stopTimer("modulecalcnew");
            //time.stop();
            Set<OWLLogicalAxiom> mappingsInSubmodule = new HashSet<OWLLogicalAxiom>(subModule);
            mappingsInSubmodule.retainAll(mappingAxioms);
            Set<OWLClass> moduleSignature = calculateSignature(subModule);
            Set<OWLClass> mappingsInModuleSignature = new HashSet<OWLClass>(moduleSignature);
            mappingsInModuleSignature.retainAll(signatureMappings);
            subModules.add(subModule);
            //moduleSizes.update(subModule.size());
            /*OWLReasoner reasonerModule = new Reasoner.ReasonerFactory().createNonBufferingReasoner(createOntology(subModule));
            Speed4JMeasurement.start("time fo module unsat classes");
            int unsatClassesNumInMod = reasonerModule.getUnsatisfiableClasses().getEntitiesMinusBottom().size();
            metricsLogger.stopTimer();
            logger.info("unsat classes in module: " + unsatClassesNumInMod);*/
            logger.info("subsignature size / module signature size / submodule size / mappings size " +
                    " / mapping concepts in submodule signature : " +
                    subsignature.size() + ", " + moduleSignature.size() + ", " +
                    subModule.size() + ", " + mappingsInSubmodule.size() + ", " + mappingsInModuleSignature.size());
        }
        metricsLogger.logStandardMetrics();
        Set<OWLLogicalAxiom> intersection = createIntersection(subModules);
        Set<OWLLogicalAxiom> union = createUnion(subModules);
        Set<OWLClass> intersectionSig = calculateSignature(intersection);
        List<Set<OWLLogicalAxiom>> onlyInOneModule = new LinkedList<Set<OWLLogicalAxiom>>();
        for (Set<OWLLogicalAxiom> module : subModules) {
            Set<Set<OWLLogicalAxiom>> otherModules = new HashSet<Set<OWLLogicalAxiom>>(subModules);
            otherModules.remove(module);
            Set<OWLLogicalAxiom> axiomsOnlyInModule = new HashSet<OWLLogicalAxiom>(module);
            axiomsOnlyInModule.removeAll(createUnion(otherModules));
            onlyInOneModule.add(axiomsOnlyInModule);
        }
        logger.info("intersection signature / intersection size / union size: " +
                    intersectionSig.size() + ", " + intersection.size() + ", " + union.size());
        for (Set<OWLLogicalAxiom> restModule : onlyInOneModule)
            logger.info("axioms only in module: " + restModule.size());
        //metricsManager.logAllMetrics();

        /*OWLReasoner reasonerFullOnto = new Reasoner.ReasonerFactory().createNonBufferingReasoner(createOntology(fullOnto));
        Speed4JMeasurement.start("time fo all unsat");
        int unsatClassesNum = reasonerFullOnto.getUnsatisfiableClasses().getEntitiesMinusBottom().size();
        metricsLogger.stopTimer();
        logger.info("unsat classes in full onto: " + unsatClassesNum);*/



    }

    @Test
    public void testModulesExtract() throws OWLOntologyCreationException {

        String onto = "snomed2nci";
        ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());

        Set<OWLLogicalAxiom> onto1Axioms = getAxioms("ontologies/" + onto + "_gen1_onto1.owl");
        Set<OWLLogicalAxiom> onto2Axioms = getAxioms("ontologies/" + onto + "_gen1_onto2.owl");
        Set<OWLLogicalAxiom> mappingAxioms = getAxioms("ontologies/" + onto + "_gen1_mappings.owl");
        Set<OWLLogicalAxiom> ontoAxioms = new HashSet<OWLLogicalAxiom>();
        ontoAxioms.addAll(onto1Axioms);
        ontoAxioms.addAll(onto2Axioms);

        Set<OWLLogicalAxiom> fullOnto = new LinkedHashSet<OWLLogicalAxiom>(ontoAxioms);
        fullOnto.addAll(mappingAxioms);
        logger.info("onto size: " + fullOnto.size());

        List<OWLClass> signatureMappings = new LinkedList<OWLClass>(calculateSignature(mappingAxioms));
        logger.info("signature mappings size: " + signatureMappings.size());

        OWLModuleExtractor extractor = new OWLModuleExtractor(fullOnto);
        List<Set<OWLLogicalAxiom>> modules = new LinkedList<Set<OWLLogicalAxiom>>();
        Set<OWLClass> satClasse = new HashSet<OWLClass>();
        Set<OWLClass> alreadyInModule = new HashSet<OWLClass>();
        for (OWLClass cls : signatureMappings) {
            if (alreadyInModule.contains(cls))
                continue;
            Set<OWLLogicalAxiom> module = extractor.calculateModule(Collections.singleton(cls));
            Set<OWLClass> signatureMappingsModule = calculateSignature(module);
            signatureMappingsModule.retainAll(signatureMappings);
            alreadyInModule.addAll(signatureMappingsModule);
            modules.add(module);
        }
        logger.info("sat classes: " + satClasse.size() + ", " + modules.size());


    }

    @Test
    public void testMappingsDistinctModules() throws OWLOntologyCreationException {

        String onto = "snomed2nci";
        ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());

        Set<OWLLogicalAxiom> onto1Axioms = getAxioms("ontologies/" + onto + "_gen1_onto1.owl");
        Set<OWLLogicalAxiom> onto2Axioms = getAxioms("ontologies/" + onto + "_gen1_onto2.owl");
        Set<OWLLogicalAxiom> mappingAxioms = getAxioms("ontologies/" + onto + "_gen1_mappings.owl");
        Set<OWLLogicalAxiom> ontoAxioms = new HashSet<OWLLogicalAxiom>();
        ontoAxioms.addAll(onto1Axioms);
        ontoAxioms.addAll(onto2Axioms);

        Set<OWLLogicalAxiom> fullOnto = new LinkedHashSet<OWLLogicalAxiom>(ontoAxioms);
        fullOnto.addAll(mappingAxioms);
        logger.info("onto size: " + fullOnto.size());

        List<OWLClass> signatureMappings = new LinkedList<OWLClass>(calculateSignature(mappingAxioms));
        logger.info("signature mappings size: " + signatureMappings.size());

        OWLModuleExtractor owlModuleExtractor = new OWLModuleExtractor(fullOnto);
        List<Set<OWLLogicalAxiom>> modules = new LinkedList<Set<OWLLogicalAxiom>>();
        Set<OWLClass> alreadyInModule = new HashSet<OWLClass>();
        for (OWLClass cls : signatureMappings) {
            if (alreadyInModule.contains(cls)) {
                continue;
            }
            Set<OWLLogicalAxiom> module = owlModuleExtractor.calculateModule(Collections.singleton(cls));
            boolean isBiggest = false;
            while (!isBiggest) {
                Set<OWLClass> signature = calculateSignature(module);
                Set<OWLLogicalAxiom> bigger = owlModuleExtractor.calculateModule(signature);
                if (bigger.size() == module.size())
                    isBiggest = true;
                else
                    module = bigger;
            }
            modules.add(module);
            alreadyInModule.addAll(calculateSignature(module));
        }

        for (Set<OWLLogicalAxiom> module : modules) {
            Set<OWLLogicalAxiom> mappingsInModule = new HashSet<OWLLogicalAxiom>(module);
            mappingsInModule.retainAll(mappingAxioms);
            Set<OWLClass> moduleSignature = calculateSignature(module);
            logger.info("signature size / module size / mappings size: " +
                    moduleSignature.size() + ", " + module.size() + ", " + mappingsInModule.size());
        }



    }

    @Test
    public void testModuleGenerat() throws OWLOntologyCreationException {

        String onto = "snomed2nci";
        ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());

        Set<OWLLogicalAxiom> onto1Axioms = getAxioms("ontologies/" + onto + "_gen1_onto1.owl");
        Set<OWLLogicalAxiom> onto2Axioms = getAxioms("ontologies/" + onto + "_gen1_onto2.owl");
        Set<OWLLogicalAxiom> mappingAxioms = getAxioms("ontologies/" + onto + "_gen1_mappings.owl");
        //Set<OWLLogicalAxiom> onto1Axioms = getAxioms("ontologies/" + onto + "genonto1.owl");
        //Set<OWLLogicalAxiom> onto2Axioms = getAxioms("ontologies/" + onto + "genonto2.owl");
        //Set<OWLLogicalAxiom> mappingAxioms = getAxioms("ontologies/" + onto + "genmapp.owl");
        Set<OWLLogicalAxiom> ontoAxioms = new HashSet<OWLLogicalAxiom>();
        ontoAxioms.addAll(onto1Axioms);
        ontoAxioms.addAll(onto2Axioms);

        Set<OWLLogicalAxiom> fullOnto = new LinkedHashSet<OWLLogicalAxiom>(ontoAxioms);
        fullOnto.addAll(mappingAxioms);
        logger.info("onto size: " + fullOnto.size());

        List<OWLClass> signature = new LinkedList<OWLClass>(getClassesInModuleSignature(fullOnto));
        logger.info("signature size: " + signature.size());

        /* Speed4JMeasurement.start("whole onto unsat classes");
        Set<OWLClass> unsatClasses = getUnsatClasses(fullOnto);
        logger.info("unsat classes: " + unsatClasses.size());
        metricsLogger.stopTimer(); */

        OWLModuleExtractor extractor = new OWLModuleExtractor(fullOnto);
        final int MAX_PART = 20;
        for (int i = 20; i <= MAX_PART; i++) {
            //List<List<OWLClass>> subsignatures = calculateSubSignatureInterleaved(signature,i);
            List<List<OWLClass>> subsignatures = calculateSubSignaturePartitioned (signature, i);

            metricsLogger.startTimer("moduleextract_");
            List<Set<OWLLogicalAxiom>> modules = recursiveModuleExtract(fullOnto,signature,20);
            metricsLogger.stopTimer("moduleextract_");
            for (Set<OWLLogicalAxiom> module : modules) {
                logger.info("module size / module signature size: " + module.size() + ", " + getClassesInModuleSignature(module).size());
                final List<OWLClass> classesInModuleSignature = new LinkedList<OWLClass>(getClassesInModuleSignature(module));
                metricsLogger.startTimer("submoduleextract ");
                final List<Set<OWLLogicalAxiom>> submodules = recursiveModuleExtract(module, classesInModuleSignature, 20);
                metricsLogger.stopTimer("submoduleextract ");

                for (Set<OWLLogicalAxiom> submodule : submodules)
                    logger.info("submodule size / submodule signature size: " + submodule.size() + ", " + getClassesInModuleSignature(submodule).size());

                //logger.info("parts / time / start signature size / submodul size / signature size: " +
                //            i + ", " + time + ", " + module.size() + ", " + submod.size() + ", " + getClassesInModuleSignature(submod).size());

                final Set<OWLLogicalAxiom> intersectionsub = createIntersection(submodules);
                logger.info ("submodules intersection  size / submodules intersection  size signature: " + intersectionsub.size() + ", " + getClassesInModuleSignature(intersectionsub).size());

            }

            final Set<OWLLogicalAxiom> intersection = createIntersection(modules);
            logger.info ("modules intersection  size: " + intersection.size());

            final List<Set<OWLLogicalAxiom>> intersectionsubmodules = recursiveModuleExtract(intersection, new LinkedList<OWLClass>(getClassesInModuleSignature(intersection)), 20);
            final Set<OWLLogicalAxiom> intersection2 = createIntersection(intersectionsubmodules);
            logger.info ("intersectionsubmodules intersection  size / intersectionsubmodules intersection  size signature: " + intersection2.size() + ", " + getClassesInModuleSignature(intersection2).size());

        }

    }

    protected List<Set<OWLLogicalAxiom>> recursiveModuleExtract (Set<OWLLogicalAxiom> ontology, List<OWLClass> signature, int split) {
        List<Set<OWLLogicalAxiom>> submodules = new LinkedList<Set<OWLLogicalAxiom>>();
        OWLModuleExtractor extractor = new OWLModuleExtractor(ontology);
        List<List<OWLClass>> subsignatures = calculateSubSignaturePartitioned (signature, split);
        for (List<OWLClass> s : subsignatures) {
            metricsLogger.startTimer("submodules_extraction_single");
            Set<OWLLogicalAxiom> submod = extractor.calculateModule(s);
            long time = metricsLogger.stopTimer("submodules_extraction_single");
            //logger.info("parts / time / start signature size / submodul size / signature size: " +
            //        split + ", " + time + ", " + s.size() + ", " + submod.size() + ", " + getClassesInModuleSignature(submod).size());
            submodules.add(submod);
        }
        return submodules;
    }

    @Test
    public void testExtracionModule() throws OWLOntologyCreationException {

        String onto = "snomed2nci";
        ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());

        Set<OWLLogicalAxiom> onto1Axioms = getAxioms("ontologies/" + onto + "_gen1_onto1.owl");
        Set<OWLLogicalAxiom> onto2Axioms = getAxioms("ontologies/" + onto + "_gen1_onto2.owl");
        Set<OWLLogicalAxiom> mappingAxioms = getAxioms("ontologies/" + onto + "_gen1_mappings.owl");
        Set<OWLLogicalAxiom> ontoAxioms = new HashSet<OWLLogicalAxiom>();
        ontoAxioms.addAll(onto1Axioms);
        ontoAxioms.addAll(onto2Axioms);

        Set<OWLLogicalAxiom> fullOnto = new LinkedHashSet<OWLLogicalAxiom>(ontoAxioms);
        fullOnto.addAll(mappingAxioms);
        logger.info("onto size: " + fullOnto.size());

        List<OWLClass> signature = new LinkedList<OWLClass>(getClassesInModuleSignature(fullOnto));
        logger.info("signature size: " + signature.size());

        //List<List<OWLClass>> subsignatures = calculateSubSignatureInterleaved(signature,10);
        List<List<OWLClass>> subsignatures = calculateSubSignaturePartitioned(signature,10);
        //List<List<OWLClass>> subsignatures = calculateSubSignatureOfSize(signature,50,10);

        List<Set<OWLLogicalAxiom>> submodules = new LinkedList<Set<OWLLogicalAxiom>>();
        metricsLogger.startTimer("submodules_extraction_overall");
        for (List<OWLClass> s : subsignatures) {
            metricsLogger.startTimer("submodules_extraction_single");
            Set<OWLLogicalAxiom> submod = extractModule(fullOnto, s);
            metricsLogger.stopTimer("submodules_extraction_single");
            logger.info("submodul size: " + submod.size() + ", signature size: " + getClassesInModuleSignature(submod).size());
            submodules.add(submod);
        }
        metricsLogger.stopTimer("submodules_extraction_overall");

        metricsLogger.startTimer("intersection_computation");
        Set<OWLLogicalAxiom> intersection = new LinkedHashSet<OWLLogicalAxiom>();
            if (!submodules.isEmpty()) {
            Iterator<Set<OWLLogicalAxiom>> i = submodules.iterator();
            intersection.addAll(i.next());
            logger.info("submodul size: " + intersection.size() + ", signature size: " + getClassesInModuleSignature(intersection).size());
            while (i.hasNext()) {
                intersection.retainAll(i.next());
                logger.info("submodul size: " + intersection.size() + ", signature size: " + getClassesInModuleSignature(intersection).size());
            }
        }
        metricsLogger.stopTimer("intersection_computation");

        OWLReasonerFactory reasonerFactory = new HornSatReasonerFactory();
        //OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();

        for (Set<OWLLogicalAxiom> submodul : submodules) {
            metricsLogger.startTimer("hornreasoner_submodul_classifiy");
            final HornSatReasoner reasoner = (HornSatReasoner) reasonerFactory.createNonBufferingReasoner(createOntology(submodul));
            //logger.info("unsat classes in submodul: " + reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom().size());

            List<OWLClass> sortedClassesInSignature = new LinkedList<OWLClass>(getClassesInModuleSignature(submodul));
            /*Collections.sort(sortedClassesInSignature, new Comparator<OWLClass>() {
                @Override
                public int compare(OWLClass o1, OWLClass o2) {
                    Multimap<Integer,Integer> symbolMap = reasoner.getRelevantCore().getSymbolsMap();
                    int o1Index = reasoner.getIndex(o1);
                    int o2Index = reasoner.getIndex(o2);
                    if (!symbolMap.containsKey(o1Index) || !symbolMap.containsKey(o2Index))
                        return 0;

                    Integer levelO1 = Collections.min(symbolMap.get(o1Index));
                    Integer levelO2 = Collections.min(symbolMap.get(o2Index));
                    return levelO1.compareTo(levelO2);
                }
            });*/

            OWLClass firstUnsat = null;
            int cnt = 1;
            for (OWLClass cls : sortedClassesInSignature) {
                if (!reasoner.isSatisfiable(cls)) {
                    firstUnsat = cls;
                    break;
                }
                cnt++;
            }
            logger.info("needed iterations to find unsat:" + cnt);

            Set<OWLLogicalAxiom> subsubmod = extractModule(submodul, Collections.singletonList(firstUnsat));
            logger.info("subsubmodul size: " + subsubmod.size() + ", signature size: " + getClassesInModuleSignature(subsubmod).size());
            metricsLogger.stopTimer("hornreasoner_submodul_classifiy");

            metricsLogger.startTimer("subsub_classifiy");
            logger.info("unsat classes in subsubmodul: " + reasonerFactory.createNonBufferingReasoner(createOntology(subsubmod)).getUnsatisfiableClasses().getEntitiesMinusBottom().size());
            metricsLogger.stopTimer("subsub_classifiy");

        }

        metricsLogger.startTimer("hornreasoner_intersection_classifiy");
        logger.info("unsat classes in intersection: " + reasonerFactory.createNonBufferingReasoner(createOntology(intersection)).getUnsatisfiableClasses().getEntitiesMinusBottom().size());
        metricsLogger.stopTimer("hornreasoner_intersection_classifiy");

        metricsLogger.startTimer("interesction_classifiy");

        final HornSatReasoner reasoner = (HornSatReasoner) reasonerFactory.createNonBufferingReasoner(createOntology(intersection));

        List<OWLClass> sortedClassesInSignature = new LinkedList<OWLClass>(getClassesInModuleSignature(intersection));


        OWLClass firstUnsat = null;
        int cnt = 1;
        for (OWLClass cls : sortedClassesInSignature) {
            if (!reasoner.isSatisfiable(cls)) {
                firstUnsat = cls;
                break;
            }
            cnt++;
        }
        logger.info("needed iterations to find unsat in intersection:" + cnt);

        Set<OWLLogicalAxiom> subsubmod = extractModule(intersection, Collections.singletonList(firstUnsat));
        logger.info("intersectionsub size: " + subsubmod.size() + ", signature size: " + getClassesInModuleSignature(subsubmod).size());
        metricsLogger.stopTimer("interesction_classifiy");

        metricsLogger.startTimer("intersectionsub_classifiy");
        logger.info("unsat classes in intersectionsub: " + reasonerFactory.createNonBufferingReasoner(createOntology(subsubmod)).getUnsatisfiableClasses().getEntitiesMinusBottom().size());
        metricsLogger.stopTimer("intersectionsub_classifiy");




    }


    protected Set<OWLLogicalAxiom> extractModule (Set<OWLLogicalAxiom> ontology, List<OWLClass> signature) {
        Set<OWLLogicalAxiom> result = new LinkedHashSet<OWLLogicalAxiom>();
        Set<OWLEntity> input = new LinkedHashSet<OWLEntity>();
        for (OWLEntity e : signature)
            input.add(e);
        metricsLogger.startTimer("creation_module_extractor");
        SyntacticLocalityModuleExtractor extractor =
                new SyntacticLocalityModuleExtractor(OWLManager.createOWLOntologyManager(), createOntology(ontology), ModuleType.STAR);
        metricsLogger.stopTimer("creation_module_extractor");
        metricsLogger.startTimer("extraction_module");
        for (OWLAxiom axiom : extractor.extract(input))
            result.add((OWLLogicalAxiom) axiom);
        metricsLogger.stopTimer("extraction_module");
        return result;
    }

    @Test
    public void testIterativeDiagnosis() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException, NoConflictException {

        String onto = "mouse2human";
        ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
        Set<OWLLogicalAxiom> onto1Axioms = getAxioms("ontologies/" + onto + "genonto1.owl");
        Set<OWLLogicalAxiom> onto2Axioms = getAxioms("ontologies/" + onto + "genonto2.owl");
        Set<OWLLogicalAxiom> mappingAxioms = getAxioms("ontologies/" + onto + "genmapp.owl");
        //Set<OWLLogicalAxiom> onto1Axioms = getAxioms("ontologies/" + onto + "_gen1_onto1.owl");
        //Set<OWLLogicalAxiom> onto2Axioms = getAxioms("ontologies/" + onto + "_gen1_onto2.owl");
        //Set<OWLLogicalAxiom> mappingAxioms = getAxioms("ontologies/" + onto + "_gen1_mappings.owl");
        String fileMappings = "mouse2human_reference_2011";
        //String fileMappings = "onto_mappings_FMA_NCI_cleanDG_rmbd";

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
        //ModuleDiagSearcher d = new ModuleInvTreeDiagSearcher();
        //ModuleDiagSearcher d = new ModuleTargetDiagSearcher(pathMappings);
        //ModuleDiagSearcher d = new ModuleQuerDiagSearcher(pathMappings,correctAxioms,falseAxioms, false);
        //ModuleDiagSearcher d = new ModuleOptQuerDiagSearcher(pathMappings,correctAxioms,falseAxioms, false);

        //d.setTreeCreator(new InvHSTreeCreator());

        long time = System.currentTimeMillis();
        Debugger<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> diagnosisFinder = new IterativeModuleDiagnosis(mappingAxioms, ontoAxioms,
                new Reasoner.ReasonerFactory(), d, true);
        metricsLogger.startTimer("modulediagnosiscreation");
        //ModuleDiagnosis diagnosisFinder = new RootModuleDiagnosis(mappingAxioms, ontoAxioms,
        //        new Reasoner.ReasonerFactory(), d);
        metricsLogger.stopTimer("modulediagnosiscreation");

        Set<OWLLogicalAxiom> targetDiagnosis = diagnosisFinder.start().iterator().next();
        logger.info("size of target diag: " + targetDiagnosis.size());
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
        metricsLogger.logStandardMetrics();
        Set<OWLClass> unsatClasses = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
        assertTrue(unsatClasses.isEmpty());

        logger.info("time needed: " + time);

    }


}
