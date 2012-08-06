package at.ainf.owlapi3.utils.creation.ontology;

import at.ainf.owlapi3.utils.creation.OAEI11ConferenceUtils;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 06.08.12
 * Time: 14:43
 * To change this template use File | Settings | File Templates.
 */
public class OAEI11ConferenceOntologyCreator implements OntologyCreator {

    private OWLOntology ontology;

    public OAEI11ConferenceOntologyCreator(String pathToOntologies,
                                                            String o1, String o2, String pathToMapping, String mappingName) {
        OWLOntology ontology1 = new SimpleOntologyCreator(pathToOntologies, o1 + ".owl").getOntology();
        OWLOntology ontology2 = new SimpleOntologyCreator(pathToOntologies, o2 + ".owl").getOntology();
        OWLOntology merged = OAEI11ConferenceUtils.mergeOntologies(ontology1, ontology2);
        Set<OWLLogicalAxiom> mapping = OAEI11ConferenceUtils.readRdfMapping(pathToMapping, mappingName).keySet();
        for (OWLLogicalAxiom axiom : mapping)
            merged.getOWLOntologyManager().applyChange(new AddAxiom(merged, axiom));
        ontology = merged;


    }

    public OWLOntology getOntology() {
        return ontology;
    }

}
