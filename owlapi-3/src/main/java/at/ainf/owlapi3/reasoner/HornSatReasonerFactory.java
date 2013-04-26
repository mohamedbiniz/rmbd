package at.ainf.owlapi3.reasoner;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import java.util.Collections;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: kostya
 * Date: 12.04.13
 * Time: 10:08
 * To change this template use File | Settings | File Templates.
 */
public class HornSatReasonerFactory extends StructuralReasonerFactory {

    private Set<OWLClass> unsatClasses = null;

    @Override
    public String getReasonerName() {
        return "Horn SAT Reasoner";
    }

    public void precomputeUnsatClasses(OWLOntology ontology) {
        HornSatReasoner reasoner = new HornSatReasoner(ontology, new SimpleConfiguration(), BufferingMode.NON_BUFFERING);
        unsatClasses = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
    }

    public void resetUnsatClasses(){
        this.unsatClasses = null;
    }

    public Set<OWLClass> getUnsatClasses() {
        return unsatClasses;
    }

    @Override
    public OWLReasoner createNonBufferingReasoner(OWLOntology ontology, OWLReasonerConfiguration config) throws IllegalConfigurationException {
        HornSatReasoner reasoner = new HornSatReasoner(ontology, config, BufferingMode.NON_BUFFERING);
        if (this.unsatClasses != null)
            reasoner.setUnSatClasses(this.unsatClasses);
        return reasoner;
    }

    @Override
    public OWLReasoner createReasoner(OWLOntology ontology, OWLReasonerConfiguration config) throws IllegalConfigurationException {
        HornSatReasoner reasoner = new HornSatReasoner(ontology, config, BufferingMode.BUFFERING);
        if (this.unsatClasses != null)
            reasoner.setUnSatClasses(this.unsatClasses);
        return reasoner;
    }
}
