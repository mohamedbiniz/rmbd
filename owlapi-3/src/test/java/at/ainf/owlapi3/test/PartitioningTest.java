package at.ainf.owlapi3.test;

import at.ainf.diagnosis.debugger.SimpleQueryDebugger;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.partitioning.BruteForce;
import at.ainf.diagnosis.partitioning.CKK;
import at.ainf.diagnosis.partitioning.GreedySearch;
import at.ainf.diagnosis.partitioning.Partitioning;
import at.ainf.diagnosis.partitioning.scoring.MinScoreQSS;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.storage.Partition;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.base.CalculateDiagnoses;
import at.ainf.owlapi3.model.OWLTheory;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static Logger logger = LoggerFactory.getLogger(PartitioningTest.class.getName());
    //private OWLDebugger debugger = new SimpleDebugger();
    private SimpleQueryDebugger<OWLLogicalAxiom> debugger = new SimpleQueryDebugger<OWLLogicalAxiom>(null);
    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private static final String TEST_IRI = "http://www.semanticweb.org/ontologies/2010/0/ecai.owl#";

    /*@BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlapi3-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }*/


    @Test
    public void testBruteForce() throws OWLException, InconsistentTheoryException, SolverException, NoConflictException {
        OWLTheory theory = new CalculateDiagnoses().getSimpleTheory(new CalculateDiagnoses().getOntologySimple("ontologies/partition.owl"), false);
        debugger.set_Theory(theory);
        debugger.reset();
        assertEquals(false, debugger.start().isEmpty());
        assertEquals(3, debugger.getDiagnoses().size());

        Partitioning<OWLLogicalAxiom> brute = new BruteForce<OWLLogicalAxiom>(theory, new MinScoreQSS<OWLLogicalAxiom>());
        Partition<OWLLogicalAxiom> part = brute.generatePartition(debugger.getDiagnoses());
        logger.debug(part.toString());
    }

    //@Ignore
    @Test
    public void testGreedyForce() throws OWLException, InconsistentTheoryException, SolverException, NoConflictException {
        OWLTheory theory = new CalculateDiagnoses().getSimpleTheory(new CalculateDiagnoses().getOntologySimple("ontologies/partition.owl"), false);
        debugger.set_Theory(theory);
        debugger.reset();
        assertEquals(false, debugger.start().isEmpty());
        assertEquals(3, debugger.getDiagnoses().size());

        Partitioning<OWLLogicalAxiom> greedy = new GreedySearch<OWLLogicalAxiom>(theory, new MinScoreQSS<OWLLogicalAxiom>());
        Partition<OWLLogicalAxiom> partg = greedy.generatePartition(debugger.getDiagnoses());
        logger.debug(partg.toString());
        Partitioning<OWLLogicalAxiom> brute = new BruteForce<OWLLogicalAxiom>(theory, new MinScoreQSS<OWLLogicalAxiom>());
        Partition<OWLLogicalAxiom> partb = brute.generatePartition(debugger.getDiagnoses());
        logger.debug(partb.toString());
        greedy = new CKK<OWLLogicalAxiom>(theory, new MinScoreQSS<OWLLogicalAxiom>());
        Partition<OWLLogicalAxiom> partg1 = greedy.generatePartition(debugger.getDiagnoses());
        logger.debug(partg1.toString());
        //assertEquals(partb, partg);
    }

    @Ignore
    @Test
    public void testGreedyForce2() throws OWLException, InconsistentTheoryException, SolverException, NoConflictException {
        OWLTheory theory = new CalculateDiagnoses().getSimpleTheory(new CalculateDiagnoses().getOntologySimple("ontologies/ecai.simple.owl"), false);
        debugger.set_Theory(theory);
        debugger.reset();
        assertEquals(false, debugger.start().isEmpty());
        assertEquals(4, debugger.getDiagnoses().size());

        int i = 8;
        for (FormulaSet<OWLLogicalAxiom> hs : debugger.getDiagnoses()) {
            hs.setMeasure(new BigDecimal(Integer.toString(i--)).divide(new BigDecimal("100")));
        }

        Partitioning<OWLLogicalAxiom> greedy = new GreedySearch<OWLLogicalAxiom>(theory, new MinScoreQSS<OWLLogicalAxiom>());
        Partition<OWLLogicalAxiom> partg = greedy.generatePartition(debugger.getDiagnoses());
        logger.debug(partg.toString());
        Partitioning<OWLLogicalAxiom> brute = new BruteForce<OWLLogicalAxiom>(theory, new MinScoreQSS<OWLLogicalAxiom>());
        Partition<OWLLogicalAxiom> partb = brute.generatePartition(debugger.getDiagnoses());
        logger.debug(partb.toString());
        greedy = new CKK<OWLLogicalAxiom>(theory, new MinScoreQSS<OWLLogicalAxiom>());
        Partition<OWLLogicalAxiom> partg1 = greedy.generatePartition(debugger.getDiagnoses());
        logger.debug(partg1.toString());
        assertTrue(partb.score == partg.score);
    }

    @Test
    public void testBruteForce2() throws OWLException, InconsistentTheoryException, SolverException, NoConflictException {

        OWLTheory theory = new CalculateDiagnoses().getSimpleTheory(new CalculateDiagnoses().getOntologySimple("ontologies/Univ.owl"), false);
        debugger.set_Theory(theory);
        debugger.reset();
        debugger.updateMaxHittingSets(9);
        assertEquals(false, debugger.start().isEmpty());
        //assertEquals(4, debugger.getDiagnoses().size());

        Partitioning<OWLLogicalAxiom> algo = new BruteForce<OWLLogicalAxiom>(theory, new MinScoreQSS<OWLLogicalAxiom>());
        testOntology(theory, "Brute", algo);

        algo = new CKK<OWLLogicalAxiom>(theory, new MinScoreQSS<OWLLogicalAxiom>());
        testOntology(theory, "CKK", algo);

        algo = new GreedySearch<OWLLogicalAxiom>(theory, new MinScoreQSS<OWLLogicalAxiom>());
        testOntology(theory, "Greedy", algo);
    }

    private void testOntology(OWLTheory theory, String message, Partitioning<OWLLogicalAxiom> algo) throws SolverException, InconsistentTheoryException, OWLException {
        long time = System.currentTimeMillis();
        Partition<OWLLogicalAxiom> part = algo.generatePartition(debugger.getDiagnoses());
        time = System.currentTimeMillis() - time;
        logger.info(message + " " + part.score + " dx:" + part.dx.size() + " dnx:" + part.dnx.size() + " dz:" + part.dz.size());
        logger.info("Partitioning time: " + time + " ms");
    }
}
