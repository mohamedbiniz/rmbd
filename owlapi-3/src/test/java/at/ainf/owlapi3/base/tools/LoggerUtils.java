package at.ainf.owlapi3.base.tools;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import org.slf4j.LoggerFactory;


/**
 * Created with IntelliJ IDEA.
 * User: kostya
 * Date: 12.04.13
 * Time: 09:48
 * To change this template use File | Settings | File Templates.
 */
public class LoggerUtils {

    private static boolean added = false;

    public static void addConsoleLogger(){
        if (added) return;
        added = true;
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        LoggerContext loggerContext = rootLogger.getLoggerContext();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%-5level [%thread]: %message%n");
        encoder.start();

        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<ILoggingEvent>();
        appender.setContext(loggerContext);
        appender.setEncoder(encoder);
        appender.start();

        rootLogger.addAppender(appender);

    }
}
