package at.ainf.protegeview.gui.options;

import at.ainf.protegeview.model.configuration.SearchConfiguration;

import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.09.12
 * Time: 09:50
 * To change this template use File | Settings | File Templates.
 */
public class QueryOptPanel extends AbstractOptPanel {

    private JSpinner partitioningThresholdField = new JSpinner(new SpinnerNumberModel(0.75, 0, 1, 0.01));

    private JComboBox<SearchConfiguration.QSS> scoringFunction = new JComboBox<SearchConfiguration.QSS>();

    private JCheckBox minimizeQuery_Checkbox = new JCheckBox("minimize query ", true);


    private JCheckBox incSubClassOf = new JCheckBox("include SubClassOf axioms in entailments", false);

    private JCheckBox incClassAssert = new JCheckBox("include ClassAssertion axioms in entailments", false);

    private JCheckBox incEquivClass = new JCheckBox("include EquivalentClass axioms in entailments", false);

    private JCheckBox incDisjClass = new JCheckBox("include DisjointClass axioms in entailments", false);

    private JCheckBox incProperty = new JCheckBox("include PropertyOf axioms in entailments", false);

    private JCheckBox incOntologyAxioms = new JCheckBox("include ontology axioms", true);

    private JCheckBox incRefThingAxioms = new JCheckBox("include axioms referencing top", false);


    public QueryOptPanel(SearchConfiguration configuration, SearchConfiguration newConfiguration) {
        super(configuration,newConfiguration);

        for (SearchConfiguration.QSS type : SearchConfiguration.QSS.values())
            scoringFunction.addItem(type);

        loadConfiguration();
        createPanel();
    }

    protected void createPanel() {
        setLayout(new GridLayout(10, 1));
        add(minimizeQuery_Checkbox);
        add(createPane("QSS: ", scoringFunction));
        add(createPane("EntCalcThreshold: ", partitioningThresholdField));
        add(incOntologyAxioms);
        add(incRefThingAxioms);
        add(incSubClassOf);
        add(incClassAssert);
        add(incEquivClass);
        add(incDisjClass);
        add(incProperty);

    }

    protected JPanel createPane(String option, JComponent component) {
        JPanel panel = new JPanel(new GridLayout(1,2));
        panel.add(new JLabel(option));
        panel.add(component);
        return panel;
    }

    protected void loadConfiguration() {
        minimizeQuery_Checkbox.setSelected(getConfiguration().minimizeQuery);
        scoringFunction.setSelectedItem(getConfiguration().qss);
        partitioningThresholdField.setValue(getConfiguration().entailmentCalThres);
        incRefThingAxioms.setSelected(getConfiguration().incAxiomsRefThing);
        incOntologyAxioms.setSelected(getConfiguration().incOntolAxioms);
        incSubClassOf.setSelected(getConfiguration().inclEntSubClass);
        incClassAssert.setSelected(getConfiguration().incEntClassAssert);
        incEquivClass.setSelected(getConfiguration().incEntEquivClass);
        incDisjClass.setSelected(getConfiguration().incEntDisjClasses);
        incProperty.setSelected(getConfiguration().incEntPropAssert);
    }

    @Override
    public void saveChanges() {

        getNewConfiguration().minimizeQuery = minimizeQuery_Checkbox.isSelected();
        getNewConfiguration().qss = (SearchConfiguration.QSS) scoringFunction.getSelectedItem();
        getNewConfiguration().entailmentCalThres = (Double) partitioningThresholdField.getValue();
        getNewConfiguration().incAxiomsRefThing = incRefThingAxioms.isSelected();
        getNewConfiguration().incOntolAxioms = incOntologyAxioms.isSelected();
        getNewConfiguration().inclEntSubClass = incSubClassOf.isSelected();
        getNewConfiguration().incEntClassAssert = incClassAssert.isSelected();
        getNewConfiguration().incEntEquivClass = incEquivClass.isSelected();
        getNewConfiguration().incEntDisjClasses = incDisjClass.isSelected();
        getNewConfiguration().incEntPropAssert = incProperty.isSelected();
    }

}
