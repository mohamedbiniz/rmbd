package at.ainf.logger;


/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 06.10.11
 * Time: 10:31
 * To change this template use File | Settings | File Templates.
 */


import org.apache.log4j.Logger;
import org.aspectj.lang.annotation.*;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;

@Aspect
public class TestMore {

    /*private static Logger logger = Logger.getLogger(TestMore.class.getName());

    void logMsg(String message) {
        logger.info(message);
    }*/


    /*@Pointcut(
        "execution(void org.protege.editor.core.ProtegeApplication.start(..))"
    )
    void protegeAppStart() {}

    @Before("protegeAppStart()")
    public void logProtegeAppStart() {
        logMsg("start logging mouse events ");

        Toolkit.getDefaultToolkit().addAWTEventListener(
                new AWTEventListener() {
                    public void eventDispatched(AWTEvent e)
                    {
                        if (e.getID() == MouseEvent.MOUSE_CLICKED) {
                            logMsg("User clicked button " + ((MouseEvent)e).getButton() + " click number   "  +
                                      ((MouseEvent)e).getClickCount());
                        }
                    }
                },
                AWTEvent.MOUSE_EVENT_MASK);
    }*/





}
