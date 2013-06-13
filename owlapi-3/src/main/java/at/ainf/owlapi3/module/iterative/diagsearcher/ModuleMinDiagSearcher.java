package at.ainf.owlapi3.module.iterative.diagsearcher;

//import at.ainf.diagnosis.logging.old.MetricsManager;
import at.ainf.diagnosis.Searchable;
import at.ainf.diagnosis.Searcher;
import at.ainf.diagnosis.logging.MetricsLogger;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.DirectDiagnosis;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.ConfidenceCostsEstimator;
import at.ainf.diagnosis.tree.InvHsTreeSearch;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

import static at.ainf.owlapi3.util.OWLUtils.createOntology;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 12.03.13
 * Time: 13:42
 * To change this template use File | Settings | File Templates.
 */
public class ModuleMinDiagSearcher implements ModuleDiagSearcher {

    protected int maxDiags;

    private Map<OWLLogicalAxiom, BigDecimal> confidences;

    private OWLReasonerFactory factory;


    private TreeCreator treeCreator;

    public ModuleMinDiagSearcher() {
        this (null);

    }

    public ModuleMinDiagSearcher(Map<OWLLogicalAxiom, BigDecimal> confidences) {
        this.maxDiags = 1;
        this.confidences = confidences;
        this.treeCreator = new HSTreeCreator();
    }

    public TreeCreator getTreeCreator() {
        return treeCreator;
    }

    public void setTreeCreator(TreeCreator treeCreator) {
        this.treeCreator = treeCreator;
    }

    public Map<OWLLogicalAxiom, BigDecimal> getConfidences() {
        return confidences;
    }

    @Override
    public void setReasonerFactory(OWLReasonerFactory reasonerFactory) {
        factory = reasonerFactory;
    }

    @Override
    public OWLReasonerFactory getReasonerFactory() {
        return factory;
    }

    protected TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> createSearch
            (Set<OWLLogicalAxiom> axioms, Set<OWLLogicalAxiom> backg) {
        TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = getTreeCreator().getSearch();

        OWLOntology ontology = createOntology(axioms);

        Searchable<OWLLogicalAxiom> theory = getTreeCreator().getSearchable(getReasonerFactory(), backg, ontology);

        search.setSearcher(getTreeCreator().getSearcher());

        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());

        if (getConfidences() != null)
            search.setCostsEstimator(new ConfidenceCostsEstimator<OWLLogicalAxiom>(ontology.getLogicalAxioms(), BigDecimal.ONE,confidences));
        else
            search.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory));


        search.setSearchable(theory);

        search.setMaxDiagnosesNumber(maxDiags);

        return search;
    }

    private Logger logger = LoggerFactory.getLogger(ModuleMinDiagSearcher.class.getName());

    private MetricsLogger metricsLogger = MetricsLogger.getInstance();

    //private MetricsManager metricsManager = MetricsManager.getInstance();



    @Override
    public Set<OWLLogicalAxiom> calculateDiag(Set<OWLLogicalAxiom> axioms, Set<OWLLogicalAxiom> backg) {
        TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = createSearch(axioms,backg);

        long time = System.currentTimeMillis();
        getTreeCreator().runSearch(search);
        time = System.currentTimeMillis() - time;
        logger.info ("time needed to search for diagnoses: " + time);

        //IterativeStatistics.numberCS.add((long)search.getConflicts().size());
        //IterativeStatistics.avgCardCS.createNewValueGroup();
        //for (Set<OWLLogicalAxiom> cs : search.getConflicts())
            //IterativeStatistics.avgCardCS.addValue((long)cs.size());
        for (Set<OWLLogicalAxiom> cs : search.getConflicts())
            metricsLogger.getHistogram("card-cs").update(cs.size());
        //IterativeStatistics.moduleSize.add((long)axioms.size());
          metricsLogger.createGauge("module-size", axioms.size());

        Set<FormulaSet<OWLLogicalAxiom>> diagnoses = search.getDiagnoses();
        Set<OWLLogicalAxiom> diagnosis;
        if (diagnoses.isEmpty())
            diagnosis = Collections.emptySet();
        else
            diagnosis = chooseDiagnosis(diagnoses);
        //IterativeStatistics.cardHS.add((long) diagnosis.size());
        metricsLogger.createGauge("card-hs", diagnosis.size());

        return diagnosis;

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
