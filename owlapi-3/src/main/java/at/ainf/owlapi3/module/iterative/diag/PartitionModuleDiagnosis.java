package at.ainf.owlapi3.module.iterative.diag;

import at.ainf.diagnosis.Speed4JMeasurement;
import at.ainf.owlapi3.model.OWLModuleExtractor;
import at.ainf.owlapi3.module.iterative.ModuleDiagSearcher;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 16.05.13
 * Time: 11:51
 * To change this template use File | Settings | File Templates.
 */
public class PartitionModuleDiagnosis extends AbstractRootModuleDiagnosis {

    private static Logger logger = LoggerFactory.getLogger(RootModuleDiagnosis.class.getName());

    private final int MAX_REDUCTION_TRIALS = 5;
    private final int MAX_MODULESIZE_FOR_DEBUG = 500;
    private Set<Set<OWLLogicalAxiom>> atoms = new LinkedHashSet<Set<OWLLogicalAxiom>>();

    public PartitionModuleDiagnosis(Set<OWLLogicalAxiom> mappings, Set<OWLLogicalAxiom> ontoAxioms, OWLReasonerFactory factory, ModuleDiagSearcher moduleDiagSearcher) {
        super(mappings, ontoAxioms, factory, moduleDiagSearcher);
    }

    protected Set<OWLLogicalAxiom> createFixpointModule (Set<OWLLogicalAxiom> ontology, int nParts) {

        /*List<OWLClass> classesInModuleSignature = new LinkedList<OWLClass>(getClassesInModuleSignature(ontology));
        List<Set<OWLLogicalAxiom>> modules = recursiveModuleExtract(ontology, classesInModuleSignature, nParts);
        Set<OWLLogicalAxiom> intersection = createIntersection(modules);

        logger.info ("intersection size / intersection signature size / expressiveness intersection: " + intersection.size() + ", " + getClassesInModuleSignature(intersection).size() + ", " + getExpressivity(intersection));

        if (intersection.size() == ontology.size())
            return intersection;

        return createFixpointModule(intersection, nParts);*/
        return null;
    }

    protected List<Set<OWLLogicalAxiom>> recursiveModuleExtract (Set<OWLLogicalAxiom> ontology, List<OWLClass> signature, int nParts) {

        List<Set<OWLLogicalAxiom>> submodules = new LinkedList<Set<OWLLogicalAxiom>>();
        OWLModuleExtractor extractor = new OWLModuleExtractor(ontology);
        List<List<OWLClass>> subsignatures = calculateSubSignaturePartitioned (signature, nParts);
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

    private List<List<OWLClass>> calculateSubSignaturePartitioned (List<OWLClass> signature, int nParts){
        return null;
    }


    @Override
    public Set<OWLLogicalAxiom> calculateTargetDiagnosis() {
        //Set<OWLLogicalAxiom> fixpointModule = createFixpointModule();

        //first: partition-intersection reduction until fixpoint
        // if fixpoint-module has size higher than MAX_SIZE_FOR_DEBUG, then
        //second: reduce to root until module is smaller than MAX_SIZE_FOR_DEBUG
        //
        return null;
    }

    protected Set<OWLLogicalAxiom> reduceToRootModule(Set<OWLLogicalAxiom> module, Set<OWLClass> atom) {
        ArrayList<OWLClass> moduleSignature = new ArrayList<OWLClass>(getClassesInModuleSignature(module));
        int moduleSignatureSize = moduleSignature.size();
        int classIndex = (int) Math.floor(Math.random());
        OWLClass nextClass = moduleSignature.get(classIndex);
        return null;
    }

}
