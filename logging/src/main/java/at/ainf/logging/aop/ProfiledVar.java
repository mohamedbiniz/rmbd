package at.ainf.logging.aop;

import org.perf4j.StopWatch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 14.08.12
 * Time: 09:44
 * To change this template use File | Settings | File Templates.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface ProfiledVar {

    public static final String DEFAULT_VARNAME_NAME = "@@USE_METHOD_NAME";

    String varname() default DEFAULT_VARNAME_NAME;

    String message() default "";

    String logger() default StopWatch.DEFAULT_LOGGER_NAME;

    String level() default "INFO";

    boolean el() default true;

    boolean logFailuresSeparately() default false;

}
