package at.ainf.protegeview.gui.queryview;

import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.protegeview.gui.AbstractListQueryViewComponent;
import at.ainf.protegeview.model.OntologyDiagnosisSearcher;
import at.ainf.protegeview.model.configuration.SearchCreator;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.util.Collections;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 06.09.12
 * Time: 19:20
 * To change this template use File | Settings | File Templates.
 */
public class QueryView extends AbstractListQueryViewComponent {

    protected JToolBar createNewQueryToolBar() {
        JToolBar toolBar = new JToolBar();

        toolBar.setFloatable(false);
        toolBar.add(new GetQueryButton(this));
        toolBar.add(new GetAlternativeQueryButton(this));
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(new CommitAndGetNextButton(this));
        toolBar.setMaximumSize(toolBar.getPreferredSize());

        return toolBar;
    }

    @Override
    protected void initialiseOWLView() throws Exception {
        super.initialiseOWLView();
        add(createNewQueryToolBar(), BorderLayout.NORTH);

    }

    public QueryAxiomList getList() {
        return (QueryAxiomList) super.getList();
    }

    @Override
    protected JComponent createListForComponent() {
        return new QueryAxiomList(getOWLEditorKit(),getEditorKitHook());

    }

    @Override
    public void stateChanged(ChangeEvent e) {

        OntologyDiagnosisSearcher diagnosisSearcher = (OntologyDiagnosisSearcher) e.getSource();
        OWLOntology ontology = getOWLEditorKit().getModelManager().getActiveOntology();
        switch(diagnosisSearcher.getQuerySearchStatus()) {
            case ASKING_QUERY:
                getList().updateList(diagnosisSearcher,ontology);
                break;
            case IDLE:
                getList().clearList();
                break;
        }



    }

}
