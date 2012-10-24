package at.ainf.owlapi3.model;

import at.ainf.diagnosis.model.AbstractReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import java.util.*;

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

    private static final OWLClass TOP_CLASS = OWLManager.getOWLDataFactory().getOWLThing();

    private OWLOntology ontology;

    private OWLReasoner reasoner;

    private List<InferredAxiomGenerator<? extends OWLLogicalAxiom>> axiomGenerators;

    private boolean includeAxiomsReferencingThing;

    private boolean includeOntologyAxioms;

    public ReasonerOWL(OWLOntologyManager owlOntologyManager, OWLReasonerFactory reasonerFactory) {
        try {

            OWLOntology dontology = owlOntologyManager.createOntology();
            OWLLiteral lit = owlOntologyManager.getOWLDataFactory().getOWLLiteral("Test Reasoner Ontology ");
            IRI iri = OWLRDFVocabulary.RDFS_COMMENT.getIRI();
            OWLAnnotation anno = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(iri), lit);
            owlOntologyManager.applyChange(new AddOntologyAnnotation(dontology, anno));
            this.ontology = dontology;

            reasoner = reasonerFactory.createReasoner(this.ontology);
        } catch (OWLOntologyCreationException e) {
            throw new OWLRuntimeException(e);
        }
    }

    @Override
    public boolean isConsistent() {

        return reasoner.isConsistent();
    }

    @Override
    public boolean isCoherent() {
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        return reasoner.getBottomClassNode().isSingleton();
    }

    @Override
    public boolean isEntailed(Set<OWLLogicalAxiom> test) {

        return reasoner.isEntailed(test);
    }

    @Override
    public Set<OWLLogicalAxiom> getEntailments() {

        InferenceType[] infType = new InferenceType[]{InferenceType.CLASS_HIERARCHY, InferenceType.CLASS_ASSERTIONS,
                InferenceType.DISJOINT_CLASSES, InferenceType.DIFFERENT_INDIVIDUALS, InferenceType.SAME_INDIVIDUAL};
        if (!axiomGenerators.isEmpty())
            reasoner.precomputeInferences(infType);

        Set<OWLLogicalAxiom> entailments = new LinkedHashSet<OWLLogicalAxiom>();
        for (InferredAxiomGenerator<? extends OWLLogicalAxiom> axiomGenerator : axiomGenerators) {
            for (OWLLogicalAxiom ax : axiomGenerator.createAxioms(ontology.getOWLOntologyManager(), reasoner)) {
                if (!ontology.containsAxiom(ax) || includeOntologyAxioms)
                    if (!ax.getClassesInSignature().contains(TOP_CLASS) || includeAxiomsReferencingThing) {
                        entailments.add(ax);
                    }

            }
        }

        if (includeOntologyAxioms)
            entailments.addAll(ontology.getLogicalAxioms());
        return entailments;
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

    public Set<OWLClass> getUnsatisfiableEntities() {
        return reasoner.getUnsatisfiableClasses().getEntities();
    }

    @Override
    public void sync() {
        Set<OWLLogicalAxiom> axiomsToAdd = new HashSet<OWLLogicalAxiom>();
        Set<OWLLogicalAxiom> axiomsToRemove = new HashSet<OWLLogicalAxiom>();

        for (OWLLogicalAxiom axiom : getFormularCache()) {
            if (!ontology.containsAxiom(axiom))
                axiomsToAdd.add(axiom);
        }

        for (OWLLogicalAxiom axiom : ontology.getLogicalAxioms()) {
            if (!getFormularCache().contains(axiom))
                axiomsToRemove.add(axiom);
        }

        if (!axiomsToAdd.isEmpty())
            ontology.getOWLOntologyManager().addAxioms(ontology, axiomsToAdd);
        if (!axiomsToRemove.isEmpty())
            ontology.getOWLOntologyManager().removeAxioms(ontology, axiomsToRemove);
        start("Reasoner sync ");
        reasoner.flush();
        stop();

    }

}
