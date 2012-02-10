package at.ainf.owlcontroller;

import at.ainf.diagnosis.quickxplain.FastDiagnosis;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.tree.BreadthFirstSearch;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import at.ainf.owlcontroller.parser.MyOWLRendererParser;
import at.ainf.theory.model.ITheory;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 16.01.12
 * Time: 13:11
 * To change this template use File | Settings | File Templates.
 */
public class FastDiagTest {
    private static Logger logger = Logger.getLogger(FastDiagTest.class.getName());
    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

    @BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlcontroller-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }

    @Test
    public void testFasterDiagnosisSearch() throws InconsistentTheoryException, OWLOntologyCreationException, SolverException, NoConflictException {
        DualStorage<OWLLogicalAxiom> storage = new DualStorage<OWLLogicalAxiom>();
        TreeSearch<? extends AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = new BreadthFirstSearch<OWLLogicalAxiom>(storage);
        search.setSearcher(new FastDiagnosis<OWLLogicalAxiom>());
        OWLTheory th = createTheory(manager, "queryontologies/koala.owl", true);
        search.setTheory(th);
        search.setAxiomRenderer(new MyOWLRendererParser(null));

        search.run();

        for (Set<OWLLogicalAxiom> hs : search.getStorage().getDiagnoses())
            logger.info(Utils.renderAxioms(hs));
    }

    @Test
    public void testResultsEqual() throws InconsistentTheoryException, OWLOntologyCreationException, SolverException, NoConflictException {

        String ont = "koala.owl";

        TreeSearch<? extends AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> searchNormal = new BreadthFirstSearch<OWLLogicalAxiom>(new SimpleStorage<OWLLogicalAxiom>());
        searchNormal.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
        OWLTheory theoryNormal = createTheory(manager, "queryontologies/" + ont, false);
        searchNormal.setTheory(theoryNormal);
        searchNormal.run();
        Set<? extends AxiomSet<OWLLogicalAxiom>> resultNormal = searchNormal.getStorage().getDiagnoses();

        manager = OWLManager.createOWLOntologyManager();
        TreeSearch<? extends AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> searchDual = new BreadthFirstSearch<OWLLogicalAxiom>(new DualStorage<OWLLogicalAxiom>());
        searchDual.setSearcher(new FastDiagnosis<OWLLogicalAxiom>());
        OWLTheory theoryDual = createTheory(manager, "queryontologies/" + ont, true);
        searchDual.setTheory(theoryDual);
        searchDual.run();
        Set<? extends AxiomSet<OWLLogicalAxiom>> resultDual = searchDual.getStorage().getDiagnoses();

      ////

        assert(resultNormal.equals(resultDual));
    }

    @Test
    public void testQx() throws InconsistentTheoryException, OWLOntologyCreationException, SolverException, NoConflictException {
        HashSet<OWLLogicalAxiom> set = new HashSet<OWLLogicalAxiom>();
        OWLTheory th = createTheory(manager, "queryontologies/koala.owl", true);
        MyOWLRendererParser parser = new MyOWLRendererParser(th.getOriginalOntology());
        set.add(parser.parse("Marsupials DisjointWith Person"));
        set.add(parser.parse("isHardWorking Domain Person"));
        set.add(parser.parse("Koala SubClassOf Marsupials"));
        set.add(parser.parse("Quokka SubClassOf Marsupials"));
        ArrayList<OWLLogicalAxiom> l = new ArrayList<OWLLogicalAxiom>(th.getActiveFormulas());
        Collections.sort(l);
        Set<OWLLogicalAxiom> res = new FastDiagnosis<OWLLogicalAxiom>().search(th, l, set);

        System.out.println(Utils.renderAxioms(res));

    }

    @Test
    public void testFasterDiagnosisSearchQuick() throws InconsistentTheoryException, OWLOntologyCreationException, SolverException, NoConflictException {
        HashSet<OWLLogicalAxiom> set = new HashSet<OWLLogicalAxiom>();
        OWLTheory th = createTheory(manager, "queryontologies/koala.owl", true);
        MyOWLRendererParser parser = new MyOWLRendererParser(th.getOriginalOntology());
        set.add(parser.parse("Marsupials DisjointWith Person"));
        ArrayList<OWLLogicalAxiom> l = new ArrayList<OWLLogicalAxiom>(th.getActiveFormulas());
        Collections.sort(l);
        Set<OWLLogicalAxiom> res = new FastDiagnosis<OWLLogicalAxiom>().search(th,l,set);

        System.out.println(Utils.renderManyAxioms(l) + "\n\n"+Utils.renderAxioms(res));

    }

    @Test
    public void testUnivNormal() throws InconsistentTheoryException, OWLOntologyCreationException, SolverException, NoConflictException {
        HashSet<OWLLogicalAxiom> positiveTestcase = new HashSet<OWLLogicalAxiom>();
        HashSet<OWLLogicalAxiom> negativeTestcase = new HashSet<OWLLogicalAxiom>();
        OWLTheory th = createTheory(manager, "queryontologies/Univ.owl", false);
        MyOWLRendererParser parser = new MyOWLRendererParser(th.getOriginalOntology());
        positiveTestcase.add(parser.parse("ProfessorInHCIorAI SubClassOf advisorOf only AIStudent"));
        positiveTestcase.add(parser.parse("AIStudent DisjointWith HCIStudent"));
        negativeTestcase.add(parser.parse("CS_Department SubClassOf affiliatedWith some CS_Library"));
        negativeTestcase.add(parser.parse("hasAdvisor InverseOf advisorOf"));
        th.addEntailedTest(positiveTestcase);
        th.addNonEntailedTest(negativeTestcase);
        HashSet<OWLLogicalAxiom> target = new HashSet<OWLLogicalAxiom>();
        target.add(parser.parse("AssistantProfessor EquivalentTo TeachingFaculty and (hasTenure value false)"));
        target.add(parser.parse("CS_Library SubClassOf affiliatedWith some EE_Library"));
        target.add(parser.parse("hasAdvisor InverseOf advisorOf"));

        SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        TreeSearch<? extends AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = new BreadthFirstSearch<OWLLogicalAxiom>(storage);
        search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
        search.setTheory(th);
        search.setAxiomRenderer(new MyOWLRendererParser(null));
        search.run();
        
        for (AxiomSet<OWLLogicalAxiom> d : search.getStorage().getDiagnoses()) {
            if (target.equals(d)) System.out.println("target is there  ");
        }

    }

    @Test
    public void testUnivDual() throws InconsistentTheoryException, OWLOntologyCreationException, SolverException, NoConflictException {
        HashSet<OWLLogicalAxiom> positiveTestcase = new HashSet<OWLLogicalAxiom>();
        HashSet<OWLLogicalAxiom> negativeTestcase = new HashSet<OWLLogicalAxiom>();
        OWLTheory th = createTheory(manager, "queryontologies/Univ.owl", true);
        MyOWLRendererParser parser = new MyOWLRendererParser(th.getOriginalOntology());
        positiveTestcase.add(parser.parse("ProfessorInHCIorAI SubClassOf advisorOf only AIStudent"));
        positiveTestcase.add(parser.parse("AIStudent DisjointWith HCIStudent"));
        negativeTestcase.add(parser.parse("CS_Department SubClassOf affiliatedWith some CS_Library"));
        negativeTestcase.add(parser.parse("hasAdvisor InverseOf advisorOf"));
        th.addEntailedTest(positiveTestcase);
        th.addNonEntailedTest(negativeTestcase);
        HashSet<OWLLogicalAxiom> target = new HashSet<OWLLogicalAxiom>();
        target.add(parser.parse("AssistantProfessor EquivalentTo TeachingFaculty and (hasTenure value false)"));
        target.add(parser.parse("CS_Library SubClassOf affiliatedWith some EE_Library"));
        target.add(parser.parse("hasAdvisor InverseOf advisorOf"));

        SimpleStorage<OWLLogicalAxiom> storage = new DualStorage<OWLLogicalAxiom>();
        TreeSearch<? extends AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = new BreadthFirstSearch<OWLLogicalAxiom>(storage);
        search.setSearcher(new FastDiagnosis<OWLLogicalAxiom>());
        search.setTheory(th);
        search.setAxiomRenderer(new MyOWLRendererParser(null));
        search.run();

        for (AxiomSet<OWLLogicalAxiom> d : search.getStorage().getDiagnoses()) {
            if (target.equals(d)) System.out.println("target is there");
        }


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
        SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        TreeSearch<? extends AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = new BreadthFirstSearch<OWLLogicalAxiom>(storage);
        search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
        OWLTheory th = createTheory(manager, "queryontologies/koala.owl", false);
        search.setTheory(th);
        search.setAxiomRenderer(new MyOWLRendererParser(null));
        search.run();

        OWLLogicalAxiom axiom = search.getStorage().getDiagnoses().iterator().next().iterator().next();
        System.out.println(axiom);

        for (Set<OWLLogicalAxiom> hs : search.getStorage().getDiagnoses())
            System.out.println(Utils.renderAxioms(hs));

        /*Searcher<OWLLogicalAxiom> searcher = new NewQuickXplain<OWLLogicalAxiom>();
        Set<OWLLogicalAxiom> diagnosis = searcher.search(new OWLDiagnosisSearchableObject(th), th.getActiveFormulas(), null);

        String logd = "Hitting set: {" + Utils.logCollection(diagnosis);
        logger.info(logd);*/
    }
}
