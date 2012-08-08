package at.ainf.owlapi3.utils.creation.target;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 08.08.12
 * Time: 13:01
 * To change this template use File | Settings | File Templates.
 */
public class OAEI08TargetProvider implements TargetProvider {

    private static Logger logger = Logger.getLogger(OAEI08TargetProvider.class.getName());

    private OWLOntology onto;

    private String matcher;

    private String ontologies;

    public OAEI08TargetProvider(String matcher, String ontologies, OWLOntology onto) {
        this.onto = onto;
        this.matcher = matcher;
        this.ontologies = ontologies;

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

    @Override
    public Set<OWLLogicalAxiom> getDiagnosisTarget() {
        return getDiagnosis(getDiagnosis(matcher,ontologies),onto);
    }

}
