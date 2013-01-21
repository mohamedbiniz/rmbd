package at.ainf.owlapi3.model;

import at.ainf.diagnosis.model.AbstractReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static _dev.TimeLog.start;
import static _dev.TimeLog.stop;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 19.10.12
 * Time: 10:03
 * To change this template use File | Settings | File Templates.
 */
public class ReasonerOWL extends AbstractReasoner<OWLLogicalAxiom> {


    protected static final OWLClass TOP_CLASS = OWLManager.getOWLDataFactory().getOWLThing();

    private static int cnt = 0;

    protected OWLOntology ontology;

    private OWLReasoner reasoner;
    private List<InferredAxiomGenerator<? extends OWLLogicalAxiom>> axiomGenerators;
    private boolean includeAxiomsReferencingThing;
    private boolean includeOntologyAxioms;
    private OWLReasonerFactory reasonerFactory;

    protected int num = cnt++;
    private final OWLOntologyManager owlOntologyManager;

    public ReasonerOWL(OWLOntologyManager owlOntologyManager, OWLReasonerFactory reasonerFactory) {
        this(owlOntologyManager);

        this.reasonerFactory = reasonerFactory;
        reasoner = reasonerFactory.createReasoner(this.ontology);
    }

    protected ReasonerOWL(OWLOntologyManager owlOntologyManager) {
        try {
            this.owlOntologyManager = owlOntologyManager;

            OWLOntology dontology = owlOntologyManager.createOntology();
            OWLLiteral lit = owlOntologyManager.getOWLDataFactory().getOWLLiteral("Test Reasoner Ontology " + num);
            IRI iri = OWLRDFVocabulary.RDFS_COMMENT.getIRI();
            OWLAnnotation anno = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(iri), lit);
            owlOntologyManager.applyChange(new AddOntologyAnnotation(dontology, anno));
            this.ontology = dontology;

        } catch (OWLOntologyCreationException e) {
            throw new OWLRuntimeException(e);
        }
    }

    @Override
    public boolean isConsistent() {
        sync();
        return reasoner.isConsistent();
    }

    @Override
    public boolean isCoherent() {
        sync();
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        return reasoner.getBottomClassNode().isSingleton();
    }

    @Override
    public boolean isEntailed(Set<OWLLogicalAxiom> test) {
        sync();
        return reasoner.isEntailed(test);
    }

    @Override
    public ReasonerOWL newInstance() {
        return new ReasonerOWL(getOWLOntologyManager(), reasonerFactory);
    }

    @Override
    public Set<OWLLogicalAxiom> getEntailments() {

        sync();
        InferenceType[] infType = new InferenceType[]{InferenceType.CLASS_HIERARCHY, InferenceType.CLASS_ASSERTIONS,
                InferenceType.DISJOINT_CLASSES, InferenceType.DIFFERENT_INDIVIDUALS, InferenceType.SAME_INDIVIDUAL};
        if (!getAxiomGenerators().isEmpty())
            reasoner.precomputeInferences(infType);

        Set<OWLLogicalAxiom> entailments = new LinkedHashSet<OWLLogicalAxiom>();
        for (InferredAxiomGenerator<? extends OWLLogicalAxiom> axiomGenerator : getAxiomGenerators()) {
            for (OWLLogicalAxiom ax : axiomGenerator.createAxioms(ontology.getOWLOntologyManager(), reasoner)) {
                if (!ontology.containsAxiom(ax) || isIncludeOntologyAxioms())
                    if (!ax.getClassesInSignature().contains(TOP_CLASS) || isIncludeAxiomsReferencingThing()) {
                        entailments.add(ax);
                    }

            }
        }

        if (isIncludeOntologyAxioms())
            entailments.addAll(ontology.getLogicalAxioms());
        return entailments;
    }

    protected void doReasonerFlush() {
        start("Reasoner sync ");
        reasoner.flush();
        stop();
    }

    @Override
    protected void updateReasonerModel(Set<OWLLogicalAxiom> axiomsToAdd, Set<OWLLogicalAxiom> axiomsToRemove) {
        if (lock != null)
            lock.lock();
        try {
            if (!axiomsToAdd.isEmpty())
                ontology.getOWLOntologyManager().addAxioms(ontology, axiomsToAdd);
            if (!axiomsToRemove.isEmpty())
                ontology.getOWLOntologyManager().removeAxioms(ontology, axiomsToRemove);
            doReasonerFlush();
        } finally {
            if (lock != null)
                lock.unlock();
        }
    }

    public void setAxiomGenerators(List<InferredAxiomGenerator<? extends OWLLogicalAxiom>> axiomGenerators) {
        this.axiomGenerators = axiomGenerators;
    }

    public void setIncludeOntologyAxioms(boolean incOntologyAxioms) {
        includeOntologyAxioms = incOntologyAxioms;
    }

    public void setIncludeAxiomsReferencingThing(boolean incAxiomsReferencingThing) {
        includeAxiomsReferencingThing = incAxiomsReferencingThing;

    }

    public boolean isIncludeAxiomsReferencingThing() {
        return includeAxiomsReferencingThing;
    }

    public boolean isIncludeOntologyAxioms() {
        return includeOntologyAxioms;
    }

    public List<InferredAxiomGenerator<? extends OWLLogicalAxiom>> getAxiomGenerators() {
        return axiomGenerators;
    }

    public Set<OWLClass> getUnsatisfiableEntities() {
        return reasoner.getUnsatisfiableClasses().getEntities();
    }

    public String toString() {
        return "reasoner " + num;
    }

    public OWLOntologyManager getOWLOntologyManager() {
        return this.owlOntologyManager;
    }
}
