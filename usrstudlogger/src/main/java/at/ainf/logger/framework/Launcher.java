package at.ainf.logger.framework;

import org.eclipse.osgi.framework.internal.core.AbstractBundle;
import org.eclipse.osgi.framework.internal.core.BundleFragment;
import org.eclipse.osgi.framework.internal.core.BundleHost;
import org.osgi.framework.Bundle;
import org.protege.osgi.framework.DirectoryWithBundles;
import org.protege.osgi.framework.Parser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 01.09.11
 * Time: 13:30
 * To change this template use File | Settings | File Templates.
 */
public class Launcher {

    public static final String ARG_PROPERTY = "command.line.arg.";
    public static final String LAUNCH_LOCATION_PROPERTY = "org.protege.launch.config";
    public static final String PROTEGE_DIR_PROPERTY = "protege.dir";
    public static String PROTEGE_DIR = System.getProperty(PROTEGE_DIR_PROPERTY);

    private Properties frameworkProperties;
    private List<DirectoryWithBundles> directories;
    private String     factoryClass;
    private Framework  framework;


    public Launcher(File config) throws IOException, ParserConfigurationException, SAXException {
        parseConfig(config);
		locateOSGi();
		File frameworkDir = new File(System.getProperty("java.io.tmpdir"), "ProtegeCache-" + UUID.randomUUID().toString());
		frameworkProperties.setProperty("org.osgi.framework.storage", frameworkDir.getCanonicalPath());
		frameworkDir.deleteOnExit();
    }

    public Framework getFramework() {
        return framework;
    }

    private void parseConfig(File config) throws ParserConfigurationException, SAXException, IOException {
        Parser p = new Parser();
        p.parse(config);
        setSystemProperties(p);
        directories = p.getDirectories();
        frameworkProperties = p.getFrameworkProperties();
    }

    private void locateOSGi() throws IOException {
        BufferedReader factoryReader = new BufferedReader(new InputStreamReader(
             getClass().getClassLoader().getResourceAsStream("META-INF/services/org.osgi.framework.launch.FrameworkFactory")));
        factoryClass = factoryReader.readLine();
        factoryClass = factoryClass.trim();
        factoryReader.close();
    }

    private void setSystemProperties(Parser p) {
		Properties systemProperties = p.getSystemProperties();
        System.setProperty("org.protege.plugin.dir", p.getPluginDirectory());
		for (Map.Entry<Object, Object> entry : systemProperties.entrySet()) {
			System.setProperty((String) entry.getKey(), (String) entry.getValue());
		}
	}

    public void start() throws InstantiationException, IllegalAccessException, ClassNotFoundException, BundleException, IOException {
    	FrameworkFactory factory = (FrameworkFactory) Class.forName(factoryClass).newInstance();

        Map<String, String> table = new Hashtable<String, String>();
        for (Map.Entry<Object,Object> entry : frameworkProperties.entrySet()) {
            table.put((String)entry.getKey(),(String)entry.getValue());
        }
    	framework = factory.newFramework(table);

    	framework.start();
    	BundleContext context = framework.getBundleContext();
    	List<Bundle> bundles = new ArrayList<Bundle>();
    	for (DirectoryWithBundles directory : directories) {
    	    bundles.addAll(installBundles(context, directory.getDirectory(), directory.getBundles()));
    	}
    	startBundles(context, bundles);
    }

    private List<Bundle> installBundles(BundleContext context, String dir, List<String> bundles) throws BundleException {
        File directory;
        if (PROTEGE_DIR != null) {
            directory = new File(PROTEGE_DIR, dir);
        }
        else {
            directory = new File(dir);
        }
    	List<Bundle> core = new ArrayList<Bundle>();
    	for (String bundleName :  bundles) {
    		boolean success = false;
    		try {
    			core.add(context.installBundle(new File(directory, bundleName).toURI().toString()));
    			success = true;
    		}
    		finally {
    			if (!success) {
    				System.out.println("Core Bundle " + bundleName + " failed to install.");
    			}
    		}
    	}
    	return core;
    }

    private void startBundles(BundleContext context, List<Bundle> bundles) throws BundleException {
        List<Bundle> toRes = new LinkedList<Bundle>();
        for (Bundle bundle : bundles)
            if (bundle instanceof BundleHost)
                toRes.add(bundle);
        BundleHost host = (BundleHost)toRes.iterator().next();
        host.getFramework().getPackageAdmin().resolveBundles(toRes);

    	for (Bundle b : bundles) {
    		boolean success = false;
    		try {
                String symName = ((AbstractBundle) b).getBundleDescription().getSymbolicName();
                if (symName.equals("org.aspectj.weaver") ||
                      symName.equals("org.aspectj.runtime") || symName.equals("org.eclipse.equinox.weaving.hook")) {
                    // System.out.println("Not Started " + symName);
                } else {
                    b.start();
                }
                    success = true;
    		}
    		finally {
    			if (!success) {
    				System.out.println("Core Bundle " + b.getBundleId() + " failed to start.");
    			}
    		}
    	}
    }


	/**
	 * @param args
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws BundleException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
    public static void main(String[] args) throws Exception {
        setArguments(args);
        String config = System.getProperty(LAUNCH_LOCATION_PROPERTY, "config.xml");
        File configFile = new File("logging/config_logging.xml");
        /*if (PROTEGE_DIR != null) {
            configFile = new File(PROTEGE_DIR + "/logging", config);
        }
        else {
            configFile = new File(config);
        }*/
        new Launcher(configFile).start();
    }

    public static void setArguments(String... args) {
		if (args != null) {
			int counter = 0;
			for (String arg : args) {
				System.setProperty(ARG_PROPERTY + (counter++), arg);
			}
		}
    }

}
