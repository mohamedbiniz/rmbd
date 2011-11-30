package at.ainf.pluginprotege.configwizard.frames;

import at.ainf.pluginprotege.configwizard.AbstractPanel;
import org.protege.editor.owl.OWLEditorKit;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 20.07.11
 * Time: 12:25
 * To change this template use File | Settings | File Templates.
 */
public class NumLeadingDiags extends AbstractPanel {
    public static final String ID = NumLeadingDiags.class.getName();

    final static int maxLeadingDiags = 14;

    private JSpinner numofLeadingDiagsField;


    public NumLeadingDiags(OWLEditorKit editorKit, int number) {
        super(ID, "Number of Leading Diagnoses", editorKit, number);
    }


    protected void createUI(JComponent parent) {

        setBackgroundImage();
        setInstructions("Because the computations are very CPU intensive you can here specifiy the number of diagnoses you want to process in one step when calculating queries.");

        numofLeadingDiagsField = new JSpinner(new SpinnerNumberModel(9, 1, maxLeadingDiags + 1, 1));

        numofLeadingDiagsField.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                Integer numOfLeadingDiags = (Integer) numofLeadingDiagsField.getValue();
                if (numOfLeadingDiags > maxLeadingDiags) {
                    numofLeadingDiagsField.setValue(maxLeadingDiags);
                    //getWS().setNumOfLeadingDiagnoses(maxLeadingDiags);
                    JOptionPane.showMessageDialog(null, "The number of leading diagnoses is to big!", "Max num of leading diags ", JOptionPane.INFORMATION_MESSAGE);
                }
                //getWS().setNumOfLeadingDiagnoses((Integer) numofLeadingDiagsField.getValue());
            }
        });
        parent.add(numofLeadingDiagsField);

    }

    public int getNumLeadingDiags() {
        return (Integer)numofLeadingDiagsField.getValue();
    }

    public Object getNextPanelDescriptor() {
        return ChoScoringFunction.ID;
    }


    public Object getBackPanelDescriptor() {
        return ErrorProbabilityPanel.ID;
    }
}
