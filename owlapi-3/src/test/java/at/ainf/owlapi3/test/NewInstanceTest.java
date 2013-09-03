package at.ainf.owlapi3.test;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.model.ReasonerOWL;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 10.12.12
 * Time: 12:00
 * To change this template use File | Settings | File Templates.
 */
public class NewInstanceTest {

    private static Logger logger = LoggerFactory.getLogger(TreeTest.class.getName());

    @Test
    public void createTheorMult() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {

        InputStream koalaStream = ClassLoader.getSystemResourceAsStream("ontologies/koala.owl");
        OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream);

        Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        OWLTheory theory = new OWLTheory(new Reasoner.ReasonerFactory(), ontology, bax);

        ReasonerOWL reasoner1 = theory.getReasoner();
        ReasonerOWL reasoner2 = reasoner1.newInstance();

        logger.info("reasoner1: " + reasoner1);
        logger.info("reasoner2: " + reasoner2);
    }

}
