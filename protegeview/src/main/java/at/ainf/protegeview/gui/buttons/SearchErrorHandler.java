package at.ainf.protegeview.gui.buttons;

import at.ainf.protegeview.model.ErrorHandler;
import at.ainf.protegeview.model.OntologyDiagnosisSearcher;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 10.09.12
 * Time: 11:22
 * To change this template use File | Settings | File Templates.
 */
public class SearchErrorHandler extends ErrorHandler {

    @Override
    public void errorHappend(OntologyDiagnosisSearcher.ErrorStatus error) {
        switch (error) {
            case SOLVER_EXCEPTION:
                JOptionPane.showMessageDialog(null, "There are problems with the solver", "IReasoner Exception", JOptionPane.ERROR_MESSAGE);
                break;
            case INCONSISTENT_THEORY_EXCEPTION:
                JOptionPane.showMessageDialog(null, "The set of testcases itself is inconsistent with the theory.", "Inconsistent Theory Exception", JOptionPane.ERROR_MESSAGE);
                break;
            case NO_CONFLICT_EXCEPTION:
                JOptionPane.showMessageDialog(null, "There are no conflicts and therefore no diagnoses", "No Conflict Exception", JOptionPane.INFORMATION_MESSAGE);
                break;
        }
    }

}
