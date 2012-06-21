package at.ainf.owlcontroller;

import at.ainf.owlcontroller.oaei11align.RdfMatchingFileParser;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.05.12
 * Time: 10:02
 * To change this template use File | Settings | File Templates.
 */
public class RDFUtils {
    public static Map<OWLLogicalAxiom,BigDecimal> readRdfMapping(String path, String name) {
        RdfMatchingFileParser handler = new RdfMatchingFileParser();
        SAXParserFactory factory = SAXParserFactory.newInstance();

        try {
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse( ClassLoader.getSystemResource(path+"/"+name ).getPath(), handler );
        } catch (ParserConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SAXException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return handler.getMappings();
    }

    public static OWLOntology createOntologyWithMappings(String pathToOntologies,
                                                 String o1, String o2, String pathToMapping, String mappingName) {
        OWLOntology ontology1 = CreationUtils.createOwlOntology(pathToOntologies,o1+".owl");
        OWLOntology ontology2 = CreationUtils.createOwlOntology(pathToOntologies,o2+".owl");
        OWLOntology merged = CreationUtils.mergeOntologies(ontology1, ontology2);
        Set<OWLLogicalAxiom> mapping = RDFUtils.readRdfMapping(pathToMapping,mappingName).keySet();
        for (OWLLogicalAxiom axiom : mapping)
            merged.getOWLOntologyManager().applyChange(new AddAxiom(merged, axiom));
        return merged;
    }
}
