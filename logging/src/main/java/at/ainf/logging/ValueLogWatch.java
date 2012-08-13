package at.ainf.logging;

import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;

/**
* Created with IntelliJ IDEA.
* User: pfleiss
* Date: 13.08.12
* Time: 11:24
* To change this template use File | Settings | File Templates.
*/
public class ValueLogWatch extends Slf4JStopWatch {

    public ValueLogWatch(long elapsedTime, String tag, String message, Logger logger) {
        super(0,elapsedTime,tag,message,logger,INFO_LEVEL,WARN_LEVEL);
    }

    public void logValue() {
        String retVal = this.toString();
        log(retVal, null);
    }
}
