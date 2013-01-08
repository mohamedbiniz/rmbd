package at.ainf.owlapi3.test;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.base.CalculateDiagnoses;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.model.OWLTheory;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 20.12.12
 * Time: 11:44
 * To change this template use File | Settings | File Templates.
 */
public class MultipleReasonersTest {

    private static Logger logger = LoggerFactory.getLogger(MultipleReasonersTest.class.getName());

    public Set<FormulaSet<OWLLogicalAxiom>> getDiagnoses(String ontologyString, List<OWLReasonerFactory> factoryList) throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {
        HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        InputStream koalaStream = ClassLoader.getSystemResourceAsStream(ontologyString);
        OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream);
        Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        OWLTheory theory = new OWLTheory(factoryList, ontology, bax);
        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        search.setSearcher(new QuickXplain<OWLLogicalAxiom>());
        search.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory));
        search.setSearchable(theory);
        long time = 0;
        try {
            search.setMaxDiagnosesNumber(9);
            time = System.currentTimeMillis();
            search.start();
            time = System.currentTimeMillis() - time;
        } catch (NoConflictException e) {
            logger.info("no conflict found ");
        }

        return new LinkedHashSet<FormulaSet<OWLLogicalAxiom>>(search.getDiagnoses());
    }

    @Test
    public void multipleReasonersTest() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {

        String ontology = "ontologies/Transportation-SDA.owl";
        long timeHermit = System.currentTimeMillis();
        Set<FormulaSet<OWLLogicalAxiom>> hermitDiagnoses = getDiagnoses(ontology, Collections.<OWLReasonerFactory>singletonList(new Reasoner.ReasonerFactory()));
        timeHermit = System.currentTimeMillis() - timeHermit;
        long timeRElk = System.currentTimeMillis();
        Set<FormulaSet<OWLLogicalAxiom>> elkDiagnoses = getDiagnoses(ontology, Collections.<OWLReasonerFactory>singletonList(new ElkReasonerFactory()));
        timeRElk = System.currentTimeMillis() - timeRElk;
        long timeMultiple = System.currentTimeMillis();
        Set<FormulaSet<OWLLogicalAxiom>> multipleDiagnoses = getDiagnoses(ontology, Collections.<OWLReasonerFactory>singletonList(new ElkReasonerFactory()));
        timeMultiple = System.currentTimeMillis() - timeMultiple;

        Set<FormulaSet<OWLLogicalAxiom>> elkCorrectDiagnoses = new LinkedHashSet<FormulaSet<OWLLogicalAxiom>>(elkDiagnoses);
        elkCorrectDiagnoses.retainAll(hermitDiagnoses);

        for (FormulaSet<OWLLogicalAxiom> diagnosis : hermitDiagnoses)
            logger.info("hermit: " + CalculateDiagnoses.renderAxioms(diagnosis));
        for (FormulaSet<OWLLogicalAxiom> diagnosis : elkDiagnoses)
            logger.info("elk: " + CalculateDiagnoses.renderAxioms(diagnosis));
        for (FormulaSet<OWLLogicalAxiom> diagnosis : elkCorrectDiagnoses)
            logger.info("both: " + CalculateDiagnoses.renderAxioms(diagnosis));
        logger.info("time (elk, hermit, combined): " + timeRElk + ", " + timeHermit + ", " + timeMultiple
                + " found (elk, hermit, combined): " + elkCorrectDiagnoses.size() + ", " + hermitDiagnoses.size()
                + ", " + multipleDiagnoses.size());



    }

}
