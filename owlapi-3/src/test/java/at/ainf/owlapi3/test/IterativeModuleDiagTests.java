package at.ainf.owlapi3.test;

import at.ainf.diagnosis.Speed4JMeasurement;
import at.ainf.owlapi3.module.OtfModuleProvider;
import at.ainf.owlapi3.module.iterative.*;
import at.ainf.owlapi3.module.iterative.diag.IterativeModuleDiagnosis;
import at.ainf.owlapi3.module.iterative.diag.ModuleDiagnosis;
import at.ainf.owlapi3.module.iterative.diag.RootModuleDiagnosis;
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

    @Test
    public void testExtracionModule() throws OWLOntologyCreationException {

        String onto = "fma2nci";
        ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());

        Set<OWLLogicalAxiom> onto1Axioms = getAxioms("ontologies/" + onto + "genonto1.owl");
        Set<OWLLogicalAxiom> onto2Axioms = getAxioms("ontologies/" + onto + "genonto2.owl");
        Set<OWLLogicalAxiom> mappingAxioms = getAxioms("ontologies/" + onto + "genmapp.owl");
        Set<OWLLogicalAxiom> ontoAxioms = new HashSet<OWLLogicalAxiom>();
        ontoAxioms.addAll(onto1Axioms);
        ontoAxioms.addAll(onto2Axioms);

        Set<OWLLogicalAxiom> fullOnto = new LinkedHashSet<OWLLogicalAxiom>(ontoAxioms);
        fullOnto.addAll(mappingAxioms);
        logger.info("onto size: " + fullOnto.size());

        List<OWLClass> signature = new LinkedList<OWLClass>(getClassesInModuleSignature(fullOnto));
        logger.info("signature size: " + signature.size());

        //List<List<OWLClass>> subsignatures = calculateSubSignaturePartitioned(signature,20);
        List<List<OWLClass>> subsignatures = calculateSubSignatureOfSize(signature,500,10);

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
            Collections.sort(sortedClassesInSignature, new Comparator<OWLClass>() {
                @Override
                public int compare(OWLClass o1, OWLClass o2) {
                    Integer levelO1 = Collections.min(reasoner.getRelevantCore().getSymbolsMap().get(reasoner.getIndex(o1)));
                    Integer levelO2 = Collections.min(reasoner.getRelevantCore().getSymbolsMap().get(reasoner.getIndex(o2)));
                    return levelO1.compareTo(levelO2);
                }
            });

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
        SyntacticLocalityModuleExtractor extractor =
                new SyntacticLocalityModuleExtractor(OWLManager.createOWLOntologyManager(), createOntology(ontology), ModuleType.STAR);
        for (OWLAxiom axiom : extractor.extract(input))
            result.add((OWLLogicalAxiom) axiom);
        return result;
    }

    @Test
    public void testIterativeDiagnosis() throws OWLOntologyCreationException {

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

        //ModuleDiagSearcher d = new ModuleMinDiagSearcher();
        //ModuleDiagSearcher d = new ModuleTargetDiagSearcher(pathMappings);
        ModuleDiagSearcher d = new ModuleQuerDiagSearcher(pathMappings,correctAxioms,falseAxioms, false);

        long time = System.currentTimeMillis();
        //ModuleDiagnosis diagnosisFinder = new IterativeModuleDiagnosis(mappingAxioms, ontoAxioms,
        //                                                 new Reasoner.ReasonerFactory(), d, true);
        Speed4JMeasurement.start("modulediagnosiscreation");
        ModuleDiagnosis diagnosisFinder = new RootModuleDiagnosis(mappingAxioms, ontoAxioms,
                new Reasoner.ReasonerFactory(), d);
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
