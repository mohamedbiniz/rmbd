package at.ainf.owlcontroller;

import at.ainf.owlcontroller.oaei11align.RdfMatchingFileParser;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.05.12
 * Time: 10:02
 * To change this template use File | Settings | File Templates.
 */
public class RDFUtils {
    public static Map<OWLLogicalAxiom,Double> readRdfMapping(String path, String name) {
        RdfMatchingFileParser handler = new RdfMatchingFileParser();
        SAXParserFactory factory = SAXParserFactory.newInstance();

        try {
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse( ClassLoader.getSystemResource(path+"/"+name + ".rdf").getPath(), handler );
        } catch (ParserConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SAXException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return handler.getMappings();
    }
}
