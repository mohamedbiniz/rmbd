package at.ainf.owlcontroller;

import at.ainf.diagnosis.debugger.SimpleQueryDebugger;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
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
        //exclude.add("koala2.owl");
        //exclude.add("buggy-sweet-jpl.owl");
        //exclude.add("miniTambis.owl");
        //exclude.add("Univ2.owl");
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

        // matched
        exclude.add("CMT.owl");
        exclude.add("coma_CMT-CONFTOOL.owl");
        exclude.add("coma_CMT-EKAW.owl"); // bad
        exclude.add("coma_CONFTOOL-EKAW.owl");
        exclude.add("coma_CONFTOOL-SIGKDD.owl");
        exclude.add("coma_CRS-CONFTOOL.owl");
        exclude.add("coma_CRS-EKAW.owl");
        exclude.add("coma_CRS-PCS.owl");
        exclude.add("coma_PCS-CONFTOOL.owl");
        exclude.add("coma_PCS-EKAW.owl"); // bad
        exclude.add("CONFTOOL.owl");
        exclude.add("CRS.owl");
        exclude.add("EKAW.owl");
        exclude.add("falcon_CONFTOOL-EKAW.owl"); // bad
        exclude.add("falcon_CRS-CONFTOOL.owl");
        exclude.add("falcon_CRS-SIGKDD.owl");
        exclude.add("hmatch_CMT-CONFTOOL.owl");
        exclude.add("hmatch_CMT-CRS.owl");
        exclude.add("hmatch_CONFTOOL-CMT.owl");
        exclude.add("hmatch_CONFTOOL-CRS.owl");
        exclude.add("hmatch_CONFTOOL-EKAW.owl"); // bad
        exclude.add("hmatch_CONFTOOL-SIGKDD.owl");
        exclude.add("hmatch_CRS-CMT.owl");
        exclude.add("hmatch_CRS-EKAW.owl"); // bad
        exclude.add("hmatch_CRS-PCS.owl");
        exclude.add("hmatch_CRS-SIGKDD.owl");
        exclude.add("hmatch_EKAW-CMT.owl"); // bad
        exclude.add("hmatch_EKAW-CONFTOOL.owl");
        exclude.add("hmatch_EKAW-CRS.owl"); // bad
        exclude.add("hmatch_EKAW-PCS.owl"); // bad
        exclude.add("hmatch_EKAW-SIGKDD.owl"); // bad
        exclude.add("hmatch_PCS-CONFTOOL.owl");
        exclude.add("hmatch_PCS-CRS.owl");
        exclude.add("hmatch_PCS-EKAW.owl");
        exclude.add("hmatch_SIGKDD-EKAW.owl");
        // exclude.add("owlctxmatch_CMT-CONFTOOL.owl"); // bad
        exclude.add("owlctxmatch_CONFTOOL-EKAW.owl"); // bad
        exclude.add("owlctxmatch_CRS-CMT.owl");
        exclude.add("owlctxmatch_SIGKDD-EKAW.owl"); // bad
        exclude.add("PCS.owl");
        exclude.add("SIGKDD.owl");




        //String testDir = ClassLoader.getSystemResource("alignment/ontologies").getPath();
        String testDir = ClassLoader.getSystemResource("oaei11conference/matchings/incoherent").getPath();
        //String testDir =   "C:\\Daten\\OpBO";
        LinkedList<File> files = new LinkedList<File>();
        collectAllFiles(new File(testDir), files);
        for (File file : files)
            try {
                if (!exclude.contains(file.getName()) && (file.getName().endsWith(".owl") || file.getName().endsWith("._owl_")
                        || file.getName().endsWith(".owl2")|| file.getName().endsWith(".rdf")) ) {
                    logger.info("started " + file.getName());
                    test(file, 9);
                    printStatsAndClear("Leading diagnoses " + file.getName());
                    test(file, -1);
                    printStatsAndClear("All diagnoses " + file.getName());
                    logger.info("stopped " + file.getName());
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
        OWLOntology ontology = null;
        if (file.getName().endsWith(".rdf")) {
            String pathOntologies = "oaei11conference/ontology";
            String o1 = file.getName().split("-")[1];
            String o2 = file.getName().split("-")[2];
            o2 = o2.substring(0,o2.length()-4);
            String pathMapping = "oaei11conference/matchings/" + file.getParentFile().getName();
            String mappingName = file.getName();
            ontology = RDFUtils.createOntologyWithMappings(pathOntologies,o1,o2,pathMapping,mappingName);
        }
        else {
            ontology = CreationUtils.createOwlOntology(file);

        }
        OWLTheory th = createTheory(ontology);
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

    private OWLTheory createTheory(OWLOntology ontology) throws URISyntaxException,
            OWLOntologyCreationException, SolverException, InconsistentTheoryException {


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
