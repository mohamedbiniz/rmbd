package at.ainf.owlapi3.test;

import at.ainf.diagnosis.model.*;
import at.ainf.diagnosis.quickxplain.PredefinedConflictSearcher;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.storage.AxiomSetFactory;
import at.ainf.diagnosis.storage.AxiomSetImpl;
import at.ainf.diagnosis.tree.BinaryTreeSearch;
import at.ainf.diagnosis.tree.SimpleCostsEstimator;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.BreadthFirstSearchStrategy;
import at.ainf.diagnosis.tree.searchstrategy.DepthFirstSearchStrategy;
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
import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: chicco
 * Date: 22.11.12
 * Time: 16:28
 * To change this template use File | Settings | File Templates.
 */
public class BinaryTreeTest {

    private static Logger logger = LoggerFactory.getLogger(TreeTest.class.getName());

    @Test
    /**
     * This testcase is a simple example how to start diagnoses
     */
    public void searchKoalaTest() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {

        // We have to create a start object using Reiter's Treee
        BinaryTreeSearch<AxiomSet<Integer>,Integer> search = new BinaryTreeSearch<AxiomSet<Integer>,Integer>();

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
      //  OWLTheory theory = new OWLTheory(new Reasoner.ReasonerFactory(), ontology, bax);

        // we want to use UniformCostSearch as our start strategy
        search.setSearchStrategy(new DepthFirstSearchStrategy<Integer>());

        // because we use Reiter's Tree nodes are conflicts which we start using QuickXplain

        Set<AxiomSet<Integer>> conflicts = new LinkedHashSet<AxiomSet<Integer>>();

        LinkedHashSet<Integer> axioms = new LinkedHashSet<Integer>();
        axioms.add(1);
        axioms.add(2);
        axioms.add(3);
       conflicts.add(AxiomSetFactory.createConflictSet(new BigDecimal("1"),axioms, Collections.<Integer>emptySet()));


        LinkedHashSet<Integer> axioms2 = new LinkedHashSet<Integer>();
        axioms2.add(3);
        axioms2.add(4);
        axioms2.add(2);
        conflicts.add(AxiomSetFactory.createConflictSet(new BigDecimal("1"),axioms2, Collections.<Integer>emptySet()));

        LinkedHashSet<Integer> axioms3 = new LinkedHashSet<Integer>();
        axioms3.add(5);
        conflicts.add(AxiomSetFactory.createConflictSet(new BigDecimal("1"),axioms3, Collections.<Integer>emptySet()));

        LinkedHashSet<Integer> axioms4 = new LinkedHashSet<Integer>();
        axioms4.add(4);
        axioms4.add(2);
        axioms4.add(7);
        axioms4.add(1);
        conflicts.add(AxiomSetFactory.createConflictSet(new BigDecimal("1"),axioms4, Collections.<Integer>emptySet()));


        // We have to create a Theory object which holds the information about the ontology and specified testcases
        // here we need to have a reasoner factory, our ontology, and the background theory (can be emtpy)
        BaseSearchableObject<Integer> theory = new BaseSearchableObject<Integer>();


        theory.setReasoner(new SimpleSetReasoner<Integer>(conflicts));
        theory.setKnowledgeBase(new KnowledgeBase<Integer>());


       PredefinedConflictSearcher searcher= new PredefinedConflictSearcher<Integer>(conflicts);

        search.setSearcher(searcher);

        // because we use UniformCostSearch we have to give a cost estimator to the start
        search.setCostsEstimator(new SimpleCostsEstimator<Integer>());

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
        Set<AxiomSet<Integer>> result = new LinkedHashSet<AxiomSet<Integer>>(search.getDiagnoses());

        System.out.println("Number of hitting sets: " + result.size());
        System.out.println("Hitting sets:");

        for(AxiomSet<Integer> axs:result){

            for(int i:axs){
                System.out.print(i);
            }
           System.out.println();
        }


        assertTrue(result.size() == 5);


    }



}
