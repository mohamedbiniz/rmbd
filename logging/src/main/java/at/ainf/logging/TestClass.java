package at.ainf.logging;

import at.ainf.logging.aop.ProfiledVar;
import org.perf4j.aop.Profiled;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 20.06.12
 * Time: 11:44
 * To change this template use File | Settings | File Templates.
 */
public class TestClass {

    private int var = 4711;

    @Profiled(tag = "time_diagnoses")
    @ProfiledVar(tag = "diagnoses")
    public int print() {
        int retv = 9;
        System.out.println("testclass Method ");
        return retv;
    }

}
