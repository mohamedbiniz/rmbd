package at.ainf.owlapi3.module.iterative.diagsearcher;

import at.ainf.diagnosis.Searchable;
import at.ainf.diagnosis.Searcher;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.DirectDiagnosis;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.InvHsTreeSearch;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.util.Set;

/**
* Created with IntelliJ IDEA.
* User: pfleiss
* Date: 04.06.13
* Time: 14:00
* To change this template use File | Settings | File Templates.
*/
public class InvHSTreeCreator implements TreeCreator {

    @Override
    public TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> getSearch() {
        return new InvHsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
    }

    @Override
    public Searchable<OWLLogicalAxiom> getSearchable(OWLReasonerFactory factory, Set<OWLLogicalAxiom> background, OWLOntology ontology) {
        try {
            return new DualTreeOWLTheory(factory, ontology, background);
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        throw new IllegalStateException();
    }

    @Override
    public Searcher<OWLLogicalAxiom> getSearcher() {
        return new DirectDiagnosis<OWLLogicalAxiom>();
    }
}
