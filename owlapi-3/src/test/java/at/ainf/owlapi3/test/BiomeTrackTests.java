package at.ainf.owlapi3.test;

import at.ainf.owlapi3.module.OtfModuleProvider;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.DLExpressivityChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import java.io.File;
import java.io.InputStream;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 02.04.13
 * Time: 09:37
 * To change this template use File | Settings | File Templates.
 */
public class BiomeTrackTests {

    private static Logger logger = LoggerFactory.getLogger(BiomeTrackTests.class.getName());

    protected OWLOntology loadBigOntology (String filename) {

        File file = new File(System.getenv("bigontosdir") + filename + ".owl");
        OWLOntology ontology = null;
        try {
            ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(file);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return ontology;
    }

    public void classifyOntology(OWLOntology ontology, OWLReasonerFactory factory) {
        ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
        OWLReasoner reasoner = factory.createNonBufferingReasoner(ontology);
        //reasoner.precomputeInferences(InferenceType.values());
        long time = System.currentTimeMillis();
        boolean isConsistent = reasoner.isConsistent();
        int numOfUnsatClasses = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom().size();
        time = System.currentTimeMillis() - time;
        DLExpressivityChecker checker = new DLExpressivityChecker(Collections.singleton(ontology));
        String e = checker.getDescriptionLogicName();
        String name = ontology.getOntologyID().getOntologyIRI().getFragment();
        logger.info("ontology: " + name + ", reasoner: " + factory.getReasonerName() + ", expressivity: " + e
            + ", consistent: " + isConsistent + ", unsat classes: " + numOfUnsatClasses + ", time: " + time);

    }

    @Test
    public void classifiyMouseWithElk() {
        classifyOntology(loadBigOntology("mouse"), new ElkReasonerFactory());

    }

    @Test
    public void classifyMouseHermit() {
        classifyOntology(loadBigOntology("mouse"), new Reasoner.ReasonerFactory());

    }

    @Test
    public void classifyMousePellet() {
        classifyOntology(loadBigOntology("mouse"), new PelletReasonerFactory());

    }

    @Test
    public void classifiyHumanWithElk() {
        classifyOntology(loadBigOntology("human"), new ElkReasonerFactory());

    }

    @Test
    public void classifyHumanHermit() {
        classifyOntology(loadBigOntology("human"), new Reasoner.ReasonerFactory());

    }

    @Test
    public void classifyHumanPellet() {
        classifyOntology(loadBigOntology("human"), new PelletReasonerFactory());

    }

    @Test
    public void classifiyFmaWithElk() {
        classifyOntology(loadBigOntology("oaei2012_FMA_whole_ontology"), new ElkReasonerFactory());

    }

    @Test
    public void classifyFmaHermit() {
        classifyOntology(loadBigOntology("oaei2012_FMA_whole_ontology"), new Reasoner.ReasonerFactory());

    }

    @Test
    public void classifyFmaPellet() {
        classifyOntology(loadBigOntology("oaei2012_FMA_whole_ontology"), new PelletReasonerFactory());

    }

    @Test
    public void classifiyNciWithElk() {
        classifyOntology(loadBigOntology("oaei2012_NCI_whole_ontology"), new ElkReasonerFactory());

    }

    @Test
    public void classifyNciHermit() {
        classifyOntology(loadBigOntology("oaei2012_NCI_whole_ontology"), new Reasoner.ReasonerFactory());

    }

    @Test
    public void classifyNciPellet() {
        classifyOntology(loadBigOntology("oaei2012_NCI_whole_ontology"), new PelletReasonerFactory());

    }

    @Test
    public void classifiySnomedWithElk() {
        classifyOntology(loadBigOntology("oaei2012_SNOMED_extended_overlapping_fma_nci"), new ElkReasonerFactory());

    }

    @Test
    public void classifySnomedHermit() {
        classifyOntology(loadBigOntology("oaei2012_SNOMED_extended_overlapping_fma_nci"), new Reasoner.ReasonerFactory());

    }

    @Test
    public void classifySnomedPellet() {
        classifyOntology(loadBigOntology("oaei2012_SNOMED_extended_overlapping_fma_nci"), new PelletReasonerFactory());

    }

    @Test
    public void expressivityTest() {
        OWLOntology ontology = loadBigOntology("mouse");
        DLExpressivityChecker checker = new DLExpressivityChecker(Collections.singleton(ontology));
        logger.info(checker.getDescriptionLogicName());
    }

}
