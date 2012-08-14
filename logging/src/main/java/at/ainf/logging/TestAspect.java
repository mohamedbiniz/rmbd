package at.ainf.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.LoggerFactory;

@Aspect
public class TestAspect {

    //public static boolean ACTIVE = true;

    /*@Pointcut("execution(void at.ainf.logging.TestClass.print(..))")
    public static void mainCodeAspectPointcut() {
        //return ACTIVE;
    }

    @Before("mainCodeAspectPointcut()")
    public void addMethod() {
        new ProfVarLogWatch(9,"diagnosis","number of diagnoses",
                LoggerFactory.getLogger(ProfVarLogWatch.DEFAULT_LOGGER_NAME)).logValue();
    }*/

    @Around(value = "execution(* *(..)) && @annotation(profiledVar)", argNames = "pjp,profiledVar")
    public Object doPerfLogging(ProceedingJoinPoint pjp, ProfiledVar profiledVar) throws Throwable {

        Object ret = pjp.proceed();

        new ProfVarLogWatch((Integer)ret,profiledVar.varname(),"number of diagnoses",
                LoggerFactory.getLogger(ProfVarLogWatch.DEFAULT_LOGGER_NAME)).logValue();

        return ret;
          }

}