package at.ainf.protegeview.controlpanel;

import at.ainf.protegeview.WorkspaceTab;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 07.07.11
 * Time: 08:54
 * To change this template use File | Settings | File Templates.
 */
public class OptionsDialog extends JPanel {


    final int maxLeadingDiags = 14;

    private JTabbedPane tabbedPane;

    private WorkspaceTab workspaceTab;

    private static OptionsDialog optionsDlg = null;

    private JCheckBox test_Tbox_Checkbox = new JCheckBox("include TBox in Background Knowledge", false);

    private JCheckBox test_Abox_Checkbox = new JCheckBox("include ABox in Background Knowledge", true);

    private JRadioButton uniformCostSearch = new JRadioButton("Uniform Cost Search");

    private JRadioButton breathFirstSearch = new JRadioButton("Breath First Search");

    private ButtonGroup searchTypeButtonGroup = new ButtonGroup();

    private JSpinner numofLeadingDiagsField = new JSpinner(new SpinnerNumberModel(9, 1, maxLeadingDiags + 1, 1));

    private JSpinner partitioningThresholdField = new JSpinner(new SpinnerNumberModel(0.75, 0, 1, 0.01));

    private JComboBox scoringFunction = new JComboBox();

    private JCheckBox test_incoherency_inconsistency_Checkbox = new JCheckBox("reduce incoherency to inconsistency ", false);

    private JCheckBox calcAllDiags_checkbox = new JCheckBox("calc all diagnoses ", true);

    private ConfEntailmentPanel confEntailmentPanel = new ConfEntailmentPanel();

    private ConfSpecialEntailmentPanel confSpecialEntailmentPanel = new ConfSpecialEntailmentPanel();

    private JTable probabTable;

    private JCheckBox minimizeQuery_Checkbox = new JCheckBox("minimize query ", true);

    public WorkspaceTab getWorkspaceTab() {
        return workspaceTab;
    }

    public static OptionsDialog getDialog() {
        return optionsDlg;
    }

