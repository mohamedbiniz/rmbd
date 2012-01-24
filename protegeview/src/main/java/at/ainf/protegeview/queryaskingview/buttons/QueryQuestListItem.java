package at.ainf.protegeview.queryaskingview.buttons;

import at.ainf.diagnosis.tree.UniformCostSearch;
import at.ainf.protegeview.WorkspaceTab;
import at.ainf.protegeview.views.ResultsListSectionItem;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 25.05.11
 * Time: 13:13
 * To change this template use File | Settings | File Templates.
 */
public class QueryQuestListItem extends ResultsListSectionItem {

    private WorkspaceTab workspace;

    private boolean nonEntailedMarked = false;

    private boolean entailedMarked = false;

    private boolean unknowMarked = false;

    public QueryQuestListItem(OWLLogicalAxiom axiom, WorkspaceTab workspaceTab) {
        super(axiom, axiom, ((UniformCostSearch<OWLLogicalAxiom>) workspaceTab.getSearch()).getNodeCostsEstimator());
        workspace=workspaceTab;


    }

    public boolean isUnknowMarked() {
        return unknowMarked;
    }

    public void setUnknowMarked(boolean unknowMarked) {
        this.unknowMarked = unknowMarked;
    }

    public boolean isNonEntailedMarked() {
        return nonEntailedMarked;
    }

    public void setNonEntailedMarked(boolean nonEntailedMarked) {
        this.nonEntailedMarked = nonEntailedMarked;
    }

    public boolean isEntailedMarked() {
        return entailedMarked;
    }

    public void setEntailedMarked(boolean entailedMarked) {
        this.entailedMarked = entailedMarked;
    }

    public void handleEntailed() {
        entailedMarked = !entailedMarked;
        nonEntailedMarked = false;
        if (entailedMarked) askUserLN();

    }

    int lineNumber;

    public int getLineNumber() {
        return lineNumber;

    }

    private void askUserLN() {

        int number = 0;
        boolean redo = true;

        while (redo) {
            /*JTextArea area = new JTextArea("");
            Object complexMsg[] = {"Why line in the text did you use to answer this axiom:", area};
            JOptionPane optionPane = new JOptionPane();
            optionPane.setMessage(complexMsg);
            optionPane.setMessageType(JOptionPane.QUESTION_MESSAGE);
            area.requestFocusInWindow();
            JDialog dialog = optionPane.createDialog(null, "Line Number");
            dialog.setVisible(true);*/

            String sq  = (String) JOptionPane.showInputDialog(
                    null,
                    "Which line in the text did you use to answer this axiom:",
                    "LineNumber",
                    JOptionPane.QUESTION_MESSAGE);

            try {
                number = Integer.parseInt(sq);
               redo = false;

            }
            catch(NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Please give a line number ", "NumberFormatException", JOptionPane.ERROR_MESSAGE);

            }

        }
        lineNumber = number;
    }

    public void handleNotEntailed() {
        entailedMarked = false;
        nonEntailedMarked = !nonEntailedMarked;
        if (nonEntailedMarked) askUserLN();

    }

    public void handleUnknownEntailed() {
        unknowMarked = !unknowMarked;
    }


}
