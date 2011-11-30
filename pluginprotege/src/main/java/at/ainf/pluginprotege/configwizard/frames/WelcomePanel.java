package at.ainf.pluginprotege.configwizard.frames;

import at.ainf.pluginprotege.configwizard.AbstractPanel;
import org.protege.editor.owl.OWLEditorKit;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 18.07.11
 * Time: 16:06
 * To change this template use File | Settings | File Templates.
 */
public class WelcomePanel extends AbstractPanel {
    public static final String INSTRUCTIONS = "This wizard will guide you step by step through the options of the debugging plugin. For each point an explanation is given so you can easily decide what you want. If you don't know which option is better you can leave it " +
            "at the default state.\n" +
            "\n" +
            "You can at any time leave the wizard and no changes are made. ";




    public static final String ID = "WELCOME_PANEL";

    public WelcomePanel(OWLEditorKit editorKit, int number) {
        super(ID, "Welcome", null, number);
    }

    protected void createUI(JComponent parent) {

        setBackgroundImage();
        setInstructions(INSTRUCTIONS);



    }

    public Object getNextPanelDescriptor() {
        return ChooseBoxes.ID;
    }


}
