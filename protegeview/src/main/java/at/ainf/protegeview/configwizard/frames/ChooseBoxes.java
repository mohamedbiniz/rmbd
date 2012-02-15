package at.ainf.protegeview.configwizard.frames;

import at.ainf.protegeview.configwizard.AbstractPanel;
import org.protege.editor.owl.OWLEditorKit;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 18.07.11
 * Time: 16:10
 * To change this template use File | Settings | File Templates.
 */
public class ChooseBoxes extends AbstractPanel {
    public static final String ID = ChooseBoxes.class.getName();

    private JCheckBox aBoxCheckbox;

    private JCheckBox tBoxCheckbox;

    public boolean isTBoxSelected() {
        return tBoxCheckbox.isSelected();
    }

    public boolean isABoxSelected() {
        return aBoxCheckbox.isSelected();
    }
    public ChooseBoxes(OWLEditorKit editorKit,int number) {
        super(ID, "Background Knowledge", editorKit,number);
    }


    protected void createUI(JComponent parent) {
        setBackgroundImage();
        setInstructions("Here you can add all axioms of the Abox and/or all " +
                        "axioms of the Tbox to the background axioms. If an " +
                        "axiom is in the background knowledge it is considered to be correct.\n" +
                        "\n" +
                        "The Abox consist of all the facts about individuals of the ontology (e.g. KITT type Car) " +
                        "and of relations between concepts (e.g. KITT hasDriver Michael_Knight).\n" +
                        "\n" +
                        "The Tbox consist of the general rules in the ontology (e.g. Car SubClassOf TransportiObject) \n" +
                        "\n" +
                        "So if you add the Abox all individuals are considered to be correct and only the errors in " +
                          "Tbox axioms are searched.");

        tBoxCheckbox = new JCheckBox("include axioms in TBox", false);
        aBoxCheckbox = new JCheckBox("include axioms in ABox",true);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
        panel.add(aBoxCheckbox);
        panel.add(tBoxCheckbox);

        parent.add(panel);

    }

    public Object getNextPanelDescriptor() {
        return ChooseSearchType.ID;
    }


    public Object getBackPanelDescriptor() {
        return WelcomePanel.ID;
    }



}
