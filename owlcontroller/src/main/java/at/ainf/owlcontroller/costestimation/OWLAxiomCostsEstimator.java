package at.ainf.owlcontroller.costestimation;

import at.ainf.diagnosis.partitioning.BigFunctions;
import at.ainf.diagnosis.tree.CostsEstimator;
import at.ainf.owlapi3.model.OWLTheory;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 01.03.12
 * Time: 11:05
 * To change this template use File | Settings | File Templates.
 */
public class OWLAxiomCostsEstimator implements CostsEstimator<OWLLogicalAxiom> {

    protected Map<OWLLogicalAxiom,BigDecimal> axiomProb;
    
    protected BigDecimal STATIC_COSTS = new BigDecimal("0.001");
    
    protected OWLTheory theory;
    
    public OWLAxiomCostsEstimator(OWLTheory t, String file) throws IOException {
        Map<String, BigDecimal> axioms = new LinkedHashMap<String, BigDecimal>();
        Set<String> targetDiag = new LinkedHashSet<String>();
        if (file!=null)
            readData(file, axioms, targetDiag);
        axiomProb = getAx(t.getOriginalOntology().getLogicalAxioms(),axioms);
        theory = t;
    }

    public OWLAxiomCostsEstimator(OWLTheory theory, Map<OWLLogicalAxiom, BigDecimal> probMap) {
        this.theory = theory;
        axiomProb = probMap;
    }

    protected Map<OWLLogicalAxiom,BigDecimal> getAx(Set<OWLLogicalAxiom> logicalAxioms, Map<String,BigDecimal> axioms) {
        Map<OWLLogicalAxiom,BigDecimal> res=new LinkedHashMap<OWLLogicalAxiom, BigDecimal>();
        for (String targetAxiom : axioms.keySet()) {
            for (OWLLogicalAxiom axiom : logicalAxioms) {
                if (axiom.toString().contains(targetAxiom.trim()))
                    res.put(axiom, axioms.get(targetAxiom));
            }
        }
        return res;
    }

    public void readData(String filename, Map<String, BigDecimal> axioms, Set<String> targetDiag) throws IOException {
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
                            BigDecimal.valueOf(Double.parseDouble(sub.substring(sub.indexOf("|") + 1))));
                    axioms.put(createAxiom(targetNamespace, target, sourceNamespace, source),
                            BigDecimal.valueOf(Double.parseDouble(sub.substring(sub.indexOf("|") + 1))));
                }
                if (sub.contains(">")) {
                    source = sub.substring(0, sub.indexOf(">")).trim();
                    target = sub.substring(sub.indexOf(">") + 1, sub.indexOf("|")).trim();
                    axioms.put(createAxiom(sourceNamespace, source, targetNamespace, target),
                            BigDecimal.valueOf(Double.parseDouble(sub.substring(sub.indexOf("|") + 1))));
                }
                if (sub.contains("<")) {
                    source = sub.substring(0, sub.indexOf("<")).trim();
                    target = sub.substring(sub.indexOf("<") + 1, sub.indexOf("|")).trim();
                    axioms.put(createAxiom(targetNamespace, target, sourceNamespace, source),
                            BigDecimal.valueOf(Double.parseDouble(sub.substring(sub.indexOf("|") + 1))));
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
    
    public BigDecimal getAxiomSetCosts(Set<OWLLogicalAxiom> labelSet) {

        BigDecimal probability = BigDecimal.ONE;
        for (OWLLogicalAxiom axiom : labelSet) {
            probability = probability.multiply(getAxiomCosts(axiom));
        }
        Collection<OWLLogicalAxiom> activeFormulas = new ArrayList<OWLLogicalAxiom>(theory.getActiveFormulas());
        activeFormulas.removeAll(labelSet);
        for (OWLLogicalAxiom axiom : activeFormulas) {
                probability = probability.multiply(BigDecimal.ONE.subtract(getAxiomCosts(axiom)));
        }
        return probability;
    }

    
    public BigDecimal getAxiomCosts(OWLLogicalAxiom label) {
        if (axiomProb.get(label) != null) {
            BigDecimal t = BigDecimal.ONE.subtract(axiomProb.get(label));
            BigDecimal p = t.divide(BigDecimal.valueOf(10));
            if (p.compareTo(BigDecimal.ZERO)==0) {
                BigDecimal r = new BigDecimal("10");

                return BigFunctions.intPower(r,-100,r.scale());
            }
            else
                return p;
            }
        else
            return STATIC_COSTS;

    }
    
    
}
