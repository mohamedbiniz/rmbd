package at.ainf.owlapi3.module.iterative.modulediagnosis;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.model.OWLModuleExtractor;
import at.ainf.owlapi3.module.iterative.diagsearcher.ModuleDiagSearcher;
import at.ainf.owlapi3.module.iterative.modulediagnosis.AbstractRootModuleDiagnosis;
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

    private static Logger logger = LoggerFactory.getLogger(PartitionModuleDiagnosis.class.getName());

    private final int MAX_REDUCTION_TRIALS = 5;
    private final int MAX_MODULESIZE_FOR_DEBUG = 500;
    private Set<Set<OWLLogicalAxiom>> atoms = new LinkedHashSet<Set<OWLLogicalAxiom>>();



    public PartitionModuleDiagnosis(Set<OWLLogicalAxiom> mappings, Set<OWLLogicalAxiom> ontoAxioms, OWLReasonerFactory factory, ModuleDiagSearcher moduleDiagSearcher) {
        super(mappings, ontoAxioms, factory, moduleDiagSearcher);
    }


    private class RepairInfo {
        Set<OWLLogicalAxiom> partialDiag;
        Set<OWLEntity> repairedClasses;

        RepairInfo(){

        }

        private Set<OWLLogicalAxiom> getPartialDiag() {
            return partialDiag;
        }

        private void setPartialDiag(Set<OWLLogicalAxiom> partialDiag) {
            this.partialDiag = partialDiag;
        }

        private Set<OWLEntity> getRepairedClasses() {
            return repairedClasses;
        }

        private void setRepairedClasses(Set<OWLEntity> repairedClasses) {
            this.repairedClasses = repairedClasses;
        }
    }

    private class SplitSignature {
        Set<OWLClass> left;
        Set<OWLClass> right;

        SplitSignature(){

        }

        private Set<OWLClass> getLeft() {
            return left;
        }

        private void setLeft(Set<OWLClass> left) {
            this.left = left;
        }

        private Set<OWLClass> getRight() {
            return right;
        }

        private void setRight(Set<OWLClass> right) {
            this.right = right;
        }
    }

    private SplitSignature splitSignature (Set<OWLClass> signature){
        int sigSize = signature.size();
        int splitPoint = sigSize/10;
        List<OWLClass> sig = new LinkedList<OWLClass>(signature);
        List<OWLClass> left = sig.subList(0,splitPoint);
        List<OWLClass> right = sig.subList(splitPoint,sigSize);
        SplitSignature ss = new SplitSignature();
        ss.setLeft(new HashSet<OWLClass>(left));
        ss.setRight(new HashSet<OWLClass>(right));
        return ss;
    }

    private Set<OWLLogicalAxiom> debug(Set<OWLLogicalAxiom> axioms){
        Set<OWLLogicalAxiom> background = new HashSet<OWLLogicalAxiom>(getOntoAxioms());
        background.retainAll(axioms);
        Set<OWLLogicalAxiom> diagnosis = getDiagSearcher().calculateDiag(axioms, background);
        return diagnosis;
    }

    private Set<OWLLogicalAxiom> fastRepair(Set<OWLLogicalAxiom> ontology){
        Set<OWLClass> ontoSignature = getClassesInModuleSignature(ontology);
        SplitSignature ss;
        OWLModuleExtractor extractor = new OWLModuleExtractor(ontology);
        Set<OWLLogicalAxiom> targetDiag = new HashSet<OWLLogicalAxiom>();
        while(!isDebuggable(ontology,ontoSignature)){
            ss = splitSignature(ontoSignature);
            Set<OWLLogicalAxiom> leftModule = extractor.calculateModule(ss.getLeft());
            Set<OWLLogicalAxiom> rightModule = extractor.calculateModule(ss.getRight());
            Set<OWLLogicalAxiom> iModule = leftModule;
            iModule.retainAll(rightModule);
            Set<OWLClass> iModSignature = getClassesInModuleSignature(iModule);
            Set<OWLLogicalAxiom> iModDiag = fastRepair(iModule);
            targetDiag.addAll(iModDiag);
            ontology.removeAll(iModDiag);
            ontoSignature.removeAll(iModSignature);
        }
        Set<OWLLogicalAxiom> partialDiag = debug(ontology);
        targetDiag.addAll(partialDiag);
        return targetDiag;
    }

    private boolean isDebuggable(Set<OWLLogicalAxiom> axioms, Set<OWLClass> signature){
        if(axioms.size() < 500 || signature.size() < 50)
            return true;
        return false;
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

//        List<Set<OWLLogicalAxiom>> submodules = new LinkedList<Set<OWLLogicalAxiom>>();
//        OWLModuleExtractor extractor = new OWLModuleExtractor(ontology);
//        List<List<OWLClass>> subsignatures = calculateSubSignaturePartitioned (signature, nParts);
//        for (List<OWLClass> s : subsignatures) {
//            Speed4JMeasurement.start("submodules_extraction_single");
//            Set<OWLLogicalAxiom> submod = extractor.calculateModule(s);
//            long time = Speed4JMeasurement.stop();
//            //logger.info("parts / time / start signature size / submodul size / signature size: " +
//            //        split + ", " + time + ", " + s.size() + ", " + submod.size() + ", " + getClassesInModuleSignature(submod).size());
//            submodules.add(submod);
//        }
//        return submodules;
        return null;
    }

    private List<List<OWLClass>> calculateSubSignaturePartitioned (List<OWLClass> signature, int nParts){
        return null;
    }



    protected Set<OWLLogicalAxiom> reduceToRootModule(Set<OWLLogicalAxiom> module, Set<OWLClass> atom) {
//        ArrayList<OWLClass> moduleSignature = new ArrayList<OWLClass>(getClassesInModuleSignature(module));
//        int moduleSignatureSize = moduleSignature.size();
//        int classIndex = (int) Math.floor(Math.random());
//        OWLClass nextClass = moduleSignature.get(classIndex);
        return null;
    }

    @Override
    public Set<FormulaSet<OWLLogicalAxiom>> start() throws SolverException, NoConflictException, InconsistentTheoryException {
        //Set<OWLLogicalAxiom> fixpointModule = createFixpointModule();

                //first: partition-intersection reduction until fixpoint
                // if fixpoint-module has size higher than MAX_SIZE_FOR_DEBUG, then
                //second: reduce to root until module is smaller than MAX_SIZE_FOR_DEBUG
                //
        Set<OWLLogicalAxiom> ontology = new HashSet<OWLLogicalAxiom>(getOntoAxioms());
        ontology.addAll(getMappings());
        return Collections.singleton(createFormularSet(fastRepair(ontology)));
    }

}
