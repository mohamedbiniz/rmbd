package at.ainf.protegeview.testcasesentailmentsview.axiomeditor;

import org.protege.editor.core.ui.util.InputVerificationStatusChangedListener;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.clsdescriptioneditor.ExpressionEditor;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionChecker;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 18.02.11
 * Time: 11:44
 * To change this template use File | Settings | File Templates.
 */
public class OWLAxiomEditorPanel {


    private ExpressionEditor<Collection<OWLLogicalAxiom>> editor;


    private JPanel pan;

    public void initialise() throws Exception {
        // final OWLEditorKit eKit = getOWLEditorKit();

        pan = new JPanel(new BorderLayout());
        pan.add(new CreateOWLEntitiesToolBar(eKit), BorderLayout.NORTH);

        final OWLExpressionChecker<Collection<OWLLogicalAxiom>> checker = new OWLAxiomChecker(eKit.getModelManager());
        editor = new ExpressionEditor<Collection<OWLLogicalAxiom>>(eKit, checker);
        JScrollPane scroller = new JScrollPane(editor);
        pan.add(scroller, BorderLayout.CENTER);


    }


    public JComponent getComponent() {
        return pan;
    }


    public boolean isValidInput() {
        return editor.isWellFormed();
    }


    public boolean setDescription(Collection<OWLLogicalAxiom> description) {
        editor.setExpressionObject(description);
        return true;
    }


    public Set<Set<OWLLogicalAxiom>> getClassExpressions() {
        try {
            if (editor.isWellFormed()) {
                //OWLClassExpression owlDescription = axiomeditor.createObject();
                Set<OWLLogicalAxiom> owlDescription = new TreeSet<OWLLogicalAxiom>(editor.createObject());
                //OWLExpressionUserCache.getInstance(getOWLEditorKit().getModelManager()).add(owlDescription, axiomeditor.getText()); !!!


                return Collections.singleton(owlDescription);
            } else {
                return null;
            }
        } catch (OWLException e) {
            return null;
        }
    }


    public void addStatusChangedListener(InputVerificationStatusChangedListener l) {
        editor.addStatusChangedListener(l);
    }


    public void removeStatusChangedListener(InputVerificationStatusChangedListener l) {
        editor.removeStatusChangedListener(l);
    }

    //****

    private OWLEditorKit eKit;

    private String label;

    private AxiomType type = null;


    public final void setup(String id, String name, OWLEditorKit eKit) {
        this.eKit = eKit;
        this.label = name;
    }


    @SuppressWarnings("unchecked")
    public final void setAxiomType(AxiomType type) {
        this.type = type;
    }


    public final String getEditorName() {
        return label;
    }


    protected final OWLEditorKit getOWLEditorKit() {
        return eKit;
    }


    protected final AxiomType getAxiomType() {
        return type;
    }

    //****


    public void dispose() throws Exception {
        // surely ExpressionEditor should be disposable?
    }
}
