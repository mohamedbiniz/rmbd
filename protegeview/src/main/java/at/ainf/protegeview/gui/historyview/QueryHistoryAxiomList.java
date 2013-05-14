package at.ainf.protegeview.gui.historyview;

import at.ainf.protegeview.gui.AbstractAxiomList;
import at.ainf.protegeview.gui.axiomsetviews.axiomslist.AxiomListItem;
import at.ainf.protegeview.gui.tcaeview.TcaeListHeader;
import at.ainf.protegeview.gui.tcaeview.TcaeListItem;
import at.ainf.protegeview.model.EditorKitHook;
import at.ainf.protegeview.model.OntologyDiagnosisSearcher;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 05.11.12
 * Time: 11:17
 * To change this template use File | Settings | File Templates.
 */
public class QueryHistoryAxiomList extends AbstractAxiomList {

    private EditorKitHook editorKitHook;

    private OWLEditorKit editorKit;

    public QueryHistoryAxiomList(OWLEditorKit editorKit, EditorKitHook editorKitHook) {
        super(editorKit);
        this.editorKitHook = editorKitHook;
        this.editorKit = editorKit;

        updateView();

    }



    public EditorKitHook getEditorKitHook() {
        return editorKitHook;
    }

    public OWLEditorKit getEditorKit() {
        return editorKit;
    }

    @Override
    protected void handleDelete() {
        super.handleDelete();
        if (this.getSelectedValue() instanceof QueryHistoryItem) {
            for (int number : getSelectedIndices()) {
                QueryHistoryItem item = (QueryHistoryItem) getModel().getElementAt(number);

                getEditorKitHook().getActiveOntologyDiagnosisSearcher().doRemoveQueryHistoryTestcase(item.getTestcase(),item.getType());

            }
        }
    }


    public void updateView() {
        OntologyDiagnosisSearcher ods = getEditorKitHook().getActiveOntologyDiagnosisSearcher();
        List<Set<OWLLogicalAxiom>> queryHistory = ods.getQueryHistory();
        Map<Set<OWLLogicalAxiom>,OntologyDiagnosisSearcher.TestCaseType> queryMap = ods.getQueryHistoryType();

        OWLOntology ontology = getEditorKit().getModelManager().getActiveOntology();

        List<Object> items = new LinkedList<Object>();

        for (int i = queryHistory.size() - 1; i >= 0; i--) {
            Set<OWLLogicalAxiom> testcase = queryHistory.get(i);
            items.add(new QueryHistoryItem(testcase,queryMap.get(testcase),i+1));
            for (OWLLogicalAxiom axiom : testcase)
                items.add(new AxiomListItem(axiom,ontology));
            items.add(" ");
        }

        setListData(items.toArray());

    }


}
