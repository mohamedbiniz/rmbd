package at.ainf.protegeview.configwizard.frames;

import at.ainf.protegeview.configwizard.AbstractPanel;
import org.protege.editor.owl.OWLEditorKit;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 20.07.11
 * Time: 12:53
 * To change this template use File | Settings | File Templates.
 */
public class IncludeEntailments  extends AbstractPanel {
    public static final String ID = IncludeEntailments.class.getName();

    private JCheckBox inferredSubClassAxiomCheckbox;
    private JCheckBox inferredClassAssertionAxiomCheckbox;
    
    private JCheckBox inferredEquivalentClassAxiomGenerator;
    private JCheckBox inferredDisjointClassesAxiomGenerator;
    private JCheckBox inferredPropertyAssertionGenerator;


    public IncludeEntailments(OWLEditorKit editorKit, int number) {
        super(ID, "Include Entailments  ", editorKit, number);
    }


    protected void createUI(JComponent parent) {

        setBackgroundImage();
        setInstructions("Please specify which entailments have to be calculated. If you include more types you get more queries and you can exclude more diagnoses ");

         inferredSubClassAxiomCheckbox = new JCheckBox("SubClass", true);
     inferredClassAssertionAxiomCheckbox = new JCheckBox("ClassAssertion", true);
    
     inferredEquivalentClassAxiomGenerator = new JCheckBox("EquivalentClass", false);
     inferredDisjointClassesAxiomGenerator = new JCheckBox("DisjointClasses", false);
     inferredPropertyAssertionGenerator = new JCheckBox("PropertyAssertion", false);

        parent.setLayout(new BoxLayout(parent,BoxLayout.Y_AXIS));
        parent.add(inferredSubClassAxiomCheckbox);
        parent.add(inferredClassAssertionAxiomCheckbox);
        parent.add(inferredEquivalentClassAxiomGenerator);
        parent.add(inferredDisjointClassesAxiomGenerator);
        parent.add(inferredPropertyAssertionGenerator);


    }

    public boolean isSubClassAxSelected() {
         return inferredSubClassAxiomCheckbox.isSelected();
    }

    public boolean isClassAssertionAxSelected() {
         return inferredClassAssertionAxiomCheckbox.isSelected();
    }

    public boolean isEquivalentClassAxSelected() {
         return inferredEquivalentClassAxiomGenerator.isSelected();
    }

    public boolean isDisjointClassesAxSelected() {
         return inferredDisjointClassesAxiomGenerator.isSelected();
    }

    public boolean isPropertyAssertionAxSelected() {
         return inferredPropertyAssertionGenerator.isSelected();
    }


    public Object getNextPanelDescriptor() {
        return OntologyAxioms.ID;
    }


    public Object getBackPanelDescriptor() {
        return ReduceIncoherencyIncon.ID;
    }
}
