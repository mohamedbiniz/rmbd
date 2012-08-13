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

    @Profiled(tag="uvar")
    public void print() {
        long uvar = 10;
        System.out.println("testclass Method ");
    }

}
