package at.ainf.pluginprotege.configwizard.frames;

import at.ainf.pluginprotege.configwizard.AbstractPanel;
import org.protege.editor.owl.OWLEditorKit;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 20.07.11
 * Time: 12:33
 * To change this template use File | Settings | File Templates.
 */
public class ReduceIncoherencyIncon extends AbstractPanel {
    public static final String ID = ReduceIncoherencyIncon.class.getName();

    private JCheckBox checkbox;


    public ReduceIncoherencyIncon(OWLEditorKit editorKit, int number) {
        super(ID, "Reduce Incoherency  ", editorKit, number);
    }


    protected void createUI(JComponent parent) {

        setBackgroundImage();
        setInstructions("Do you want to add an individual to each class. So you can find errors you otherwise would not be able to debug?");

        checkbox = new JCheckBox("reduce incoherency to inconsistency", false);

        parent.add(checkbox);

    }

    public boolean isReductionInconsistencySelected() {
        return checkbox.isSelected();
    }

    public Object getNextPanelDescriptor() {
        return IncludeEntailments.ID;
    }


    public Object getBackPanelDescriptor() {
        return QueryMinimizer.ID;
    }
}
