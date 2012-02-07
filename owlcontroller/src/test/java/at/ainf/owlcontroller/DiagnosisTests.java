package at.ainf.owlcontroller;

import at.ainf.diagnosis.debugger.SimpleQueryDebugger;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.owlapi3.model.OWLTheory;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.util.*;

import static _dev.TimeLog.printStatsAndClear;
import static junit.framework.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 14.06.2010
 * Time: 13:02:51
 * To change this template use File | Settings | File Templates.
 */
public class DiagnosisTests {

    private static Logger logger = Logger.getLogger(DiagnosisTests.class.getName());
    private long time;

    @BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlcontroller-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }

    @Test
    public void testUnsat() throws SolverException, URISyntaxException, OWLException, InconsistentTheoryException {

        List<String> exclude = new LinkedList<String>();

        // used in the paper
        //exclude.add("CHEM-A.owl");
        //exclude.add("koala.owl");
        //exclude.add("buggy-sweet-jpl.owl");
        //exclude.add("miniTambis.owl");
        //exclude.add("Univ.owl");
        //exclude.add("Economy-SDA.owl");
        //exclude.add("Transportation-SDA.owl");

        exclude.add("madcow.owl");
        exclude.add("bad-food.owl");
        exclude.add("buggyPolicy.owl");
        exclude.add("DICE-A.owl");
        exclude.add("tambis.owl");
        exclude.add("Terrorism.owl");
        exclude.add("University.owl");


        // bad
        exclude.add("FMA-1.4.0-allinone.owl");
        exclude.add("fmaOwlDlComponent_1_4_0.owl");
        exclude.add("fma_obo.owl");
        exclude.add("OBO_REL.owl");
        exclude.add("pto.owl");
        exclude.add("Thesaurus.08.08d.owl");

        // good
        //exclude.add("cton.owl");
        //exclude.add("gene_ontology_edit.obo.owl");
        //exclude.add("opengalen-no-propchains.owl");

        // unused
        exclude.add("GeoSkills.owl");


        String testDir = ClassLoader.getSystemResource("sat").getPath();
        //String testDir =   "C:\\Daten\\OpBO";
        LinkedList<File> files = new LinkedList<File>();
        collectAllFiles(new File(testDir), files);
        for (File file : files)
            try {
                if (!exclude.contains(file.getName()) && (file.getName().endsWith(".owl")
                        || file.getName().endsWith("._owl_") || file.getName().endsWith(".owl2"))) {
                    test(file, 9);
                    printStatsAndClear("Leading diagnoses " + file.getName());
                    test(file, -1);
                    printStatsAndClear("All diagnoses " + file.getName());
                }
            } catch (Exception e) {
                //file.renameTo(new File(file + ".checked"));
                logger.error(e);
            }

    }

    private void collectAllFiles(File testDir, List<File> files) {
        if (testDir.isFile()) {
            files.add(testDir);
            return;
        }
        for (File file : testDir.listFiles()) {
            collectAllFiles(file, files);
        }
    }

    private boolean test(File file, int number) throws URISyntaxException, SolverException, OWLException, InconsistentTheoryException {
        if (new File(file + ".checked").exists())
            return false;

        //SimpleDebugger debugger = new SimpleDebugger();
        logger.info("Processing " + file);
        OWLTheory th = createTheory(file, OWLManager.createOWLOntologyManager());
        //debugger.setTheory(th);
        SimpleQueryDebugger<OWLLogicalAxiom> debugger = new SimpleQueryDebugger<OWLLogicalAxiom>(th);
        OWLOntology on = th.getOriginalOntology();
        logger.info(th.getActiveFormulas().size() + "/" + th.getBackgroundFormulas().size() + " axioms, "
                + on.getClassesInSignature().size() + "/"
                + (on.getDataPropertiesInSignature().size() + on.getObjectPropertiesInSignature().size()) + "/"
                + on.getIndividualsInSignature().size());


        start();
        assertTrue(th.verifyRequirements());
        stop("Classified in ");

        //debugger.setMaxHittingSets(number);
        debugger.updateMaxHittingSets(number);
        boolean ret = false;
        start();
        if (debugger.debug()) {
            logger.info(file.getName() + " - with " + debugger.getConflictSets().size() + " minimal conflict(s) and " + debugger.getValidHittingSets().size() + " hitting set(s)");
            logResult(debugger.getValidHittingSets(), "Hitting set cardinalities: ");
            logResult(debugger.getConflictSets(), "Conflict set cardinalities: ");
            ret = true;
        } else {
            //boolean success = file.renameTo(new File(file + ".checked"));
            //if (!success) {
            //fail();
            //}

        }
        stop("Diagnosis finished in ");
        return ret;
    }

    private void logResult(Collection<? extends Set<OWLLogicalAxiom>> sets, String message) throws OWLException {
        for (Collection<OWLLogicalAxiom> hs : sets)
            message += hs.size() + "; ";
        logger.info(message);
    }

    private OWLTheory createTheory(File file, OWLOntologyManager manager) throws URISyntaxException,
            OWLOntologyCreationException, SolverException, InconsistentTheoryException {

        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();

        Set<OWLLogicalAxiom> background = new HashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            background.addAll(ontology.getClassAssertionAxioms(ind));
            background.addAll(ontology.getDataPropertyAssertionAxioms(ind));
        }

        OWLTheory theory = new OWLTheory(reasonerFactory, ontology, background);

        return theory;
    }

    private void start() {
        this.time = System.currentTimeMillis();
    }

    private void stop(String message) {
        logger.info(message + (System.currentTimeMillis() - this.time) + " ms");
    }

}
