package at.ainf.logging.aop;

import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;

/**
* Created with IntelliJ IDEA.
* User: pfleiss
* Date: 13.08.12
* Time: 11:24
* To change this template use File | Settings | File Templates.
*/
public class ProfVarLogWatch extends Slf4JStopWatch {

    public ProfVarLogWatch(long elapsedTime, String tag, String message, Logger logger) {
        super(System.currentTimeMillis(),elapsedTime,tag,message,logger,INFO_LEVEL,WARN_LEVEL);
    }

    public void logValue() {
        String retVal = this.toString();
        log(retVal, null);
    }
}
