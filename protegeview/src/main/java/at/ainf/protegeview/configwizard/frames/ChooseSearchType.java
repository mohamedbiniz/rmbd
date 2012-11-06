package at.ainf.protegeview.configwizard.frames;

import at.ainf.protegeview.configwizard.AbstractPanel;
import org.protege.editor.owl.OWLEditorKit;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 19.07.11
 * Time: 11:16
 * To change this template use File | Settings | File Templates.
 */
public class ChooseSearchType extends AbstractPanel implements ActionListener {
    public static final String ID = ChooseSearchType.class.getName();

    private String actionCommand = "BestFirst";

    public ChooseSearchType(OWLEditorKit editorKit,int number) {
        super(ID, "Search Type", editorKit, number);
    }

    public String getSearchCmd() {
        return actionCommand;
    }

    protected void createUI(JComponent parent) {

        setBackgroundImage();
        setInstructions("Now you have to specify which type of start do you want to use. " +
                        "You can use breath first start or uniform cost start. If you use uniform cost start " +
                        "the specified error probabilities for keywords are used to calculate " +
                        "the probability of each diagnosis to be the target diagnosis and the " +
                          "most probable target diagnosis is given first.");
        JRadioButton uniformCostSearch = new JRadioButton("Uniform Cost Search");
        JRadioButton breathFirstSearch = new JRadioButton("Breath First Search");
        ButtonGroup searchTypeButtonGroup = new ButtonGroup();

        uniformCostSearch.setActionCommand("BestFirst");
        uniformCostSearch.addActionListener(this);
        breathFirstSearch.setActionCommand("BreadthFirst");
        breathFirstSearch.addActionListener(this);

        searchTypeButtonGroup.add(breathFirstSearch);
        searchTypeButtonGroup.add(uniformCostSearch);

        uniformCostSearch.setSelected(true);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));

        panel.add(uniformCostSearch);
        panel.add(breathFirstSearch);
        parent.add(panel);

    }

    public Object getNextPanelDescriptor() {
        return ErrorProbabilityPanel.ID;
    }


    public Object getBackPanelDescriptor() {
        return ChooseBoxes.ID;
    }

    public void actionPerformed(ActionEvent e) {
        actionCommand = e.getActionCommand();
    }

}
