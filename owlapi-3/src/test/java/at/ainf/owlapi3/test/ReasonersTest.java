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
import at.ainf.owlapi3.model.OWLIncoherencyExtractor;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.reasoner.HornSatReasoner;
import at.ainf.owlapi3.reasoner.HornSatReasonerFactory;
import at.ainf.owlapi3.reasoner.axiomprocessors.OWLClassAxiomNegation;
import org.junit.Ignore;
import org.junit.Test;
import org.perf4j.aop.Profiled;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.UnsupportedEntailmentTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import java.io.InputStream;
import java.util.*;

import static at.ainf.owlapi3.base.tools.LoggerUtils.addConsoleLogger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 20.12.12
 * Time: 11:44
 * To change this template use File | Settings | File Templates.
 */
public class ReasonersTest {

    private static Logger logger = LoggerFactory.getLogger(ReasonersTest.class.getName());

    public Set<FormulaSet<OWLLogicalAxiom>> getDiagnoses(String ontologyString, List<OWLReasonerFactory> factoryList) throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {
        HsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = computeDianoses(ontologyString, factoryList);
        return new LinkedHashSet<FormulaSet<OWLLogicalAxiom>>(search.getDiagnoses());
    }

    @Profiled(tag="ComputationDiagnoses")
    public HsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> computeDianoses(String ontologyString, List<OWLReasonerFactory> factoryList) throws OWLOntologyCreationException, InconsistentTheoryException, SolverException {
        HsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        InputStream koalaStream = ClassLoader.getSystemResourceAsStream(ontologyString);
        OWLOntology ontologyFull = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream);

        OWLIncoherencyExtractor extractor = new OWLIncoherencyExtractor(new Reasoner.ReasonerFactory());
        OWLOntology ontology = extractor.getIncoherentPartAsOntology(ontologyFull);


         //if (factoryList.size() == 1 && factoryList.get(0) instanceof HornSatReasonerFactory)
         //   ((HornSatReasonerFactory) factoryList.get(0)).precomputeUnsatClasses(ontology);

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
            //search.setMaxDiagnosesNumber(9);
            time = System.currentTimeMillis();
            search.start();
            time = System.currentTimeMillis() - time;
        } catch (NoConflictException e) {
            logger.info("no conflict found ");
        }
        return search;
    }

    @Ignore
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

    private class Result {
        private List<Long> time = new ArrayList<Long>(10);
        private Set<FormulaSet<OWLLogicalAxiom>> diagnoses = new LinkedHashSet<FormulaSet<OWLLogicalAxiom>>();
        private Set<FormulaSet<OWLLogicalAxiom>> conflicts = new LinkedHashSet<FormulaSet<OWLLogicalAxiom>>();
        private boolean printResults = false;

        public void print(String name) {
            if (printResults){
                printAxiomSet(name, "Diagnosis",  diagnoses);
                printAxiomSet(name, "Conflict", conflicts);
            }

            Collections.sort(time);
            long avg = 0;
            for (int i = 2; i < time.size() - 2; i++)
                avg += time.get(i);

            logger.info("time " + name + ": " + (avg/(time.size()-4)) + ", " + " found : " + diagnoses.size());
        }

        private void printAxiomSet(String name, String s, Set<FormulaSet<OWLLogicalAxiom>> conflicts) {
            for (FormulaSet<OWLLogicalAxiom> diagnosis : conflicts)
                logger.info(name + " " + s + ": " +  CalculateDiagnoses.renderAxioms(diagnosis));
        }
    }

   // @Ignore
    @Test
    public void testEntailment() throws OWLOntologyCreationException {
        addConsoleLogger();
        String ontologyString = "ontologies/ecai.corrected.owl";
        InputStream koalaStream = ClassLoader.getSystemResourceAsStream(ontologyString);
        OWLOntology ontology = OWLManager.createOWLOntologyManager().
                loadOntologyFromOntologyDocument(koalaStream);

        HornSatReasoner sat = new HornSatReasoner(ontology);
        assertTrue(sat.isConsistent());
        logger.info("KB is consistent");
        for (OWLLogicalAxiom owlLogicalAxiom : ontology.getLogicalAxioms()) {
            OWLClassExpression expr = sat.processAxiom(owlLogicalAxiom, new OWLClassAxiomNegation(sat));
            if (expr != null)
                logger.info(render(owlLogicalAxiom) + " -> " + render(expr));

            try {
                boolean entailed = sat.isEntailed(owlLogicalAxiom);
                assertTrue(entailed);
            } catch (UnsupportedEntailmentTypeException e) {
                logger.info("Entailment of " + render(owlLogicalAxiom) + " cannot be verified");
            }
        }
        OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
        final String pref = "http://www.semanticweb.org/ontologies/2010/0/ecai.owl#";
        OWLAxiom axiom = df.getOWLSubClassOfAxiom(df.getOWLClass(IRI.create(pref + "A1")),
                df.getOWLClass(IRI.create(pref + "C")));
        boolean entailed = sat.isEntailed(axiom);
        assertTrue(entailed);

        axiom = df.getOWLSubClassOfAxiom(df.getOWLClass(IRI.create(pref + "T2")),
                df.getOWLObjectComplementOf(df.getOWLClass(IRI.create(pref + "A"))));
        entailed = sat.isEntailed(axiom);
        assertTrue(entailed);

        axiom = df.getOWLDisjointClassesAxiom(df.getOWLClass(IRI.create(pref + "A")),
                df.getOWLClass(IRI.create(pref + "T2")));
        entailed = sat.isEntailed(axiom);
        assertTrue(entailed);
    }

    private String render(OWLObject expr) {
        return new ManchesterOWLSyntaxOWLObjectRendererImpl().render(expr);
    }

    @Test
    public void satTest() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException, InterruptedException {
        //addConsoleLogger();
        //Thread.sleep(5000);
        String ontology = "ontologies/ecai.incoherent.owl";
        //String ontology = "ontologies/Transportation-SDA.owl";
        //addConsoleLogger();

        HornSatReasonerFactory factory = new HornSatReasonerFactory();
        //factory.setPrecomputeUnsatClasses(true);
        Result sat = reasonerTest(factory, ontology);
        Result hermit = reasonerTest(new Reasoner.ReasonerFactory(), ontology);
        //Result struct = reasonerTest(new ExtendedStructuralReasonerFactory(), ontology);
        assertEquals(sat.diagnoses.size(), hermit.diagnoses.size());
        hermit.print("Hermit");
        sat.print("SAT");
        //struct.print("Structural");
    }

    private Result reasonerTest(OWLReasonerFactory factory, String ontology) throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {
        Result res = new Result();
        for (int i = 0; i < 5; i++) {
            long time = System.currentTimeMillis();
            HsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = computeDianoses(ontology,
                    Collections.<OWLReasonerFactory>singletonList(factory));
            if (res.conflicts.isEmpty())
                res.conflicts.addAll(search.getConflicts());
            if (res.diagnoses.isEmpty())
                res.diagnoses.addAll(search.getDiagnoses());
            time = System.currentTimeMillis() - time;

            res.time.add(time);
        }
        return res;
    }

}
