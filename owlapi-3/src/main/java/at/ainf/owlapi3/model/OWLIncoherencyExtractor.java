package at.ainf.owlapi3.model;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.util.Collections;
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

    private OWLReasoner reasoner;

    private OWLOntology ontology;

    public OWLIncoherencyExtractor(OWLReasonerFactory factory, OWLOntology ontology) {
        this.reasoner = factory.createReasoner(ontology);
        this.ontology = ontology;

    }

    public OWLOntology getIncoherentPartAsOntology() {
        if(reasoner.isConsistent()) {
            OWLOntology r=null;
            Set<OWLClass> entities = reasoner.getUnsatisfiableClasses().getEntities();

            Set<OWLEntity> setOfEntities = new LinkedHashSet<OWLEntity>();
            for (OWLClass entity : entities)
                setOfEntities.add((OWLEntity)entity);

            setOfEntities.remove(OWLManager.getOWLDataFactory().getOWLNothing());
            if (!entities.isEmpty()) {
                SyntacticLocalityModuleExtractor sme = new SyntacticLocalityModuleExtractor(ontology.getOWLOntologyManager(), ontology, ModuleType.STAR);
                IRI moduleIRI = IRI.create("http://ainf.at/IncoherencyModule");
                try {
                    r = sme.extractAsOntology(setOfEntities, moduleIRI);
                } catch (OWLOntologyCreationException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            return r;
        }
        else {
//            Set<OWLAxiom> a = ontology.getABoxAxioms(false);
//            ontology.getOWLOntologyManager().removeAxioms(ontology,a);
//            reasoner.flush();
//            Set<OWLClass> e = reasoner.getUnsatisfiableClasses().getEntities();
//
//            try {
//                OWLOntology test =
//                    ontology.getOWLOntologyManager().createOntology(IRI.create("test"),Collections.singleton(ontology));
//                    ontology.getOWLOntologyManager().removeAxioms(test,a);
//                    OWLOntology t = ontology;
//                    ontology = test;
//                    reasoner.flush();
//            } catch (OWLOntologyCreationException e1) {
//                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            }
//            Set<OWLClass> f = reasoner.getUnsatisfiableClasses().getEntities();

            return ontology;
        }
    }

    public Set<OWLOntology> getIncoherentPartAsOntologies() {

        if(!reasoner.isConsistent())
            return Collections.emptySet();

        Set<OWLOntology> result =new LinkedHashSet<OWLOntology>();
        Set<OWLClass> entities = reasoner.getUnsatisfiableClasses().getEntities();
        Set<OWLEntity> setOfEntities = new LinkedHashSet<OWLEntity>();
        for (OWLClass entity : entities)
            setOfEntities.add((OWLEntity)entity);

        setOfEntities.remove(OWLManager.getOWLDataFactory().getOWLNothing());
        if (!entities.isEmpty()) {
            SyntacticLocalityModuleExtractor sme = new SyntacticLocalityModuleExtractor(ontology.getOWLOntologyManager(), ontology, ModuleType.STAR);
            IRI moduleIRI = IRI.create("http://ainf.at/IncoherencyModule");
            try {
                for (OWLEntity entitiy : entities) {
                    Set<OWLEntity> e = new LinkedHashSet<OWLEntity>();
                    e.add(entitiy);
                    result.add(sme.extractAsOntology(e, IRI.create("http://ainf.at/IncoherencyModule"
                            + entitiy.toString())));
                }
            } catch (OWLOntologyCreationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return result;
    }

}
