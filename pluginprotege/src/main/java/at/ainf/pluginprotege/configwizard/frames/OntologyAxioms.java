package at.ainf.pluginprotege.configwizard.frames;

import at.ainf.pluginprotege.configwizard.AbstractPanel;
import org.protege.editor.owl.OWLEditorKit;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 20.07.11
 * Time: 12:55
 * To change this template use File | Settings | File Templates.
 */
public class OntologyAxioms  extends AbstractPanel {
    public static final String ID = OntologyAxioms.class.getName();

    private JCheckBox ontologyAxioms;

    private JCheckBox trivialAxioms;


    public OntologyAxioms(OWLEditorKit editorKit, int number) {
        super(ID, "Ontology Axioms  ", editorKit, number);
    }


    protected void createUI(JComponent parent) {

        setBackgroundImage();
        setInstructions("Should axioms already in the ontology or axioms referencing thing be considered in queries?");
        ontologyAxioms = new JCheckBox("include ontology axioms", true);
        ontologyAxioms.setEnabled(false);

        trivialAxioms = new JCheckBox("include axioms referencing owl:thing", false);

        parent.setLayout(new BoxLayout(parent,BoxLayout.Y_AXIS));
        parent.add(ontologyAxioms);
        parent.add(trivialAxioms);


    }

    public boolean isOntologyAxiomsSelected() {
        return ontologyAxioms.isSelected();
    }

    public boolean isTrivialAxSelected() {
        return trivialAxioms.isSelected();
    }

    public Object getNextPanelDescriptor() {
        return FinishPanel.ID;
    }


    public Object getBackPanelDescriptor() {
        return IncludeEntailments.ID;
    }
}
