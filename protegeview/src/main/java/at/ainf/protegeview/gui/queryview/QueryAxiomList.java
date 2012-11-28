package at.ainf.protegeview.gui.queryview;

import at.ainf.protegeview.gui.AbstractAxiomList;
import at.ainf.protegeview.gui.axiomsetviews.axiomslist.AxiomListItem;
import at.ainf.protegeview.model.EditorKitHook;
import at.ainf.protegeview.model.OntologyDiagnosisSearcher;
import org.apache.log4j.Logger;
import org.protege.editor.core.ui.list.MListButton;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.explanation.ExplanationManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 06.09.12
 * Time: 19:35
 * To change this template use File | Settings | File Templates.
 */
public class QueryAxiomList extends AbstractAxiomList {

    private static Logger logger = Logger.getLogger(QueryAxiomList.class.getName());

    @Override
    protected List<MListButton> getButtons(Object value) {

        List<MListButton> buttons = new ArrayList<MListButton>();
        OntologyDiagnosisSearcher s = editorKitHook.getActiveOntologyDiagnosisSearcher();
        OWLLogicalAxiom axiom = ((AxiomListItem) value).getAxiom();
        buttons.addAll(super.getButtons(value));
        buttons.add(new AxiomIsEntailedButton(this,s.isMarkedEntailed(axiom)));
        buttons.add(new AxiomIsNotEntailedButton(this,s.isMarkedNonEntailed(axiom)));
        buttons.add(new DebugExplainButton(this));

        return buttons;
    }

    public void handleEntailed() {
        logger.debug("handle entailed");

        OWLLogicalAxiom axiom = ((AxiomListItem) getSelectedValue()).getAxiom()  ;
        OntologyDiagnosisSearcher s = editorKitHook.getActiveOntologyDiagnosisSearcher();
        if (s.isMarkedEntailed(axiom)) {
            s.doRemoveAxiomsMarkedEntailed(axiom);
        }
        else if (s.isMarkedNonEntailed(axiom)) {
            s.doRemoveAxiomsMarkedNonEntailed(axiom);
            s.doAddAxiomsMarkedEntailed(axiom);
        }
        else {
            s.doAddAxiomsMarkedEntailed(axiom);
        }

    }

    public void handleNotEntailed() {
        logger.debug("handle notEntailed");

        OWLLogicalAxiom axiom = ((AxiomListItem) getSelectedValue()).getAxiom()  ;
        OntologyDiagnosisSearcher s = editorKitHook.getActiveOntologyDiagnosisSearcher();
        if (s.isMarkedNonEntailed(axiom)) {
            s.doRemoveAxiomsMarkedNonEntailed(axiom);
        }
        else if (s.isMarkedEntailed(axiom)) {
            s.doRemoveAxiomsMarkedEntailed(axiom);
            s.doAddAxiomsMarkedNonEntailed(axiom);
        }
        else {
            s.doAddAxiomsMarkedNonEntailed(axiom);
        }

    }


    public void handleAxiomExplain() {
        Object obj = getSelectedValue();
        if (!(obj instanceof AxiomListItem))
            return;
        AxiomListItem item = (AxiomListItem) obj;
        OWLAxiom axiom = item.getAxiom();
        ExplanationManager explanationMngr = editorKitHook.getOWLEditorKit().getModelManager().getExplanationManager();
        if (explanationMngr.hasExplanation(axiom)) {
            explanationMngr.handleExplain((Frame) SwingUtilities.getAncestorOfClass(Frame.class, this), axiom);
        }
    }

    public QueryAxiomList(OWLEditorKit editorKit, EditorKitHook editorKitHook) {
        super(editorKit);
        this.editorKitHook = editorKitHook;
        //setCellRenderer(new BasicAxiomListItemRenderer(editorKit));
    }

    private EditorKitHook editorKitHook;

    public void clearList() {

        /*DefaultListModel model = (DefaultListModel) getModel();
        model.clear();
        */
        setListData(new ArrayList<Object>().toArray());
        //setFixedCellHeight(24);
    }

    public void updateList(OntologyDiagnosisSearcher diagnosisSearcher, OWLOntology ontology) {
        Set<OWLLogicalAxiom> query = diagnosisSearcher.getActualQuery().partition;
        List<Object> items = new ArrayList<Object>();
        for (OWLLogicalAxiom axiom : query) {
            items.add(new QueryAxiomListItem(axiom,ontology));
        }

        /*DefaultListModel model = (DefaultListModel) getModel();
        model.clear();
        for (Object item : items)
            model.addElement(item);*/

        setListData(items.toArray());
    }

}
