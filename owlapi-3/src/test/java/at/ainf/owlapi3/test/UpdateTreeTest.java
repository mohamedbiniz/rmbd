package at.ainf.owlapi3.test;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.SimpleCostsEstimator;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.BreadthFirstSearchStrategy;
import at.ainf.owlapi3.base.CalculateDiagnoses;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.parser.MyOWLRendererParser;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 05.11.12
 * Time: 16:29
 * To change this template use File | Settings | File Templates.
 */
public class UpdateTreeTest {

    private static Logger logger = LoggerFactory.getLogger(UpdateTreeTest.class.getName());
    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

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
    public void updateTest() throws InconsistentTheoryException, OWLOntologyCreationException, SolverException, NoConflictException {
        //SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        search.setCostsEstimator(new SimpleCostsEstimator<OWLLogicalAxiom>());
        search.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());
        search.setSearcher(new QuickXplain<OWLLogicalAxiom>());
        OWLTheory th = createTheory(manager, "ontologies/koala.owl", false);
        search.setSearchable(th);
        search.setFormulaRenderer(new MyOWLRendererParser(null));

        MyOWLRendererParser parser = new MyOWLRendererParser(th.getOriginalOntology());
        th.getKnowledgeBase().addEntailedTest(Collections.singleton(parser.parse("Marsupials DisjointWith Person")));
        th.getKnowledgeBase().addEntailedTest(Collections.singleton(parser.parse("Koala SubClassOf Marsupials")));
        th.getKnowledgeBase().addEntailedTest(Collections.singleton(parser.parse("hasDegree Domain Person")));
        th.getKnowledgeBase().addNonEntailedTest(Collections.singleton(parser.parse("isHardWorking Domain Person")));

        search.setMaxDiagnosesNumber(9);
        search.start();

        th.getKnowledgeBase().removeEntailedTest(Collections.singleton(parser.parse("Marsupials DisjointWith Person")));
        th.getKnowledgeBase().removeEntailedTest(Collections.singleton(parser.parse("Koala SubClassOf Marsupials")));
        th.getKnowledgeBase().removeEntailedTest(Collections.singleton(parser.parse("hasDegree Domain Person")));
        th.getKnowledgeBase().removeNonEntailedTest(Collections.singleton(parser.parse("isHardWorking Domain Person")));

        search.setMaxDiagnosesNumber(9);
        search.start();

        for (Set<OWLLogicalAxiom> hs : search.getDiagnoses())
            logger.info(new CalculateDiagnoses().renderAxioms(hs));
    }

}
