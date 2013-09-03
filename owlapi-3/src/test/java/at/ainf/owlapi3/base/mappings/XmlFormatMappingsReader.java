package at.ainf.owlapi3.base.mappings;

import at.ainf.owlapi3.tools.OAEI11ConferenceRdfMatchingParser;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 23.07.13
 * Time: 11:09
 * To change this template use File | Settings | File Templates.
 */
public class XmlFormatMappingsReader {

    private Map<OWLLogicalAxiom,BigDecimal> mappings;
    private Map<OWLLogicalAxiom,BigDecimal> gs_mappings;

    public XmlFormatMappingsReader(String mappingsFile) {
        this (mappingsFile, null);
    }

    public XmlFormatMappingsReader(String mappingsFile, String referenceMappingsFile) {
        mappings = readRdfMapping(mappingsFile);
        if (referenceMappingsFile != null)
            gs_mappings = readRdfMapping(referenceMappingsFile);
        else
            gs_mappings = new HashMap<OWLLogicalAxiom, BigDecimal>();
    }

    public Map<OWLLogicalAxiom, BigDecimal> getMappings() {
        return mappings;
    }

    public Map<OWLLogicalAxiom, BigDecimal> getGs_mappings() {
        return gs_mappings;
    }

    public Map<OWLLogicalAxiom, BigDecimal> getIncorrectMappings() {
        Map<OWLLogicalAxiom, BigDecimal> incorrectMappings = new HashMap<OWLLogicalAxiom, BigDecimal>(getMappings());
        for (OWLLogicalAxiom gsMapping : getGs_mappings().keySet())
            incorrectMappings.remove(gsMapping);
        return incorrectMappings;
    }

    protected Map<OWLLogicalAxiom,BigDecimal> readRdfMapping(String relativePathToFile) {
        OAEI11ConferenceRdfMatchingParser handler = new OAEI11ConferenceRdfMatchingParser();
        SAXParserFactory factory = SAXParserFactory.newInstance();

        try {
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse( ClassLoader.getSystemResource(relativePathToFile).getPath(), handler );
        } catch (ParserConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SAXException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        Map<OWLLogicalAxiom, BigDecimal> mappings = new HashMap<OWLLogicalAxiom, BigDecimal>();
        for (Map.Entry<OWLLogicalAxiom, BigDecimal> entry : handler.getMappings().entrySet())
            if (BigDecimal.valueOf(0.0).compareTo(entry.getValue()) == 0) {
                mappings.put(entry.getKey(), BigDecimal.valueOf(0.50001));
            }
            else
                mappings.put(entry.getKey(), entry.getValue());
        return mappings;
    }



}
