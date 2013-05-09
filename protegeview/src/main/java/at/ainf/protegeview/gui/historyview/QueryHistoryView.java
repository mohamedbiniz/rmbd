package at.ainf.protegeview.gui.historyview;

import at.ainf.protegeview.gui.AbstractListQueryViewComponent;

import javax.swing.*;
import javax.swing.event.ChangeEvent;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 05.11.12
 * Time: 11:17
 * To change this template use File | Settings | File Templates.
 */
public class QueryHistoryView extends AbstractListQueryViewComponent {

    @Override
    public QueryHistoryAxiomList getList() {
        return (QueryHistoryAxiomList) super.getList();
    }

    @Override
    protected JComponent createListForComponent() {
        return new QueryHistoryAxiomList(getOWLEditorKit(),getEditorKitHook());
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        getList().updateView();
    }

}
