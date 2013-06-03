package at.ainf.owlapi3.module.iterative.diagsearcher;

import at.ainf.diagnosis.logging.MetricsLogger;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.DirectDiagnosis;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.ConfidenceCostsEstimator;
import at.ainf.diagnosis.tree.InvHsTreeSearch;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static at.ainf.owlapi3.util.OWLUtils.createOntology;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 27.05.13
 * Time: 10:51
 * To change this template use File | Settings | File Templates.
 */
public class ModuleInvTreeDiagSearcher extends ModuleMinDiagSearcher {

    private Logger logger = LoggerFactory.getLogger(ModuleMinDiagSearcher.class.getName());

    protected TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> createSearch
            (Set<OWLLogicalAxiom> axioms, Set<OWLLogicalAxiom> backg) {
        InvHsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new InvHsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();

        DualTreeOWLTheory theory = null;
        OWLOntology ontology = createOntology(axioms);
        try {
            theory = new DualTreeOWLTheory(getReasonerFactory(), ontology, backg);
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());

        search.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());

        if (getConfidences() != null)
            search.setCostsEstimator(new ConfidenceCostsEstimator<OWLLogicalAxiom>(ontology.getLogicalAxioms(), BigDecimal.ONE,getConfidences()));
        else
            search.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory));


        search.setSearchable(theory);

        search.setMaxDiagnosesNumber(1);

        return search;
    }

    private MetricsLogger metricsLogger = MetricsLogger.getInstance();

    @Override
    public Set<OWLLogicalAxiom> calculateDiag(Set<OWLLogicalAxiom> axioms, Set<OWLLogicalAxiom> backg) {
        Set<OWLLogicalAxiom> diagnosis = super.calculateDiag(axioms, backg);
        metricsLogger.createGauge("module-size", axioms.size());
        Set<OWLLogicalAxiom> repaired = new HashSet<OWLLogicalAxiom>(axioms);
        repaired.removeAll(diagnosis);
        boolean isRepaired = getReasonerFactory().createNonBufferingReasoner(createOntology(repaired)).getUnsatisfiableClasses().getEntitiesMinusBottom().isEmpty();

        if (!isRepaired)
            logger.info("inv tree diagnosis is not fully correct");

        return diagnosis;

    }


}
