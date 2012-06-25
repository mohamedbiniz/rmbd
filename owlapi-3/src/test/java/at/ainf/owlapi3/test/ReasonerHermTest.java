package at.ainf.owlapi3.test;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.*;

import static _dev.TimeLog.start;
import static _dev.TimeLog.stop;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 15.03.12
 * Time: 15:30
 * To change this template use File | Settings | File Templates.
 */
public class ReasonerHermTest {

        private static Logger logger = Logger.getLogger(ReasonerHermTest.class.getName());



        @BeforeClass
        public static void setUp() {
            String conf = ClassLoader.getSystemResource("owlapi3-log4j.properties").getFile();
            PropertyConfigurator.configure(conf);
        }

    @Test
    public void testReasoner() throws InconsistentTheoryException, SolverException, URISyntaxException, OWLException {

        InputStream st = ClassLoader.getSystemResourceAsStream("ontologies/ecai.simple.owl");
        OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(st);

        Set<OWLLogicalAxiom> bax = new HashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        OWLOntologyManager m = ontology.getOWLOntologyManager();
        OWLOntology dontology = m.createOntology();


        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(dontology);
        dontology.getOWLOntologyManager().addAxioms(dontology, bax);
        reasoner.flush();



    }




}
