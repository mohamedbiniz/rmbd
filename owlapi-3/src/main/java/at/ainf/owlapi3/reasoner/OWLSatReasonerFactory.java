package at.ainf.owlapi3.reasoner;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: kostya
 * Date: 12.04.13
 * Time: 10:08
 * To change this template use File | Settings | File Templates.
 */
public class OWLSatReasonerFactory extends StructuralReasonerFactory {

    private OWLSatReasoner.OWLSatStructure owlSatStructure = null;

    /*
    private boolean precompute = true;
    private OWLOntology ontology = null;
    */


    @Override
    public String getReasonerName() {
        return OWLSatReasoner.NAME;
    }

    /*


    public void precomputeUnsatClasses(OWLOntology ontology) {
        OWLSatReasoner reasoner = new OWLSatReasoner(ontology, new SimpleConfiguration(), BufferingMode.BUFFERING);

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

    public OWLSatReasoner.OWLSatStructure getOWLSatStructure() {
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

    private OWLSatReasoner createReasoner(OWLOntology ontology, OWLReasonerConfiguration config, BufferingMode buffering) {
        //return new OWLSatReasoner(ontology, config, buffering);

        if (getOWLSatStructure() == null) {
            OWLSatReasoner reasoner = new OWLSatReasoner(ontology, config, buffering);
            this.owlSatStructure = reasoner.getOWLSatStructure();
            return reasoner;
        }
        return new OWLSatReasoner(ontology, config, buffering, getOWLSatStructure());

    }

    @Override
    public OWLReasoner createReasoner(OWLOntology ontology, OWLReasonerConfiguration config) throws IllegalConfigurationException {
        return createReasoner(ontology, config, BufferingMode.BUFFERING);
    }
}
