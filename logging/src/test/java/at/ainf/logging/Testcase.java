package at.ainf.logging;

import org.junit.Test;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 20.06.12
 * Time: 11:46
 * To change this template use File | Settings | File Templates.
 */
public class Testcase {

    @Test
    public void testAspects() {
        TestClass test = new TestClass();
        test.print();
    }

}
