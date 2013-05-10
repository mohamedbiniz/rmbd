package at.ainf.owlapi3.test;

import  at.ainf.diagnosis.model.*;
import java.util.Date;
import at.ainf.diagnosis.quickxplain.PredefinedConflictSearcher;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.storage.FormulaSetImpl;
import at.ainf.diagnosis.tree.*;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.*;

import org.junit.Test;
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
    public void searchKoalaTest() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException, InterruptedException {

        // We have to create a start object using Reiter's Treee
        BinaryTreeSearch<FormulaSet<Integer>,Integer> search = new BinaryTreeSearch<FormulaSet<Integer>,Integer>();

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

        Set<FormulaSet<Integer>> conflicts = new LinkedHashSet<FormulaSet<Integer>>();

        LinkedHashSet<Integer> axioms = new LinkedHashSet<Integer>();
        axioms.add(1);
        axioms.add(2);
        axioms.add(3);
        conflicts.add(new FormulaSetImpl<Integer>(new BigDecimal("1"),axioms, Collections.<Integer>emptySet()));


        LinkedHashSet<Integer> axioms2 = new LinkedHashSet<Integer>();
        axioms2.add(3);
        axioms2.add(4);
        axioms2.add(2);
        conflicts.add(new FormulaSetImpl<Integer>(new BigDecimal("1"),axioms2, Collections.<Integer>emptySet()));

        LinkedHashSet<Integer> axioms3 = new LinkedHashSet<Integer>();
        axioms3.add(5);
        conflicts.add(new FormulaSetImpl<Integer>(new BigDecimal("1"),axioms3, Collections.<Integer>emptySet()));

        LinkedHashSet<Integer> axioms4 = new LinkedHashSet<Integer>();
        axioms4.add(4);
        axioms4.add(2);
        axioms4.add(7);
        axioms4.add(1);
        conflicts.add(new FormulaSetImpl<Integer>(new BigDecimal("1"),axioms4, Collections.<Integer>emptySet()));

        LinkedHashSet<Integer> axioms5 = new LinkedHashSet<Integer>();
        axioms5.add(2);
        axioms5.add(7);
        axioms5.add(3);
        axioms5.add(9);
        conflicts.add(new FormulaSetImpl<Integer>(new BigDecimal("1"),axioms5, Collections.<Integer>emptySet()));

        LinkedHashSet<Integer> axioms6 = new LinkedHashSet<Integer>();
        axioms6.add(1);
        axioms6.add(9);
        conflicts.add(new FormulaSetImpl<Integer>(new BigDecimal("1"),axioms6, Collections.<Integer>emptySet()));

        LinkedHashSet<Integer> axioms7 = new LinkedHashSet<Integer>();
        axioms7.add(4);
        axioms7.add(3);
        axioms7.add(7);
        conflicts.add(new FormulaSetImpl<Integer>(new BigDecimal("1"),axioms7, Collections.<Integer>emptySet()));

        LinkedHashSet<Integer> axioms8 = new LinkedHashSet<Integer>();
        axioms8.add(7);
        axioms8.add(8);
        axioms8.add(5);
        axioms8.add(3);
        conflicts.add(new FormulaSetImpl<Integer>(new BigDecimal("1"),axioms8, Collections.<Integer>emptySet()));

        LinkedHashSet<Integer> axioms9 = new LinkedHashSet<Integer>();
        axioms9.add(9);
        axioms9.add(3);
        axioms9.add(5);
        axioms9.add(1);
        conflicts.add(new FormulaSetImpl<Integer>(new BigDecimal("1"),axioms9, Collections.<Integer>emptySet()));

        BHSTreeNode bhs = new BHSTreeNode(conflicts);

        Set<FormulaSet<Integer>> conflicts2 = bhs.copy3(conflicts);


        // We have to create a Theory object which holds the information about the ontology and specified testcases
        // here we need to have a reasoner factory, our ontology, and the background theory (can be emtpy)
        BaseSearchableObject<Integer> theory = new BaseSearchableObject<Integer>();


        theory.setReasoner(new SimpleSetReasoner<Integer>(conflicts));
        theory.setKnowledgeBase(new KnowledgeBase<Integer>());

        PredefinedConflictSearcher searcher= new PredefinedConflictSearcher<Integer>(conflicts);
        searcher.setCount(3);

        search.setSearcher(searcher);

        // because we use UniformCostSearch we have to give a cost estimator to the start
        search.setCostsEstimator(new SimpleCostsEstimator<Integer>());

        // at last we combine theory with start and get our ready to use object
        search.setSearchable(theory);









        HsTreeSearch<FormulaSet<Integer>,Integer> hsSearch = new HsTreeSearch<FormulaSet<Integer>,Integer>();

        // We have to create a Theory object which holds the information about the ontology and specified testcases
        // here we need to have a reasoner factory, our ontology, and the background theory (can be emtpy)
        //  OWLTheory theory = new OWLTheory(new Reasoner.ReasonerFactory(), ontology, bax);

        // we want to use UniformCostSearch as our start strategy
        hsSearch.setSearchStrategy(new DepthFirstSearchStrategy<Integer>());

        /*  PredefinedConflictSearcher searcher2= new PredefinedConflictSearcher<Integer>(conflicts2);

      // searcher2.setIsBHS(false);

       hsSearch.setSearcher(searcher2);

       // because we use UniformCostSearch we have to give a cost estimator to the start
       hsSearch.setCostsEstimator(new SimpleCostsEstimator<Integer>());


       BaseSearchableObject<Integer> theory2 = new BaseSearchableObject<Integer>();


       theory2.setReasoner(new SimpleSetReasoner<Integer>(conflicts2));
       theory2.setKnowledgeBase(new KnowledgeBase<Integer>());


       // at last we combine theory with start and get our ready to use object
       hsSearch.setSearchable(theory2); */


        /* long start2= System.currentTimeMillis();

// here we start our start and want to get 9 diagnoses
try {
    hsSearch.setMaxDiagnosesNumber(50);
    hsSearch.start();
} catch (NoConflictException e) {
    // if the ontology would have no conflicts this exception would be thrown.
}

long end2 = System.currentTimeMillis();
long time2=end2-start2;       */

        //  System.out.println("BHS time: " + time1);
        //  System.out.println("HS time: " + time2);





        // here we save the result in a new list
        //Set<FormulaSet<Integer>> result2 = new LinkedHashSet<FormulaSet<Integer>>(hsSearch.getDiagnoses());


        long start1= System.currentTimeMillis();
        // here we start our start and want to get 9 diagnoses
        try {
            search.setMaxDiagnosesNumber(50);
            search.start();
        } catch (NoConflictException e) {
            // if the ontology would have no conflicts this exception would be thrown.
        }
        long end1= System.currentTimeMillis();
        long time1=end1-start1;

        // here we save the result in a new list
        Set<FormulaSet<Integer>> result = new LinkedHashSet<FormulaSet<Integer>>(search.getDiagnoses());

        System.out.println("Number of hitting sets: " + result.size());
        System.out.println("Hitting sets:");

        for(FormulaSet<Integer> axs:result){

            for(int i:axs){
                System.out.print(i);
            }
            System.out.println();
        }




        assertTrue(result.size() == 10);
        //    assertTrue(result2.size() == 10);


        //führe update durch

        /* Set<Integer> delete = new LinkedHashSet<Integer>();
        delete.add(1);

        ((BHSTreeNode)search.getRoot()).updateNode(delete);

        //New testcase
        //OWLLogicalAxiom testcase = new MyOWLRendererParser(ontology).parse("KoalaWithPhD EquivalentTo Koala and (hasDegree value PhD)");


        Set<Set<Integer>>  result2 = ((BHSTreeNode)search.getRoot()).getHittingSets();

        for(Set<Integer> axs:result2){

            for(int i:axs){
                System.out.print(i);
            }
            System.out.println();
        }

        System.out.println();System.out.println();
        Set<HSTreeNode>  result3 = ((BHSTreeNode)search.getRoot()).getLeaves();

        for(HSTreeNode axs:result3){

            for(int i:(Set<Integer>)((BHSTreeNode)axs).getIgnoredElements()){
                System.out.print(i);
            }
            System.out.println();
        }
        */



    }


    @Test
    public void searchKoalaTest2() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException, InterruptedException {

        // We have to create a start object using Reiter's Treee
        BinaryTreeSearch<FormulaSet<Integer>,Integer> search = new BinaryTreeSearch<FormulaSet<Integer>,Integer>();

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

        Set<FormulaSet<Integer>> conflicts = new LinkedHashSet<FormulaSet<Integer>>();

        LinkedHashSet<Integer> axioms = new LinkedHashSet<Integer>();
        axioms.add(1);
        axioms.add(2);
        axioms.add(3);
        conflicts.add(new FormulaSetImpl<Integer>(new BigDecimal("1"),axioms, Collections.<Integer>emptySet()));


        LinkedHashSet<Integer> axioms2 = new LinkedHashSet<Integer>();
        axioms2.add(3);
        axioms2.add(4);
        axioms2.add(2);
        conflicts.add(new FormulaSetImpl<Integer>(new BigDecimal("1"),axioms2, Collections.<Integer>emptySet()));

        LinkedHashSet<Integer> axioms3 = new LinkedHashSet<Integer>();
        axioms3.add(5);
        conflicts.add(new FormulaSetImpl<Integer>(new BigDecimal("1"),axioms3, Collections.<Integer>emptySet()));

        LinkedHashSet<Integer> axioms4 = new LinkedHashSet<Integer>();
        axioms4.add(4);
        axioms4.add(2);
        axioms4.add(7);
        axioms4.add(1);
        conflicts.add(new FormulaSetImpl<Integer>(new BigDecimal("1"),axioms4, Collections.<Integer>emptySet()));

        LinkedHashSet<Integer> axioms5 = new LinkedHashSet<Integer>();
        axioms5.add(2);
        axioms5.add(7);
        axioms5.add(3);
        axioms5.add(9);
        conflicts.add(new FormulaSetImpl<Integer>(new BigDecimal("1"),axioms5, Collections.<Integer>emptySet()));

        LinkedHashSet<Integer> axioms6 = new LinkedHashSet<Integer>();
        axioms6.add(1);
        axioms6.add(9);
        conflicts.add(new FormulaSetImpl<Integer>(new BigDecimal("1"),axioms6, Collections.<Integer>emptySet()));

        LinkedHashSet<Integer> axioms7 = new LinkedHashSet<Integer>();
        axioms7.add(4);
        axioms7.add(3);
        axioms7.add(7);
        conflicts.add(new FormulaSetImpl<Integer>(new BigDecimal("1"),axioms7, Collections.<Integer>emptySet()));

        LinkedHashSet<Integer> axioms8 = new LinkedHashSet<Integer>();
        axioms8.add(7);
        axioms8.add(8);
        axioms8.add(5);
        axioms8.add(3);
        conflicts.add(new FormulaSetImpl<Integer>(new BigDecimal("1"),axioms8, Collections.<Integer>emptySet()));

        LinkedHashSet<Integer> axioms9 = new LinkedHashSet<Integer>();
        axioms9.add(9);
        axioms9.add(3);
        axioms9.add(5);
        axioms9.add(1);
        conflicts.add(new FormulaSetImpl<Integer>(new BigDecimal("1"),axioms9, Collections.<Integer>emptySet()));

        BHSTreeNode bhs = new BHSTreeNode(conflicts);

        Set<FormulaSet<Integer>> conflicts2 = bhs.copy3(conflicts);


        // We have to create a Theory object which holds the information about the ontology and specified testcases
        // here we need to have a reasoner factory, our ontology, and the background theory (can be emtpy)
        BaseSearchableObject<Integer> theory = new BaseSearchableObject<Integer>();


        theory.setReasoner(new SimpleSetReasoner<Integer>(conflicts));
        theory.setKnowledgeBase(new KnowledgeBase<Integer>());

        PredefinedConflictSearcher searcher= new PredefinedConflictSearcher<Integer>(conflicts);
        searcher.setCount(4);

        search.setSearcher(searcher);

        // because we use UniformCostSearch we have to give a cost estimator to the start
        search.setCostsEstimator(new SimpleCostsEstimator<Integer>());

        // at last we combine theory with start and get our ready to use object
        search.setSearchable(theory);





        // here we save the result in a new list
        /* Set<FormulaSet<Integer>> result = new LinkedHashSet<FormulaSet<Integer>>(search.getDiagnoses());

   System.out.println("Number of hitting sets: " + result.size());
   System.out.println("Hitting sets:");

   for(FormulaSet<Integer> axs:result){

       for(int i:axs){
           System.out.print(i);
       }
       System.out.println();
   }

        */


        HsTreeSearch<FormulaSet<Integer>,Integer> hsSearch = new HsTreeSearch<FormulaSet<Integer>,Integer>();

        // We have to create a Theory object which holds the information about the ontology and specified testcases
        // here we need to have a reasoner factory, our ontology, and the background theory (can be emtpy)
        //  OWLTheory theory = new OWLTheory(new Reasoner.ReasonerFactory(), ontology, bax);

        // we want to use UniformCostSearch as our start strategy
        hsSearch.setSearchStrategy(new DepthFirstSearchStrategy<Integer>());

        PredefinedConflictSearcher searcher2= new PredefinedConflictSearcher<Integer>(conflicts2);

        // searcher2.setIsBHS(false);

        hsSearch.setSearcher(searcher2);

        // because we use UniformCostSearch we have to give a cost estimator to the start
        hsSearch.setCostsEstimator(new SimpleCostsEstimator<Integer>());


        BaseSearchableObject<Integer> theory2 = new BaseSearchableObject<Integer>();


        theory2.setReasoner(new SimpleSetReasoner<Integer>(conflicts2));
        theory2.setKnowledgeBase(new KnowledgeBase<Integer>());


        // at last we combine theory with start and get our ready to use object
        hsSearch.setSearchable(theory2);


        long start2= System.currentTimeMillis();

        // here we start our start and want to get 9 diagnoses
        try {
            hsSearch.setMaxDiagnosesNumber(50);
            hsSearch.start();
        } catch (NoConflictException e) {
            // if the ontology would have no conflicts this exception would be thrown.
        }

        long end2 = System.currentTimeMillis();
        long time2=end2-start2;

        //  System.out.println("BHS time: " + time1);
        System.out.println("HS time: " + time2);





        // here we save the result in a new list
        Set<FormulaSet<Integer>> result2 = new LinkedHashSet<FormulaSet<Integer>>(hsSearch.getDiagnoses());


        /* long start1= System.currentTimeMillis();
 // here we start our start and want to get 9 diagnoses
 try {
     search.setMaxDiagnosesNumber(50);
     search.start();
 } catch (NoConflictException e) {
     // if the ontology would have no conflicts this exception would be thrown.
 }
 long end1= System.currentTimeMillis();
 long time1=end1-start1;      */





        //assertTrue(result.size() == 10);
        assertTrue(result2.size() == 10);


        //führe update durch

        /* Set<Integer> delete = new LinkedHashSet<Integer>();
        delete.add(1);

        ((BHSTreeNode)search.getRoot()).updateNode(delete);

        //New testcase
        //OWLLogicalAxiom testcase = new MyOWLRendererParser(ontology).parse("KoalaWithPhD EquivalentTo Koala and (hasDegree value PhD)");


        Set<Set<Integer>>  result2 = ((BHSTreeNode)search.getRoot()).getHittingSets();

        for(Set<Integer> axs:result2){

            for(int i:axs){
                System.out.print(i);
            }
            System.out.println();
        }

        System.out.println();System.out.println();
        Set<HSTreeNode>  result3 = ((BHSTreeNode)search.getRoot()).getLeaves();

        for(HSTreeNode axs:result3){

            for(int i:(Set<Integer>)((BHSTreeNode)axs).getIgnoredElements()){
                System.out.print(i);
            }
            System.out.println();
        }
        */



    }



}



