package at.ainf.logging.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.LoggerFactory;

@Aspect
public class AinfVarsAspect {



    @Around(value = "execution(* *(..)) && @annotation(profiledVar)", argNames = "pjp,profiledVar")
    public Object doVarLogging(ProceedingJoinPoint pjp, ProfiledVar profiledVar) throws Throwable {
        Object ret = pjp.proceed();
        String message = null; // e.g."number of calls"
        if (ret==null) {
            message = "method returns null, is void method?";
            ret = -1;
        }
        long r = Long.parseLong(ret.toString());
        new ProfVarLogWatch(r ,profiledVar.varname(),message,
                LoggerFactory.getLogger(ProfVarLogWatch.DEFAULT_LOGGER_NAME)).logValue();

        return ret;
          }

}