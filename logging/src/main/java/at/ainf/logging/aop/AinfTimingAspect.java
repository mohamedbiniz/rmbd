package at.ainf.logging.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.perf4j.aop.Profiled;
import org.perf4j.slf4j.aop.TimingAspect;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 14.08.12
 * Time: 11:43
 * Wraps around {@link org.perf4j.slf4j.aop.TimingAspect}.
 */
@Aspect
public class AinfTimingAspect {

    @Around(value = "execution(* *(..)) && @annotation(profiled)", argNames = "pjp,profiled")
    public Object doPerfLogging(ProceedingJoinPoint pjp, Profiled profiled) throws Throwable {
        return new TimingAspect().doPerfLogging(pjp,profiled);

    }

}
