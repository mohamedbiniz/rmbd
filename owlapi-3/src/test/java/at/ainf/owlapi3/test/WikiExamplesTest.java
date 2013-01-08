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
 * Date: 17.09.12
 * Time: 12:18
 * To change this template use File | Settings | File Templates.
 */
public class WikiExamplesTest {

    private static Logger logger = LoggerFactory.getLogger(WikiExamplesTest.class.getName());

    @Test
    /**
     * This testcase is a simple example how to start diagnoses
     */
    public void searchKoalaTest() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {

        // We have to create a start object using Reiter's Treee
        HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();

        // We also need to load the ontology / knowledge base to start
        InputStream koalaStream = ClassLoader.getSystemResourceAsStream("ontologies/koala.owl");
        OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream);

        // (Optional) we want to define axioms in the Abox as correct so we have to add the to the background ontology.
        // These axioms are not considered in conflicts or diagnoses
        Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        // We have to create a Theory object which holds the information about the ontology and specified testcases
        // here we need to have a reasoner factory, our ontology, and the background theory (can be emtpy)
        OWLTheory theory = new OWLTheory(new Reasoner.ReasonerFactory(), ontology, bax);

        // we want to use UniformCostSearch as our start strategy
        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());

        // because we use Reiter's Tree nodes are conflicts which we start using QuickXplain
        search.setSearcher(new QuickXplain<OWLLogicalAxiom>());

        // because we use UniformCostSearch we have to give a cost estimator to the start
        search.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory));

        // at last we combine theory with start and get our ready to use object
        search.setSearchable(theory);

        // here we start our start and want to get 9 diagnoses
        try {
            search.setMaxDiagnosesNumber(9);
            search.start();
        } catch (NoConflictException e) {
            // if the ontology would have no conflicts this exception would be thrown.
        }

        // here we save the result in a new list
        Set<FormulaSet<OWLLogicalAxiom>> result = new LinkedHashSet<FormulaSet<OWLLogicalAxiom>>(search.getDiagnoses());

        // Koala normally has 10 diagnoses so we should have found enough
        assertTrue(result.size() == 9);


    }

}
