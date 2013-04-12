package at.ainf.owlapi3.reasoner;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.BufferingMode;
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
        super(ontology);
    }

    @Override
    public String getReasonerName() {
        return "SAT Reasoner for OWL";
    }

    @Override
    public Version getReasonerVersion() {
        return new Version(1,0,0,0);
    }


}
