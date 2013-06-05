package at.ainf.owlapi3.module.iterative.diagsearcher;

import at.ainf.diagnosis.Searchable;
import at.ainf.diagnosis.Searcher;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.TreeSearch;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.util.Set;

/**
* Created with IntelliJ IDEA.
* User: pfleiss
* Date: 04.06.13
* Time: 13:59
* To change this template use File | Settings | File Templates.
*/
public interface TreeCreator {
    public TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> getSearch();

    public Searchable<OWLLogicalAxiom> getSearchable(OWLReasonerFactory factory, Set<OWLLogicalAxiom> backg, OWLOntology ontology);

    public Searcher<OWLLogicalAxiom> getSearcher();

    void runSearch(TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search);
}
