package at.ainf.owlapi3.test;

import at.ainf.owlapi3.base.mappings.XmlFormatMappingsReader;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 23.07.13
 * Time: 11:12
 * To change this template use File | Settings | File Templates.
 */
public class XmlMappingsReaderTest {

    private static Logger logger = LoggerFactory.getLogger(XmlMappingsReaderTest.class.getName());

    @Test
    public void testXmlMappingsReader() {
        String mappingsFile = "oaei12conference/matcheralignments/inconsistent/optima-edas-iasted.rdf";
        String gsMappingsFile = "oaei12conference/reference/edas-iasted.rdf";
        XmlFormatMappingsReader mappingsReader = new XmlFormatMappingsReader (mappingsFile, gsMappingsFile);
        Map<OWLLogicalAxiom,BigDecimal> mappings = mappingsReader.getMappings();
        Map<OWLLogicalAxiom, BigDecimal> gs_mappings = mappingsReader.getGs_mappings();
        Map<OWLLogicalAxiom, BigDecimal> incorrectMappings = mappingsReader.getIncorrectMappings();
    }

}
