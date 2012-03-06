package at.ainf.owlcontroller;

import at.ainf.diagnosis.tree.CostsEstimator;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.theory.model.ITheory;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 01.03.12
 * Time: 11:05
 * To change this template use File | Settings | File Templates.
 */
public class OWLAxiomCostsEstimator implements CostsEstimator<OWLLogicalAxiom> {

    Map<OWLLogicalAxiom,Double> axiomProb;
    
    protected double STATIC_COSTS = 0.001;
    
    protected OWLTheory theory;
    
    public OWLAxiomCostsEstimator(OWLTheory t, String file) throws IOException {
        Map<String, Double> axioms = new LinkedHashMap<String, Double>();
        Set<String> targetDiag = new LinkedHashSet<String>();
        if (file!=null)
            readData(file, axioms, targetDiag);
        axiomProb = getAx(t.getOriginalOntology().getLogicalAxioms(),axioms);
        theory = t;
    }

    public OWLAxiomCostsEstimator(OWLTheory theory, Map<OWLLogicalAxiom, Double> probMap) {
        this.theory = theory;
        axiomProb = probMap;
    }

    protected Map<OWLLogicalAxiom,Double> getAx(Set<OWLLogicalAxiom> logicalAxioms, Map<String,Double> axioms) {
        Map<OWLLogicalAxiom,Double> res=new LinkedHashMap<OWLLogicalAxiom, Double>();
        for (String targetAxiom : axioms.keySet()) {
            for (OWLLogicalAxiom axiom : logicalAxioms) {
                if (axiom.toString().contains(targetAxiom.trim()))
                    res.put(axiom, axioms.get(targetAxiom));
            }
        }
        return res;
    }

    public void readData(String filename, Map<String, Double> axioms, Set<String> targetDiag) throws IOException {
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
                    targetDiag.add(createAxiom(sourceNamespace, source, targetNamespace, target));
                    if (sub.contains("="))
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

    private String createAxiom(String sourceNamespace, String source, String targetNamespace, String target) {
        return "<" + sourceNamespace + "#" + source + "> <" + targetNamespace + "#" + target + ">";
    }
    
    public double getAxiomSetCosts(Set<OWLLogicalAxiom> labelSet) {
        double probability = 1.0;
        for (OWLLogicalAxiom axiom : labelSet) {
            probability *= getAxiomCosts(axiom);
        }
        Collection<OWLLogicalAxiom> activeFormulas = new ArrayList<OWLLogicalAxiom>(theory.getActiveFormulas());
        activeFormulas.removeAll(labelSet);
        for (OWLLogicalAxiom axiom : activeFormulas) {
            if (probability * (1 - getAxiomCosts(axiom)) == 0)
                probability = Double.MIN_VALUE;
            else
                probability *= (1 - getAxiomCosts(axiom));
        }
        return probability;
    }

    
    public double getAxiomCosts(OWLLogicalAxiom label) {
        if (axiomProb.get(label) != null) {
            double p = (1 - axiomProb.get(label)) / 10;
            if (p==0)
                return Math.pow(10,-100);//Double.MIN_VALUE;
            else
                return p;
            }
        else
            return STATIC_COSTS;
    }
    
    
}
