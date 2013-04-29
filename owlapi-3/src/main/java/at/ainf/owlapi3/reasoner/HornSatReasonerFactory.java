package at.ainf.owlapi3.reasoner;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import java.util.Collection;
import java.util.HashSet;
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

    private boolean precompute = true;

    @Override
    public String getReasonerName() {
        return "Horn SAT Reasoner";
    }

    public void setPrecomputeUnsatClasses(boolean precompute) {
        this.precompute = precompute;
    }

    public boolean isPrecomputingUnSatClasses() {
        return precompute;
    }

    public void precomputeUnsatClasses(OWLOntology ontology) {
        HornSatReasoner reasoner = new HornSatReasoner(ontology, new SimpleConfiguration(), BufferingMode.NON_BUFFERING);
        this.unsatClasses = reasoner.getUnsatisfiableClasses().getEntities();
    }


    public void resetUnsatClasses() {
        this.unsatClasses = null;
    }

    public Set<OWLClass> getUnsatClasses() {
        return unsatClasses;
    }

    @Override
    public OWLReasoner createNonBufferingReasoner(OWLOntology ontology, OWLReasonerConfiguration config) throws IllegalConfigurationException {
        return createReasoner(ontology, config, BufferingMode.NON_BUFFERING);
    }

    private HornSatReasoner createReasoner(OWLOntology ontology, OWLReasonerConfiguration config, BufferingMode buffering) {
        if (getUnsatClasses() == null && isPrecomputingUnSatClasses()) {
            return new HornSatReasoner(ontology, config, buffering);
        }
        return new HornSatReasoner(ontology, config, buffering, getUnsatClasses());
    }

    @Override
    public OWLReasoner createReasoner(OWLOntology ontology, OWLReasonerConfiguration config) throws IllegalConfigurationException {
        return createReasoner(ontology, config, BufferingMode.BUFFERING);
    }
}