    private JPanel createGeneralOptsPanel() {
        JPanel result = new JPanel();

        result.setLayout(new GridLayout(4, 1));
        JPanel abo = new JPanel(new GridLayout(1, 2));
        JPanel panelBox = new JPanel();
        panelBox.setLayout(new BoxLayout(panelBox, BoxLayout.Y_AXIS));
        panelBox.add(test_Abox_Checkbox);
        panelBox.add(test_Tbox_Checkbox);
        panelBox.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
                Color.LIGHT_GRAY), "Background Knowledge "), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        abo.add(panelBox);

        JPanel searchGroup = new JPanel();
        uniformCostSearch.setActionCommand("BestFirst");
        breathFirstSearch.setActionCommand("BreadthFirst");
        searchTypeButtonGroup.add(breathFirstSearch);
        searchTypeButtonGroup.add(uniformCostSearch);
        searchGroup.setLayout(new GridLayout(0, 1));
        searchGroup.add(breathFirstSearch);
        searchGroup.add(uniformCostSearch);
        searchGroup.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
                Color.LIGHT_GRAY), "Search Type "), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        abo.add(searchGroup);

        uniformCostSearch.setSelected(true);

        JPanel numOfLeadingDiagsPanel = new JPanel();
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
        numOfLeadingDiagsPanel.add(numofLeadingDiagsField);
        numOfLeadingDiagsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
                Color.LIGHT_GRAY), "Number of Leading Diagnoses"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));

        JPanel test_incoherency_inconsistency_Checkbox_Panel = new JPanel();
        JPanel panelThreshold = new JPanel();
        FlowLayout flowLayout = new FlowLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        panelThreshold.setLayout(flowLayout);
        panelThreshold.add(new JLabel("threshold"));
        partitioningThresholdField.setPreferredSize(new Dimension(45, 25));
        panelThreshold.add(partitioningThresholdField);

        JPanel panelDiag = new JPanel();
        flowLayout = new FlowLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        panelDiag.setLayout(flowLayout);
        panelDiag.add(new JLabel("num of leading diags"));
        panelDiag.add(numofLeadingDiagsField);
        JPanel scorFunc = new JPanel();
        FlowLayout flowLayout2 = new FlowLayout();
        scorFunc.setLayout(flowLayout2);
        flowLayout2.setAlignment(FlowLayout.LEFT);
        scorFunc.add(new JLabel("scoring function "));
        scorFunc.add(scoringFunction);
        JPanel panelDgFull = new JPanel();
        panelDgFull.setLayout(new BoxLayout(panelDgFull, BoxLayout.Y_AXIS));
        panelDgFull.add(panelDiag);
        panelDgFull.add(scorFunc);

        JPanel incoherencyPanel = new JPanel();
        incoherencyPanel.setLayout(new BoxLayout(incoherencyPanel, BoxLayout.Y_AXIS));
        incoherencyPanel.add(test_incoherency_inconsistency_Checkbox);
        incoherencyPanel.add(minimizeQuery_Checkbox);
        incoherencyPanel.add(calcAllDiags_checkbox);
        calcAllDiags_checkbox.setFont(calcAllDiags_checkbox.getFont().deriveFont(Font.BOLD));
        calcAllDiags_checkbox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                /*if (e.getStateChange() == ItemEvent.SELECTED) {
                    numofLeadingDiagsField.setEnabled(false);
                }
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    numofLeadingDiagsField.setEnabled(true);
                }*/
            }
        });
        test_incoherency_inconsistency_Checkbox_Panel.setLayout(new GridLayout(1, 2));
        test_incoherency_inconsistency_Checkbox_Panel.add(panelDgFull);
        test_incoherency_inconsistency_Checkbox_Panel.add(incoherencyPanel);
        test_incoherency_inconsistency_Checkbox_Panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
                Color.LIGHT_GRAY), "Query Session  "), BorderFactory.createEmptyBorder(3, 3, 3, 3)));

        result.add(abo);
        result.add(test_incoherency_inconsistency_Checkbox_Panel);
        confEntailmentPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
                Color.LIGHT_GRAY), "Entailments"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        result.add(confEntailmentPanel);
        confSpecialEntailmentPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
                Color.LIGHT_GRAY), "More Entailments"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        JPanel d = new JPanel();
        d.setLayout(new BoxLayout(d, BoxLayout.X_AXIS));
        d.add(confSpecialEntailmentPanel);
        panelThreshold.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
                Color.LIGHT_GRAY), "Entailment Calculation"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        d.add(panelThreshold);
        result.add(d);


        return result;
    }

    public OptionsDialog(WorkspaceTab workspaceTab) {


        this.workspaceTab = workspaceTab;
        probabTable = new JTable(new ProbabilityTableModel(workspaceTab));

        tabbedPane = new JTabbedPane();

        tabbedPane.addTab("General", createGeneralOptsPanel());
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_G);
        tabbedPane.addTab("Probabilities", createProbabilitesPanel());
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_P);

        workspaceTab.setCalcMap(((ProbabilityTableModel) probabTable.getModel()).getCalcMap());
        add(tabbedPane);
        scoringFunction.addItem("Entropy");
        scoringFunction.addItem("Split");
        scoringFunction.setSelectedIndex(0);
    }

    private Component createProbabilitesPanel() {
        probabTable.setPreferredScrollableViewportSize(new Dimension(160, 180));
        probabTable.setRowHeight(22);
        probabTable.setFillsViewportHeight(true);
        probabTable.getColumnModel().getColumn(1).setCellEditor(new ProbabilityTableCellEditor());
        probabTable.getColumnModel().getColumn(0).setCellRenderer(new KeywordFormatRenderer(workspaceTab));
        probabTable.getColumnModel().getColumn(1).setCellRenderer(new PrFormatRenderer());

        return new JScrollPane(probabTable);  //To change body of created methods use File | Settings | File Templates.
    }

    public static void createOptionsDialog(WorkspaceTab workspaceTab) {
        optionsDlg = new OptionsDialog(workspaceTab);
    }

    protected boolean isOkClicked(Object v) {
        return v != null &&
                (Integer) v == JOptionPane.OK_OPTION;
    }

    public static void showOptionsDialog() {

        optionsDlg.loadPrefs();
        optionsDlg.updateUsedKw();

        JOptionPane op = new JOptionPane(optionsDlg, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dlg = op.createDialog(optionsDlg.getWorkspaceTab(), "Debugging Options ");
        dlg.setResizable(true);
        dlg.setVisible(true);
        Object v = op.getValue();
        if (optionsDlg.isOkClicked(v))
            optionsDlg.applyNewPrefs();
    }

    //private HashMap<ManchesterOWLSyntax,Double> savedProbabMap;

    public void updateUsedKw() {
        ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
        ProbabilityTableModel probabTableModel = ((ProbabilityTableModel) probabTable.getModel());
        //savedProbabMap = (HashMap<ManchesterOWLSyntax, Double>) probabTableModel.getProbMap().clone();
        String axiomStr = "";
        for (OWLLogicalAxiom axiom : workspaceTab.getOWLModelManager().getActiveOntology().getLogicalAxioms()) {
            axiomStr += renderer.render(axiom) + "\t";
        }
        LinkedList<ManchesterOWLSyntax> keywords = new LinkedList<ManchesterOWLSyntax>();
        for (ManchesterOWLSyntax keyword : probabTableModel.getOwlKeywords()) {
            if (axiomStr.contains(keyword.toString()))
                keywords.add(keyword);
        }
        probabTableModel.setListKeywords(keywords);
    }

    public ProbabilityTableModel getProbabTableModel() {
        return (ProbabilityTableModel) probabTable.getModel();
    }

    protected void loadPrefs() {
        test_Abox_Checkbox.setSelected(QueryDebuggerPreference.getInstance().isTestAbox());
        test_Tbox_Checkbox.setSelected(QueryDebuggerPreference.getInstance().isTestTbox());
        setButtonGroup(QueryDebuggerPreference.getInstance().getSearchCommand());
        numofLeadingDiagsField.setValue(QueryDebuggerPreference.getInstance().getNumOfLeadingDiags());
        partitioningThresholdField.setValue(QueryDebuggerPreference.getInstance().getPartitioningThres());
        test_incoherency_inconsistency_Checkbox.setSelected(QueryDebuggerPreference.getInstance().isTestIncoherencyInconsistency());
        calcAllDiags_checkbox.setSelected(QueryDebuggerPreference.getInstance().isCalcAllDiags());
        minimizeQuery_Checkbox.setSelected(QueryDebuggerPreference.getInstance().isQueryMinimizerActive());
        //confEntailmentPanel.setAxiomGenerators(((ConfEntailmentOwlTheory) workspaceTab.getOwlTheory()).getAxiomGenerators());
        confEntailmentPanel.loadGeneratorPreferences();
        ((ProbabilityTableModel) probabTable.getModel()).loadFromOntology();
        ((ProbabilityTableModel) probabTable.getModel()).loadFromCalcMap();
        confSpecialEntailmentPanel.loadGeneratorPreferences();
        String func = QueryDebuggerPreference.getInstance().getScoringFunction();
        for (int i = 0; i < scoringFunction.getItemCount(); i++)
            if (scoringFunction.getItemAt(i).equals(func))
                scoringFunction.setSelectedIndex(i);
        //
    }

    private void setButtonGroup(String searchType) {
        for (Enumeration<AbstractButton> buttons = searchTypeButtonGroup.getElements(); buttons.hasMoreElements(); ) {
            JRadioButton button = ((JRadioButton) buttons.nextElement());
            if (button.getActionCommand().equals(searchType))
                button.setSelected(true);
            else {
                button.setSelected(false);
            }
        }
    }

    protected void applyNewPrefs() {
        QueryDebuggerPreference.getInstance().setTestAbox(test_Abox_Checkbox.isSelected());
        QueryDebuggerPreference.getInstance().setTestTbox(test_Tbox_Checkbox.isSelected());
        QueryDebuggerPreference.getInstance().setSearchCommand(searchTypeButtonGroup.getSelection().getActionCommand());
        QueryDebuggerPreference.getInstance().setPartitioningThres((Double) partitioningThresholdField.getValue());
        QueryDebuggerPreference.getInstance().setNumOfLeadingDiags((Integer) numofLeadingDiagsField.getValue());
        QueryDebuggerPreference.getInstance().setTestIncoherencyToInconsistency(test_incoherency_inconsistency_Checkbox.isSelected());
        QueryDebuggerPreference.getInstance().setCalcAllDiags(calcAllDiags_checkbox.isSelected());
        QueryDebuggerPreference.getInstance().setQueryMinimizerActive(minimizeQuery_Checkbox.isSelected());
        confEntailmentPanel.saveGeneratorPreferences();
        /*List<InferredAxiomGenerator<? extends OWLLogicalAxiom>> axiomGenerators = ((ConfEntailmentOwlTheory) workspaceTab.getOwlTheory()).getAxiomGenerators();
        axiomGenerators.clear();
        for(InferredAxiomGenerator<? extends OWLLogicalAxiom> generator : confEntailmentPanel.generateAxiomGenerator())
            axiomGenerators.add(generator);*/
        getProbabTableModel().updateCalcMapWithMap();
        getProbabTableModel().saveToTheOnt();
        confSpecialEntailmentPanel.saveGeneratorPreferences();

        QueryDebuggerPreference.getInstance().setScoringFunction((String) scoringFunction.getSelectedItem());
        getWorkspaceTab().doResetAfterOpt();


    }

    public String toString() {
        return "ABox=" + test_Abox_Checkbox.isSelected() + "," +
                "TBox=" + test_Tbox_Checkbox.isSelected() + "," +
                "SearchType=" + searchTypeButtonGroup.getSelection().getActionCommand() + "," +
                "NumLeadingDiags=" + numofLeadingDiagsField.getValue() + "," +
                "PartitioningThreshold" + partitioningThresholdField.getValue() + "," +
                "ReduceToIncoherency=" + test_incoherency_inconsistency_Checkbox.isSelected() + "," +
                "CalcAllDiags=" + calcAllDiags_checkbox.isSelected() + "," +
                "MinimizeQueries=" + minimizeQuery_Checkbox.isSelected() + "," +
                "Entailments(" + confEntailmentPanel.toString() + ")," +
                "MoreEntailment(" + confSpecialEntailmentPanel.toString() + ")," +
                "ScoringFunction=" + ((String) scoringFunction.getSelectedItem());
    }

}
