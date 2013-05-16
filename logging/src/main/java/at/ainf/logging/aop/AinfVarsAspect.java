package at.ainf.logging.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.LoggerFactory;

import java.util.Collection;

@Aspect
public class AinfVarsAspect {



    @Around(value = "execution(* *(..)) && @annotation(profiledVar)", argNames = "pjp,profiledVar")
    public Object doVarLogging(ProceedingJoinPoint pjp, ProfiledVar profiledVar) throws Throwable {

        Object ret = pjp.proceed();

        String message = null; // e.g."number of calls"
        long value = 0;

        if (ret==null) {
            message = "method returns null, is void method?";
            value = -1;
        }
        else if (profiledVar.isCollection()) {
            message = "field named time gives  size of collection ";
            value = ((Collection<?>) ret).size();
        }
        else {
            value = Long.parseLong(ret.toString());
        }

        new ProfVarLogWatch(value ,profiledVar.tag(),message,
                LoggerFactory.getLogger(ProfVarLogWatch.DEFAULT_LOGGER_NAME)).logValue();

        return ret;
    }

}