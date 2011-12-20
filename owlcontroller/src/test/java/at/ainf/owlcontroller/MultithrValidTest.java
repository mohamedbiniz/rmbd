package at.ainf.owlcontroller;

import at.ainf.diagnosis.debugger.SimpleQueryDebugger;
import at.ainf.diagnosis.partitioning.BruteForce;
import at.ainf.diagnosis.partitioning.EntropyScoringFunction;
import at.ainf.diagnosis.partitioning.Partitioning;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlcontroller.parser.MyOWLRendererParser;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.model.UnsatisfiableFormulasException;
import at.ainf.theory.storage.Partition;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.07.11
 * Time: 16:10
 * To change this template use File | Settings | File Templates.
 */
public class MultithrValidTest {
    private static Logger logger = Logger.getLogger(MultithrValidTest.class.getName());
    //private OWLDebugger debugger  = null; //new SimpleDebugger();
    private SimpleQueryDebugger<OWLLogicalAxiom> debugger = new SimpleQueryDebugger<OWLLogicalAxiom>(null);
    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

    @BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlcontroller-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }

    @Test
    public void testBruteForce2() throws OWLException, UnsatisfiableFormulasException, SolverException {

        InputStream st = ClassLoader.getSystemResourceAsStream("queryontologies/Univ.owl");
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(st);
        Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        OWLTheory theory = new OWLTheory(reasonerFactory, ontology, bax);
        MyOWLRendererParser parser = new MyOWLRendererParser(ontology);

        debugger.set_Theory(theory);
        debugger.reset();
        debugger.updateMaxHittingSets(9);
        theory.addEntailedTest(parser.parse("AI_Dept SubClassOf CS_Department"));
        theory.addNonEntailedTest(parser.parse("CS_Library SubClassOf EE_Department"));
        assertEquals(true, debugger.debug());

        long time = System.currentTimeMillis();
        Partitioning<OWLLogicalAxiom> algo = new BruteForce<OWLLogicalAxiom>(theory, new EntropyScoringFunction<OWLLogicalAxiom>());
        Partition<OWLLogicalAxiom> part = algo.generatePartition(debugger.getValidHittingSets());
        time = System.currentTimeMillis() - time;
        logger.info(part.score + " dx:" + part.dx.size() + " dnx:" + part.dnx.size() + " dz:" + part.dz.size());
        logger.info("Partitioning time: " + time + " ms");
    }


}
