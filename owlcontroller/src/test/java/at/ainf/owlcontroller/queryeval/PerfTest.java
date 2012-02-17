package at.ainf.owlcontroller.queryeval;

import at.ainf.diagnosis.Searcher;
import at.ainf.diagnosis.quickxplain.FastDiagnosis;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.tree.BreadthFirstSearch;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlcontroller.Utils;
import at.ainf.owlcontroller.parser.MyOWLRendererParser;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.DualStorage;
import at.ainf.theory.storage.SimpleStorage;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 16.02.12
 * Time: 19:23
 * To change this template use File | Settings | File Templates.
 */
public class PerfTest {

    private static Logger logger = Logger.getLogger(PerfTest.class.getName());

    @BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlcontroller-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }
    @Ignore @Test
    public void testDualTreePruning() throws InconsistentTheoryException, OWLOntologyCreationException, SolverException, NoConflictException {

        String ont = "queryontologies/example1302.owl";
        List<String> testCases = new LinkedList<String>();
        testCases.add("w Type D");
        runComparison(ont, testCases);
        testCases.add("w Type C");
        runComparison(ont, testCases);
    }

    private void runComparison(String ont, List<String> testCases) throws SolverException, InconsistentTheoryException, OWLOntologyCreationException, NoConflictException {
        logger.info("----- Computing dual case -----");
        Searcher<OWLLogicalAxiom> dualSearcher = new FastDiagnosis<OWLLogicalAxiom>();
        SimpleStorage<OWLLogicalAxiom> dualStorage = new DualStorage<OWLLogicalAxiom>();

        TreeSearch<? extends AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> searchDual =
                new BreadthFirstSearch<OWLLogicalAxiom>(dualStorage);

        computeQueryExample(ont, true, dualSearcher, searchDual, testCases);

        logger.info("----- Computing normal case -----");
        Searcher<OWLLogicalAxiom> searcher = new NewQuickXplain<OWLLogicalAxiom>();
        SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();

        TreeSearch<? extends AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> searchNormal =
                new BreadthFirstSearch<OWLLogicalAxiom>(storage);

        computeQueryExample(ont, false, searcher, searchNormal, testCases);

        assert (storage.getDiagnoses().containsAll(dualStorage.getDiagnoses()));
        assert (storage.getConflicts().containsAll(dualStorage.getConflicts()));
        assert (dualStorage.getDiagnoses().containsAll(storage.getDiagnoses()));
        assert (dualStorage.getConflicts().containsAll(storage.getConflicts()));
    }

    private void computeQueryExample(String ont, boolean dual, Searcher<OWLLogicalAxiom> searcher, TreeSearch<? extends AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> searchNormal, List<String> testCases) throws SolverException, InconsistentTheoryException, OWLOntologyCreationException, NoConflictException {

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        searchNormal.setSearcher(searcher);
        OWLTheory theoryNormal = createTheory(manager, ont, dual);
        searchNormal.setTheory(theoryNormal);
        searchNormal.run(2);

        logger.info("First 2 Diagnoses and corresponding conflicts before test case");
        for (AxiomSet<OWLLogicalAxiom> hs : searchNormal.getStorage().getDiagnoses())
            logger.info("HS " + Utils.renderAxioms(hs));
        for (AxiomSet<OWLLogicalAxiom> confl : searchNormal.getStorage().getConflicts())
            logger.info("cs " + Utils.renderAxioms(confl));

        HashSet<OWLLogicalAxiom> positiveTestcase = new HashSet<OWLLogicalAxiom>();
        MyOWLRendererParser parser = new MyOWLRendererParser(theoryNormal.getOriginalOntology());
        for (String testcase : testCases)
            positiveTestcase.add(parser.parse(testcase));

        logger.info("All diagnoses and conflicts with test cases");
        theoryNormal.addEntailedTest(positiveTestcase);
        searchNormal.continueSearch();
        for (AxiomSet<OWLLogicalAxiom> hs : searchNormal.getStorage().getDiagnoses())
            logger.info("HS " + Utils.renderAxioms(hs));
        for (AxiomSet<OWLLogicalAxiom> confl : searchNormal.getStorage().getConflicts())
            logger.info("cs " + Utils.renderAxioms(confl));
    }


    @Test
    public void testResultsEqualTime() throws InconsistentTheoryException, OWLOntologyCreationException, SolverException, NoConflictException {

        String ont = "koala.owl";
        //String ont = "koala.owl";

        long normal = System.currentTimeMillis();
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        TreeSearch<? extends AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> searchNormal = new BreadthFirstSearch<OWLLogicalAxiom>(new SimpleStorage<OWLLogicalAxiom>());
        searchNormal.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
        OWLTheory theoryNormal = createTheory(manager, "queryontologies/" + ont, false);
        searchNormal.setTheory(theoryNormal);
        searchNormal.run();
        Set<? extends AxiomSet<OWLLogicalAxiom>> resultNormal = searchNormal.getStorage().getDiagnoses();
        normal = System.currentTimeMillis() - normal;

        long dual = System.currentTimeMillis();
        manager = OWLManager.createOWLOntologyManager();
        TreeSearch<? extends AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> searchDual = new BreadthFirstSearch<OWLLogicalAxiom>(new DualStorage<OWLLogicalAxiom>());
        searchDual.setSearcher(new FastDiagnosis<OWLLogicalAxiom>());
        OWLTheory theoryDual = createTheory(manager, "queryontologies/" + ont, true);
        searchDual.setTheory(theoryDual);
        searchDual.run();
        Set<? extends AxiomSet<OWLLogicalAxiom>> resultDual = searchDual.getStorage().getDiagnoses();
        dual = System.currentTimeMillis() - dual;

        logger.info("normal " + Utils.getStringTime(normal) + " subsets: " + theoryNormal.getCache().size());
        logger.info("dual " + Utils.getStringTime(dual) + " subsets: " + theoryDual.getCache().size());

        assert (resultNormal.equals(resultDual));

    }

    public OWLTheory createTheory(OWLOntologyManager manager, String path, boolean dual) throws SolverException, InconsistentTheoryException, OWLOntologyCreationException {
        InputStream st = ClassLoader.getSystemResourceAsStream(path);
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(st);
        Set<OWLLogicalAxiom> bax = new HashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        OWLTheory theory = null;
        if (dual)
            theory = new DualTreeOWLTheory(reasonerFactory, ontology, bax);
        else
            theory = new OWLTheory(reasonerFactory, ontology, bax);
        //assert (theory.verifyRequirements());

        return theory;
    }

}
