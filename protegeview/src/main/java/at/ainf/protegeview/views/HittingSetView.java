package at.ainf.protegeview.views;

import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.protegeview.WorkspaceTab;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.protegeview.debugmanager.DebugManager;
import at.ainf.diagnosis.tree.CostsEstimator;
import at.ainf.protegeview.debugmanager.HittingSetsChangedEvent;
import at.ainf.protegeview.debugmanager.HittingSetsChangedListener;
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
                    AxiomSet<OWLLogicalAxiom> hs = (AxiomSet<OWLLogicalAxiom>) section
                            .getAxiomSets();
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

    protected void updateListModel(Set<? extends AxiomSet<OWLLogicalAxiom>> validHs) {
        WorkspaceTab workspace = (WorkspaceTab) getOWLWorkspace().getWorkspaceTab("at.ainf.protegeview.WorkspaceTab");
         if(validHs == null) {
            ((DefaultListModel)list.getModel()).clear();
              return;
        }
        TreeSet<? extends AxiomSet<OWLLogicalAxiom>> hsTree = new TreeSet<AxiomSet<OWLLogicalAxiom>>(validHs);
        Set<? extends AxiomSet<OWLLogicalAxiom>> hsReverse = hsTree.descendingSet();
        //workspace.addAxiomToResultsList( (DefaultListModel)list.getModel(), "Diagnosis", hsReverse);
        CostsEstimator<OWLLogicalAxiom> es = null;
        if (workspace.getSearch().getSearchStrategy() instanceof UniformCostSearchStrategy) {
            es = workspace.getSearch().getCostsEstimator();
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
