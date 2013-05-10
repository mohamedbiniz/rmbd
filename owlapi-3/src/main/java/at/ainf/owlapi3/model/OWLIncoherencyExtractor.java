package at.ainf.owlapi3.model;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.util.Collections;
import java.util.HashSet;
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

    protected OWLReasonerFactory reasonerFactory;

    public OWLIncoherencyExtractor(OWLReasonerFactory reasonerFactory) {
        this.reasonerFactory = reasonerFactory;
    }

    public OWLOntology getIncoherentPartAsOntology(OWLOntology ontology) {
        return extract(ontology,false, false).iterator().next();
    }

    public OWLOntology getIncoherentPartAsOntologyUsingMultiple(OWLOntology ontology) {
        return extract(ontology,false, true).iterator().next();
    }

    public Set<OWLOntology> getIncoherentPartAsMultipleOntologies(OWLOntology ontology) {
        return extract(ontology, true, false);
    }

    protected OWLOntology createCopyForExtraction(OWLOntology ontology) {

        OWLOntology result = null;
        try {
            result = OWLManager.createOWLOntologyManager().createOntology(IRI.create("http://ainf.at/TempExtractionOntology"));
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        result.getOWLOntologyManager().addAxioms(result,ontology.getLogicalAxioms());
        return result;
    }

    protected Set<OWLOntology> extract(OWLOntology ont, boolean multiple, boolean useMultiple) {

        Set<OWLEntity> signature = new LinkedHashSet<OWLEntity>();
        OWLOntology ontology = createCopyForExtraction(ont);
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
        Set<OWLAxiom> aBoxAxioms = null;
        boolean consistent = reasoner.isConsistent();
        if (!consistent) {
            //OWLOntologyManager man = OWLManager.createOWLOntologyManager(); //ontology.getOWLOntologyManager();
            aBoxAxioms = ontology.getABoxAxioms(false);      //true
            ontology.getOWLOntologyManager().removeAxioms(ontology, aBoxAxioms);
            reasoner.flush();
            if (!reasoner.isConsistent())
                throw new RuntimeException("The ontology without ABox is not consistent! Reasoner Flush Problem? ");
            /*for (OWLAxiom aBoxAxiom : aBoxAxioms) {
                // if contains negation
                signature.addAll(aBoxAxiom.getClassesInSignature());
            }*/
        }

        for (OWLClass entity : reasoner.getUnsatisfiableClasses().getEntities())
            signature.add(entity);

        signature.remove(OWLManager.getOWLDataFactory().getOWLNothing());


        Set<OWLOntology> result;

        SyntacticLocalityModuleExtractor sme = new SyntacticLocalityModuleExtractor(OWLManager.createOWLOntologyManager(), ontology, ModuleType.STAR);


        String iriString = "http://ainf.at/IncoherencyModule";
        try {
            if (!signature.isEmpty()) {
                if (multiple) {
                    result = new LinkedHashSet<OWLOntology>();
                    int cnt = 0;
                    for (OWLEntity i : signature) {
                        result.add(sme.extractAsOntology(Collections.singleton(i), IRI.create(iriString + "_" + cnt)));
                        cnt++;
                    }
                }
                else {
                    if (!useMultiple) {
                        result = Collections.singleton(sme.extractAsOntology(signature, IRI.create(iriString)));
                    }
                    else {
                        result = new LinkedHashSet<OWLOntology>();
                        int cnt = 0;
                        for (OWLEntity i : signature) {
                            result.add(sme.extractAsOntology(Collections.singleton(i), IRI.create(iriString + "_" + cnt)));
                            cnt++;
                        }
                        Set<OWLLogicalAxiom> axioms = new HashSet<OWLLogicalAxiom>();
                        for (OWLOntology on : result)
                            axioms.addAll(on.getLogicalAxioms());
                        result = Collections.singleton(OWLManager.createOWLOntologyManager().createOntology(IRI.create(iriString)));
                        OWLOntology on = result.iterator().next();
                        on.getOWLOntologyManager().addAxioms(on,axioms);
                    }
                }

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
