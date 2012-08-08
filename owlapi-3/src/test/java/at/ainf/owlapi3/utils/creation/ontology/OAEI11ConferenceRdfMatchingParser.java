package at.ainf.owlapi3.utils.creation.ontology;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.PublicKey;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 10.05.12
 * Time: 13:30
 * To change this template use File | Settings | File Templates.
 */
public class OAEI11ConferenceRdfMatchingParser extends DefaultHandler {

    Stack stack = new Stack();

    String lastCharacters = "";

    String onto1;

    String onto2;

    Set<Mapping> mappings = new LinkedHashSet<Mapping>();

    Mapping lastMapping;

    class Mapping {
        public String onto1axiom;
        public String onto2axiom;
        public BigDecimal measure;
        public String relation;
    }

    public void startDocument() throws SAXException
    {
    }

    public void endDocument() throws SAXException
    {
    }

    public Map<OWLLogicalAxiom,BigDecimal> getMappings() {
        Map<OWLLogicalAxiom,BigDecimal> mappingsOwl = new HashMap<OWLLogicalAxiom, BigDecimal>();
        for (Mapping mapping : mappings) {
            if (mapping.relation.equals("=")) {
                mappingsOwl.put(createAxiomOAEI(mapping.onto1axiom,mapping.onto2axiom),mapping.measure);
                mappingsOwl.put(createAxiomOAEI(mapping.onto2axiom,mapping.onto1axiom),mapping.measure);
            }
            else if (mapping.relation.equals(">")) {
                mappingsOwl.put(createAxiomOAEI(mapping.onto1axiom,mapping.onto2axiom),mapping.measure);
            }
            else if (mapping.relation.equals("<")) {
                mappingsOwl.put(createAxiomOAEI(mapping.onto2axiom,mapping.onto1axiom),mapping.measure);
            }
            else {
                throw new RuntimeException("relations other than = > < are not implemented ");
            }
        }
        return mappingsOwl;
    }

    static OWLLogicalAxiom createAxiomOAEI(String source, String target) {
        OWLDataFactory factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        OWLClass clsA = factory.getOWLClass(IRI.create(source));
        OWLClass clsB = factory.getOWLClass(IRI.create(target));

        return factory.getOWLSubClassOfAxiom(clsA, clsB);
    }

    public void startElement(java.lang.String uri, java.lang.String localName, java.lang.String qName, Attributes attributes) throws SAXException
    {


        if (qName.equals("entity1")) {
            lastMapping = new Mapping();
            lastMapping.onto1axiom = attributes.getValue("rdf:resource");
        }
        else if (qName.equals("entity2")) {
            lastMapping.onto2axiom = attributes.getValue("rdf:resource");
        }


    }


    public void endElement(java.lang.String uri, java.lang.String localName, java.lang.String qName) throws SAXException
    {
        if (qName.equals("onto1"))
             onto1 = lastCharacters;
        else if (qName.equals("onto2"))
            onto2 = lastCharacters;
        else if (qName.equals("measure"))
            lastMapping.measure = new BigDecimal(Double.valueOf(lastCharacters).toString());
        else if (qName.equals("relation")) {
            lastMapping.relation = lastCharacters;
            mappings.add(lastMapping);
        }

    }

    public void characters(char[] ch, int start, int length)
    {
        lastCharacters = new String(Arrays.copyOfRange(ch,start,start+length));

    }

}
