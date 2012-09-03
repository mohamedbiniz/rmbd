package at.ainf.protegeview;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.io.InputStream;
import java.util.logging.LogManager;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 22.12.11
 * Time: 15:28
 * To change this template use File | Settings | File Templates.
 */
public class Activator implements BundleActivator {

    static {
        System.setProperty("org.slf4j.simplelogger.defaultlog","warn");
    }

    private static Logger logger = Logger.getLogger(Activator.class.getName());

    public void configureLogging (BundleContext bundleContext) throws Exception {
        Bundle bundle = bundleContext.getBundle();
        InputStream inputStream = bundle.getEntry("/logging.properties").openStream();

        LogManager.getLogManager().readConfiguration(inputStream);
    }

    public void start(BundleContext bundleContext) throws Exception {
        logger.debug("bundle started");

    }

    public void stop(BundleContext bundleContext) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}
