package at.ainf.protegeview.gui.tcaeview;

import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.protegeview.gui.AbstractAxiomList;
import at.ainf.protegeview.gui.axiomsetviews.axiomslist.AxiomListItem;
import at.ainf.protegeview.model.EditorKitHook;
import at.ainf.protegeview.model.OntologyDiagnosisSearcher;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.*;

import static at.ainf.protegeview.model.OntologyDiagnosisSearcher.TestCaseType;
import static at.ainf.protegeview.model.OntologyDiagnosisSearcher.TestCaseType.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 05.09.12
 * Time: 16:55
 * To change this template use File | Settings | File Templates.
 */
public class TcaeAxiomList extends AbstractAxiomList {

    private EditorKitHook editorKitHook;

    private OWLEditorKit editorKit;

    public TcaeAxiomList(OWLEditorKit editorKit, EditorKitHook editorKitHook) {
        super(editorKit);
        this.editorKitHook = editorKitHook;
        this.editorKit = editorKit;
        //setCellRenderer(new BasicAxiomListItemRenderer(editorKit));
        setupKeyboardHandlers();
        updateView();

    }

    private void setupKeyboardHandlers() {
        InputMap im = getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE_SEL");
        am.put("DELETE_SEL", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                handleDelete();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ADD");
        am.put("ADD", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                handleEdit();
            }
        });
    }

    public EditorKitHook getEditorKitHook() {
        return editorKitHook;
    }

    public OWLEditorKit getEditorKit() {
        return editorKit;
    }

    @Override
    protected void handleAdd() {
        super.handleAdd();
        if (this.getSelectedValue() instanceof TcaeListHeader) {
            TcaeHeaderEditor editor = new TcaeHeaderEditor((TcaeListHeader) getSelectedValue(),getEditorKit(),getEditorKitHook());
            editor.show();
        }
    }

    @Override
    protected void handleEdit() {
        super.handleEdit();
        if (this.getSelectedValue() instanceof TcaeListItem) {
            TcaeItemEditor editor = new TcaeItemEditor((TcaeListItem) getSelectedValue(),getEditorKit(),getEditorKitHook());
            editor.show();
        }
    }

    @Override
    protected void handleDelete() {
        super.handleDelete();
        if (this.getSelectedValue() instanceof TcaeListItem) {
            for (int number : getSelectedIndices()) {
                TcaeListItem item = (TcaeListItem) getModel().getElementAt(number);

                getEditorKitHook().getActiveOntologyDiagnosisSearcher().doRemoveTestcase(item.getTestcase(),item.getType());

            }
        }
    }

    public void updateView() {
        OntologyDiagnosisSearcher diagnosisSearcher = getEditorKitHook().getActiveOntologyDiagnosisSearcher();
        OWLTheory theory = (OWLTheory) diagnosisSearcher.getSearchCreator().getSearch().getSearchable();

        List<Object> items = new ArrayList<Object>();
        addToItems(items, POSITIVE_TC, theory.getPositiveTests());
        addToItems(items, NEGATIVE_TC, theory.getNegativeTests());
        addToItems(items, ENTAILED_TC, theory.getEntailedTests());
        addToItems(items, NON_ENTAILED_TC, theory.getNonentailedTests());

        setListData(items.toArray());
        setFixedCellHeight(24);
    }

    protected void addToItems(List<Object> items, TestCaseType type, Collection<Set<OWLLogicalAxiom>> testcases) {
        OWLOntology ontology = getEditorKit().getModelManager().getActiveOntology();

        items.add(new TcaeListHeader(type));
        for (Set<OWLLogicalAxiom> testcase : testcases) {
            items.add(new TcaeListItem(testcase,type));
            for (OWLLogicalAxiom axiom : testcase)
                items.add(new AxiomListItem(axiom,ontology));
            items.add(" ");
        }

    }

}
