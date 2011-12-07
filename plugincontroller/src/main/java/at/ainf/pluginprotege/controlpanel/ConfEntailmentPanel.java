package at.ainf.pluginprotege.controlpanel;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 13.05.11
 * Time: 08:40
 * To change this template use File | Settings | File Templates.
 */
public class ConfEntailmentPanel extends JPanel {


    private JCheckBox inferredSubClassAxiomCheckbox = new JCheckBox("SubClass", true);
    private JCheckBox inferredClassAssertionAxiomCheckbox = new JCheckBox("ClassAssertion", true);

    private JCheckBox inferredEquivalentClassAxiomGenerator = new JCheckBox("EquivalentClass", false);
    private JCheckBox inferredDisjointClassesAxiomGenerator = new JCheckBox("DisjointClasses", false);
    private JCheckBox inferredPropertyAssertionGenerator = new JCheckBox("PropertyAssertion", false);


    public void loadGeneratorPreferences() {
        inferredSubClassAxiomCheckbox.setSelected(QueryDebuggerPreference.getInstance().isSubClassOfActivated());
        inferredClassAssertionAxiomCheckbox.setSelected(QueryDebuggerPreference.getInstance().isClassAssertionActivated());
        inferredEquivalentClassAxiomGenerator.setSelected(QueryDebuggerPreference.getInstance().isEquivalentClassActivated());
        inferredDisjointClassesAxiomGenerator.setSelected(QueryDebuggerPreference.getInstance().isDisjointClassesActivated());
        inferredPropertyAssertionGenerator.setSelected(QueryDebuggerPreference.getInstance().isPropertyAssertActivated());
    }


    public void saveGeneratorPreferences() {
        QueryDebuggerPreference.getInstance().setSubClassOfActivated(inferredSubClassAxiomCheckbox.isSelected());
        QueryDebuggerPreference.getInstance().setClassAssertionActivated(inferredClassAssertionAxiomCheckbox.isSelected());
        QueryDebuggerPreference.getInstance().setEquivalentClassActivated(inferredEquivalentClassAxiomGenerator.isSelected());
        QueryDebuggerPreference.getInstance().setDisjointClassesActivated(inferredDisjointClassesAxiomGenerator.isSelected());
        QueryDebuggerPreference.getInstance().setPropertyAssertActivated(inferredPropertyAssertionGenerator.isSelected());
    }

    public String toString() {
        return "SubClassAxioms=" + inferredSubClassAxiomCheckbox.isSelected() + "," +
                "ClassAssertionAxioms=" + inferredClassAssertionAxiomCheckbox.isSelected() + "," +
                "EquivalentClassAxioms" + inferredEquivalentClassAxiomGenerator.isSelected() + "," +
                "DisjointClassesAxioms" + inferredDisjointClassesAxiomGenerator.isSelected() + "," +
                "PropertyAssertionAxioms" + inferredPropertyAssertionGenerator.isSelected();
    }


    public ConfEntailmentPanel() {


        JPanel result = new JPanel(new GridLayout(2, 4));
        result.add(inferredSubClassAxiomCheckbox);
        result.add(inferredClassAssertionAxiomCheckbox);
        result.add(inferredEquivalentClassAxiomGenerator);
        result.add(inferredDisjointClassesAxiomGenerator);
        result.add(inferredPropertyAssertionGenerator);

        add(result);

    }


}
