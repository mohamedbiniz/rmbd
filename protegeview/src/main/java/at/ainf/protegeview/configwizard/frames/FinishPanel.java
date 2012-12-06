package at.ainf.protegeview.configwizard.frames;

import at.ainf.protegeview.configwizard.AbstractPanel;
import org.protege.editor.core.ui.wizard.WizardPanel;
import org.protege.editor.owl.OWLEditorKit;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 18.07.11
 * Time: 16:06
 * To change this template use File | Settings | File Templates.
 */
public class FinishPanel extends AbstractPanel {
    public static final String ID = "FINISH_PANEL";



    public FinishPanel(OWLEditorKit editorKit, int number) {
        super(ID, "Finish", editorKit, number);
    }


    protected void createUI(JComponent parent) {

        setBackgroundImage();
        setInstructions("You have no configured the plugin. To confirm your choice and to start a query session click on Finish");


    }





    public Object getBackPanelDescriptor() {
        return OntologyAxioms.ID;
    }


    public Object getNextPanelDescriptor() {
        return WizardPanel.FINISH;
    }
}
