package at.ainf.owlapi3.model;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 24.04.12
 * Time: 10:44
 * To change this template use File | Settings | File Templates.
 */
public class OWLIncoherencyExtractor {

    private OWLReasonerFactory reasonerFactory;

    public OWLIncoherencyExtractor(OWLReasonerFactory reasonerFactory) {
        this.reasonerFactory = reasonerFactory;
    }

    public OWLOntology getIncoherentPartAsOntology(OWLOntology ontology) {
        return extract(ontology,false).iterator().next();
    }

    public Set<OWLOntology> getIncoherentPartAsMultipleOntologies(OWLOntology ontology) {
        return extract(ontology, true);
    }

    private Set<OWLOntology> extract(OWLOntology ontology, boolean multiple) {
        Set<OWLEntity> incoherentEntities = new LinkedHashSet<OWLEntity>();
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
        Set<OWLAxiom> aBoxAxioms = null;
        boolean consistent = reasoner.isConsistent();
        if (!consistent) {
            OWLOntologyManager man = OWLManager.createOWLOntologyManager(); //ontology.getOWLOntologyManager();
            aBoxAxioms = ontology.getABoxAxioms(false);
            man.removeAxioms(ontology, aBoxAxioms);
        }

        reasoner.flush();
        for (OWLClass entity : reasoner.getUnsatisfiableClasses().getEntities())
            incoherentEntities.add(entity);

        incoherentEntities.remove(OWLManager.getOWLDataFactory().getOWLNothing());


        Set<OWLOntology> result;

        SyntacticLocalityModuleExtractor sme = new SyntacticLocalityModuleExtractor(OWLManager.createOWLOntologyManager(), ontology, ModuleType.STAR);


        String iriString = "http://ainf.at/IncoherencyModule";
        try {
            if (!incoherentEntities.isEmpty()) {
                if (multiple) {
                    result = new LinkedHashSet<OWLOntology>();
                    int cnt = 0;
                    for (OWLEntity i : incoherentEntities) {
                        result.add(sme.extractAsOntology(Collections.singleton(i), IRI.create(iriString + "_" + cnt)));
                        cnt++;
                    }
                }
                else
                    result = Collections.singleton(sme.extractAsOntology(incoherentEntities, IRI.create(iriString)));

            } else
                result = Collections.singleton(OWLManager.createOWLOntologyManager().createOntology(IRI.create(iriString)));
        } catch (OWLOntologyCreationException e) {
            result = null;
        }

        if (!consistent)
            ontology.getOWLOntologyManager().addAxioms(ontology, aBoxAxioms);

        return result;

    }



}
