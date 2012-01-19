package at.ainf.owlcontroller;

import at.ainf.owlcontroller.parser.MyOWLRendererParser;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.theory.storage.HittingSet;
import at.ainf.theory.storage.SimpleStorage;
import at.ainf.diagnosis.tree.UniformCostSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.model.OWLTheory;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

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
    private static Logger logger = Logger.getLogger(SimpleQueryTest.class.getName());
    //private OWLDebugger debugger = new SimpleDebugger();
    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

    @BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlcontroller-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }

    @Test
    public void univNoDiagnosesTest() throws OWLException, InconsistentTheoryException, SolverException, NoConflictException {

        SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        HashMap<ManchesterOWLSyntax, Double> map = Utils.getProbabMap();

        UniformCostSearch<OWLLogicalAxiom> search = new UniformCostSearch<OWLLogicalAxiom>(storage);
        search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());

        OWLOntology ontology =
                manager.loadOntologyFromOntologyDocument(ClassLoader.getSystemResourceAsStream("queryontologies/Univ.owl"));

        Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();

        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();

        OWLTheory theory = new OWLTheory(reasonerFactory, ontology, bax);
        search.setTheory(theory);
        search.setNodeCostsEstimator(new OWLAxiomNodeCostsEstimator(theory));
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

        Collection<? extends HittingSet<OWLLogicalAxiom>> res = search.getStorage().getValidHittingSets();
        System.out.println(res.size());
        //Partition<OWLLogicalAxiom> query = diagProvider.getBestQuery(diagnoses);
        //theory.addNonEntailedTest(query.partition);
        //diagnoses = diagProvider.getDiagnoses(9);
        //query = diagProvider.getBestQuery(diagnoses);
        //QueryMinimizer<OWLLogicalAxiom> mnz = new QueryMinimizer<OWLLogicalAxiom>(query, theory);
        //NewQuickXplain<OWLLogicalAxiom> q = new NewQuickXplain<OWLLogicalAxiom>();
        //Set<OWLLogicalAxiom> r = q.search(mnz,query.partition);

        //theory.addEntailedTest(query.getQueryAxioms());
    }

    //@Test options in diag provider are not set correctly
    public void queryMnTest() throws OWLException, InconsistentTheoryException, SolverException, NoConflictException {

        SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        HashMap<ManchesterOWLSyntax, Double> map = Utils.getProbabMap();

        UniformCostSearch<OWLLogicalAxiom> search = new UniformCostSearch<OWLLogicalAxiom>(storage);
        search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());

        OWLOntology ontology =
          manager.loadOntologyFromOntologyDocument(ClassLoader.getSystemResourceAsStream("queryontologies/Univ.owl"));

        Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();

        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();

        OWLTheory theory = new OWLTheory(reasonerFactory, ontology, bax);
        search.setNodeCostsEstimator(new OWLAxiomNodeCostsEstimator(theory));
        search.setTheory(theory);

        //theory.getAxiomGenerators().add(new InferredEquivalentClassAxiomGenerator());
        //theory.getAxiomGenerators().add(new InferredDisjointClassesAxiomGenerator());
        //theory.getAxiomGenerators().add(new InferredPropertyAssertionGenerator());
        theory.setIncludeTrivialEntailments(true);
        // theory.setIncludeOntAxioms(true);

        /*DiagProvider diagProvider = new DiagProvider(search, false, 9);
          LinkedList<HittingSet<OWLLogicalAxiom>> diagnoses = diagProvider.getDiagnoses(9);
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

        SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        HashMap<ManchesterOWLSyntax, Double> map = Utils.getProbabMap();

        UniformCostSearch<OWLLogicalAxiom> search = new UniformCostSearch<OWLLogicalAxiom>(storage);
        search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());

        OWLOntology ontology =
                manager.loadOntologyFromOntologyDocument(ClassLoader.getSystemResourceAsStream("koala.owl"));

        Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();

        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();

        OWLTheory theory = new OWLTheory(reasonerFactory, ontology, bax);
        search.setNodeCostsEstimator(new OWLAxiomNodeCostsEstimator(theory));
        search.setTheory(theory);


        Collection<? extends HittingSet<OWLLogicalAxiom>> res = search.run(9);
        TreeSet<HittingSet<OWLLogicalAxiom>> result = new TreeSet<HittingSet<OWLLogicalAxiom>>(res);
        for (HittingSet<OWLLogicalAxiom> hs : result) {
            TreeSet<HittingSet<OWLLogicalAxiom>> ts = new TreeSet<HittingSet<OWLLogicalAxiom>>();
            ts.add(hs);
            assertTrue(ts.contains(hs));
            for (HittingSet<OWLLogicalAxiom> hs1 : result) {

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
