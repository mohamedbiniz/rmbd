package at.ainf.protegeview;

import at.ainf.protegeview.debugmanager.LogToolsImp;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 22.12.11
 * Time: 15:28
 * To change this template use File | Settings | File Templates.
 */
public class Activator implements BundleActivator {

    public void start(BundleContext bundleContext) throws Exception {
        createLogTool();
    }

    public Object createLogTool() {
        return new LogToolsImp();
    }

    public void stop(BundleContext bundleContext) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}
