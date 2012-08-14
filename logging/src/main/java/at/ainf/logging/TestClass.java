package at.ainf.logging;

import org.perf4j.aop.Profiled;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 20.06.12
 * Time: 11:44
 * To change this template use File | Settings | File Templates.
 */
public class TestClass {

    @ProfiledVar(varname="diagnoses")
    @Profiled(tag="time_diagnoses")
    public int print() {
        int retv = 9;
        System.out.println("testclass Method ");
        return retv;
    }

}
