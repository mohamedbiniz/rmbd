package at.ainf.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.perf4j.aop.AbstractTimingAspect;
import org.perf4j.aop.Profiled;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.perf4j.slf4j.aop.TimingAspect;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 14.08.12
 * Time: 11:43
 * To change this template use File | Settings | File Templates.
 */
@Aspect
public class AinfSL4JTimingAspect {

    @Around(value = "execution(* *(..)) && @annotation(profiled)", argNames = "pjp,profiled")
    public Object doPerfLogging(ProceedingJoinPoint pjp, Profiled profiled) throws Throwable {
        System.out.println("test");
        try {
        TimingAspect test = new TimingAspect();
            return test.doPerfLogging(pjp,profiled);
        }
        catch (Throwable e) {
            System.out.println(e.toString());
        }
        return pjp.proceed();    }

}
