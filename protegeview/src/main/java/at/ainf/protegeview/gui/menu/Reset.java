package at.ainf.protegeview.gui.menu;

import at.ainf.protegeview.model.EditorKitHook;
import at.ainf.protegeview.model.OntologyDiagnosisSearcher;
import org.protege.editor.core.ModelManager;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 30.10.12
 * Time: 16:53
 * To change this template use File | Settings | File Templates.
 */
public class Reset extends ProtegeOWLAction {

    @Override
    public void actionPerformed(ActionEvent e) {

        EditorKitHook editorKitHook = (EditorKitHook) getOWLModelManager().
                get("at.ainf.protegeview.at.ainf.protegeview.model.EditorKitHook");
        OntologyDiagnosisSearcher ods = editorKitHook.getActiveOntologyDiagnosisSearcher();
        if (!ods.isTestcasesEmpty()) {
            int answer = JOptionPane.showConfirmDialog(null,
                    "Do you also want to delete testcases?", "Reset Type", JOptionPane.YES_NO_CANCEL_OPTION);
            if (answer == JOptionPane.YES_OPTION)
                ods.doFullReset();
            else if (answer == JOptionPane.NO_OPTION)
                ods.doReset();
        }
        else
            ods.doFullReset();
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
