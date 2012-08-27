package at.ainf.owlapi3.base;

import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.tree.CostsEstimator;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 23.08.12
 * Time: 12:02
 * To change this template use File | Settings | File Templates.
 */
public class OAEI08Session extends SimulatedSession {

    private static Logger logger = LoggerFactory.getLogger(OAEI08Session.class.getName());

    public static <X> int minCard(Set<? extends Set<X>> s) {
        int r = -1;

        try {
            for (Set<X> set : s)
                if (r == -1 || set.size() < r)
                    r = set.size();
        } catch (NoSuchElementException e) {

        }

        return r;
    }

    public static <X> int maxCard(Set<? extends Set<X>> s) {
        int r = -1;

        try {
            for (Set<X> set : s)
                if (set.size() > r)
                    r = set.size();
        } catch (NoSuchElementException e) {

        }

        return r;
    }

    public static <X> double meanCard(Set<? extends Set<X>> s) {
        double sum = 0;
        int cnt = 0;

        for (Set<X> set : s) {
            sum += set.size();
            cnt++;
        }

        if (cnt == 0) return -1;
        return sum / cnt;
    }

    public AxiomSet<OWLLogicalAxiom> getDgTarget(Set<AxiomSet<OWLLogicalAxiom>> diagnoses, final CostsEstimator<OWLLogicalAxiom> estimator) {
        Comparator<AxiomSet<OWLLogicalAxiom>> c = new Comparator<AxiomSet<OWLLogicalAxiom>>() {
            public int compare(AxiomSet<OWLLogicalAxiom> o1, AxiomSet<OWLLogicalAxiom> o2) {
                int numOfOntologyAxiomsO1 = 0;
                int numOfMatchingAxiomO1 = 0;
                for (OWLLogicalAxiom axiom : o1) {
                    if (estimator.getAxiomCosts(axiom).compareTo(new BigDecimal("0.001")) != 0)
                        numOfMatchingAxiomO1++;
                    else
                        numOfOntologyAxiomsO1++;
                }
                double percAxiomFromOntO1 = (double) numOfOntologyAxiomsO1;// / (numOfOntologyAxiomsO1 + numOfMatchingAxiomO1);

                int numOfOntologyAxiomsO2 = 0;
                int numOfMatchingAxiomO2 = 0;
                for (OWLLogicalAxiom axiom : o2) {
                    if (estimator.getAxiomCosts(axiom).compareTo(new BigDecimal("0.001")) != 0)
                        numOfMatchingAxiomO2++;
                    else
                        numOfOntologyAxiomsO2++;
                }
                double percAxiomFromOntO2 = (double) numOfOntologyAxiomsO2;// / (numOfOntologyAxiomsO2 + numOfMatchingAxiomO2);


                if (percAxiomFromOntO1 < percAxiomFromOntO2)
                    return -1;
                else if (percAxiomFromOntO1 == percAxiomFromOntO2)
                    return 0;
                else
                    return 1;
            }
        };

        return Collections.max(diagnoses, c);
    }

    public Set<OWLLogicalAxiom> getDiagnosisTarget(String matcher, String ontologies, OWLOntology onto) {
        return getDiagnosis(getDiagnosis(matcher, ontologies), onto);
    }

    protected static String[] getDiagnosis(String matcher, String ontology) {
        String filename = ClassLoader.getSystemResource("alignment/evaluation/" + matcher.trim() + "-incoherent-evaluation/" + ontology.trim() + ".txt").getFile();
        Map<String, Double> axioms = new LinkedHashMap<String, Double>();
        Set<String> targetDiag = new LinkedHashSet<String>();
        try {
            readData(filename, axioms, targetDiag);
        } catch (IOException e) {
            logger.info("can't read diagnosis from file ");
        }

        //logger.info("number of mappingaxioms:," + matcher + "," + ontology + "," + axioms.keySet().size());

        String[] result = new String[targetDiag.size()];
        int i = 0;
        for (String s : targetDiag) {
            result[i] = s;
            i++;
        }
        return result;
    }

    protected static Set<OWLLogicalAxiom> getDiagnosis(String[] targetAxioms, OWLOntology ontology) {

        Set<OWLLogicalAxiom> res = new LinkedHashSet<OWLLogicalAxiom>();
        for (String targetAxiom : targetAxioms) {
            for (OWLLogicalAxiom axiom : ontology.getLogicalAxioms()) {
                if (axiom.toString().contains(targetAxiom.trim()))
                    res.add(axiom);
            }
        }
        return res;
    }

    public static void readData(String filename, Map<String, Double> axioms, Set<String> targetDiag) throws IOException {
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
                    axioms.put(createAxiom(sourceNamespace, source, targetNamespace, target),
                            Double.parseDouble(sub.substring(sub.indexOf("|") + 1)));
                    axioms.put(createAxiom(targetNamespace, target, sourceNamespace, source),
                            Double.parseDouble(sub.substring(sub.indexOf("|") + 1)));
                }
                if (sub.contains(">")) {
                    source = sub.substring(0, sub.indexOf(">")).trim();
                    target = sub.substring(sub.indexOf(">") + 1, sub.indexOf("|")).trim();
                    axioms.put(createAxiom(sourceNamespace, source, targetNamespace, target),
                            Double.parseDouble(sub.substring(sub.indexOf("|") + 1)));
                }
                if (sub.contains("<")) {
                    source = sub.substring(0, sub.indexOf("<")).trim();
                    target = sub.substring(sub.indexOf("<") + 1, sub.indexOf("|")).trim();
                    axioms.put(createAxiom(targetNamespace, target, sourceNamespace, source),
                            Double.parseDouble(sub.substring(sub.indexOf("|") + 1)));
                }
                if (status.equals("-")) {
                    if (sub.contains("=")) {
                        targetDiag.add(createAxiom(sourceNamespace, source, targetNamespace, target));
                        targetDiag.add(createAxiom(targetNamespace, target, sourceNamespace, source));
                    }
                    else if(sub.contains("<"))
                        targetDiag.add(createAxiom(sourceNamespace, source, targetNamespace, target));
                    else if(sub.contains(">"))
                        targetDiag.add(createAxiom(targetNamespace, target, sourceNamespace, source));
                }
                if (status.equals(">")) {
                    targetDiag.add(createAxiom(sourceNamespace, source, targetNamespace, target));
                }
                if (status.equals("<")) {
                    targetDiag.add(createAxiom(targetNamespace, target, sourceNamespace, source));
                }

            }
        }
    }

    protected static String createAxiom(String sourceNamespace, String source, String targetNamespace, String target) {
        return "<" + sourceNamespace + "#" + source + "> <" + targetNamespace + "#" + target + ">";
    }

    public OWLOntology getOntologyOAEI08(String matcher, String name) {
        return getOntologySimple("alignment/" + matcher + "_incoherent_matched_ontologies", name + ".owl");

    }

}
