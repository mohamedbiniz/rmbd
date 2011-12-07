package at.ainf.pluginprotege;

import at.ainf.theory.model.SolverException;
import at.ainf.theory.model.UnsatisfiableFormulasException;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.theory.storage.HittingSet;
import at.ainf.theory.storage.SimpleStorage;
import at.ainf.diagnosis.tree.BreadthFirstSearch;
import at.ainf.diagnosis.tree.UniformCostSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.model.OWLAxiomNodeCostsEstimator;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.pluginprotege.controlpanel.*;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 13.04.11
 * Time: 10:18
 * To change this template use File | Settings | File Templates.
 */
public class UniformCostSearchTest {


    OWLTheory theory;

    @Test
    public void testEcai2010Abox() throws OWLOntologyCreationException, UnsatisfiableFormulasException, SolverException, NoConflictException {
        SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        HashMap<ManchesterOWLSyntax, Double> map = (new ProbabilityTableModel()).getProbMap();

        UniformCostSearch<OWLLogicalAxiom> search = new UniformCostSearch<OWLLogicalAxiom>(storage);
        search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology =
                manager.loadOntologyFromOntologyDocument(ClassLoader.getSystemResourceAsStream("ecai2010.owl"));

        Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();

        /*for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }*/

        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();

        OWLTheory theory = new OWLTheory(reasonerFactory, ontology, bax);
        search.setNodeCostsEstimator(new OWLAxiomNodeCostsEstimator(theory,map));
        search.setTheory(theory);


        Collection<? extends HittingSet<OWLLogicalAxiom>> res = search.run(9);

    }


    @Test
    public void testEcai2010Simple() throws OWLOntologyCreationException, UnsatisfiableFormulasException, SolverException, NoConflictException {
        SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        BreadthFirstSearch<OWLLogicalAxiom> search = new BreadthFirstSearch<OWLLogicalAxiom>(storage);

        search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology =
                manager.loadOntologyFromOntologyDocument(ClassLoader.getSystemResourceAsStream("ecai2010.owl"));

        Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();


        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();

        OWLTheory theory = new OWLTheory(reasonerFactory, ontology, bax);
        search.setTheory(theory);


        Collection<? extends HittingSet<OWLLogicalAxiom>> res = search.run(9);

    }

    @Test
    public void testEcai2010BreadthFirst() throws OWLOntologyCreationException, UnsatisfiableFormulasException, SolverException, NoConflictException {
        SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        BreadthFirstSearch<OWLLogicalAxiom> search = new BreadthFirstSearch<OWLLogicalAxiom>(storage);

        search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());

        createTh();

        search.setTheory(theory);
        search.setMaxHittingSets(0);

        search.run();
        printDiagnoses(search.getStorage().getHittingSets());

    }

    /*public void testEcai2010UniformCost() throws OWLOntologyCreationException, UnsatisfiableFormulasException, SolverException, NoConflictException {
        SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        TreeSearch<OWLLogicalAxiom> search = new UniformCostSearch<OWLLogicalAxiom>(storage);

        search.setConflictSearcher(new NewQuickXplain<OWLLogicalAxiom>());

        createTh();

        search.setTheory(theory);
        search.setMaxHittingSets(0);

        search.run();
        printDiagnoses(search.getStorage().getHittingSets());
    }

    public String getFullDiagString(Collection<OWLLogicalAxiom> axioms, int num) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        double p = theory.getFailureProbabilityDiagnosis(axioms);
        nf.setMinimumFractionDigits(16);

        return "num: " + num + " p = " + nf.format(p);

    }

    public String getAxiomString(OWLLogicalAxiom axiom) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
        double p = theory.getFailureProbability(axiom);
        nf.setMinimumFractionDigits(16);

        return " \t p = " + nf.format(p) + " " + renderer.render(axiom);

    }*/

    public void printDiagnoses(Collection<? extends Set<OWLLogicalAxiom>> diagnos) {
        int num = 0;
        System.out.println("********************************************************************************");
        for (Collection<OWLLogicalAxiom> axioms : diagnos) {
            num++;
            //System.out.println(getFullDiagString(axioms, num));
            for (OWLLogicalAxiom axiom : axioms) {
                //System.out.println(getAxiomString(axiom));
            }
            System.out.println("");
        }
        System.out.println("********************************************************************************");

    }

    public void createTh() throws OWLOntologyCreationException, UnsatisfiableFormulasException, SolverException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        File file = new File(ClassLoader.getSystemResource("ecai2010.owl").getFile());
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);

        Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();

        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();

        theory = new OWLTheory(reasonerFactory, ontology, bax);

    }

}
