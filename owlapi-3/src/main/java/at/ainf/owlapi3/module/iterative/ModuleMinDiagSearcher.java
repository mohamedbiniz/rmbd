package at.ainf.owlapi3.module.iterative;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.model.OWLTheory;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 12.03.13
 * Time: 13:42
 * To change this template use File | Settings | File Templates.
 */
public class ModuleMinDiagSearcher implements ModuleDiagSearcher {

    protected OWLOntology createOntology (Set<? extends OWLAxiom> axioms) {
        OWLOntology debuggingOntology = null;
        try {
            debuggingOntology = OWLManager.createOWLOntologyManager().createOntology();
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        debuggingOntology.getOWLOntologyManager().addAxioms(debuggingOntology, axioms);
        return debuggingOntology;
    }

    protected HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> createSearch
            (Set<OWLLogicalAxiom> axioms, Set<OWLLogicalAxiom> backg) {
        HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();

        OWLTheory theory = null;
        try {
            theory = new OWLTheory(new Reasoner.ReasonerFactory(), createOntology(axioms), backg);
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());

        search.setSearcher(new QuickXplain<OWLLogicalAxiom>());

        search.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory));

        search.setSearchable(theory);

        search.setMaxDiagnosesNumber(1);

        return search;
    }

    private Logger logger = LoggerFactory.getLogger(ModuleMinDiagSearcher.class.getName());

    protected void runSearch (HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search) {
        try {
            search.start();
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (NoConflictException e) {
            logger.info("no more conflicts can be found ");
        }
    }

    @Override
    public Set<OWLLogicalAxiom> calculateDiag(Set<OWLLogicalAxiom> axioms, Set<OWLLogicalAxiom> backg) {
        HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = createSearch(axioms,backg);

        runSearch(search);

        return chooseDiagnosis(search.getDiagnoses());

    }

    protected Set<OWLLogicalAxiom> chooseDiagnosis(Collection<? extends Set<OWLLogicalAxiom>> diags) {
        return Collections.min(diags, new Comparator<Set<OWLLogicalAxiom>>() {
            @Override
            public int compare(Set<OWLLogicalAxiom> o1, Set<OWLLogicalAxiom> o2) {
                return new Integer(o1.size()).compareTo(o2.size());
            }
        });
    }

}
