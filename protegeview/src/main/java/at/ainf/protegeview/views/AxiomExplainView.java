package at.ainf.protegeview.views;

import at.ainf.diagnosis.tree.CostsEstimator;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.protegeview.debugmanager.*;
import at.ainf.protegeview.WorkspaceTab;
import at.ainf.theory.storage.AxiomSet;
import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 04.08.11
 * Time: 09:36
 * To change this template use File | Settings | File Templates.
 */
public class AxiomExplainView extends AbstractOWLViewComponent implements AxiomChangedListener, ResetReqListener {

    protected WorkspaceTab getWS() {
        return ((WorkspaceTab) getOWLWorkspace().getWorkspaceTab("at.ainf.protegeview.WorkspaceTab"));
    }

    ResultsList list;

    protected void initialiseOWLView() throws Exception {

        setLayout(new BorderLayout(10, 10));

        list = new ResultsList(getOWLEditorKit());
        model = new DefaultListModel();
        list.setModel(model);

        JComponent panel =  new JPanel(new BorderLayout(10, 10));
        panel.add(ComponentFactory.createScrollPane(list));

        add(panel, BorderLayout.CENTER);
        if (DebugManager.getInstance().getAx() != null)
            processAxiom(DebugManager.getInstance().getAx());

            DebugManager.getInstance().addAxiomChangedListener(this);
                DebugManager.getInstance().addResetReqListener(this);


            // ((WorkspaceTab) getOWLWorkspace().getWorkspaceTab("at.ainf.protegeview.WorkspaceTab")).setExpl(this);

    }

    DefaultListModel model;




    protected void disposeOWLView() {
        DebugManager.getInstance().removeResetReqListener(this);
        DebugManager.getInstance().removeAxiomChangedListener(this);
    }

    public void processAxiom (OWLLogicalAxiom axiom) {
        Set<? extends AxiomSet<OWLLogicalAxiom>> conflSetAxxx
          = getWS().getSearch().getStorage().getConflictSets(axiom);
        ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
        if (axiom != null) {
            setHeaderText (renderer.render(axiom));
        }
        else {
            setHeaderText (" ");
        }
        //getWS().addAxiomToResultsList(model, "Conflict Set ", conflSetAxxx);
        CostsEstimator<OWLLogicalAxiom> es = null;
        if (getWS().getSearch().getSearchStrategy() instanceof UniformCostSearchStrategy) {
            es = (getWS().getSearch()).getCostsEstimator();
        }
        list.addAxiomToResultsList(null,"Conflict Set ", conflSetAxxx, null);
    }

    public void processResetReq(ResetReqEvent e) {
        model.clear();
    }

    public void axiomChanged(AxiomChangedEvent e) {
        processAxiom (e.getAxiom());
        getWS().bringViewToFront(getView().getId());


    }
}
