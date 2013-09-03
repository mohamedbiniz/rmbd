package at.ainf.owlapi3.base;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.tools.OAEI11ConferenceRdfMatchingParser;
import at.ainf.owlapi3.costestimation.OWLAxiomCostsEstimator;
import at.ainf.owlapi3.model.OWLIncoherencyExtractor;
import at.ainf.owlapi3.model.OWLTheory;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLOntologyMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static at.ainf.owlapi3.util.SetUtils.createIntersection;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 23.08.12
 * Time: 12:03
 * To change this template use File | Settings | File Templates.
 */
public class OAEI11ConferenceSession extends SimulatedSession {

    private Logger logger = LoggerFactory.getLogger(OAEI11ConferenceSession.class.getName());


    public OWLOntology getOntology(String pathToOntologies,
                                          String o1, String o2, String pathToMapping, String mappingName) {
        OWLOntology ontology1 = getOntologySimple(pathToOntologies + "/" + o1 + ".owl");
        OWLOntology ontology2 = getOntologySimple(pathToOntologies + "/" + o2 + ".owl");
        OWLOntology merged = mergeOntologies(ontology1, ontology2);
        Set<OWLLogicalAxiom> mapping = readRdfMapping(pathToMapping, mappingName).keySet();
        for (OWLLogicalAxiom axiom : mapping)
            merged.getOWLOntologyManager().applyChange(new AddAxiom(merged, axiom));
        return merged;
    }

    public Map<OWLLogicalAxiom,BigDecimal> readRdfMapping(String path, String name) {
        OAEI11ConferenceRdfMatchingParser handler = new OAEI11ConferenceRdfMatchingParser();
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

        Map<OWLLogicalAxiom, BigDecimal> mappings = new HashMap<OWLLogicalAxiom, BigDecimal>();
        for (Map.Entry<OWLLogicalAxiom, BigDecimal> entry : handler.getMappings().entrySet())
            if (BigDecimal.valueOf(0.0).compareTo(entry.getValue()) == 0) {
                mappings.put(entry.getKey(),BigDecimal.valueOf(0.00001));
                logger.info ("confidence of mappings is zero: " + entry.getKey());
            }
            else
                mappings.put(entry.getKey(), entry.getValue());
        return mappings;
    }

    public OWLOntology mergeOntologies (OWLOntology ontology1, OWLOntology ontology2) {
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



    protected Set<FormulaSet<OWLLogicalAxiom>> getRandomDiagSet(File file, String directory ) throws SolverException, InconsistentTheoryException {
        String conferenceYear = getConferenceYear();
        String matchingsDir = conferenceYear + getMatchingsDir();
        String matchingsDirName = "/ontology";
        String mapd = matchingsDir + directory;

        boolean dual = false;
        String fileName = file.getName();
        StringTokenizer t = new StringTokenizer(fileName, "-");
        String matcher = t.nextToken();

        String o1 = t.nextToken();
        String o2 = t.nextToken();
        o2 = o2.substring(0, o2.length() - 4);

        String n = file.getName().substring(0, file.getName().length() - 4);
        OWLOntology merged = getOntology(conferenceYear + matchingsDirName,
                o1, o2, mapd, n + ".rdf");

        OWLOntology ontology = new OWLIncoherencyExtractor(
                new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(merged);
        OWLTheory theory = getExtendTheory(ontology, dual);
        TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = getUniformCostSearch(theory, dual);

        LinkedHashSet<OWLLogicalAxiom> bx = new LinkedHashSet<OWLLogicalAxiom>();
        OWLOntology ontology1 = getOntologySimple(conferenceYear + matchingsDirName, o1 + ".owl");
        OWLOntology ontology2 = getOntologySimple(conferenceYear + matchingsDirName, o2 + ".owl");

        bx.addAll(createIntersection(ontology.getLogicalAxioms(), ontology1.getLogicalAxioms()));
        bx.addAll(createIntersection(ontology.getLogicalAxioms(), ontology2.getLogicalAxioms()));
        theory.getKnowledgeBase().addBackgroundFormulas(bx);

        Map<OWLLogicalAxiom, BigDecimal> map1 = readRdfMapping(mapd, n + ".rdf");

        OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, map1);


        search.setCostsEstimator(es);

        search.reset();

        OWLTheory th30 = getExtendTheory(ontology, dual);
        TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search30 = getUniformCostSearch(th30, dual);
        th30.getKnowledgeBase().addBackgroundFormulas(bx);
        OWLAxiomCostsEstimator es30 = new OWLAxiomCostsEstimator(th30, readRdfMapping(mapd, n + ".rdf"));
        search30.setCostsEstimator(es30);

        try {
            search30.setMaxDiagnosesNumber(30);
            search30.start();
        } catch (NoConflictException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        Set<FormulaSet<OWLLogicalAxiom>> diagnoses = new TreeSet<FormulaSet<OWLLogicalAxiom>>(search30.getDiagnoses());

        search30.reset();

        return diagnoses;

    }

    protected String getMatchingsDir() {
        return "/matchings/";
    }

    protected String getConferenceYear() {
        return "oaei11conference";
    }


    protected int chooseRandomNum(Set<FormulaSet<OWLLogicalAxiom>> diagnoses, Random random) {

        return random.nextInt(diagnoses.size());
    }

    protected Set<OWLLogicalAxiom> chooseRandomDiag(Set<FormulaSet<OWLLogicalAxiom>> diagnoses,File file, int random) {
        Set<OWLLogicalAxiom> targetDg = null;

        logger.info(file.getName() + ",diagnosis selected as target," + random);
        targetDg = new LinkedHashSet<OWLLogicalAxiom>((FormulaSet<OWLLogicalAxiom>) diagnoses.toArray()[random]);
        logger.info(file.getName() + ",target diagnosis axioms," + renderAxioms(targetDg));


        return targetDg;
    }

}
