package at.ainf.logging;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class TestAspect {

    public static boolean ACTIVE = true;

    @Pointcut("execution(void at.ainf.logging.TestClass.print(..)) && if()")
    public static boolean mainCodeAspectPointcut() {
        return ACTIVE;
    }

    @Before("mainCodeAspectPointcut()")
    public void addMethod() {
        System.out.println("main code aspect");
    }

}