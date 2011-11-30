package at.ainf.pluginprotege.views;

import org.protege.editor.core.ui.list.MListSectionHeader;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Set;

public class ResultsListSection implements MListSectionHeader {

    private String label;

    private double p;

    private int num;

    private Set<OWLLogicalAxiom> axiomSet;

    private int userTargetConfidence =   80;

    public int getUserTargetConfidence() {
        return userTargetConfidence;
    }

    public void setUserTargetConfidence(int userTargetConfidence) {
        this.userTargetConfidence = userTargetConfidence;
    }

    public ResultsListSection(String label, int num, Set<OWLLogicalAxiom> axiomSet) {
      this (label,num,-1,axiomSet);
    }

    public ResultsListSection (String label, int num, double p, Set<OWLLogicalAxiom> axiomSet) {
        this.label = label;
        this.num = num;
        this.p = p;
        this.axiomSet = axiomSet;

    }

    private boolean userMarkedThisTarget = false;

    public boolean isUserMarkedThisTarget() {
        return userMarkedThisTarget;
    }

    public void setUserMarkedThisTarget(boolean userMarkedThisTarget) {
        this.userMarkedThisTarget = userMarkedThisTarget;
    }

    private boolean showEntailments = false;

    public boolean isShowEntailments() {
        return showEntailments;
    }

    public void setShowEntailments(boolean showEntailments) {
        this.showEntailments = showEntailments;
    }

    public int getNum() {
        return num;
    }

    public Set<OWLLogicalAxiom> getAxiomSet() {
        return axiomSet;
    }

    public String getName() {
        NumberFormat nf = new DecimalFormat("00.00E0");
        if (p != -1)
            return label + " (num=" + num + ",   p=" + nf.format(p) + ")";
        else
            return label + " (num=" + num + ")";
    }

    public boolean canAdd() {
        return false;
    }


}
