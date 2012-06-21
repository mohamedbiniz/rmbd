package at.ainf.logging;

import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 20.06.12
 * Time: 12:24
 * To change this template use File | Settings | File Templates.
 */
@org.aspectj.lang.annotation.Aspect
public class Aspect {

    public static boolean ACTIVE = true;

    @Pointcut("execution(void at.ainf.logging.TestClass.print(..)) && if()")
    public static boolean testCodeAspectPointcut() {
        return ACTIVE;
    }

    @Before("testCodeAspectPointcut()")
    public void addMethod() {
        System.out.println("test code aspect");
    }
}
