package at.ainf.owlapi3.test;

import at.ainf.diagnosis.debugger.SimpleQueryDebugger;
import at.ainf.diagnosis.partitioning.*;
import at.ainf.diagnosis.partitioning.scoring.MinScoreQSS;
import at.ainf.owlapi3.utils.Utils;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.storage.Partition;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.math.BigDecimal;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 04.05.11
 * Time: 14:26
 * To change this template use File | Settings | File Templates.
 */
public class PartitioningTest {

    private static Logger logger = Logger.getLogger(PartitioningTest.class.getName());
    //private OWLDebugger debugger = new SimpleDebugger();
    private SimpleQueryDebugger<OWLLogicalAxiom> debugger = new SimpleQueryDebugger<OWLLogicalAxiom>(null);
    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private static final String TEST_IRI = "http://www.semanticweb.org/ontologies/2010/0/ecai.owl#";

    @BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlapi3-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }


    @Test
    public void testBruteForce() throws OWLException, InconsistentTheoryException, SolverException {
        OWLTheory theory = Utils.loadTheory(manager, "ontologies/partition.owl");
        debugger.set_Theory(theory);
        debugger.reset();
        assertEquals(true, debugger.debug());
        assertEquals(3, debugger.getValidHittingSets().size());

        Partitioning<OWLLogicalAxiom> brute = new BruteForce<OWLLogicalAxiom>(theory, new MinScoreQSS<OWLLogicalAxiom>());
        Partition<OWLLogicalAxiom> part = brute.generatePartition(debugger.getValidHittingSets());
        logger.debug(part);
    }

    //@Ignore
    @Test
    public void testGreedyForce() throws OWLException, InconsistentTheoryException, SolverException {
        OWLTheory theory = Utils.loadTheory(manager, "ontologies/partition.owl");
        debugger.set_Theory(theory);
        debugger.reset();
        assertEquals(true, debugger.debug());
        assertEquals(3, debugger.getValidHittingSets().size());

        Partitioning<OWLLogicalAxiom> greedy = new GreedySearch<OWLLogicalAxiom>(theory, new MinScoreQSS<OWLLogicalAxiom>());
        Partition<OWLLogicalAxiom> partg = greedy.generatePartition(debugger.getValidHittingSets());
        logger.debug(partg);
        Partitioning<OWLLogicalAxiom> brute = new BruteForce<OWLLogicalAxiom>(theory, new MinScoreQSS<OWLLogicalAxiom>());
        Partition<OWLLogicalAxiom> partb = brute.generatePartition(debugger.getValidHittingSets());
        logger.debug(partb);
        greedy = new CKK<OWLLogicalAxiom>(theory, new MinScoreQSS<OWLLogicalAxiom>());
        Partition<OWLLogicalAxiom> partg1 = greedy.generatePartition(debugger.getValidHittingSets());
        logger.debug(partg1);
        //assertEquals(partb, partg);
    }

    @Ignore
    @Test
    public void testGreedyForce2() throws OWLException, InconsistentTheoryException, SolverException {
        OWLTheory theory = Utils.loadTheory(manager, "ontologies/ecai.simple.owl");
        debugger.set_Theory(theory);
        debugger.reset();
        assertEquals(true, debugger.debug());
        assertEquals(4, debugger.getValidHittingSets().size());

        int i = 8;
        for (AxiomSet<OWLLogicalAxiom> hs : debugger.getValidHittingSets()) {
            hs.setMeasure(new BigDecimal(Integer.toString(i--)).divide(new BigDecimal("100")));
        }

        Partitioning<OWLLogicalAxiom> greedy = new GreedySearch<OWLLogicalAxiom>(theory, new MinScoreQSS<OWLLogicalAxiom>());
        Partition<OWLLogicalAxiom> partg = greedy.generatePartition(debugger.getValidHittingSets());
        logger.debug(partg);
        Partitioning<OWLLogicalAxiom> brute = new BruteForce<OWLLogicalAxiom>(theory, new MinScoreQSS<OWLLogicalAxiom>());
        Partition<OWLLogicalAxiom> partb = brute.generatePartition(debugger.getValidHittingSets());
        logger.debug(partb);
        greedy = new CKK<OWLLogicalAxiom>(theory, new MinScoreQSS<OWLLogicalAxiom>());
        Partition<OWLLogicalAxiom> partg1 = greedy.generatePartition(debugger.getValidHittingSets());
        logger.debug(partg1);
        assertTrue(partb.score == partg.score);
    }

    @Test
    public void testBruteForce2() throws OWLException, InconsistentTheoryException, SolverException {

        OWLTheory theory = Utils.loadTheory(manager, "ontologies/Univ.owl");
        debugger.set_Theory(theory);
        debugger.reset();
        debugger.updateMaxHittingSets(9);
        assertEquals(true, debugger.debug());
        //assertEquals(4, debugger.getHittingSets().size());

        Partitioning<OWLLogicalAxiom> algo = new BruteForce<OWLLogicalAxiom>(theory, new MinScoreQSS<OWLLogicalAxiom>());
        testOntology(theory, "Brute", algo);

        algo = new CKK<OWLLogicalAxiom>(theory, new MinScoreQSS<OWLLogicalAxiom>());
        testOntology(theory, "CKK", algo);

        algo = new GreedySearch<OWLLogicalAxiom>(theory, new MinScoreQSS<OWLLogicalAxiom>());
        testOntology(theory, "Greedy", algo);
    }

    private void testOntology(OWLTheory theory, String message, Partitioning<OWLLogicalAxiom> algo) throws SolverException, InconsistentTheoryException, OWLException {
        long time = System.currentTimeMillis();
        Partition<OWLLogicalAxiom> part = algo.generatePartition(debugger.getHittingSets());
        time = System.currentTimeMillis() - time;
        logger.info(message + " " + part.score + " dx:" + part.dx.size() + " dnx:" + part.dnx.size() + " dz:" + part.dz.size());
        logger.info("Partitioning time: " + time + " ms");
    }
}
