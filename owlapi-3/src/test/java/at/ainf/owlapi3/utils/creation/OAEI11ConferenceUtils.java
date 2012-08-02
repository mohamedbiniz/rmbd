package at.ainf.owlapi3.utils.creation;

import at.ainf.owlapi3.utils.RdfMatchingFileParser;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLOntologyMerger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 02.08.12
 * Time: 09:02
 * To change this template use File | Settings | File Templates.
 */
public class OAEI11ConferenceUtils extends CommonUtils {
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

    public static OWLOntology createOntologyWithRdfMappings(String pathToOntologies,
                                                            String o1, String o2, String pathToMapping, String mappingName) {
        OWLOntology ontology1 = createOwlOntology(pathToOntologies, o1 + ".owl");
        OWLOntology ontology2 = createOwlOntology(pathToOntologies, o2 + ".owl");
        OWLOntology merged = mergeOntologies(ontology1, ontology2);
        Set<OWLLogicalAxiom> mapping = readRdfMapping(pathToMapping, mappingName).keySet();
        for (OWLLogicalAxiom axiom : mapping)
            merged.getOWLOntologyManager().applyChange(new AddAxiom(merged, axiom));
        return merged;
    }

    public static OWLOntology mergeOntologies (OWLOntology ontology1, OWLOntology ontology2) {
        try {
            OWLOntologyManager man = ontology1.getOWLOntologyManager();
            final Set<OWLOntology> ontologies = new LinkedHashSet<OWLOntology>();
            ontologies.add(ontology1);
            ontologies.add(ontology2);

            OWLOntologyMerger merger = new OWLOntologyMerger(new OWLOntologySetProvider() {
                public Set<OWLOntology> getOntologies() {
                    return ontologies;
                }
            });
            String o1I = ontology1.getOntologyID().getOntologyIRI().toString();
            String o2I = ontology2.getOntologyID().getOntologyIRI().toString();
            IRI mergedIRI = IRI.create("matched_" + o1I + "_" + o2I);

            return merger.createMergedOntology(man, mergedIRI);
        } catch (OWLOntologyCreationException e) {
            return null;
        }
    }

    public static Set<OWLLogicalAxiom> getIntersection (Set<OWLLogicalAxiom> axioms1, Set<OWLLogicalAxiom> axioms2) {
        Set<OWLLogicalAxiom> intersection = new LinkedHashSet<OWLLogicalAxiom>();
        intersection.addAll(axioms1);
        intersection.retainAll(axioms2);

        return intersection;
    }
}
