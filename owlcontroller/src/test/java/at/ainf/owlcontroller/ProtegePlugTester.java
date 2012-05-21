package at.ainf.owlcontroller;

import at.ainf.diagnosis.debugger.SimpleQueryDebugger;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 21.05.12
 * Time: 12:14
 * To change this template use File | Settings | File Templates.
 */
public class ProtegePlugTester {

    private static Logger logger = Logger.getLogger(ProtegePlugTester.class.getName());

    @BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlcontroller-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }

    @Test
    public void testWriteOptions() {
        ConfigFileManager.writeConfiguration(ConfigFileManager.getDefaultConfig());
        SearchConfiguration config = ConfigFileManager.readConfiguration();
        System.out.println(config);
    }

}
