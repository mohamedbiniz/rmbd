package at.ainf.owlcontroller;

import at.ainf.diagnosis.quickxplain.FastDiagnosis;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.tree.BreadthFirstSearch;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import at.ainf.owlcontroller.parser.MyOWLRendererParser;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.SimpleStorage;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
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
        SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        TreeSearch<? extends AxiomSet<OWLLogicalAxiom>, Set<OWLLogicalAxiom>, OWLLogicalAxiom> search = new BreadthFirstSearch<OWLLogicalAxiom>(storage);
        search.setSearcher(new FastDiagnosis<OWLLogicalAxiom>());
        DualTreeOWLTheory th = loadTheory(manager, "queryontologies/koala.owl");
        search.setTheory(th);
        search.setAxiomRenderer(new MyOWLRendererParser(null));

        search.run();

        for (Set<OWLLogicalAxiom> hs : search.getStorage().getConflictSets())
            logger.info(Utils.renderAxioms(hs));
    }

    @Test
    public void testFasterDiagnosisSearchQuick() throws InconsistentTheoryException, OWLOntologyCreationException, SolverException, NoConflictException {
        HashSet<OWLLogicalAxiom> set = new HashSet<OWLLogicalAxiom>();
        DualTreeOWLTheory th = loadTheory(manager, "queryontologies/koala.owl");
        MyOWLRendererParser parser = new MyOWLRendererParser(th.getOriginalOntology());
        set.add(parser.parse("Marsupials DisjointWith Person"));
        ArrayList<OWLLogicalAxiom> l = new ArrayList<OWLLogicalAxiom>(th.getActiveFormulas());
        Collections.sort(l);
        Set<OWLLogicalAxiom> res = new FastDiagnosis<OWLLogicalAxiom>().search(th,l,set);

        System.out.println(Utils.renderManyAxioms(l) + "\n"+Utils.renderAxioms(res));

    }

    public static DualTreeOWLTheory loadTheory(OWLOntologyManager manager, String path) throws SolverException, InconsistentTheoryException, OWLOntologyCreationException {
        InputStream st = ClassLoader.getSystemResourceAsStream(path);
        return createTheory(manager.loadOntologyFromOntologyDocument(st));
    }

    public static DualTreeOWLTheory createTheory(OWLOntology ontology) throws SolverException, InconsistentTheoryException {
        Set<OWLLogicalAxiom> bax = new HashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        DualTreeOWLTheory theory = new DualTreeOWLTheory(reasonerFactory, ontology, bax);
        assert (theory.isConsistent());

        return theory;
    }

    @Test
    public void testConflictDiagnosisSearch() throws InconsistentTheoryException, OWLOntologyCreationException, SolverException, NoConflictException {
        SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        TreeSearch<? extends AxiomSet<OWLLogicalAxiom>, Set<OWLLogicalAxiom>, OWLLogicalAxiom> search = new BreadthFirstSearch<OWLLogicalAxiom>(storage);
        search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
        OWLTheory th = Utils.loadTheory(manager, "queryontologies/koala.owl");
        search.setTheory(th);
        search.setAxiomRenderer(new MyOWLRendererParser(null));
        search.run();

        OWLLogicalAxiom axiom = search.getStorage().getValidHittingSets().iterator().next().iterator().next();
        System.out.println(axiom);

        for (Set<OWLLogicalAxiom> hs : search.getStorage().getValidHittingSets())
            System.out.println(Utils.renderAxioms(hs));

        /*Searcher<OWLLogicalAxiom> searcher = new NewQuickXplain<OWLLogicalAxiom>();
        Set<OWLLogicalAxiom> diagnosis = searcher.search(new OWLDiagnosisSearchableObject(th), th.getActiveFormulas(), null);

        String logd = "Hitting set: {" + Utils.logCollection(diagnosis);
        logger.info(logd);*/
    }
}
