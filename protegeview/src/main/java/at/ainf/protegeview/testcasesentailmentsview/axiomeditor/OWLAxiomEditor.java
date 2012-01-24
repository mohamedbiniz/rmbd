package at.ainf.protegeview.testcasesentailmentsview.axiomeditor;

import at.ainf.protegeview.testcasesentailmentsview.SectionType;
import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.editor.core.ui.util.InputVerificationStatusChangedListener;
import org.protege.editor.core.ui.util.VerifiedInputEditor;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.AbstractOWLObjectEditor;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.*;

//import org.protege.axiomeditor.owl.ui.axiomeditor.OWLClassExpressionEditor;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 17.02.11
 * Time: 15:14
 * To change this template use File | Settings | File Templates.
 */
public class OWLAxiomEditor extends AbstractOWLObjectEditor<Set<OWLLogicalAxiom>>  //<OWLClassExpression>
        implements VerifiedInputEditor {
    public static final String PREFERRED_CLASS_EXPRESSION_EDITOR = "preferred.class.expression.axiomeditor";

    private JComponent editingComponent;

    private JTabbedPane tabbedPane;

    private java.util.List<OWLAxiomEditorPanel> activeEditors = new ArrayList<OWLAxiomEditorPanel>();

    private Set<OWLAxiomEditorPanel> editors = new TreeSet<OWLAxiomEditorPanel>();

    private boolean currentStatus = false;

    private Set<InputVerificationStatusChangedListener> listeners = new HashSet<InputVerificationStatusChangedListener>();

    private ChangeListener changeListener = new ChangeListener() {
        public void stateChanged(ChangeEvent changeEvent) {
            handleVerifyEditorContents();
        }
    };

    private Collection<OWLLogicalAxiom> expression;
    //private OWLClassExpression expression;

    private InputVerificationStatusChangedListener inputListener = new InputVerificationStatusChangedListener() {
        public void verifiedStatusChanged(boolean newState) {
            handleVerifyEditorContents();
        }
    };

    OWLEditorKit editorKit;

    private Preferences preferences = PreferencesManager.getInstance().getApplicationPreferences(OWLAxiomEditor.class);

    public OWLAxiomEditor(OWLEditorKit editorKit, Collection<OWLLogicalAxiom> expression, SectionType type) {
        //public OWLAxiomEditor(OWLEditorKit editorKit, OWLClassExpression expression) {

        this.editorKit = editorKit;
        //System.out.println("my owl axiom axiomeditor ");
        this.expression = expression;

        editingComponent = new JPanel(new BorderLayout());

        tabbedPane = new JTabbedPane();
        tabbedPane.setFocusable(false);

        editingComponent.add(tabbedPane);

        editingComponent.setPreferredSize(new Dimension(600, 400));

        tabbedPane.addChangeListener(changeListener);
        this.type = type;
    }
    private SectionType type;

    public String getSectionType() {

        return  type.getLabel();

    }

    public void addPanel(OWLAxiomEditorPanel editorPanel) {
        editors.add(editorPanel);

        if (editorPanel.setDescription(expression)) {
            activeEditors.add(editorPanel);
            tabbedPane.add(editorPanel.getEditorName(), editorPanel.getComponent());
            editorPanel.addStatusChangedListener(inputListener);
            tabbedPane.setSelectedIndex(0);
        }
    }

    public void addPanel() {

        OWLAxiomEditorPanel editorPan = new OWLAxiomEditorPanel();


        try {
            editorPan.setup("id", "LogicalAxiom Editor", editorKit);
            editorPan.initialise();
        } catch (Exception exception) {
            System.err.println("Problem initializing  ");
        }

        addPanel(editorPan);
    }

    private void handleVerifyEditorContents() {
        boolean newStatus = isValidated();
        currentStatus = newStatus;
        for (InputVerificationStatusChangedListener l : listeners) {
            l.verifiedStatusChanged(newStatus);
        }
    }


    private boolean isValidated() {
        OWLAxiomEditorPanel editor = getSelectedEditor();
        return editor.isValidInput();
    }


    public String getEditorTypeName() {
        return "Class expression";
    }


    public boolean canEdit(Object object) {
        return object instanceof OWLLogicalAxiom; // OWLClassExpression;
    }


    /**
     * Gets a component that will be used to edit the specified
     * object.
     *
     * @return The component that will be used to edit the object
     */
    public JComponent getEditorComponent() {
        return editingComponent;
    }


    public boolean setEditedObject(Set<OWLLogicalAxiom> expression) {
        //public boolean setEditedObject(OWLClassExpression expression) {
        this.expression = expression;

        activeEditors.clear();
        tabbedPane.removeChangeListener(changeListener);
        tabbedPane.removeAll();

        for (OWLAxiomEditorPanel editor : editors) {
            if (editor.setDescription(this.expression)) {
                activeEditors.add(editor);
                tabbedPane.add(editor.getEditorName(), editor.getComponent());
                editor.addStatusChangedListener(inputListener);
            }
        }

        tabbedPane.validate();
        tabbedPane.addChangeListener(changeListener);

        selectPreferredEditor();

        return !activeEditors.isEmpty(); // then no editors are appropriate for this expression
    }


    public Set<OWLLogicalAxiom> getEditedObject() {
        setSelectedEditorPreferred();
        Collection<Set<OWLLogicalAxiom>> sel = getSelectedEditor().getClassExpressions(); //OWLClassExpression
        if (sel.isEmpty()) {
            return null;
        } else {
            return sel.iterator().next();
        }
    }

    public Set<Set<OWLLogicalAxiom>> getEditedObjects() {
        //    public Set<OWLClassExpression> getEditedObjects() {
        setSelectedEditorPreferred();
        return getSelectedEditor().getClassExpressions();
    }

    private OWLAxiomEditorPanel getSelectedEditor() {
        return activeEditors.get(tabbedPane.getSelectedIndex());
    }


    public void setSelectedEditor(OWLAxiomEditorPanel editor) {
        int index = activeEditors.indexOf(editor);
        if (index >= 0) {
            tabbedPane.setSelectedIndex(index);
        }
    }

    private void setSelectedEditorPreferred() {
        OWLAxiomEditorPanel editor = getSelectedEditor();
        preferences.putString(PREFERRED_CLASS_EXPRESSION_EDITOR, editor.getClass().getCanonicalName());
    }

    public void selectPreferredEditor() {
        String preferredEditor = preferences.getString(PREFERRED_CLASS_EXPRESSION_EDITOR, null);
        if (preferredEditor != null) {
            int index = 0;
            for (OWLAxiomEditorPanel editor : activeEditors) {
                if (preferredEditor.equals(editor.getClass().getCanonicalName())) {
                    tabbedPane.setSelectedIndex(index);
                    break;
                }
                index++;
            }
        }

    }


    public void addStatusChangedListener(InputVerificationStatusChangedListener l) {
        listeners.add(l);
        l.verifiedStatusChanged(isValidated());
    }


    public void removeStatusChangedListener(InputVerificationStatusChangedListener l) {
        listeners.remove(l);
    }


    public void dispose() {
        for (OWLAxiomEditorPanel editor : editors) {
            try {
                editor.dispose();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}