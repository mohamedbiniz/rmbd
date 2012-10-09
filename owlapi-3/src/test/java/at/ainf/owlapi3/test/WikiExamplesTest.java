package at.ainf.owlapi3.test;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.model.OWLTheory;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
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

    private static Logger logger = LoggerFactory.getLogger(TreeTest.class.getName());

    @Test
    /**
     * This testcase is a simple example how to search diagnoses
     */
    public void searchKoalaTest() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {

        // We have to create a search object using Reiter's Treee
        HsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom>();

        // We also need to load the ontology / knowledge base to debug
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

        // we want to use UniformCostSearch as our search strategy
        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());

        // because we use Reiter's Tree nodes are conflicts which we search using QuickXplain
        search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());

        // because we use UniformCostSearch we have to give a cost estimator to the search
        search.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory));

        // at last we combine theory with search and get our ready to use object
        search.setSearchable(theory);

        // here we run our search and want to get 9 diagnoses
        try {
            search.run(9);
        } catch (NoConflictException e) {
            // if the ontology would have no conflicts this exception would be thrown.
        }

        // here we save the result in a new list
        Set<AxiomSet<OWLLogicalAxiom>> result = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>(search.getDiagnoses());

        // Koala normally has 10 diagnoses so we should have found enough
        assertTrue(result.size() == 9);


    }

}
