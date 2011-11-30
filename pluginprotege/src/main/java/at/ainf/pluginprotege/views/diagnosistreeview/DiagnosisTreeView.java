package at.ainf.pluginprotege.views.diagnosistreeview;

import at.ainf.diagnosis.storage.HittingSetImpl;
import at.ainf.pluginprotege.WorkspaceTab;
import at.ainf.pluginprotege.debugmanager.*;
import org.protege.editor.owl.ui.view.cls.AbstractOWLClassViewComponent;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 20.08.11
 * Time: 13:25
 * To change this template use File | Settings | File Templates.
 */
public class DiagnosisTreeView extends AbstractOWLClassViewComponent implements TreeNodeChangedListener,
                HittingSetsChangedListener {


    private DiagnosesTree tr;


    public void initialiseClassView() throws Exception {
        setLayout(new BorderLayout());
        tr = new DiagnosesTree(getOWLEditorKit());
        JScrollPane sp = new JScrollPane(tr);
        add(sp, BorderLayout.CENTER);
        DebugManager.getInstance().addTreeNodeChangedListener(this);
        DebugManager.getInstance().addHittingSetsChangedListener(this);
        WorkspaceTab workspace = (WorkspaceTab) getOWLWorkspace().getWorkspaceTab("at.ainf.pluginprotege.WorkspaceTab");
        ((DiagnosesTreeModel)tr.getModel()).setConflictSets(workspace.getSearch().getStorage().getConflictSets(), getOWLWorkspace());
        tr.hittingSets(DebugManager.getInstance().getValidHittingSets());
        HittingSetImpl<OWLLogicalAxiom> hs = (HittingSetImpl<OWLLogicalAxiom>)DebugManager.getInstance().getTreeNode();
        if (hs == null)
            tr.setDisplayNodeChanged(null);
        else
            tr.setDisplayNodeChanged(hs.getNode());
    }


    protected OWLClass updateView(OWLClass selectedClass) {

        return null;
    }


    public void disposeView() {
        DebugManager.getInstance().removeTreeNodeChangedListener(this);
        DebugManager.getInstance().removeHittingSetsChangedListener(this);
    }

    public void hittingSetsChanged(HittingSetsChangedEvent e) {
        WorkspaceTab workspace = (WorkspaceTab) getOWLWorkspace().getWorkspaceTab("at.ainf.pluginprotege.WorkspaceTab");
        ((DiagnosesTreeModel)tr.getModel()).setConflictSets(workspace.getSearch().getStorage().getConflictSets(), getOWLWorkspace());
        tr.hittingSets(e.getValidHS());
    }

    public void treeNodeChanged(TreeNodeChangedEvent e) {
        if (e.getTreenode() == null)
            tr.setDisplayNodeChanged(null);
        else
            tr.setDisplayNodeChanged(((HittingSetImpl<OWLLogicalAxiom>)e.getTreenode()).getNode());
    }

}
