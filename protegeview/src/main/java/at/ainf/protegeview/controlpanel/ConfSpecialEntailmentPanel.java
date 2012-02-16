package at.ainf.protegeview.controlpanel;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 21.09.11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
public class ConfSpecialEntailmentPanel extends JPanel {


    private JCheckBox ontologyAxiomsCheckbox = new JCheckBox("Include ontology axioms ", true);
    private JCheckBox trivialEntailmentCheckbox = new JCheckBox("Include axioms ref owl:thing ", false);


    /*public  boolean getTrivialEntailments() {
        return trivialEntailmentCheckbox.isSelected();
    }*/


    public void loadGeneratorPreferences() {
        ontologyAxiomsCheckbox.setSelected(QueryDebuggerPreference.getInstance().isIncludeOntologyAxiomsActivated());
        trivialEntailmentCheckbox.setSelected(QueryDebuggerPreference.getInstance().isIncludeTrivialAxiomsActivated());
    }


    public void saveGeneratorPreferences() {
        QueryDebuggerPreference.getInstance().setIncludeOntologyAxiomsActivatd(ontologyAxiomsCheckbox.isSelected());
        QueryDebuggerPreference.getInstance().setIncludeTrivialAxiomsActivated(trivialEntailmentCheckbox.isSelected());

    }

    public String toString() {
        return "OntologyAxioms=" + ontologyAxiomsCheckbox.isSelected() + "," +
                "AxiomsRefThing" + trivialEntailmentCheckbox.isSelected();
    }


    public ConfSpecialEntailmentPanel() {

        JPanel result = new JPanel(new GridLayout(1, 2));
        ontologyAxiomsCheckbox.setEnabled(false);

        result.add(ontologyAxiomsCheckbox);
        result.add(trivialEntailmentCheckbox);

        add(result);

    }

}
