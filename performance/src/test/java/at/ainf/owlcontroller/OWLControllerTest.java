package at.ainf.owlcontroller;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;

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
