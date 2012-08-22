package at.ainf.logging;

import at.ainf.logging.aop.ProfiledVar;
import org.perf4j.aop.Profiled;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 20.08.12
 * Time: 10:25
 * To change this template use File | Settings | File Templates.
 */
public class SimulatedCalculationTest {

    public void doSimulation() {

        Random rnd = new Random(2);
        List<Integer> list = Arrays.asList(2,3,5,8,7,6,4);

        for (Integer val : list ) {
            simpleFunc(val,50+rnd.nextInt(50));
        }

    }

    @Profiled(tag = "time_simpletestfunc")
    @ProfiledVar(tag = "simpletestfunc")
    private Integer simpleFunc(Integer next, int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return next;
    }


}
