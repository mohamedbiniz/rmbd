package at.ainf.protegeview.gui.backgroundview;

import at.ainf.protegeview.gui.tcaeview.AbstractEditorTCaE;
import at.ainf.protegeview.gui.tcaeview.TcaeListHeader;
import at.ainf.protegeview.model.EditorKitHook;
import at.ainf.protegeview.model.ErrorHandler;
import at.ainf.protegeview.model.OntologyDiagnosisSearcher;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import javax.swing.*;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 25.09.12
 * Time: 09:35
 * To change this template use File | Settings | File Templates.
 */
public class CreateAxiomEditor extends AbstractEditorTCaE {

    protected String getEditorTitle() {
        return "Create new Background Axiom";
    }

    public CreateAxiomEditor(EditorKitHook editorKitHook) {
        super(editorKitHook.getOWLEditorKit(), editorKitHook);
    }

    protected void handleEditorConfirmed(Set<OWLLogicalAxiom> axioms) {
        OntologyDiagnosisSearcher diagnosisSearcher = getEditorKitHook().getActiveOntologyDiagnosisSearcher();

        diagnosisSearcher.doAddBackgroundAxioms(axioms,new ErrorHandler() {
            @Override
            public void errorHappend(OntologyDiagnosisSearcher.ErrorStatus error) {
                JOptionPane.showMessageDialog(null, "This background axioms would not be compatible with axioms already in background", "Inconsistent Theory Exception", JOptionPane.ERROR_MESSAGE);
            }
        });

    }

}
