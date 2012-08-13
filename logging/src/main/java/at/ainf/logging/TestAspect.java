package at.ainf.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.perf4j.aop.Profiled;
import org.slf4j.LoggerFactory;

@Aspect
public class TestAspect {

    public static boolean ACTIVE = true;

    @Pointcut("execution(void at.ainf.logging.TestClass.print(..))")
    public static void mainCodeAspectPointcut() {
        //return ACTIVE;
    }

    @Before("mainCodeAspectPointcut()")
    public void addMethod() {
        new ValueLogWatch(9,"diagnosis","number of diagnoses",
                LoggerFactory.getLogger(ValueLogWatch.DEFAULT_LOGGER_NAME)).logValue();
    }

    @Before(value = "execution(* *(..)) && @annotation(profiled)", argNames = "pjp,profiled")
    public void doPerfLogging(JoinPoint pjp, Profiled profiled) throws Throwable {

        new ValueLogWatch(10,profiled.tag(),"number of diagnoses",
                LoggerFactory.getLogger(ValueLogWatch.DEFAULT_LOGGER_NAME)).logValue();
        //return pjp.proceed();
          }

}