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

    private OWLReasonerFactory reasonerFactory;

    private OWLOntology ontology;

    public OWLIncoherencyExtractor(OWLReasonerFactory reasonerFactory, OWLOntology ontology) {
        this.reasonerFactory = reasonerFactory;
        this.ontology = ontology;

    }

    public OWLOntology getIncoherentPartAsOntology() {
        Set<OWLEntity> incoherentEntities = new LinkedHashSet<OWLEntity>();
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);

        if (reasoner.isConsistent()) {
            for(OWLClass entity : reasoner.getUnsatisfiableClasses().getEntities())
                incoherentEntities.add(entity);
        }
        else {
            try {
            OWLOntologyManager man = ontology.getOWLOntologyManager();
            OWLOntology test = man.createOntology(IRI.create("test"), Collections.singleton(ontology));

            man.removeAxioms(test,ontology.getABoxAxioms(false));

            OWLReasoner woAboxReasoner = reasonerFactory.createReasoner(test);
            for(OWLClass entity : woAboxReasoner.getUnsatisfiableClasses().getEntities())
                incoherentEntities.add(entity);
            man.removeOntology(test);
            }
            catch(OWLOntologyCreationException e) {
                return null;
            }
        }
        incoherentEntities.remove(OWLManager.getOWLDataFactory().getOWLNothing());

        OWLOntology r=null;
        if (!incoherentEntities.isEmpty()) {
            SyntacticLocalityModuleExtractor sme = new SyntacticLocalityModuleExtractor(ontology.getOWLOntologyManager(), ontology, ModuleType.STAR);
            IRI moduleIRI = IRI.create("http://ainf.at/IncoherencyModule");
            try {
                r = sme.extractAsOntology(incoherentEntities, moduleIRI);
            } catch (OWLOntologyCreationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return r;
        }
        else {
            return ontology;
        }
    }

    public Set<OWLOntology> getIncoherentPartAsOntologies() {

        if(!reasonerFactory.createReasoner(ontology).isConsistent())
            return Collections.emptySet();

        Set<OWLOntology> result =new LinkedHashSet<OWLOntology>();
        Set<OWLClass> entities = reasonerFactory.createReasoner(ontology).getUnsatisfiableClasses().getEntities();
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
