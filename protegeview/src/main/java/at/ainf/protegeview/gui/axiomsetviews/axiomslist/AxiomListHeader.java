package at.ainf.protegeview.gui.axiomsetviews.axiomslist;

import at.ainf.diagnosis.storage.FormulaSet;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.math.MathContext;

/**
* Created with IntelliJ IDEA.
* User: pfleiss
* Date: 05.09.12
* Time: 16:37
* To change this template use File | Settings | File Templates.
*/
public class AxiomListHeader {

    private FormulaSet<OWLLogicalAxiom> formulaSet;

    private String headerPref;

    private boolean isIncludeMeasure;

    public AxiomListHeader(FormulaSet<OWLLogicalAxiom> formulaSet, String headerPref, boolean isIncludeMeasure) {
        this.formulaSet = formulaSet;
        this.headerPref = headerPref;
        this.isIncludeMeasure = isIncludeMeasure;

    }

    public FormulaSet<OWLLogicalAxiom> getFormulaSet() {
        return formulaSet;
    }

    /*public Color getColor() {

        BigDecimal alphaVal = BigDecimal.valueOf(255).multiply(getAxiomSets().getMeasure());
        double valRounded = Double.parseDouble(alphaVal.round(new MathContext(4)).toPlainString());

        return new Color(255,0,0,(int) valRounded);
    }*/

    public String toString() {
        String roundedMeas = getFormulaSet().getMeasure().round(new MathContext(6)).toEngineeringString();
        String r = headerPref + "(Size: " + getFormulaSet().size();
        if (isIncludeMeasure)
            r += ", Measure: " + roundedMeas;
        return r + ")";

    }

}
