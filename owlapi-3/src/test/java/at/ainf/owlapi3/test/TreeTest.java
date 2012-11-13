package at.ainf.owlapi3.test;

import at.ainf.diagnosis.Searcher;
import at.ainf.diagnosis.debugger.SimpleQueryDebugger;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.base.CalculateDiagnoses;
import at.ainf.owlapi3.debugging.OWLNegateAxiom;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.*;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 18.01.2010
 * Time: 08:58:55
 * To change this template use File | Settings | File Templates.
 */
public class TreeTest {

    private static Logger logger = LoggerFactory.getLogger(TreeTest.class.getName());
    //private OWLDebugger debugger = new SimpleDebugger();
    private SimpleQueryDebugger<OWLLogicalAxiom> debug = new SimpleQueryDebugger<OWLLogicalAxiom>(null);
    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private static final String TEST_IRI = "http://www.semanticweb.org/ontologies/2010/0/ecai.owl#";

    public static <E extends Set<OWLLogicalAxiom>> String logCollection(Logger logger, String name, Set<E> col) {
        StringBuilder buf = new StringBuilder();
        //TreeSet<E> col  = new TreeSet<E>();
        //col.addAll(col1);
        buf.append(name).append(" {");
        for (Iterator<? extends Set<OWLLogicalAxiom>> sub = col.iterator(); sub.hasNext(); ) {
            buf.append(" {");
            buf.append(logCollection(sub.next()));
            if (sub.hasNext())
                buf.append(",");

        }
        buf.append(" }");
        String message = buf.toString();
        logger.info(message);
        return message;
    }

    public static String logCollection(Set<OWLLogicalAxiom> sub) {
        //TreeSet<OWLLogicalAxiom> sub  = new TreeSet<OWLLogicalAxiom>();
        //sub.addAll(sub1);
        StringBuilder buf = new StringBuilder();
        for (Iterator<OWLLogicalAxiom> iter = sub.iterator(); iter.hasNext(); ) {
            OWLLogicalAxiom ax = iter.next();
            OWLClass cls;
            switch (ax.getAxiomType().getIndex()) {
                case 1:
                    OWLClass cl = ((OWLEquivalentClassesAxiom) ax).getNamedClasses().iterator().next();
                    buf.append(cl.asOWLClass().getIRI().getFragment());
                    break;
                case 2:
                    OWLClassExpression cle = ((OWLSubClassOfAxiom) ax).getSubClass();
                    buf.append(cle.asOWLClass().getIRI().getFragment());
                    break;
                case 3:
                    buf.append("D[ ");
                    Set<OWLClass> dja = ax.getClassesInSignature();
                    for (OWLClass ocl : dja)
                        buf.append(ocl.getIRI().getFragment()).append(" ");
                    buf.append("]");
                    break;
                case 5:
                    cls = ax.getClassesInSignature().iterator().next();
                    OWLIndividual ind = ((OWLClassAssertionAxiom) ax).getIndividual();
                    buf.append(cls.getIRI().getFragment()).append("(").append(ind.asOWLNamedIndividual()
                            .getIRI().getFragment()).append(")");
                    break;
                default:
                    buf.append(ax.getAxiomType());
                    for (Iterator<OWLEntity> iterator = ax.getSignature().iterator(); iterator.hasNext(); ) {
                        OWLEntity next = iterator.next();
                        buf.append(" [").append(next.getIRI().getFragment()).append("] ");
                    }
                    //throw new RuntimeException(ax.getAxiomType() + " has unknown index " + ax.getAxiomType().getIndex() + " !");
            }
            if (iter.hasNext())
                buf.append(",");
        }
        buf.append("}");
        return buf.toString();
    }

    /*@BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlapi3-log4j.properties").getFile();
        PropertyConfigurator.configure(conf); }*/

    @Test
    public void testDiagnosisSearcher() throws InconsistentTheoryException, OWLOntologyCreationException, SolverException, NoConflictException {
        OWLTheory th = new CalculateDiagnoses().getSimpleTheory(new CalculateDiagnoses().getOntologySimple("ontologies/ecai.simple.owl"), true);
        Searcher<OWLLogicalAxiom> searcher = new QuickXplain<OWLLogicalAxiom>();
        Set<OWLLogicalAxiom> diagnosis = searcher
                .search(th, th.getKnowledgeBase().getFaultyFormulas(), null).iterator().next();

        String logd = "Hitting set: {" + logCollection(diagnosis);
        logger.info(logd);
    }

