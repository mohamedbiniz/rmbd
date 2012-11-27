package at.ainf.protegeview.gui.tcaeview;

import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.protegeview.gui.AbstractListQueryViewComponent;
import at.ainf.protegeview.gui.AbstractQueryViewComponent;
import at.ainf.protegeview.gui.axiomsetviews.axiomslist.SimpleAxiomList;
import org.protege.editor.core.ui.util.ComponentFactory;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 05.09.12
 * Time: 16:51
 * To change this template use File | Settings | File Templates.
 */
public class TestcaseView extends AbstractListQueryViewComponent {

    @Override
    public TcaeAxiomList getList() {
        return (TcaeAxiomList) super.getList();
    }

    @Override
    protected JComponent createListForComponent() {
        return new TcaeAxiomList(getOWLEditorKit(),getEditorKitHook());
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        getList().updateView();
    }

}
