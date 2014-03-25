package at.ainf.owlapi3.model.intersection;

import at.ainf.owlapi3.model.OWLModuleExtractor;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static at.ainf.owlapi3.util.OWLUtils.calculateSignature;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 16.05.13
 * Time: 11:47
 * To change this template use File | Settings | File Templates.
 */
public class OWLEqualIntersectionExtractor extends AbstractOWLIntersectionExtractor {

    private static Logger logger = LoggerFactory.getLogger(OWLEqualIntersectionExtractor.class.getName());

    private final static int USABLE_CORES = Runtime.getRuntime().availableProcessors() - 1;

    private int split;

    public OWLEqualIntersectionExtractor(int split) {
        this.split = split;
    }

    public class ModuleCalculationTask implements Callable<Set<OWLLogicalAxiom>> {

        private final Set<OWLLogicalAxiom> axioms;

        private final List<OWLClass> subSig;

        public ModuleCalculationTask(Set<OWLLogicalAxiom> axioms, List<OWLClass> subSig) {
            this.axioms = axioms;
            this.subSig = subSig;
        }

        @Override
        public Set<OWLLogicalAxiom> call() throws Exception {

            OWLModuleExtractor owlModuleExtractor = new OWLModuleExtractor(axioms);
            Set<OWLLogicalAxiom> module = owlModuleExtractor.calculateModule(subSig);
            logger.info("extracted module of size: " + module.size());

            return module;
        }


    }

    @Override
    protected List<Set<OWLLogicalAxiom>> calculateModules (Set<OWLLogicalAxiom> axioms) {
        List<OWLClass> signature = new LinkedList<OWLClass>(calculateSignature(axioms));
        OWLModuleExtractor extractor = new OWLModuleExtractor(axioms);

        List<List<OWLClass>> subsignatures = calculateSubSignatures(signature);

        ExecutorService pool = Executors.newFixedThreadPool(USABLE_CORES);
        List<Future<Set<OWLLogicalAxiom>>> futures = new ArrayList<Future<Set<OWLLogicalAxiom>>>(split);
        for (List<OWLClass> subsignature : subsignatures)
            futures.add(pool.submit(new ModuleCalculationTask(axioms,subsignature)));
        List<Set<OWLLogicalAxiom>> submodules = new LinkedList<Set<OWLLogicalAxiom>>();
        for (List<OWLClass> s : subsignatures)
            submodules.add(extractor.calculateModule(s));

        return submodules;
    }

    public List<List<OWLClass>> calculateSubSignatures(List<OWLClass> signature) {
        int size = signature.size() / split;
        List<List<OWLClass>> subsignatures = new LinkedList<List<OWLClass>>();
        for (int i = 0; i < split; i++) {
            LinkedList<OWLClass> s = new LinkedList<OWLClass>(signature.subList(size * i, size * (i + 1)));
            subsignatures.add(s);
        }
        return  subsignatures;
    }

}
