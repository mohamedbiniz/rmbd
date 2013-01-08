package at.ainf.owlapi3.test;

import at.ainf.diagnosis.quickxplain.DirectDiagnosis;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.tree.*;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.SimpleCostsEstimator;
import at.ainf.diagnosis.tree.searchstrategy.BreadthFirstSearchStrategy;
import at.ainf.owlapi3.base.CalculateDiagnoses;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.parser.MyOWLRendererParser;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.FormulaSet;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 16.01.12
 * Time: 13:11
 * To change this template use File | Settings | File Templates.
 */
public class QXDiagTest {
    private static Logger logger = LoggerFactory.getLogger(QXDiagTest.class.getName());
    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

    /*@BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlapi3-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }*/

    @Ignore
    @Test
    public void testFasterDiagnosisSearch() throws InconsistentTheoryException, OWLOntologyCreationException, SolverException, NoConflictException {
        //SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        InvHsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new InvHsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        search.setCostsEstimator(new SimpleCostsEstimator<OWLLogicalAxiom>());
        search.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());
        search.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
        OWLTheory th = createTheory(manager, "ontologies/koala.owl", true);
        search.setSearchable(th);
        search.setFormulaRenderer(new MyOWLRendererParser(null));

        search.start();

        for (Set<OWLLogicalAxiom> hs : search.getDiagnoses())
            logger.info(new CalculateDiagnoses().renderAxioms(hs));
    }

    @Ignore
    @Test
    public void testResultsEqual() throws InconsistentTheoryException, OWLOntologyCreationException, SolverException, NoConflictException {

        String ont = "koala.owl";

        HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> searchNormal = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        searchNormal.setCostsEstimator(new SimpleCostsEstimator<OWLLogicalAxiom>());
        searchNormal.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());
        searchNormal.setSearcher(new QuickXplain<OWLLogicalAxiom>());
        OWLTheory theoryNormal = createTheory(manager, "ontologies/" + ont, false);
        searchNormal.setSearchable(theoryNormal);
        searchNormal.start();
        Set<? extends FormulaSet<OWLLogicalAxiom>> resultNormal = searchNormal.getDiagnoses();

        manager = OWLManager.createOWLOntologyManager();
        InvHsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> searchDual = new InvHsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        searchDual.setCostsEstimator(new SimpleCostsEstimator<OWLLogicalAxiom>());
        searchDual.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());
        searchDual.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
        OWLTheory theoryDual = createTheory(manager, "ontologies/" + ont, true);
        searchDual.setSearchable(theoryDual);
        searchDual.start();
        Set<? extends FormulaSet<OWLLogicalAxiom>> resultDual = searchDual.getDiagnoses();

      ////

        assert(resultNormal.equals(resultDual));
    }

    @Test
    public void testQx() throws InconsistentTheoryException, OWLOntologyCreationException, SolverException, NoConflictException {
        HashSet<OWLLogicalAxiom> set = new HashSet<OWLLogicalAxiom>();
        OWLTheory th = createTheory(manager, "ontologies/koala.owl", true);
        MyOWLRendererParser parser = new MyOWLRendererParser(th.getOriginalOntology());
        set.add(parser.parse("Marsupials DisjointWith Person"));
        set.add(parser.parse("isHardWorking Domain Person"));
        set.add(parser.parse("Koala SubClassOf Marsupials"));
        set.add(parser.parse("Quokka SubClassOf Marsupials"));
        ArrayList<OWLLogicalAxiom> l = new ArrayList<OWLLogicalAxiom>(th.getKnowledgeBase().getFaultyFormulas());
        Collections.sort(l);
        Set<OWLLogicalAxiom> res = new DirectDiagnosis<OWLLogicalAxiom>().search(th, l, set).iterator().next();

        logger.info(new CalculateDiagnoses().renderAxioms(res));

    }

    @Test
    public void testFasterDiagnosisSearchQuick() throws InconsistentTheoryException, OWLOntologyCreationException, SolverException, NoConflictException {
        HashSet<OWLLogicalAxiom> set = new HashSet<OWLLogicalAxiom>();
        OWLTheory th = createTheory(manager, "ontologies/koala.owl", true);
        MyOWLRendererParser parser = new MyOWLRendererParser(th.getOriginalOntology());
        set.add(parser.parse("Marsupials DisjointWith Person"));
        ArrayList<OWLLogicalAxiom> l = new ArrayList<OWLLogicalAxiom>(th.getKnowledgeBase().getFaultyFormulas());
        Collections.sort(l);
        Set<OWLLogicalAxiom> res = new DirectDiagnosis<OWLLogicalAxiom>().search(th,l,set).iterator().next();

        logger.info(new CalculateDiagnoses().renderAxioms(l) + "\n\n"+ new CalculateDiagnoses().renderAxioms(res));

    }

    @Test
    public void testUnivNormal() throws InconsistentTheoryException, OWLOntologyCreationException, SolverException, NoConflictException {
        HashSet<OWLLogicalAxiom> positiveTestcase = new HashSet<OWLLogicalAxiom>();
        HashSet<OWLLogicalAxiom> negativeTestcase = new HashSet<OWLLogicalAxiom>();
        OWLTheory th = createTheory(manager, "ontologies/Univ.owl", false);
        MyOWLRendererParser parser = new MyOWLRendererParser(th.getOriginalOntology());
        positiveTestcase.add(parser.parse("ProfessorInHCIorAI SubClassOf advisorOf only AIStudent"));
        positiveTestcase.add(parser.parse("AIStudent DisjointWith HCIStudent"));
        negativeTestcase.add(parser.parse("CS_Department SubClassOf affiliatedWith some CS_Library"));
        negativeTestcase.add(parser.parse("hasAdvisor InverseOf advisorOf"));
        th.getKnowledgeBase().addEntailedTest(positiveTestcase);
        th.getKnowledgeBase().addNonEntailedTest(negativeTestcase);
        HashSet<OWLLogicalAxiom> target = new HashSet<OWLLogicalAxiom>();
        target.add(parser.parse("AssistantProfessor EquivalentTo TeachingFaculty and (hasTenure value false)"));
        target.add(parser.parse("CS_Library SubClassOf affiliatedWith some EE_Library"));
        target.add(parser.parse("hasAdvisor InverseOf advisorOf"));

        //SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        search.setCostsEstimator(new SimpleCostsEstimator<OWLLogicalAxiom>());
        search.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());
        search.setSearcher(new QuickXplain<OWLLogicalAxiom>());
        search.setSearchable(th);
        search.setFormulaRenderer(new MyOWLRendererParser(null));
        search.start();

        boolean targetIsThere = false;
        for (FormulaSet<OWLLogicalAxiom> d : search.getDiagnoses()) {
            if (target.equals(d)) targetIsThere = true;
        }
        assertTrue(targetIsThere);

    }

    @Ignore
    @Test
    public void testUnivDual() throws InconsistentTheoryException, OWLOntologyCreationException, SolverException, NoConflictException {
        HashSet<OWLLogicalAxiom> positiveTestcase = new HashSet<OWLLogicalAxiom>();
        HashSet<OWLLogicalAxiom> negativeTestcase = new HashSet<OWLLogicalAxiom>();
        OWLTheory th = createTheory(manager, "ontologies/Univ.owl", true);
        MyOWLRendererParser parser = new MyOWLRendererParser(th.getOriginalOntology());
        positiveTestcase.add(parser.parse("ProfessorInHCIorAI SubClassOf advisorOf only AIStudent"));
        positiveTestcase.add(parser.parse("AIStudent DisjointWith HCIStudent"));
        negativeTestcase.add(parser.parse("CS_Department SubClassOf affiliatedWith some CS_Library"));
        negativeTestcase.add(parser.parse("hasAdvisor InverseOf advisorOf"));
        th.getKnowledgeBase().addEntailedTest(positiveTestcase);
        th.getKnowledgeBase().addNonEntailedTest(negativeTestcase);
        HashSet<OWLLogicalAxiom> target = new HashSet<OWLLogicalAxiom>();
        target.add(parser.parse("AssistantProfessor EquivalentTo TeachingFaculty and (hasTenure value false)"));
        target.add(parser.parse("CS_Library SubClassOf affiliatedWith some EE_Library"));
        target.add(parser.parse("hasAdvisor InverseOf advisorOf"));

        //SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();

        InvHsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new InvHsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        search.setCostsEstimator(new SimpleCostsEstimator<OWLLogicalAxiom>());
        search.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());
        search.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
        search.setSearchable(th);
        search.setFormulaRenderer(new MyOWLRendererParser(null));
        search.start();

        boolean targetIsThere = false;
        for (FormulaSet<OWLLogicalAxiom> d : search.getDiagnoses()) {
            if (target.equals(d)) targetIsThere = true;
        }
        assertTrue(targetIsThere);

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
        if(dual)
            theory = new DualTreeOWLTheory(reasonerFactory, ontology, bax);
        else
            theory = new OWLTheory(reasonerFactory, ontology, bax);
        //assert (theory.verifyRequirements());

        return theory;
    }

    @Test
    public void testConflictDiagnosisSearch() throws InconsistentTheoryException, OWLOntologyCreationException, SolverException, NoConflictException {
        //SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        search.setCostsEstimator(new SimpleCostsEstimator<OWLLogicalAxiom>());
        search.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());
        search.setSearcher(new QuickXplain<OWLLogicalAxiom>());
        OWLTheory th = createTheory(manager, "ontologies/koala.owl", false);
        search.setSearchable(th);
        search.setFormulaRenderer(new MyOWLRendererParser(null));
        search.start();

        OWLLogicalAxiom axiom = search.getDiagnoses().iterator().next().iterator().next();
        logger.info(axiom.toString());


        for (Set<OWLLogicalAxiom> hs : search.getDiagnoses())
            logger.info(new CalculateDiagnoses().renderAxioms(hs));

        /*Searcher<OWLLogicalAxiom> searcher = new QuickXplain<OWLLogicalAxiom>();
        Set<OWLLogicalAxiom> diagnosis = searcher.start(new OWLDiagnosisSearchableObject(th), th.getFaultyFormulas(), null);

        String logd = "Hitting set: {" + LogUtil.logCollection(diagnosis);
        logger.info(logd);*/
    }
}
