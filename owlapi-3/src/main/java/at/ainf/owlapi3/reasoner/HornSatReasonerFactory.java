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
        computeUnSatClasses(reasoner);
    }

    private Collection<OWLClass> computeUnSatClasses(HornSatReasoner reasoner) {
        reasoner.extractPossiblyUnsatClasses();
        unsatClasses = reasoner.getUnsatisfiableClasses().getEntities();
        return this.unsatClasses;
    }

    public void resetUnsatClasses() {
        this.unsatClasses = null;
    }

    public Set<OWLClass> getUnsatClasses() {
        return unsatClasses;
    }

    @Override
    public OWLReasoner createNonBufferingReasoner(OWLOntology ontology, OWLReasonerConfiguration config) throws IllegalConfigurationException {
        HornSatReasoner reasoner = new HornSatReasoner(ontology, config, BufferingMode.NON_BUFFERING);
        initReasoner(reasoner);
        return reasoner;
    }

    private void initReasoner(HornSatReasoner reasoner) {
        if (getUnsatClasses() != null)
            reasoner.setUnSatClasses(getUnsatClasses());
        else if (isPrecomputingUnSatClasses()) {
            Collection<OWLClass> classes = computeUnSatClasses(reasoner);
            if (classes != null)
                reasoner.setUnSatClasses(new HashSet<OWLClass>(classes));
        }
    }

    @Override
    public OWLReasoner createReasoner(OWLOntology ontology, OWLReasonerConfiguration config) throws IllegalConfigurationException {
        HornSatReasoner reasoner = new HornSatReasoner(ontology, config, BufferingMode.BUFFERING);
        initReasoner(reasoner);
        return reasoner;
    }
}
