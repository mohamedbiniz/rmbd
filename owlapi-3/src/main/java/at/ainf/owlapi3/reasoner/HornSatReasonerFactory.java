package at.ainf.owlapi3.reasoner;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: kostya
 * Date: 12.04.13
 * Time: 10:08
 * To change this template use File | Settings | File Templates.
 */
public class HornSatReasonerFactory extends StructuralReasonerFactory {

    private HornSatReasoner.OWLSatStructure owlSatStructure = null;

    /*
    private boolean precompute = true;
    private OWLOntology ontology = null;
    */


    @Override
    public String getReasonerName() {
        return HornSatReasoner.NAME;
    }

    /*


    public void precomputeUnsatClasses(OWLOntology ontology) {
        HornSatReasoner reasoner = new HornSatReasoner(ontology, new SimpleConfiguration(), BufferingMode.BUFFERING);

    }


     public Collection<OWLClass> getUnsatClasses() {
        return getOWLSatStructure().getUnsatClasses();
    }

    public void setPrecomputeUnsatClasses(boolean precompute) {
        this.precompute = precompute;
    }

    public boolean isPrecomputingUnSatClasses() {
        return precompute;
    }
    */

    public HornSatReasoner.OWLSatStructure getOWLSatStructure() {
        return owlSatStructure;
    }

    @Override
    public OWLReasoner createNonBufferingReasoner(OWLOntology ontology) {
        return createNonBufferingReasoner(ontology, new SimpleConfiguration());
    }

    @Override
    public OWLReasoner createNonBufferingReasoner(OWLOntology ontology, OWLReasonerConfiguration config) throws IllegalConfigurationException {
        return createReasoner(ontology, config, BufferingMode.NON_BUFFERING);
    }

    private HornSatReasoner createReasoner(OWLOntology ontology, OWLReasonerConfiguration config, BufferingMode buffering) {
        //return new HornSatReasoner(ontology, config, buffering);

        if (getOWLSatStructure() == null) {
            HornSatReasoner reasoner = new HornSatReasoner(ontology, config, buffering);
            this.owlSatStructure = reasoner.getOWLSatStructure();
            return reasoner;
        }
        return new HornSatReasoner(ontology, config, buffering, getOWLSatStructure());

    }

    @Override
    public OWLReasoner createReasoner(OWLOntology ontology, OWLReasonerConfiguration config) throws IllegalConfigurationException {
        return createReasoner(ontology, config, BufferingMode.BUFFERING);
    }
}
