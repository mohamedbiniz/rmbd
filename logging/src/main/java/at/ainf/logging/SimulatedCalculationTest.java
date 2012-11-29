package at.ainf.logging;

import at.ainf.logging.aop.ProfVarLogWatch;
import at.ainf.logging.aop.ProfiledVar;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.perf4j.aop.Profiled;
import org.slf4j.LoggerFactory;

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
            simpleFunc(val,50+val*10);
        }


    }

    @ProfiledVar(tag = "simpletestfunc")
    @Profiled(tag = "time_simpletestfunc")
    private Integer simpleFunc(Integer next, int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return next;
    }


}
