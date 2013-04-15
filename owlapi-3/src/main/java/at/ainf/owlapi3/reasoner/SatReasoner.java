package at.ainf.owlapi3.reasoner;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.util.Version;

/**
 * Created with IntelliJ IDEA.
 * User: Kostya
 * Date: 11.04.13
 * Time: 23:20
 * To change this template use File | Settings | File Templates.
 */
public class SatReasoner extends ExtendedStructuralReasoner {


    public SatReasoner(OWLOntology ontology) {
        this(ontology, new SimpleConfiguration(), BufferingMode.NON_BUFFERING);
    }

    public SatReasoner(OWLOntology ontology, OWLReasonerConfiguration config, BufferingMode buffering) {
        super(ontology, config, buffering);
    }

    @Override
    public String getReasonerName() {
        return "SAT Reasoner for OWL";
    }

    @Override
    public Version getReasonerVersion() {
        return new Version(1,0,0,0);
    }


    @Override
    public boolean isConsistent() throws ReasonerInterruptedException, TimeOutException {
        // SAT implementation of consistency

        // extract horn clauses from subsumption hierarchy (structural)

        // process disjointness and equivalence

        // run SAT4J

        return true;
    }

    @Override
    public boolean isCoherent() throws ReasonerInterruptedException, TimeOutException {
        return isConsistent();
    }
}
