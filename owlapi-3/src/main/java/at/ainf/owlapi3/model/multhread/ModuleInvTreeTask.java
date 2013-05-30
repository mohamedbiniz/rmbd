package at.ainf.owlapi3.model.multhread;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.DirectDiagnosis;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.ConfidenceCostsEstimator;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.InvHsTreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import at.ainf.owlapi3.model.OWLModuleExtractor;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.model.intersection.OWLEqualIntersectionExtractor;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static at.ainf.owlapi3.util.OWLUtils.createOntology;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 27.05.13
 * Time: 09:18
 * To change this template use File | Settings | File Templates.
 */
public class ModuleInvTreeTask implements Callable<Set<OWLLogicalAxiom>> {

    private static final int SPLIT = 40;

    private static Logger logger = LoggerFactory.getLogger(ModuleInvTreeTask.class.getName());

    private final Set<OWLLogicalAxiom> allMappings;

    private final Set<OWLLogicalAxiom> allOntoAxioms;

    private final List<OWLClass> subSig;

    private OWLReasonerFactory factory;

    public ModuleInvTreeTask(Set<OWLLogicalAxiom> mappings, Set<OWLLogicalAxiom> ontoAxioms, List<OWLClass> subSig,
                             OWLReasonerFactory factory) {
        this.allMappings = mappings;
        this.allOntoAxioms = ontoAxioms;
        this.factory = factory;
        this.subSig = subSig;
    }

    protected Set<OWLLogicalAxiom> calculateModule (List<OWLClass> signature) {
        Set<OWLLogicalAxiom> allAxioms = new HashSet<OWLLogicalAxiom>();
        allAxioms.addAll(allMappings);
        allAxioms.addAll(allOntoAxioms);
        OWLModuleExtractor owlModuleExtractor = new OWLModuleExtractor(allAxioms);
        Set<OWLLogicalAxiom> module = owlModuleExtractor.calculateModule(signature);
        logger.info("extracted module of size: " + module.size());
        return module;
    }

    private Set<OWLLogicalAxiom> calculateDiagnosis(Set<OWLLogicalAxiom> module) {
        Set<OWLLogicalAxiom> background = new HashSet<OWLLogicalAxiom>(module);
        background.retainAll(allOntoAxioms);
        InvHsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = createSearch(module, background);
        Set<OWLLogicalAxiom> diag=null;
        try {
            Set<FormulaSet<OWLLogicalAxiom>> diagnoses = search.start();
            if (!diagnoses.isEmpty()) {
                diag = diagnoses.iterator().next();
                logger.info("found diagnosis");
            }
            else
                logger.info("found no diagnosis");
        } catch (SolverException e) {
            logger.info("got solver exception");
            e.printStackTrace();
        } catch (NoConflictException e) {
            logger.info("got noConflict exception");
            e.printStackTrace();
        } catch (InconsistentTheoryException e) {
            logger.info("got inconsistentTheory exception");
            e.printStackTrace();
        } catch (NullPointerException e) {
            logger.info("got nullPointer exception");
            e.printStackTrace();
        }
        if (diag== null)
            diag = Collections.emptySet();
        return diag;
    }

    protected InvHsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> createSearch
            (Set<OWLLogicalAxiom> axioms, Set<OWLLogicalAxiom> backg) {
        InvHsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new InvHsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();

        DualTreeOWLTheory theory = null;
        OWLOntology ontology = createOntology(axioms);
        try {
            theory = new DualTreeOWLTheory(factory, ontology, backg);
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());

        search.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());

        search.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory));

        search.setSearchable(theory);

        search.setMaxDiagnosesNumber(1);

        return search;
    }

    @Override
    public Set<OWLLogicalAxiom> call() throws Exception {
        Set<OWLLogicalAxiom> module = calculateModule(subSig);
        Set<OWLLogicalAxiom> minModule = createIntersectionModule(module);
        //Set<OWLLogicalAxiom> diagnosis = calculateDiagnosis(minModule);
        return minModule;
    }

    protected Set<OWLLogicalAxiom> createIntersectionModule(Set<OWLLogicalAxiom> module) {
        Set<OWLLogicalAxiom> minModule = new OWLEqualIntersectionExtractor(SPLIT).calculateMinModule(module);
        logger.info("module size / reduced module size: " + module.size() + ", " + minModule.size());
        return minModule;
    }

}
