package at.ainf.owlapi3.utils.creation.search;

import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.tree.TreeSearch;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 07.08.12
 * Time: 10:50
 * To change this template use File | Settings | File Templates.
 */
public interface SearchCreator {

    public TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> getSearch();

}
