package at.ainf.owlapi3.utils.creation.ontology;

import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 06.08.12
 * Time: 13:40
 * To change this template use File | Settings | File Templates.
 */
public class OAEI08OntologyCreator extends SimpleOntologyCreator {

    public OAEI08OntologyCreator(String matcher, String name) {
        super("alignment/" + matcher + "_incoherent_matched_ontologies", name + ".owl");
    }

    public OAEI08OntologyCreator(String name) {
        super(ClassLoader.getSystemResource("alignment").getPath(),name+".owl");
    }

}
