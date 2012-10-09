package at.ainf.protegeview.views;

import at.ainf.diagnosis.Searchable;
import at.ainf.protegeview.WorkspaceTab;
import at.ainf.diagnosis.model.ITheory;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.protegeview.debugmanager.*;
import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 29.09.11
 * Time: 14:13
 * To change this template use File | Settings | File Templates.
 */
public class RepairView extends AbstractOWLViewComponent implements EntailmentsShowListener,
        HittingSetsChangedListener, ResetReqListener {

    protected WorkspaceTab getWS() {
        return ((WorkspaceTab) getOWLWorkspace().getWorkspaceTab("at.ainf.protegeview.WorkspaceTab"));
    }

    ResultsList list;

    protected void initialiseOWLView() throws Exception {

        setLayout(new BorderLayout(10, 10));

        list = new ResultsList(getOWLEditorKit());
        model = new DefaultListModel();
        list.setModel(model);

        JComponent panel = new JPanel(new BorderLayout(10, 10));
        panel.add(ComponentFactory.createScrollPane(list));

        add(panel, BorderLayout.CENTER);
        if (DebugManager.getInstance().getAx() != null)
            processAxiom(DebugManager.getInstance().getEntSets());

        DebugManager.getInstance().addEntailmentsShowListener(this);
        DebugManager.getInstance().addResetReqListener(this);
        DebugManager.getInstance().addHittingSetsChangedListener(this);
    }

    DefaultListModel model;


    protected void disposeOWLView() {
        DebugManager.getInstance().removeResetReqListener(this);
        DebugManager.getInstance().removeEntailmentsShowListener(this);
        DebugManager.getInstance().removeHittingSetsChangedListener(this);

    }

    public void processAxiom(Set<ResultsListSection> hs) {
        HashMap<ResultsListSection, Set<OWLLogicalAxiom>> map = new HashMap<ResultsListSection, Set<OWLLogicalAxiom>>();
        Searchable<OWLLogicalAxiom> theory = getWS().getSearch().getSearchable();

        for (ResultsListSection section : hs) {
            try {
                map.put(section, theory.getEntailments(section.getAxiomSet()));
            } catch (SolverException e) { //
            }
        }

        Set<OWLLogicalAxiom> intersection = null;
        for (Set<OWLLogicalAxiom> set : map.values()) {
            if (intersection == null) {
                intersection = new HashSet<OWLLogicalAxiom>();
                intersection.addAll(set);
            }
            intersection.retainAll(set);
        }

        for (ResultsListSection section : map.keySet()) {
            map.get(section).removeAll(intersection);
        }


        list.addAxToResultsLst2(map);

    }

    public void processResetReq(ResetReqEvent e) {
        model.clear();
    }

    public void entailmentSetChanged(EntailmentsShowEvent e) {
        processAxiom(e.getEntHSets());
        getWS().bringViewToFront(getView().getId());
    }

    public void hittingSetsChanged(HittingSetsChangedEvent e) {
        model.clear();
    }

}
