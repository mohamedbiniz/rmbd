package at.ainf.owlapi3.base;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLOntologyMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 23.08.12
 * Time: 12:03
 * To change this template use File | Settings | File Templates.
 */
public class OAEI11AnatomySession extends SimulatedSession {

    private Logger logger = LoggerFactory.getLogger(OAEI11AnatomySession.class.getName());

    public Set<OWLLogicalAxiom> getDiagnosisTarget(String file, String dir, String s) {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();

        Map<OWLLogicalAxiom, Double> axioms = new HashMap<OWLLogicalAxiom, Double>();
        Set<OWLLogicalAxiom> targetDiagnosis = new LinkedHashSet<OWLLogicalAxiom>();
        try {
            String path = ClassLoader.getSystemResource(dir).getPath() + "/";
            readDataOAEI(path + file + s, axioms, targetDiagnosis, man);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return targetDiagnosis;
    }

    public Set<OWLLogicalAxiom> getAxiomsInMappingOAEI(String path, String source, String fileSuffix) {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();

        Map<OWLLogicalAxiom, Double> axioms = new HashMap<OWLLogicalAxiom, Double>();
        Set<OWLLogicalAxiom> targetDiagnosis = new LinkedHashSet<OWLLogicalAxiom>();
        try {
            readDataOAEI(path + source + fileSuffix, axioms, targetDiagnosis, man);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return axioms.keySet();
    }

    public void readDataOAEI(String filename, Map<OWLLogicalAxiom, Double> axioms, Set<OWLLogicalAxiom> targetDiag, OWLOntologyManager man) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        String sourceNamespace = "";
        String targetNamespace = "";
        while ((line = br.readLine()) != null) {
            if (line.startsWith("sourceNamespace"))
                sourceNamespace = line.substring(line.indexOf("=") + 1).trim();
            if (line.startsWith("targetNamespace"))
                targetNamespace = line.substring(line.indexOf("=") + 1).trim();
            if (line.startsWith(">") || line.startsWith("<") || line.startsWith("+") || line.startsWith("-")) {
                String status = line.substring(0, 2).trim();
                String sub = line.substring(2);
                String source = "";
                String target = "";
                if (sub.contains("=")) {
                    source = sub.substring(0, sub.indexOf("=")).trim();
                    target = sub.substring(sub.indexOf("=") + 1, sub.indexOf("|")).trim();
                    axioms.put(createAxiomOAEI(sourceNamespace, source, targetNamespace, target, man),
                            Double.parseDouble(sub.substring(sub.indexOf("|") + 1)));
                    axioms.put(createAxiomOAEI(targetNamespace, target, sourceNamespace, source, man),
                            Double.parseDouble(sub.substring(sub.indexOf("|") + 1)));
                }
                if (sub.contains(">")) {
                    source = sub.substring(0, sub.indexOf(">")).trim();
                    target = sub.substring(sub.indexOf(">") + 1, sub.indexOf("|")).trim();
                    axioms.put(createAxiomOAEI(sourceNamespace, source, targetNamespace, target, man),
                            Double.parseDouble(sub.substring(sub.indexOf("|") + 1)));
                }
                if (sub.contains("<")) {
                    source = sub.substring(0, sub.indexOf("<")).trim();
                    target = sub.substring(sub.indexOf("<") + 1, sub.indexOf("|")).trim();
                    axioms.put(createAxiomOAEI(targetNamespace, target, sourceNamespace, source, man),
                            Double.parseDouble(sub.substring(sub.indexOf("|") + 1)));
                }
                if (status.equals("-")) {
                    if (sub.contains("=")) {
                        targetDiag.add(createAxiomOAEI(sourceNamespace, source, targetNamespace, target, man));
                        targetDiag.add(createAxiomOAEI(targetNamespace, target, sourceNamespace, source, man));
                    } else if (sub.contains(">"))
                        targetDiag.add(createAxiomOAEI(sourceNamespace, source, targetNamespace, target, man));
                    else if (sub.contains("<"))
                        targetDiag.add(createAxiomOAEI(targetNamespace, target, sourceNamespace, source, man));
                }
                if (status.equals(">")) {
                    targetDiag.add(createAxiomOAEI(sourceNamespace, source, targetNamespace, target, man));
                }
                if (status.equals("<")) {
                    targetDiag.add(createAxiomOAEI(targetNamespace, target, sourceNamespace, source, man));
                }

            }
        }
    }

    public OWLLogicalAxiom createAxiomOAEI(String sourceNamespace, String source, String targetNamespace, String target, OWLOntologyManager man) {
        OWLDataFactory factory = man.getOWLDataFactory();
        OWLClass clsA = factory.getOWLClass(IRI.create(sourceNamespace + "#" + source));
        OWLClass clsB = factory.getOWLClass(IRI.create(targetNamespace + "#" + target));
        OWLLogicalAxiom axiom = factory.getOWLSubClassOfAxiom(clsA, clsB);
        // "<" + sourceNamespace + "#" + source + "> <" + targetNamespace + "#" + target + ">";

        return axiom;
        // "<" + sourceNamespace + "#" + source + "> <" + targetNamespace + "#" + target + ">";
    }

    public OWLOntology getOntology(String file, final String ontoDir, String fileSuffix) {
        try {
            OWLOntologyManager man = OWLManager.createOWLOntologyManager();

            InputStream st = ClassLoader.getSystemResourceAsStream(ontoDir + "/mouse.owl");
            OWLOntology mouse = man.loadOntologyFromOntologyDocument(st);
            st = ClassLoader.getSystemResourceAsStream(ontoDir + "/human.owl");
            OWLOntology human = man.loadOntologyFromOntologyDocument(st);

            OWLOntologyMerger merger = new OWLOntologyMerger(man);
            OWLOntology merged = merger.createMergedOntology(man, IRI.create("matched" + file + fileSuffix));
            Set<OWLLogicalAxiom> mappAx = getAxiomsInMappingOAEI(ClassLoader.getSystemResource(ontoDir).
                    getPath() + "/", file, fileSuffix);
            for (OWLLogicalAxiom axiom : mappAx)
                man.applyChange(new AddAxiom(merged, axiom));

            return merged;
        } catch (OWLOntologyCreationException e) {
            return null;
        }
    }


}
