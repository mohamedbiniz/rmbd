package at.ainf.protegeview;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 22.12.11
 * Time: 15:28
 * To change this template use File | Settings | File Templates.
 */
public class Activator implements BundleActivator {

    private static Logger logger = LoggerFactory.getLogger(Activator.class.getName());

    public void start(BundleContext bundleContext) throws Exception {
        logger.info("bundle started");
    }

    public void stop(BundleContext bundleContext) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}
