package at.ainf.owlapi3.test;

import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.base.CalculateDiagnoses;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.parser.MyOWLRendererParser;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.AxiomSet;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 10.05.11
 * Time: 09:25
 * To change this template use File | Settings | File Templates.
 */
public class SimpleQueryTest {
    private static Logger logger = LoggerFactory.getLogger(SimpleQueryTest.class.getName());
    //private OWLDebugger debugger = new SimpleDebugger();
    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

    /*@BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlapi3-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }*/

    @Test
    public void univNoDiagnosesTest() throws OWLException, InconsistentTheoryException, SolverException, NoConflictException {

        //SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();


        HsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());

        OWLOntology ontology =
                manager.loadOntologyFromOntologyDocument(ClassLoader.getSystemResourceAsStream("ontologies/Univ.owl"));

        Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();

        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();

        OWLTheory theory = new OWLTheory(reasonerFactory, ontology, bax);
        search.setTheory(theory);
        search.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory));
        //theory.getAxiomGenerators().add(new InferredEquivalentClassAxiomGenerator());
        //theory.getAxiomGenerators().add(new InferredDisjointClassesAxiomGenerator());
        //theory.getAxiomGenerators().add(new InferredPropertyAssertionGenerator());
        theory.setIncludeTrivialEntailments(true);
        // theory.setIncludeOntAxioms(true);

        MyOWLRendererParser parser = new MyOWLRendererParser(theory.getOriginalOntology());
        theory.addNonEntailedTest(parser.parse("AssistantProfessor DisjointWith Lecturer"));
        theory.addNonEntailedTest(parser.parse("AIStudent DisjointWith HCIStudent"));
        theory.addNonEntailedTest(parser.parse("AI_Dept DisjointWith EE_Department"));
        theory.addNonEntailedTest(parser.parse("CS_Library SubClassOf EE_Department"));
        search.setMaxHittingSets(-1);
        //BackgroundSearcher s = new BackgroundSearcher(search, null);

        search.run(search.getMaxHittingSets());
        //s.doBackgroundSearch();

        Collection<? extends AxiomSet<OWLLogicalAxiom>> res = search.getDiagnoses();
        logger.info(((Integer)res.size()).toString());
        //Partition<OWLLogicalAxiom> query = diagProvider.getBestQuery(diagnoses);
        //theory.addNonEntailedTest(query.partition);
        //diagnoses = diagProvider.getDiagnoses(9);
        //query = diagProvider.getBestQuery(diagnoses);
        //QueryMinimizer<OWLLogicalAxiom> mnz = new QueryMinimizer<OWLLogicalAxiom>(query, theory);
        //NewQuickXplain<OWLLogicalAxiom> q = new NewQuickXplain<OWLLogicalAxiom>();
        //Set<OWLLogicalAxiom> r = q.search(mnz,query.partition);

        //theory.addEntailedTest(query.getQueryAxioms());
    }

    @Test // options in diag provider are not set correctly
    public void queryMnTest() throws OWLException, InconsistentTheoryException, SolverException, NoConflictException {

        //SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();


        HsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());

        OWLOntology ontology =
          manager.loadOntologyFromOntologyDocument(ClassLoader.getSystemResourceAsStream("ontologies/Univ.owl"));

        Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();

        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();

        OWLTheory theory = new OWLTheory(reasonerFactory, ontology, bax);
        search.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory));
        search.setTheory(theory);

        //theory.getAxiomGenerators().add(new InferredEquivalentClassAxiomGenerator());
        //theory.getAxiomGenerators().add(new InferredDisjointClassesAxiomGenerator());
        //theory.getAxiomGenerators().add(new InferredPropertyAssertionGenerator());
        theory.setIncludeTrivialEntailments(true);
        // theory.setIncludeOntAxioms(true);

        /*DiagProvider diagProvider = new DiagProvider(search, false, 9);
          LinkedList<AxiomSet<OWLLogicalAxiom>> diagnoses = diagProvider.getDiagnoses(9);
          Partition<OWLLogicalAxiom> query = diagProvider.getBestQuery(diagnoses);
        theory.addNonEntailedTest(query.partition);
        diagnoses = diagProvider.getDiagnoses(9);
        query = diagProvider.getBestQuery(diagnoses);
        QueryMinimizer<OWLLogicalAxiom> mnz = new QueryMinimizer<OWLLogicalAxiom>(query, theory);*/
        NewQuickXplain<OWLLogicalAxiom> q = new NewQuickXplain<OWLLogicalAxiom>();
        //Set<OWLLogicalAxiom> r = q.search(mnz, query.partition);

        //theory.addEntailedTest(query.getQueryAxioms());
    }

    @Test
    public void koalaTest() throws OWLException, InconsistentTheoryException, SolverException, NoConflictException {

        //SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();


        HsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());

        OWLOntology ontology =
                manager.loadOntologyFromOntologyDocument(ClassLoader.getSystemResourceAsStream("ontologies/koala.owl"));

        Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();

        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();

        OWLTheory theory = new OWLTheory(reasonerFactory, ontology, bax);
        search.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory));
        search.setTheory(theory);


        Collection<? extends AxiomSet<OWLLogicalAxiom>> res = search.run(9);
        TreeSet<AxiomSet<OWLLogicalAxiom>> result = new TreeSet<AxiomSet<OWLLogicalAxiom>>(res);
        for (AxiomSet<OWLLogicalAxiom> hs : result) {
            TreeSet<AxiomSet<OWLLogicalAxiom>> ts = new TreeSet<AxiomSet<OWLLogicalAxiom>>();
            ts.add(hs);
            assertTrue(ts.contains(hs));
            for (AxiomSet<OWLLogicalAxiom> hs1 : result) {

                if (hs.getName().equals(hs1.getName())) {
                    assertTrue(hs.equals(hs1));
                    assertEquals(0, hs.compareTo(hs1));
                    assertFalse(ts.add(hs1));
                } else {
                    assertFalse(hs.equals(hs1));
                    assertNotSame(0, hs.compareTo(hs1));
                    assertTrue(ts.add(hs1));
                    ts.remove(hs1);
                }

            }
        }
        /*BruteForceMultPart<OWLLogicalAxiom> brute = new BruteForceMultPart<OWLLogicalAxiom>(theory, new EntropyScoringFunction());
        Partition<OWLLogicalAxiom> best = brute.generatePartition(result);
        List<Partition<OWLLogicalAxiom>> partitions = brute.getPartitions();
        for (Partition<OWLLogicalAxiom> part : partitions) {
            assertFalse(part == null);
            assertFalse(part.partition == null || part.partition.isEmpty());
            //logger.info("Found partition: \n dx:" + part.dx + "\n dnx:" + part.dnx + "\n dz:" + part.dz);
            //logger.info("Query: " + part.partition + "\n score=" + part.score); }*/

    }

}