    @Test
    public void testSimpleTestCases() throws InconsistentTheoryException, SolverException, URISyntaxException, OWLException {

        OWLTheory th = new CalculateDiagnoses().getSimpleTheory(new CalculateDiagnoses().getOntologySimple("ontologies/ecai.simple.owl"), false);
        debug.set_Theory(th);
        debug.reset();
        OWLOntologyManager manager = th.getOntology().getOWLOntologyManager();

        // test only entailed test case

        OWLDataFactory owlDataFactory = manager.getOWLDataFactory();

        OWLClassAssertionAxiom axiom = owlDataFactory.getOWLClassAssertionAxiom(owlDataFactory.getOWLClass(IRI.create(TEST_IRI + "C")),
                owlDataFactory.getOWLNamedIndividual(IRI.create(TEST_IRI + "w")));
        th.getKnowledgeBase().addEntailedTest(Collections.<OWLLogicalAxiom>singleton(axiom));

        assertTrue(debug.debug());

        String logd = logCollection(logger, "Hitting sets", debug.getValidHittingSets());
        String logc = logCollection(logger, "Conflicts", debug.getConflictSets());
        assertEquals("Hitting sets { {C}, {D} }", logd);
        assertEquals("Conflicts { {C,D} }", logc);

        // test only nonentailed test case

        debug.reset();

        th.getKnowledgeBase().removeEntailedTest(Collections.<OWLLogicalAxiom>singleton(axiom));
        th.getKnowledgeBase().addNonEntailedTest(Collections.<OWLLogicalAxiom>singleton(axiom));
        assertTrue(debug.debug());

        logd = logCollection(logger, "Hitting sets", debug.getValidHittingSets());
        logc = logCollection(logger, "Conflicts", debug.getConflictSets());
        assertEquals("Hitting sets { {A}, {B} }", logd);
        assertEquals("Conflicts { {A,B} }", logc);

        // test both test casea
        debug.reset();
        th.getKnowledgeBase().removeNonEntailedTest(Collections.<OWLLogicalAxiom>singleton(axiom));

        th.getKnowledgeBase().addEntailedTest(Collections.<OWLLogicalAxiom>singleton(axiom));

        OWLClassAssertionAxiom naxiom = owlDataFactory.getOWLClassAssertionAxiom(owlDataFactory.getOWLClass(IRI.create(TEST_IRI + "D")),
                owlDataFactory.getOWLNamedIndividual(IRI.create(TEST_IRI + "v")));
        th.getKnowledgeBase().addNonEntailedTest(Collections.<OWLLogicalAxiom>singleton(naxiom));

        assertTrue(debug.debug());

        logd = logCollection(logger, "Hitting sets", debug.getValidHittingSets());
        logc = logCollection(logger, "Conflicts", debug.getConflictSets());
        assertEquals("Hitting sets { {C}, {A,D}, {B,D} }", logd);
        assertEquals("Conflicts { {A,B,C}, {C,D} }", logc);

        // test without test cases

        debug.reset();
        th.getKnowledgeBase().removeEntailedTest(Collections.<OWLLogicalAxiom>singleton(axiom));
        th.getKnowledgeBase().removeNonEntailedTest(Collections.<OWLLogicalAxiom>singleton(naxiom));
        assertTrue(debug.debug());

        logd = logCollection(logger, "Hitting sets", debug.getValidHittingSets());
        logc = logCollection(logger, "Conflicts", debug.getConflictSets());
        assertEquals("Hitting sets { {A}, {B}, {C}, {D} }", logd);
        assertEquals("Conflicts { {A,B,C,D} }", logc);

    }

