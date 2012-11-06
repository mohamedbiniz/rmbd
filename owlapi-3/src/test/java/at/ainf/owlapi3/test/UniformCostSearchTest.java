package at.ainf.owlapi3.test;

import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.tree.*;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.BreadthFirstSearchStrategy;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.AxiomSet;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.File;
import java.util.Collection;
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
    public void testEcai2010Abox() throws OWLOntologyCreationException, InconsistentTheoryException, SolverException, NoConflictException {
        //SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        // HashMap<ManchesterOWLSyntax, BigDecimal> map = CalculateDiagnoses.getProbabMap();

        HsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());

        search.setSearcher(new QuickXplain<OWLLogicalAxiom>());

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology =
                manager.loadOntologyFromOntologyDocument(ClassLoader.getSystemResourceAsStream("ontologies/ecai2010.owl"));

        Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();

        /*for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }*/

        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();

        OWLTheory theory = new OWLTheory(reasonerFactory, ontology, bax);
        search.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory));
        search.setSearchable(theory);

        search.setMaxDiagnosesNumber(9);
        Collection<? extends AxiomSet<OWLLogicalAxiom>> res = search.start();

    }


    @Test
    public void testEcai2010Simple() throws OWLOntologyCreationException, InconsistentTheoryException, SolverException, NoConflictException {
        //SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        search.setCostsEstimator(new SimpleCostsEstimator<OWLLogicalAxiom>());
        search.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());

        search.setSearcher(new QuickXplain<OWLLogicalAxiom>());
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology =
                manager.loadOntologyFromOntologyDocument(ClassLoader.getSystemResourceAsStream("ontologies/ecai2010.owl"));

        Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();


        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();

        OWLTheory theory = new OWLTheory(reasonerFactory, ontology, bax);
        search.setSearchable(theory);

        search.setMaxDiagnosesNumber(9);
        Collection<? extends AxiomSet<OWLLogicalAxiom>> res = search.start();

    }

    @Test
    public void testEcai2010BreadthFirst() throws OWLOntologyCreationException, InconsistentTheoryException, SolverException, NoConflictException {
        //SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        search.setCostsEstimator(new SimpleCostsEstimator<OWLLogicalAxiom>());
        search.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());

        search.setSearcher(new QuickXplain<OWLLogicalAxiom>());

        createTh();

        search.setSearchable(theory);
        search.setMaxDiagnosesNumber(0);

        search.start();
        printDiagnoses(search.getDiagnoses() );

    }

    /*public void testEcai2010UniformCost() throws OWLOntologyCreationException, InconsistentTheoryException, SolverException, NoConflictException {
        SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        TreeSearch<OWLLogicalAxiom> start = new UniformCostSearch<OWLLogicalAxiom>(storage);

        start.setConflictSearcher(new QuickXplain<OWLLogicalAxiom>());

        createTh();

        start.setSearchable(theory);
        start.setMaxDiagnosesNumber(0);

        start.runPostprocessor();
        printDiagnoses(start.getStorage().getHittingSets());
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

        for (Collection<OWLLogicalAxiom> axioms : diagnos) {
            num++;
            //System.out.println(getFullDiagString(axioms, num));
            for (OWLLogicalAxiom axiom : axioms) {
                //System.out.println(getAxiomString(axiom));
            }

        }
        //System.out.println("********************************************************************************");
    }

    public void createTh() throws OWLOntologyCreationException, InconsistentTheoryException, SolverException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        File file = new File(ClassLoader.getSystemResource("ontologies/ecai2010.owl").getFile());
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
