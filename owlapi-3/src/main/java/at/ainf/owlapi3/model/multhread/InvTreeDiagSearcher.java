package at.ainf.owlapi3.model.multhread;

import at.ainf.owlapi3.model.intersection.OWLEqualIntersectionExtractor;
import at.ainf.owlapi3.module.iterative.diag.AbstractModuleDiagnosis;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static at.ainf.owlapi3.util.OWLUtils.calculateSignature;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 27.05.13
 * Time: 08:59
 * To change this template use File | Settings | File Templates.
 */
public class InvTreeDiagSearcher extends AbstractModuleDiagnosis {

    private final static int USABLE_CORES = Runtime.getRuntime().availableProcessors() - 1;

    public InvTreeDiagSearcher(Set<OWLLogicalAxiom> mappings, Set<OWLLogicalAxiom> ontoAxioms, OWLReasonerFactory factory) {
        super (mappings, ontoAxioms, factory);
    }

    @Override
    public Set<OWLLogicalAxiom> calculateTargetDiagnosis() {
        List<List<OWLClass>> subSignatures = calculateSubSignatures(getMappings(), USABLE_CORES);

        ExecutorService pool = Executors.newFixedThreadPool(USABLE_CORES);
        List<Future<Set<OWLLogicalAxiom>>> futures = startWorkerThreads(pool, subSignatures, USABLE_CORES);

        return computeTargetDiagnosis(futures);
    }

    protected Set<OWLLogicalAxiom> computeTargetDiagnosis(List<Future<Set<OWLLogicalAxiom>>> futures) {
        Set<OWLLogicalAxiom> targetDiagnosis = new HashSet<OWLLogicalAxiom>();
        for (Future<Set<OWLLogicalAxiom>> future : futures) {
            try {
                Set<OWLLogicalAxiom> diagnosis = future.get();
                targetDiagnosis.addAll(diagnosis);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ExecutionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return targetDiagnosis;
    }

    protected List<Future<Set<OWLLogicalAxiom>>> startWorkerThreads(ExecutorService pool, List<List<OWLClass>> subSignatures, int usableCores) {
        List<Future<Set<OWLLogicalAxiom>>> futures = new ArrayList<Future<Set<OWLLogicalAxiom>>>(usableCores);
        for (List<OWLClass> signature : subSignatures)
            futures.add(pool.submit(new ModuleInvTreeTask(getMappings(), getOntoAxioms(), signature,getReasonerFactory())));
        return futures;

    }

    protected List<List<OWLClass>> calculateSubSignatures(Set<OWLLogicalAxiom> mappings, int parts) {
        List<OWLClass> signatureMappings = new LinkedList<OWLClass>(calculateSignature(mappings));
        OWLEqualIntersectionExtractor extractor = new OWLEqualIntersectionExtractor(parts);

        return extractor.calculateSubSignatures(signatureMappings);
    }


}