    @Test
    public void testTestCases() throws OWLException, SolverException, URISyntaxException, InconsistentTheoryException {

        testOntology("ontologies/ecai.owl", false);

        String logd = logCollection(logger, "Hitting sets", debug.getValidHittingSets());
        String logc = logCollection(logger, "Conflicts", debug.getConflictSets());
        assertEquals("Hitting sets { {A1}, {M1}, {M3,M2}, {A2,M2} }", logd);
        assertEquals("Conflicts { {M3,A1,A2,M1}, {A1,M1,M2} }", logc);


        // testConsistency that there are no changes in the theory

        debug.reset();
        assertTrue(debug.debug());

        assertEquals(logd, logCollection(logger, "Hitting sets", debug.getValidHittingSets()));
        assertEquals(logc, logCollection(logger, "Conflicts", debug.getConflictSets()));


        OWLDataFactory owlDataFactory = ((OWLTheory)debug.getTheory()).getOntology().getOWLOntologyManager().getOWLDataFactory();
        List<OWLLogicalAxiom> negTest = new LinkedList<OWLLogicalAxiom>();
        OWLSubClassOfAxiom ax = owlDataFactory.getOWLSubClassOfAxiom(owlDataFactory.getOWLClass(IRI.create(TEST_IRI + "B")),
                owlDataFactory.getOWLClass(IRI.create(TEST_IRI + "M3")));
        OWLLogicalAxiom negate = ((OWLTheory)debug.getTheory()).negate(ax);
        negTest.add(negate);


        debug.reset();
        assertTrue(debug.debug());

        logCollection(logger, "Hitting sets", debug.getValidHittingSets());
        logCollection(logger, "Conflicts", debug.getConflictSets());


    }

    /*
    @Test
    public void testNegation() throws SolverException, URISyntaxException, OWLException, InconsistentTheoryException {


        OWLDataFactory owlDataFactory = manager.getOWLDataFactory();
        List<OWLLogicalAxiom> list = new LinkedList<OWLLogicalAxiom>();

        OWLLogicalAxiom ax = owlDataFactory.getOWLSubClassOfAxiom(owlDataFactory.getOWLClass(IRI.create(TEST_IRI + "B")),
                owlDataFactory.getOWLClass(IRI.create(TEST_IRI + "M3")));
        list.add(ax);
        ax = owlDataFactory.getOWLClassAssertionAxiom(owlDataFactory.getOWLClass(IRI.create(TEST_IRI + "C")),
                owlDataFactory.getOWLNamedIndividual(IRI.create(TEST_IRI + "w")));
        list.add(ax);

        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        OWLOntology ontology = manager.createOntology();

        for (OWLLogicalAxiom axiom : list) {

            manager.addAxiom(ontology, axiom);
            OWLLogicalAxiom nax = negateFormulas(axiom);
            manager.addAxiom(ontology, nax);
            OWLTheory th = new OWLTheory(reasonerFactory, ontology);
            Assert.assertFalse(th.verifyRequirements());
        }

    }


    @Test
    public void readTest() throws InconsistentTheoryException, OWLOntologyCreationException, SolverException, FileNotFoundException {
        FileInputStream st = new FileInputStream("C:\\Users\\kostya\\ontologies\\Ontology1300710969086\\Ontology1300710969086.owl");
        OWLOntology o = manager.loadOntologyFromOntologyDocument(st);
        o.getLogicalAxioms();
    }
    */

    public OWLLogicalAxiom negate(OWLLogicalAxiom ax) {
        OWLNegateAxiom vis = new OWLNegateAxiom(manager.getOWLDataFactory());
        OWLLogicalAxiom negated = (OWLLogicalAxiom) ax.accept(vis);
        return negated;
    }

    private void testEntailment(OWLTheory th, OWLReasoner solver, OWLLogicalAxiom ax, boolean res) {
        OWLLogicalAxiom neg = th.negate(ax);
        th.getOwlOntologyManager().addAxiom(th.getOntology(), neg);
        solver.flush();
        assertEquals(solver.isConsistent(), res);
        //if (res)
        //assertEquals(solver.getUnsatisfiableClasses().getSize(), 1);
        th.getOwlOntologyManager().removeAxiom(th.getOntology(), neg);
    }

    @Test
    public void testDebug1() throws OWLException, SolverException, URISyntaxException, InconsistentTheoryException {
        testOntology("ontologies/ecai.1.owl", false);
        String logd = logCollection(logger, "Hitting sets", debug.getValidHittingSets());
        String logc = logCollection(logger, "Conflicts", debug.getConflictSets());
        assertEquals("Hitting sets { {A1}, {M1}, {M2} }", logd);
        assertEquals("Conflicts { {A1,M1,M2} }", logc);
    }

