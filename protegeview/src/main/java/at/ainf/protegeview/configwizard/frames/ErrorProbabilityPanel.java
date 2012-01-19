package at.ainf.protegeview.configwizard.frames;

import at.ainf.protegeview.WorkspaceTab;
import at.ainf.protegeview.configwizard.AbstractPanel;
import at.ainf.protegeview.controlpanel.*;
import org.protege.editor.owl.OWLEditorKit;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 19.07.11
 * Time: 13:57
 * To change this template use File | Settings | File Templates.
 */
public class ErrorProbabilityPanel extends AbstractPanel {
    public static final String ID = ErrorProbabilityPanel.class.getName();



    private JTable probabTable;

    public ErrorProbabilityPanel(OWLEditorKit editorKit, int number) {

        super(ID, "Error Probabilites", editorKit, number);
    }


    protected void createUI(JComponent parent) {

        setBackgroundImage();


        setInstructions("Here you can give for each keyword in the ontology a value describing the probability it causes an error.");

        //((ProbabilityTableModel) probabTable.getModel()).loadFromOntology();
        OptionsDialog.getDialog().getProbabTableModel().loadFromCalcMap();
        probabTable = new JTable(OptionsDialog.getDialog().getProbabTableModel());
        //probabTable.setPreferredScrollableViewportSize(new Dimension(160, 180));
        probabTable.setRowHeight(22);
        probabTable.setFillsViewportHeight(true);
        probabTable.getColumnModel().getColumn(1).setCellEditor(new ProbabilityTableCellEditor());
        probabTable.getColumnModel().getColumn(0).setCellRenderer(new KeywordFormatRenderer(
          ((WorkspaceTab) getOWLEditorKit().getOWLWorkspace().getWorkspaceTab("at.ainf.protegeview.WorkspaceTab"))));
        probabTable.getColumnModel().getColumn(1).setCellRenderer(new PrFormatRenderer());
        parent.add(new JScrollPane (probabTable));

    }

    public Object getNextPanelDescriptor() {
        return NumLeadingDiags.ID;
    }


    public Object getBackPanelDescriptor() {
        return ChooseSearchType.ID;
    }
}
