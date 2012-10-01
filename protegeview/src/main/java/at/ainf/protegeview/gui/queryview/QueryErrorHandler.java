package at.ainf.protegeview.gui.queryview;

import at.ainf.protegeview.gui.buttons.SearchErrorHandler;
import at.ainf.protegeview.model.OntologyDiagnosisSearcher;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 10.09.12
 * Time: 11:24
 * To change this template use File | Settings | File Templates.
 */
public class QueryErrorHandler extends SearchErrorHandler {

    @Override
    public void errorHappend(OntologyDiagnosisSearcher.ErrorStatus error) {
        super.errorHappend(error);
        switch (error) {
            case NO_QUERY:
                JOptionPane.showMessageDialog(null, "There is no possible query", "No Query", JOptionPane.INFORMATION_MESSAGE);
                break;
        }
    }

}
