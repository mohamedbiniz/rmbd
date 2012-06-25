package at.ainf.owlapi3;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 10.04.12
 * Time: 10:40
 * To change this template use File | Settings | File Templates.
 */
public class AlignmentUtils {

    public static Properties readProps() {
        return readProps("alignment.unsolvable.properties");

    }

    public static Properties readProps(String str) {
        Properties properties = new Properties();
        String config = ClassLoader.getSystemResource("alignment/"+str  ).getFile();
        BufferedInputStream stream = null;
        try {
            stream = new BufferedInputStream(new FileInputStream(config));
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            properties.load(stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return properties;

    }

    public static Map<String, List<String>> readOntologiesFromFile(Properties properties) {

        String[] testsuites = properties.getProperty("alignment.testsuites").split(",");

        Map<String, List<String>> ontologies = new HashMap<String, List<String>>();

        for (String testsuite : testsuites) {
            List<String> ontologie = Arrays.asList(properties.getProperty(testsuite.trim()).split(","));
            ontologies.put(testsuite, ontologie);
        }
        return ontologies;
    }

    public static boolean compareDiagnoses(String[] d1, String[] d2) {
        List<String> diags1 = Arrays.asList(d1);
        List<String> diags2 = Arrays.asList(d2);

        if (diags1.size() != diags2.size())
            return false;

        for (String diags1d : diags1) {
            boolean found = false;
            for (String diags2d : diags2) {
                if (diags1d.trim().equals(diags2d.trim())) {
                    found = true;
                    break;
                }
            }
            if(!found)
                return false;
        }

        return true;
    }

    private static Logger logger = Logger.getLogger(AlignmentUtils.class.getName());

    public static String[] getDiagnosis(String matcher, String ontology) throws IOException {
        String filename = ClassLoader.getSystemResource("alignment/evaluation/" + matcher.trim() + "-incoherent-evaluation/" + ontology.trim() + ".txt").getFile();
        Map<String, Double> axioms = new LinkedHashMap<String, Double>();
        Set<String> targetDiag = new LinkedHashSet<String>();
        readData(filename, axioms, targetDiag);

        logger.info("number of mappingaxioms:," + matcher + "," + ontology + "," + axioms.keySet().size());

        String[] result = new String[targetDiag.size()];
        int i = 0;
        for (String s : targetDiag) {
            result[i] = s;
            i++;
        }
        return result;
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

    private static String createAxiom(String sourceNamespace, String source, String targetNamespace, String target) {
        return "<" + sourceNamespace + "#" + source + "> <" + targetNamespace + "#" + target + ">";
    }

}
