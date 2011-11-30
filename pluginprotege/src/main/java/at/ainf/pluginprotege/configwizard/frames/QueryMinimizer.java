package at.ainf.pluginprotege.configwizard.frames;

import at.ainf.pluginprotege.configwizard.AbstractPanel;
import org.protege.editor.owl.OWLEditorKit;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 20.07.11
 * Time: 12:31
 * To change this template use File | Settings | File Templates.
 */
public class QueryMinimizer extends AbstractPanel {
    public static final String ID = QueryMinimizer.class.getName();

    private JCheckBox checkbox;


    public QueryMinimizer(OWLEditorKit editorKit, int number) {
        super(ID, "Query Minimizer", editorKit, number);
    }


    protected void createUI(JComponent parent) {

        setBackgroundImage();
        setInstructions("Do you want to use the query minimizer so queries become shorter and are easier to read?");
        checkbox = new JCheckBox("minimize queries", true);

        parent.add(checkbox);

    }

    public boolean isMinimizerSelected() {
        return checkbox.isSelected();
    }

    public Object getNextPanelDescriptor() {
        return ReduceIncoherencyIncon.ID;
    }


    public Object getBackPanelDescriptor() {
        return ChoScoringFunction.ID;
    }

}
