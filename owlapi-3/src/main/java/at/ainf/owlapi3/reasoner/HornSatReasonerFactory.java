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
public class HornSatReasonerFactory extends StructuralReasonerFactory{

    @Override
    public String getReasonerName() {
        return "Horn SAT Reasoner";
    }

    @Override
    public OWLReasoner createNonBufferingReasoner(OWLOntology ontology, OWLReasonerConfiguration config) throws IllegalConfigurationException {
        return new HornSatReasoner(ontology, config, BufferingMode.NON_BUFFERING);
    }

    @Override
    public OWLReasoner createReasoner(OWLOntology ontology, OWLReasonerConfiguration config) throws IllegalConfigurationException {
        return new HornSatReasoner(ontology, config, BufferingMode.BUFFERING);
    }
}
