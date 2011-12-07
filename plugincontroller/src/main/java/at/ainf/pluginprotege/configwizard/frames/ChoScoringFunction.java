package at.ainf.pluginprotege.configwizard.frames;

import at.ainf.pluginprotege.configwizard.AbstractPanel;
import org.protege.editor.owl.OWLEditorKit;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 19.09.11
 * Time: 08:55
 * To change this template use File | Settings | File Templates.
 */
public class ChoScoringFunction extends AbstractPanel implements ActionListener {
    public static final String ID = ChoScoringFunction.class.getName();

    private String actionCommand = "Entropy";

    public ChoScoringFunction(OWLEditorKit editorKit,int number) {
        super(ID, "Scoring Function", editorKit, number);
    }

    public String getScoringFunctionCmd() {
        return actionCommand;
    }

    protected void createUI(JComponent parent) {

        setBackgroundImage();
        setInstructions("Now you have to specify the " +
                        "scoring function");
        JRadioButton entropy = new JRadioButton("Entropy Scoring Function");
        JRadioButton split = new JRadioButton("Split Scoring Function");
        ButtonGroup scoringFunctionButtonGroup = new ButtonGroup();

        entropy.setActionCommand("Entropy");
        entropy.addActionListener(this);
        split.setActionCommand("Split");
        split.addActionListener(this);

        scoringFunctionButtonGroup.add(split);
        scoringFunctionButtonGroup.add(entropy);

        entropy.setSelected(true);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));

        panel.add(entropy);
        panel.add(split);
        parent.add(panel);

    }

    public Object getNextPanelDescriptor() {
        return QueryMinimizer.ID;
    }


    public Object getBackPanelDescriptor() {
        return NumLeadingDiags.ID;
    }

    public void actionPerformed(ActionEvent e) {
        actionCommand = e.getActionCommand();
    }

}
