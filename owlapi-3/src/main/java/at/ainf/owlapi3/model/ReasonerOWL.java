package at.ainf.owlapi3.model;

import at.ainf.diagnosis.model.AbstractReasoner;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 19.10.12
 * Time: 10:03
 * To change this template use File | Settings | File Templates.
 */
public class ReasonerOWL extends AbstractReasoner<OWLLogicalAxiom> {

    private OWLOntology ontology;

    private OWLReasoner reasoner;

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
        return false;
    }

    @Override
    public void sync() {
        Set<OWLLogicalAxiom> axiomsToAdd = new HashSet<OWLLogicalAxiom>();
        Set<OWLLogicalAxiom> axiomsToRemove = new HashSet<OWLLogicalAxiom>();

        for (OWLLogicalAxiom axiom : getReasonendFormulars()) {
            if (!ontology.containsAxiom(axiom))
                axiomsToAdd.add(axiom);
        }

        for (OWLLogicalAxiom axiom : ontology.getLogicalAxioms()) {
            if (!getReasonendFormulars().contains(axiom))
                axiomsToRemove.add(axiom);
        }

        if (!axiomsToAdd.isEmpty())
            ontology.getOWLOntologyManager().addAxioms(ontology, axiomsToAdd);
        if (!axiomsToRemove.isEmpty())
            ontology.getOWLOntologyManager().removeAxioms(ontology, axiomsToRemove);
    }

}
