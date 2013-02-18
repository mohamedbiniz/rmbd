package at.ainf.owlapi3.debugging;

import at.ainf.diagnosis.Searchable;
import at.ainf.diagnosis.debugger.SimpleQueryDebugger;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.DirectDiagnosis;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.InvHsTreeSearch;
import at.ainf.diagnosis.tree.SimpleCostsEstimator;
import at.ainf.diagnosis.tree.searchstrategy.DepthFirstSearchStrategy;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.module.SatisfiableQuickXplain;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 03.12.12
 * Time: 17:52
 * To change this template use File | Settings | File Templates.
 */
public class SimpleOWLDebugger extends SimpleQueryDebugger<OWLLogicalAxiom> {

    public SimpleOWLDebugger(OWLReasonerFactory factory, OWLOntology ontology, Set<OWLLogicalAxiom> background) {
        this(factory,ontology,background,Mode.HS_TREE);

    }

    public SimpleOWLDebugger(OWLReasonerFactory factory, OWLOntology ontology, Set<OWLLogicalAxiom> background, Mode mode) {
        super(null,mode);
        set_Theory(createTheory(factory,ontology,background,mode));
        getSearch().setSearchable(get_Theory());

    }

    @Override
    public void init() {
        super.init();
        if (getMode().equals(Mode.HS_TREE) || getMode().equals(Mode.HS_TREE_QUERY)) {
            search.setSearcher(new SatisfiableQuickXplain<OWLLogicalAxiom>());
        }
    }

    protected OWLTheory createTheory(OWLReasonerFactory factory, OWLOntology ontology, Set<OWLLogicalAxiom> background, Mode mode) {
        OWLTheory theory = null;
        try {
            if (mode.equals(Mode.HS_TREE) || mode.equals(Mode.HS_TREE_QUERY))
                theory = new OWLTheory(factory, ontology, background);
            if (mode.equals(Mode.INV_HS_TREE))
                theory = new DualTreeOWLTheory(factory, ontology, background);
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return theory;
    }

}