    @Test
    public void testDebug2() throws OWLException, SolverException, URISyntaxException, InconsistentTheoryException {
        testOntology("ontologies/ecai.2.owl", false);

        String logd = logCollection(logger, "Hitting sets", debug.getValidHittingSets());
        String logc = logCollection(logger, "Conflicts", debug.getConflictSets());
        assertEquals("Hitting sets { {M3}, {A1}, {A2}, {M1} }", logd);
        assertEquals("Conflicts { {M3,A1,A2,M1} }", logc);
    }

    @Test
    public void testCommonEntailments() throws OWLException, SolverException, URISyntaxException, InconsistentTheoryException {
        OWLTheory owlTheory = testOntology("ontologies/ecai.owl", false);

        Iterator<? extends Set<OWLLogicalAxiom>> hsi = debug.getValidHittingSets().iterator();
        Set<OWLLogicalAxiom> entailments = new TreeSet<OWLLogicalAxiom>();
        while (hsi.hasNext()) {
            Collection<OWLLogicalAxiom> inferredAxioms = owlTheory.getEntailments(hsi.next());
            if (entailments.isEmpty())
                entailments.addAll(inferredAxioms);
            else
                entailments.retainAll(inferredAxioms);
        }
        String log = logCollection(entailments);
        logger.info(log);

    }

    @Test
    public void testDebugFull() throws OWLException, SolverException, URISyntaxException, InconsistentTheoryException {
        testOntology("ontologies/ecai.owl", false);

        String logd = logCollection(logger, "Hitting sets", debug.getValidHittingSets());
        String logc = logCollection(logger, "Conflicts", debug.getConflictSets());
        assertEquals("Hitting sets { {A1}, {M1}, {M3,M2}, {A2,M2} }", logd);
        assertEquals("Conflicts { {M3,A1,A2,M1}, {A1,M1,M2} }", logc);
    }

    @Test
    public void testDebug3() throws OWLException, SolverException, URISyntaxException, InconsistentTheoryException {
        testOntology("ontologies/ecai.3.owl", false);

        String logd = logCollection(logger, "Hitting sets", debug.getValidHittingSets());
        String logc = logCollection(logger, "Conflicts", debug.getConflictSets());
        //assertEquals("Hitting sets { {M2,A2}, {M2,M3}, {M1}, {A1} }", logd);
        //assertEquals("Conflicts { {M2,M1,A1}, {M3,A2,M1,A1} }", logc);
    }

    //@Test
    public void testDebugCorrect() throws OWLException, SolverException, URISyntaxException, InconsistentTheoryException {
        testOntology("ontologies/ecai.corrected.owl", true);

        String logd = logCollection(logger, "Hitting sets", debug.getValidHittingSets());
        String logc = logCollection(logger, "Conflicts", debug.getConflictSets());
        assertEquals("Hitting sets { }", logd);
        assertEquals("Conflicts { }", logc);
    }

    /* @Test
    public void testEntailment() throws OWLException, SolverException, URISyntaxException {
        testOntology("ontologies/ecai.corrected.owl", true);

        Collection<OWLLogicalAxiom> inferredAxioms = debugger.getSearchable().getEntailments();
        OWLLogicalAxiom ax = inferredAxioms.iterator().next();
        OWLOntology owlOntology = debugger.getOWLOntology();
        OWLReasoner reasoner = debugger.getSearchable().getSolver();

        assertTrue(reasoner.isEntailed(ax));
        OWLLogicalAxiom neg = debugger.getSearchable().negateFormulas(ax);
        owlOntology.getOWLOntologyManager().addAxiom(owlOntology, neg);
        reasoner.flush();
        boolean consistent = reasoner.verifyRequirements();
        assertFalse(consistent);
    }               */

    @Test
    public void testDebugIncoherent() throws OWLException, SolverException, URISyntaxException, InconsistentTheoryException {
        testOntology("ontologies/ecai.incoherent.owl", false);

        String logd = logCollection(logger, "Hitting sets", debug.getValidHittingSets());
        String logc = logCollection(logger, "Conflicts", debug.getConflictSets());
        assertEquals("Hitting sets { {A1}, {M2}, {D[ C A1 ]} }", logd);
        assertEquals("Conflicts { {A1,M2,D[ C A1 ]} }", logc);
    }


