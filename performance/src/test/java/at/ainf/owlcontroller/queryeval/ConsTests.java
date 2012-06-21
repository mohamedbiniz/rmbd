package at.ainf.owlcontroller.queryeval;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 02.04.12
 * Time: 14:53
 * To change this template use File | Settings | File Templates.
 */
public class ConsTests {

    private static Logger logger = Logger.getLogger(ConsTests.class.getName());

    @BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlcontroller-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }

}
