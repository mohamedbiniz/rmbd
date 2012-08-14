package at.ainf.logging.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.LoggerFactory;

@Aspect
public class AinfVarsAspect {



    @Around(value = "execution(* *(..)) && @annotation(profiledVar)", argNames = "pjp,profiledVar")
    public Object doPerfLogging(ProceedingJoinPoint pjp, ProfiledVar profiledVar) throws Throwable {

        Object ret = pjp.proceed();

        new ProfVarLogWatch((Integer)ret,profiledVar.varname(),"number of diagnoses",
                LoggerFactory.getLogger(ProfVarLogWatch.DEFAULT_LOGGER_NAME)).logValue();

        return ret;
          }

}