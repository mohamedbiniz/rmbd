package at.ainf.owlapi3.module.iterative.diagsearcher;

import at.ainf.diagnosis.Searchable;
import at.ainf.diagnosis.Searcher;
import at.ainf.diagnosis.logging.MetricsLogger;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.model.OWLTheory;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
* Created with IntelliJ IDEA.
* User: pfleiss
* Date: 04.06.13
* Time: 14:00
* To change this template use File | Settings | File Templates.
*/
public class HSTreeCreator implements TreeCreator {

    private Logger logger = LoggerFactory.getLogger(HSTreeCreator.class.getName());

    private MetricsLogger metricsLogger = MetricsLogger.getInstance();

    @Override
    public TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> getSearch() {
        return new HsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
    }

    @Override
    public Searchable<OWLLogicalAxiom> getSearchable(OWLReasonerFactory factory, Set<OWLLogicalAxiom> background, OWLOntology ontology) {
        try {
            return new OWLTheory(factory, ontology, background);
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        throw new IllegalStateException();
    }

    @Override
    public Searcher<OWLLogicalAxiom> getSearcher() {
        return new QuickXplain<OWLLogicalAxiom>();
    }

    @Override
    public void runSearch (TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search) {
        metricsLogger.startTimer("runofhstree");
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
        String label = metricsLogger.getLabelManager().getLabelsConc();
        long timeTreeSearch = metricsLogger.stopTimer("runofhstree");
        //IterativeStatistics.diagnosisTime.add(timeTreeSearch);
        String conflicts = label + " found conflicts with sizes: ";
        for (Set<OWLLogicalAxiom> cs : search.getConflicts())
            conflicts += cs.size() + ", ";
        String diagnoses = label + " found diagnoses with sizes: ";
        for (Set<OWLLogicalAxiom> hs : search.getDiagnoses())
            diagnoses += hs.size() + ", ";
        logger.info(conflicts);
        logger.info(diagnoses);

    }

}
