package at.ainf.protegeview.gui.menu;

import at.ainf.protegeview.gui.buttons.SearchErrorHandler;
import at.ainf.protegeview.model.EditorKitHook;
import at.ainf.protegeview.model.OntologyDiagnosisSearcher;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 30.10.12
 * Time: 17:44
 * To change this template use File | Settings | File Templates.
 */
public class CalculateDiagnoses extends ProtegeOWLAction {

    @Override
    public void actionPerformed(ActionEvent e) {

        EditorKitHook editorKitHook = (EditorKitHook) getOWLModelManager().
                get("at.ainf.protegeview.at.ainf.protegeview.model.EditorKitHook");
        OntologyDiagnosisSearcher ods = editorKitHook.getActiveOntologyDiagnosisSearcher();
        ods.doCalculateDiagnosis(new SearchErrorHandler());
    }

    @Override
    public void initialise() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void dispose() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}
