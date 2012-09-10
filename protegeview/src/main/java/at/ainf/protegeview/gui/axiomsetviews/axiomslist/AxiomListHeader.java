package at.ainf.protegeview.gui.axiomsetviews.axiomslist;

import at.ainf.diagnosis.storage.AxiomSet;
import org.protege.editor.core.ui.list.MListSectionHeader;
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

    private AxiomSet<OWLLogicalAxiom> axiomSet;

    private String headerPref;

    private boolean isIncludeMeasure;

    public AxiomListHeader(AxiomSet<OWLLogicalAxiom> axiomSet, String headerPref, boolean isIncludeMeasure) {
        this.axiomSet = axiomSet;
        this.headerPref = headerPref;
        this.isIncludeMeasure = isIncludeMeasure;

    }

    public AxiomSet<OWLLogicalAxiom> getAxiomSet() {
        return axiomSet;
    }

    /*public Color getColor() {

        BigDecimal alphaVal = BigDecimal.valueOf(255).multiply(getAxiomSet().getMeasure());
        double valRounded = Double.parseDouble(alphaVal.round(new MathContext(4)).toPlainString());

        return new Color(255,0,0,(int) valRounded);
    }*/

    public String toString() {
        String roundedMeas = getAxiomSet().getMeasure().round(new MathContext(6)).toEngineeringString();
        String r = headerPref + "(Size: " + getAxiomSet().size();
        if (isIncludeMeasure)
            r += ", Measure: " + roundedMeas;
        return r + ")";

    }

}
