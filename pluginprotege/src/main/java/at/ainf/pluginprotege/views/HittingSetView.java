package at.ainf.pluginprotege.views;

import at.ainf.diagnosis.storage.HittingSet;
import at.ainf.diagnosis.tree.UniformCostSearch;
import at.ainf.pluginprotege.debugmanager.DebugManager;
import at.ainf.pluginprotege.WorkspaceTab;
import at.ainf.diagnosis.tree.NodeCostsEstimator;
import at.ainf.pluginprotege.debugmanager.HittingSetsChangedEvent;
import at.ainf.pluginprotege.debugmanager.HittingSetsChangedListener;
import org.protege.editor.core.ui.util.ComponentFactory;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 09.02.11
 * Time: 13:14
 * To change this template use File | Settings | File Templates.
 */
public class HittingSetView extends AbstractProtegeResultView implements HittingSetsChangedListener {

    protected void initialiseOWLView() throws Exception {

        setLayout(new BorderLayout(10, 10));
        list = new ResultsLstHs(getOWLEditorKit());

        list.setModel(new DefaultListModel());
        JComponent panel =  new JPanel(new BorderLayout(10, 10));
        panel.add(ComponentFactory.createScrollPane(list));

        add(panel, BorderLayout.CENTER);

        DebugManager.getInstance().addHittingSetsChangedListener(this);
        updateListModel(DebugManager.getInstance().getValidHittingSets());

        list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (list.getSelectedValue() instanceof ResultsListSection) {
                    /*ResultsListSection section = ((ResultsListSection) list.getSelectedValue());
                    HittingSet<OWLLogicalAxiom> hs = (HittingSet<OWLLogicalAxiom>) section
                            .getAxiomSet();
                    DebugManager.getInstance().setTreeNode(hs);
                    DebugManager.getInstance().notifyTreeNodeChanged();*/
                    return;
                }

                ResultsListSectionItem item = ((ResultsListSectionItem) list.getSelectedValue());

                if (item == null)
                    return;
                if (item.getAxiom().equals(DebugManager.getInstance().getAx()))
                    return;
                DebugManager.getInstance().setAxiom(item.getAxiom());
                DebugManager.getInstance().notifyAxiomChanged();

            }
        });

    }

    protected void updateListModel(Set<? extends HittingSet<OWLLogicalAxiom>> validHs) {
        WorkspaceTab workspace = (WorkspaceTab) getOWLWorkspace().getWorkspaceTab("at.ainf.pluginprotege.WorkspaceTab");
         if(validHs == null) {
            ((DefaultListModel)list.getModel()).clear();
              return;
        }
        TreeSet<? extends HittingSet<OWLLogicalAxiom>> hsTree = (TreeSet<? extends HittingSet<OWLLogicalAxiom>>) validHs;
        Set<? extends HittingSet<OWLLogicalAxiom>> hsReverse = hsTree.descendingSet();
        //workspace.addAxiomToResultsList( (DefaultListModel)list.getModel(), "Diagnosis", hsReverse);
        NodeCostsEstimator<OWLLogicalAxiom> es = null;
        if (workspace.getSearch() instanceof UniformCostSearch) {
            es = ((UniformCostSearch<OWLLogicalAxiom>) workspace.getSearch()).getNodeCostsEstimator();
        }
        list.addAxiomToResultsList(es,"Diagnosis", hsReverse,null);


    }

    public void hittingSetsChanged(HittingSetsChangedEvent e) {
        updateListModel(e.getValidHS());
    }

    protected void disposeOWLView() {
        DebugManager.getInstance().removeHittingSetsChangedListener(this);
    }

}
