package at.ainf.protegeview.gui.options;

import at.ainf.protegeview.model.configuration.SearchConfiguration;

import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.09.12
 * Time: 09:49
 * To change this template use File | Settings | File Templates.
 */
public class DiagnosisOptPanel extends AbstractOptPanel {

    public static final int maxLeadingDiags = 18;

    private JCheckBox test_Tbox_Checkbox = new JCheckBox("include TBox in Background Knowledge", false);

    private JCheckBox test_Abox_Checkbox = new JCheckBox("include ABox in Background Knowledge", true);

    private JSpinner numofLeadingDiagsField = new JSpinner(new SpinnerNumberModel(9, 1, maxLeadingDiags + 1, 1));

    private JCheckBox test_incoherency_inconsistency_Checkbox = new JCheckBox("reduce incoherency to inconsistency ", false);

    private JCheckBox calcAllDiags_checkbox = new JCheckBox("calc all diagnoses ", true);

    private JComboBox<SearchConfiguration.TreeType> treeType = new JComboBox<SearchConfiguration.TreeType>();

    private JComboBox<SearchConfiguration.SearchType> searchType = new JComboBox<SearchConfiguration.SearchType>();

    public DiagnosisOptPanel(SearchConfiguration configuration, SearchConfiguration newConfiguration) {
        super(configuration,newConfiguration);

        for (SearchConfiguration.SearchType type : SearchConfiguration.SearchType.values())
            searchType.addItem(type);
        for (SearchConfiguration.TreeType type : SearchConfiguration.TreeType.values())
            treeType.addItem(type);

        loadConfiguration();
        createPanel();

    }

    protected void createPanel() {
        setLayout(new GridLayout(7, 1));
        add(test_Abox_Checkbox);
        add(test_Tbox_Checkbox);


        add(createPane("Search Type: ", searchType));
        add(createPane("Tree Type: ", treeType));
        add(createPane("NumOfLeadingDiag: ", numofLeadingDiagsField));

        add(calcAllDiags_checkbox);
        add(test_incoherency_inconsistency_Checkbox);

    }

    protected JPanel createPane(String option, JComponent component) {
        JPanel panel = new JPanel(new GridLayout(1,2));
        panel.add(new JLabel(option));
        panel.add(component);
        return panel;
    }

    protected void loadConfiguration() {
        test_Tbox_Checkbox.setSelected(getConfiguration().tBoxInBG);
        test_Abox_Checkbox.setSelected(getConfiguration().aBoxInBG);

        test_incoherency_inconsistency_Checkbox.setSelected(getConfiguration().reduceIncoherency);
        calcAllDiags_checkbox.setSelected(getConfiguration().calcAllDiags);

        numofLeadingDiagsField.setValue(getConfiguration().numOfLeadingDiags);
        searchType.setSelectedItem(getConfiguration().searchType);
        treeType.setSelectedItem(getConfiguration().treeType);
    }

    @Override
    public void saveChanges() {

        getNewConfiguration().tBoxInBG = test_Tbox_Checkbox.isSelected();
        getNewConfiguration().aBoxInBG = test_Abox_Checkbox.isSelected();

        getNewConfiguration().reduceIncoherency = test_incoherency_inconsistency_Checkbox.isSelected();
        getNewConfiguration().calcAllDiags = calcAllDiags_checkbox.isSelected();

        getNewConfiguration().numOfLeadingDiags = (Integer) numofLeadingDiagsField.getValue();
        getNewConfiguration().searchType = (SearchConfiguration.SearchType) searchType.getSelectedItem();
        getNewConfiguration().treeType = (SearchConfiguration.TreeType) treeType.getSelectedItem();

    }

}
