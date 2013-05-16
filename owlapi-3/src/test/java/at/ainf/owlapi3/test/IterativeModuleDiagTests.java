package at.ainf.owlapi3.test;

import at.ainf.diagnosis.Speed4JMeasurement;
import at.ainf.owlapi3.model.OWLModuleExtractor;
import at.ainf.owlapi3.module.OtfModuleProvider;
import at.ainf.owlapi3.module.iterative.*;
import at.ainf.owlapi3.module.iterative.diag.IterativeModuleDiagnosis;
import at.ainf.owlapi3.module.iterative.diag.ModuleDiagnosis;
import at.ainf.owlapi3.reasoner.HornSatReasoner;
import at.ainf.owlapi3.reasoner.HornSatReasonerFactory;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.DLExpressivityChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

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

        Speed4JMeasurement.start("calculationsubsignaturemodule");
        Set<OWLLogicalAxiom> submodul = extractModule(fullOnto, subsignature);
        Speed4JMeasurement.stop();

        List<OWLClass> submodulsignature = new LinkedList<OWLClass>(getClassesInModuleSignature(submodul));
        logger.info("submodulsignature size: " + submodulsignature.size());

        Speed4JMeasurement.start("cc");
        //boolean consistent = new Reasoner.ReasonerFactory().createNonBufferingReasoner(createOntology(submodul)).getUnsatisfiableClasses().getEntitiesMinusBottom().isEmpty();
        Speed4JMeasurement.stop();
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

    protected Set<OWLLogicalAxiom> loadOntology (OntoName name) {
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

    @Test
    public void testModuleInconsistency() throws OWLOntologyCreationException {

        ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
        Set<OWLLogicalAxiom> fullOnto = loadOntology(OntoName.MOUSE2HUMAN);


        logger.info("onto size / expressivity: " + fullOnto.size() + ", " + getExpressivity(fullOnto));

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
                        ", " + getExpressivity(module));
        }

    }

    @Test
    public void testModuleIntersection() throws OWLOntologyCreationException {

        ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
        Set<OWLLogicalAxiom> fullOnto = loadOntology(OntoName.SNOMED2NCI);

        logger.info("onto size / expressivity: " + fullOnto.size() + ", " + getExpressivity(fullOnto));

        List<OWLClass> signature = new LinkedList<OWLClass>(getClassesInModuleSignature(fullOnto));
        logger.info("signature size: " + signature.size());

        Set<OWLLogicalAxiom> minimalModule = createMinimalModule(fullOnto, 40);
        Set<OWLClass> unsatClasses = new Reasoner.ReasonerFactory().createNonBufferingReasoner(createOntology(minimalModule)).getUnsatisfiableClasses().getEntitiesMinusBottom();

        logger.info ("unsat classes in minimal module / expressiveness minimal module: " + unsatClasses.size() + ", " + getExpressivity(minimalModule));

    }

    protected String getExpressivity (Set<OWLLogicalAxiom> axioms) {
        return new DLExpressivityChecker(Collections.singleton(createOntology(axioms))).getDescriptionLogicName();
    }

    protected Set<OWLLogicalAxiom> createMinimalModule (Set<OWLLogicalAxiom> ontology, int split) {

        List<OWLClass> classesInModuleSignature = new LinkedList<OWLClass>(getClassesInModuleSignature(ontology));
        List<Set<OWLLogicalAxiom>> modules = recursiveModuleExtract(ontology, classesInModuleSignature, split);
        Set<OWLLogicalAxiom> intersection = createIntersection(modules);

        logger.info ("intersection size / intersection signature size / expressiveness intersection: " + intersection.size() + ", " + getClassesInModuleSignature(intersection).size() + ", " + getExpressivity(intersection));

        if (intersection.size() == ontology.size())
            return intersection;

        return createMinimalModule(intersection, split);
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
        Speed4JMeasurement.stop(); */

        OWLModuleExtractor extractor = new OWLModuleExtractor(fullOnto);
        final int MAX_PART = 20;
        for (int i = 20; i <= MAX_PART; i++) {
            //List<List<OWLClass>> subsignatures = calculateSubSignatureInterleaved(signature,i);
            List<List<OWLClass>> subsignatures = calculateSubSignaturePartitioned (signature, i);

            Speed4JMeasurement.start("moduleextract_");
            List<Set<OWLLogicalAxiom>> modules = recursiveModuleExtract(fullOnto,signature,20);
            Speed4JMeasurement.stop();
            for (Set<OWLLogicalAxiom> module : modules) {
                logger.info("module size / module signature size: " + module.size() + ", " + getClassesInModuleSignature(module).size());
                final List<OWLClass> classesInModuleSignature = new LinkedList<OWLClass>(getClassesInModuleSignature(module));
                Speed4JMeasurement.start("submoduleextract ");
                final List<Set<OWLLogicalAxiom>> submodules = recursiveModuleExtract(module, classesInModuleSignature, 20);
                Speed4JMeasurement.stop();

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
            Speed4JMeasurement.start("submodules_extraction_single");
            Set<OWLLogicalAxiom> submod = extractor.calculateModule(s);
            long time = Speed4JMeasurement.stop();
            //logger.info("parts / time / start signature size / submodul size / signature size: " +
            //        split + ", " + time + ", " + s.size() + ", " + submod.size() + ", " + getClassesInModuleSignature(submod).size());
            submodules.add(submod);
        }
        return submodules;
    }

    protected <X> Set<X> createIntersection (Collection<Set<X>> collection) {
        final Iterator<Set<X>> iterator = collection.iterator();
        Set<X> intersection = new LinkedHashSet<X>(iterator.next());
        while (iterator.hasNext())
            intersection.retainAll(iterator.next());
        return intersection;
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
        Speed4JMeasurement.start("submodules_extraction_overall");
        for (List<OWLClass> s : subsignatures) {
            Speed4JMeasurement.start("submodules_extraction_single");
            Set<OWLLogicalAxiom> submod = extractModule(fullOnto, s);
            Speed4JMeasurement.stop();
            logger.info("submodul size: " + submod.size() + ", signature size: " + getClassesInModuleSignature(submod).size());
            submodules.add(submod);
        }
        Speed4JMeasurement.stop();

        Speed4JMeasurement.start("intersection_computation");
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
        Speed4JMeasurement.stop();

        OWLReasonerFactory reasonerFactory = new HornSatReasonerFactory();
        //OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();

        for (Set<OWLLogicalAxiom> submodul : submodules) {
            Speed4JMeasurement.start("hornreasoner_submodul_classifiy");
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
            Speed4JMeasurement.stop();

            Speed4JMeasurement.start("subsub_classifiy");
            logger.info("unsat classes in subsubmodul: " + reasonerFactory.createNonBufferingReasoner(createOntology(subsubmod)).getUnsatisfiableClasses().getEntitiesMinusBottom().size());
            Speed4JMeasurement.stop();

        }

        Speed4JMeasurement.start("hornreasoner_intersection_classifiy");
        logger.info("unsat classes in intersection: " + reasonerFactory.createNonBufferingReasoner(createOntology(intersection)).getUnsatisfiableClasses().getEntitiesMinusBottom().size());
        Speed4JMeasurement.stop();

        Speed4JMeasurement.start("interesction_classifiy");

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
        Speed4JMeasurement.stop();

        Speed4JMeasurement.start("intersectionsub_classifiy");
        logger.info("unsat classes in intersectionsub: " + reasonerFactory.createNonBufferingReasoner(createOntology(subsubmod)).getUnsatisfiableClasses().getEntitiesMinusBottom().size());
        Speed4JMeasurement.stop();




    }

    protected boolean isConsistent (Set<OWLLogicalAxiom> ontology) {
        return new Reasoner.ReasonerFactory().createNonBufferingReasoner(createOntology(ontology)).getUnsatisfiableClasses().getEntitiesMinusBottom().isEmpty();
    }

    protected Set<OWLClass> getUnsatClasses (Set<OWLLogicalAxiom> ontology) {
        return new Reasoner.ReasonerFactory().createNonBufferingReasoner(createOntology(ontology)).getUnsatisfiableClasses().getEntitiesMinusBottom();
    }

    protected Set<OWLLogicalAxiom> extractModule (Set<OWLLogicalAxiom> ontology, List<OWLClass> signature) {
        Set<OWLLogicalAxiom> result = new LinkedHashSet<OWLLogicalAxiom>();
        Set<OWLEntity> input = new LinkedHashSet<OWLEntity>();
        for (OWLEntity e : signature)
            input.add(e);
        Speed4JMeasurement.start("creation_module_extractor");
        SyntacticLocalityModuleExtractor extractor =
                new SyntacticLocalityModuleExtractor(OWLManager.createOWLOntologyManager(), createOntology(ontology), ModuleType.STAR);
        Speed4JMeasurement.stop();
        Speed4JMeasurement.start("extraction_module");
        for (OWLAxiom axiom : extractor.extract(input))
            result.add((OWLLogicalAxiom) axiom);
        Speed4JMeasurement.stop();
        return result;
    }

    @Test
    public void testIterativeDiagnosis() throws OWLOntologyCreationException {

        String onto = "fma2nci";
        ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
        Set<OWLLogicalAxiom> onto1Axioms = getAxioms("ontologies/" + onto + "genonto1.owl");
        Set<OWLLogicalAxiom> onto2Axioms = getAxioms("ontologies/" + onto + "genonto2.owl");
        Set<OWLLogicalAxiom> mappingAxioms = getAxioms("ontologies/" + onto + "genmapp.owl");
        //Set<OWLLogicalAxiom> onto1Axioms = getAxioms("ontologies/" + onto + "_gen1_onto1.owl");
        //Set<OWLLogicalAxiom> onto2Axioms = getAxioms("ontologies/" + onto + "_gen1_onto2.owl");
        //Set<OWLLogicalAxiom> mappingAxioms = getAxioms("ontologies/" + onto + "_gen1_mappings.owl");
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

        //ModuleDiagSearcher d = new ModuleMinDiagSearcher();
        //ModuleDiagSearcher d = new ModuleTargetDiagSearcher(pathMappings);
        //ModuleDiagSearcher d = new ModuleQuerDiagSearcher(pathMappings,correctAxioms,falseAxioms, false);
        ModuleDiagSearcher d = new ModuleOptQuerDiagSearcher(pathMappings,correctAxioms,falseAxioms, false);

        long time = System.currentTimeMillis();
        ModuleDiagnosis diagnosisFinder = new IterativeModuleDiagnosis(mappingAxioms, ontoAxioms,
                                                         new HornSatReasonerFactory(), d, true);
        Speed4JMeasurement.start("modulediagnosiscreation");
        //ModuleDiagnosis diagnosisFinder = new RootModuleDiagnosis(mappingAxioms, ontoAxioms,
        //        new Reasoner.ReasonerFactory(), d);
        Speed4JMeasurement.stop();

        Set<OWLLogicalAxiom> targetDiagnosis = diagnosisFinder.calculateTargetDiagnosis();
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
        assertTrue(reasoner.getUnsatisfiableClasses().getEntities().size() == 1);

        logger.info("time needed: " + time);

    }


}
