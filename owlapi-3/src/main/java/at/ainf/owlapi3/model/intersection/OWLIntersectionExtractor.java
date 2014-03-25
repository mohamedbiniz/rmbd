package at.ainf.owlapi3.model.intersection;

import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 16.05.13
 * Time: 11:47
 * To change this template use File | Settings | File Templates.
 */
public interface OWLIntersectionExtractor {

    public Set<OWLLogicalAxiom> calculateMinModule (Set<OWLLogicalAxiom> ontology);

}
