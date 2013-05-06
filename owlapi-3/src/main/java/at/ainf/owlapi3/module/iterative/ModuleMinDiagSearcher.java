package at.ainf.owlapi3.module.iterative;

import at.ainf.diagnosis.Speed4JMeasurement;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.ConfidenceCostsEstimator;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.module.iterative.diag.IterativeStatistics;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 12.03.13
 * Time: 13:42
 * To change this template use File | Settings | File Templates.
 */
public class ModuleMinDiagSearcher implements ModuleDiagSearcher {

    private int maxDiags;

    private Map<OWLLogicalAxiom, BigDecimal> confidences;

    private OWLReasonerFactory factory;

    public ModuleMinDiagSearcher() {
        this (null);

    }

    public ModuleMinDiagSearcher(Map<OWLLogicalAxiom, BigDecimal> confidences) {
        this.maxDiags = 1;
        this.confidences = confidences;
    }

    @Override
    public void setReasonerFactory(OWLReasonerFactory reasonerFactory) {
        factory = reasonerFactory;
    }

    @Override
    public OWLReasonerFactory getReasonerFactory() {
        return factory;
    }

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
        OWLOntology ontology = createOntology(axioms);
        try {
            theory = new OWLTheory(getReasonerFactory(), ontology, backg);
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());

        search.setSearcher(new QuickXplain<OWLLogicalAxiom>());

        if (confidences != null)
            search.setCostsEstimator(new ConfidenceCostsEstimator<OWLLogicalAxiom>(ontology.getLogicalAxioms(), BigDecimal.ONE,confidences));
        else
            search.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory));


        search.setSearchable(theory);

        search.setMaxDiagnosesNumber(maxDiags);

        return search;
    }

    private Logger logger = LoggerFactory.getLogger(ModuleMinDiagSearcher.class.getName());

    protected void runSearch (HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search) {
        Speed4JMeasurement.start("runofhstree");
        try {
            search.start();
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (NoConflictException e) {
            logger.info("no more conflicts can be found - trying to reset");
            search.reset();
            try {
                search.start();
            } catch (SolverException e1) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InconsistentTheoryException e1) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (NoConflictException e1) {
                logger.info("there are really no more conflicts");
            }
        }
        String label = Speed4JMeasurement.getLabelOfLastStopWatch();
        long timeTreeSearch = Speed4JMeasurement.stop();
        IterativeStatistics.diagnosisTime.add(timeTreeSearch);
        String conflicts = label + " found conflicts with sizes: ";
        for (Set<OWLLogicalAxiom> cs : search.getConflicts())
            conflicts += cs.size() + ", ";
        String diagnoses = label + " found diagnoses with sizes: ";
        for (Set<OWLLogicalAxiom> hs : search.getDiagnoses())
            diagnoses += hs.size() + ", ";
        logger.info(conflicts);
        logger.info(diagnoses);

    }

    @Override
    public Set<OWLLogicalAxiom> calculateDiag(Set<OWLLogicalAxiom> axioms, Set<OWLLogicalAxiom> backg) {
        HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = createSearch(axioms,backg);

        long time = System.currentTimeMillis();
        runSearch(search);
        time = System.currentTimeMillis() - time;
        logger.info ("time needed to search for diagnoses: " + time);

        IterativeStatistics.numberCS.add((long)search.getConflicts().size());
        IterativeStatistics.avgCardCS.createNewValueGroup();
        for (Set<OWLLogicalAxiom> cs : search.getConflicts())
            IterativeStatistics.avgCardCS.addValue((long)cs.size());
        IterativeStatistics.moduleSize.add((long)axioms.size());

        Set<OWLLogicalAxiom> diagnosis = chooseDiagnosis(search.getDiagnoses());
        IterativeStatistics.cardHS.add((long)diagnosis.size());

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
