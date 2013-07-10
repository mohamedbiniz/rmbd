package at.ainf.protegeview;

import at.ainf.protegeview.model.configuration.SearchConfiguration;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 10.09.12
 * Time: 13:33
 * To change this template use File | Settings | File Templates.
 */
public class ConfigFileTest {

    private static Logger logger = LoggerFactory.getLogger(ConfigFileTest.class.getName());

    @Test @Ignore
    public void serializationTest() throws FileNotFoundException  {
        SearchConfiguration conf = new SearchConfiguration();

        logger.info(conf.toString());

        conf.qss = SearchConfiguration.QSS.DYNAMIC;

        logger.info(conf.toString());

        XMLEncoder encoder = new XMLEncoder (new BufferedOutputStream( new FileOutputStream("c:/daten/testconf.xml")));
        encoder.writeObject(conf);
        encoder.close();

        XMLDecoder decoder = new XMLDecoder(new FileInputStream("c:/daten/testconf.xml"));
        SearchConfiguration conf2 = (SearchConfiguration) decoder.readObject();
        decoder.close();

        logger.info(conf2.toString());



    }

}
