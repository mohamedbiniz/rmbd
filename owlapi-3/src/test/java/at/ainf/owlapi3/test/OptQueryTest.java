package at.ainf.owlapi3.test;

import at.ainf.diagnosis.Debugger;
import at.ainf.diagnosis.logging.MetricsLogger;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.module.iterative.diagsearcher.ModuleDiagSearcher;
import at.ainf.owlapi3.module.iterative.diagsearcher.ModuleOptQuerDiagSearcher;
import at.ainf.owlapi3.module.iterative.diagsearcher.ModuleTargetDiagSearcher;
import at.ainf.owlapi3.module.iterative.modulediagnosis.IterativeModuleDiagnosis;
import at.ainf.owlapi3.parser.MyOWLRendererParser;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static at.ainf.owlapi3.util.OWLUtils.createOntology;
import static at.ainf.owlapi3.util.OWLUtils.loadOntology;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 18.06.13
 * Time: 11:09
 * To change this template use File | Settings | File Templates.
 */
public class OptQueryTest {

    private static Logger logger = LoggerFactory.getLogger(OptQueryTest.class.getName());

    private static MetricsLogger metricsLogger = MetricsLogger.getInstance();

    @Test
    public void testOptQuery() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException, NoConflictException {

        ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());

        OWLOntology ontology = loadOntology ("ontologies/testoptquery.owl");

        Set<OWLLogicalAxiom> correctAxioms = new HashSet<OWLLogicalAxiom>();
        Set<OWLLogicalAxiom> falseAxioms = new HashSet<OWLLogicalAxiom>();
        MyOWLRendererParser parser = new MyOWLRendererParser(ontology);
        falseAxioms.add(parser.parse("A8 SubclassOf not A1"));
        correctAxioms.addAll(ontology.getLogicalAxioms());
        correctAxioms.remove(parser.parse("A8 SubclassOf not A1"));

        ModuleDiagSearcher optquery = new ModuleOptQuerDiagSearcher(null,correctAxioms,falseAxioms, false);
        optquery.setReasonerFactory(new Reasoner.ReasonerFactory());
        optquery.calculateDiag(ontology.getLogicalAxioms(), Collections.<OWLLogicalAxiom>emptySet());
    }

}
