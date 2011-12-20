package at.ainf.owlcontroller;

import at.ainf.diagnosis.debugger.ProbabilityQueryDebugger;
import at.ainf.diagnosis.debugger.QueryDebugger;
import at.ainf.diagnosis.debugger.SimpleQueryDebugger;
import at.ainf.owlcontroller.parser.MyOWLRendererParser;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.model.UnsatisfiableFormulasException;
import at.ainf.theory.storage.HittingSet;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.model.OWLTheory;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.File;
import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 18.04.11
 * Time: 10:15
 * To change this template use File | Settings | File Templates.
 */
public class Example2005 {

    static OWLTheory theory;

    static OWLOntology ontology;

    static OWLReasonerFactory reasonerFactory;

    static Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();

    public static File file = new File(ClassLoader.getSystemResource("iswc2005.owl").getFile());

    MyOWLRendererParser parser;

    @Test
    public void testCSandHS() throws OWLOntologyCreationException, UnsatisfiableFormulasException, SolverException, NoConflictException, ParserException {
        //SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        //UniformCostSearch<OWLLogicalAxiom> search = new UniformCostSearch<OWLLogicalAxiom>(storage);

        //search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());

        HashMap<ManchesterOWLSyntax, Double> map = Utils.getProbabMap();
        createOntology();
        //theory = new OWLTheory(reasonerFactory, ontology, bax);
        //search.setNodeCostsEstimator(new OWLAxiomNodeCostsEstimator(theory,map));

        //search.setTheory(theory);

        OWLTheory t = new OWLTheory(new Reasoner.ReasonerFactory(),ontology,bax);
        QueryDebugger<OWLLogicalAxiom> debugger = new ProbabilityQueryDebugger<OWLLogicalAxiom>(t,new OWLAxiomNodeCostsEstimator(t, map));
        debugger.updateMaxHittingSets(0);

        debugger.debug();

        Set<? extends HittingSet<OWLLogicalAxiom>> hittingset = debugger.getHittingSets();
        Set<Set<OWLLogicalAxiom>> conflictset = debugger.getConflictSets();

        assertTrue(hittingset.size() == 3);
        assertTrue(hittingset.iterator().next().size() == 1);
        Iterator<? extends HittingSet<OWLLogicalAxiom>> hittingSetItr = hittingset.iterator();
        assertTrue(MyOWLRendererParser.render(hittingSetItr.next().iterator().next()).equals("A3 SubClassOf A4 and A5"));
        assertTrue(MyOWLRendererParser.render(hittingSetItr.next().iterator().next()).equals("A4 SubClassOf C and (s only F)"));
        assertTrue(MyOWLRendererParser.render(hittingSetItr.next().iterator().next()).equals("A5 SubClassOf s some (not (F))"));

        assertTrue(conflictset.size() == 1);
        Collection<OWLLogicalAxiom> hset = conflictset.iterator().next();
        for (OWLLogicalAxiom axiom : hset) {
            assertTrue(MyOWLRendererParser.render(axiom).equals("A3 SubClassOf A4 and A5") ||
                    MyOWLRendererParser.render(axiom).equals("A4 SubClassOf C and (s only F)") ||
                    MyOWLRendererParser.render(axiom).equals("A5 SubClassOf s some (not (F))"));
        }

    }


    @Test
    public void testposT() throws OWLOntologyCreationException, UnsatisfiableFormulasException, SolverException, NoConflictException {
        //SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        //BreadthFirstSearch<OWLLogicalAxiom> search = new BreadthFirstSearch<OWLLogicalAxiom>(storage);

        //search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
        createOntology();
        //theory = new OWLTheory(reasonerFactory, ontology, bax);

        //search.setTheory(theory);
        OWLTheory t = new OWLTheory(new Reasoner.ReasonerFactory(),ontology,bax);
        QueryDebugger<OWLLogicalAxiom> debugger = new SimpleQueryDebugger<OWLLogicalAxiom>(t);
        debugger.updateMaxHittingSets(0);

        debugger.getTheory().addPositiveTest(parser.parse("w Type not C"));

        debugger.debug();

        Set<? extends HittingSet<OWLLogicalAxiom>> hittingset = debugger.getHittingSets();
        Set<Set<OWLLogicalAxiom>> conflictset = debugger.getConflictSets();

        assertTrue(hittingset.size() == 3);
        Iterator<? extends HittingSet<OWLLogicalAxiom>> hsItr = hittingset.iterator();
        Set<OWLLogicalAxiom> hs = hsItr.next();
        assertTrue(hs.size() == 2);
        for (OWLLogicalAxiom a : hs) {
            assertTrue(MyOWLRendererParser.render(a).equals("A6 SubClassOf A4 and D") ||
                    MyOWLRendererParser.render(a).equals("A5 SubClassOf s some (not (F))"));
        }

        hs = hsItr.next();
        assertTrue(hs.size() == 2);
        for (OWLLogicalAxiom a : hs) {
            assertTrue(MyOWLRendererParser.render(a).equals("A3 SubClassOf A4 and A5") ||
                    MyOWLRendererParser.render(a).equals("A6 SubClassOf A4 and D"));
        }
        hs = hsItr.next();
        assertTrue(hs.size() == 1);
        assertTrue(MyOWLRendererParser.render(hs.iterator().next()).equals("A4 SubClassOf C and (s only F)"));


        assertTrue(conflictset.size() == 2);


    }

    public void createOntology() throws OWLOntologyCreationException, UnsatisfiableFormulasException, SolverException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        ontology = manager.loadOntologyFromOntologyDocument(file);

        bax = new LinkedHashSet<OWLLogicalAxiom>();

        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        parser = new MyOWLRendererParser(ontology);
        reasonerFactory = new Reasoner.ReasonerFactory();

    }

}