    private OWLTheory testOntology(String path, boolean sat) throws OWLException, SolverException, URISyntaxException, InconsistentTheoryException {
        return testOntology(path, sat, null, null);
    }

    private OWLTheory testOntology(String path, boolean sat, Collection<OWLLogicalAxiom> positiveCases,
                                   Collection<OWLLogicalAxiom> negativeCases) throws OWLException, SolverException,
            URISyntaxException, InconsistentTheoryException {
        //debugger.reset();
        OWLTheory th = new CalculateDiagnoses().getSimpleTheory(new CalculateDiagnoses().getOntologySimple(path), false);

        if (positiveCases != null)
            for (OWLLogicalAxiom test : positiveCases)
                th.getKnowledgeBase().addPositiveTest(Collections.singleton(test));
        if (negativeCases != null)
            for (OWLLogicalAxiom test : negativeCases)
                th.getKnowledgeBase().addNegativeTest(Collections.singleton(test));

        debug.set_Theory(th);
        debug.reset();
        debug.debug();
        //debugger.setSearchable(th);
        //assertEquals(debugger.debug(), !sat);
        return th;
    }


    private Collection<OWLLogicalAxiom> getInferredAxioms(OWLTheory th, Set<OWLLogicalAxiom> hs) {
        th.removeAxioms(hs, th.getOntology());
        OWLReasoner solver = th.getSolver();
        //testConsistency(solver, true);

        OWLOntology ontology = th.getOntology();
        Set<OWLClass> classes = new TreeSet<OWLClass>();
        for (OWLOntology ont : solver.getRootOntology().getImportsClosure()) {
            classes.addAll(ont.getClassesInSignature(true));
        }

        Collection<OWLLogicalAxiom> axs = new TreeSet<OWLLogicalAxiom>();

        for (OWLClass cl : classes) {
            getSubClasses(axs, cl);
        }

        th.addAxioms(hs, th.getOntology());
        return axs;
    }

    private void getSubClasses(Collection<OWLLogicalAxiom> axs, OWLClass cl) {
        if (cl.isTopEntity() || cl.isBottomEntity())
            return;
        OWLReasoner solver = ((OWLTheory)debug.getTheory()).getSolver();
        OWLOntology ontology = ((OWLTheory)debug.getTheory()).getOntology();
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        OWLDataFactory dataFactory = manager.getOWLDataFactory();

        for (OWLClass sub : solver.getSubClasses(cl, true).getFlattened()) {
            if (!sub.isTopEntity() && !sub.isBottomEntity()) {
                axs.add(dataFactory.getOWLSubClassOfAxiom(sub, cl));
                getSubClasses(axs, sub);
            }
        }
    }

    private Collection<Collection<OWLLogicalAxiom>> getDiagnosesEntailments(OWLTheory th,
                                                                            Collection<Set<OWLLogicalAxiom>> hittingSets)
            throws OWLException, SolverException {
        List<Collection<OWLLogicalAxiom>> entailments = new LinkedList<Collection<OWLLogicalAxiom>>();

        if (hittingSets == null || hittingSets.isEmpty()) {
            entailments.add(th.getEntailments(new TreeSet<OWLLogicalAxiom>()));
            return entailments;
        }
        for (Set<OWLLogicalAxiom> hs : hittingSets) {
            Collection<OWLLogicalAxiom> ax = th.getEntailments(hs);
            entailments.add(ax);
        }
        return entailments;
    }

    private Collection<Collection<OWLLogicalAxiom>> createTestCases(Collection<OWLLogicalAxiom> ent, OWLTheory theory) {
        Collection<Collection<OWLLogicalAxiom>> tests = new LinkedList<Collection<OWLLogicalAxiom>>();
        for (OWLLogicalAxiom ax : ent) {
            Collection<OWLLogicalAxiom> test = new LinkedList<OWLLogicalAxiom>();
            test.add(theory.negate(ax));
            tests.add(test);
        }
        return tests;
    }

}
