package at.ainf.owlcontroller;

import at.ainf.owlcontroller.listeners.OWLControllerConflictSetListener;
import at.ainf.owlcontroller.listeners.OWLControllerHittingSetListener;
import at.ainf.theory.storage.AxiomSet;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 10.01.12
 * Time: 10:16
 * To change this template use File | Settings | File Templates.
 */
public class OWLControllerTest {

    private static Logger logger = Logger.getLogger(OWLControllerTest.class.getName());

    @BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlcontroller-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }

}
