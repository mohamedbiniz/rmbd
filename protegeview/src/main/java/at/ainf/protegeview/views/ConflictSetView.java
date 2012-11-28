package at.ainf.protegeview.views;

import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.CostsEstimator;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.protegeview.debugmanager.*;
import at.ainf.protegeview.WorkspaceTab;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import javax.swing.*;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 09.02.11
 * Time: 13:14
 * To change this template use File | Settings | File Templates.
 */
public class ConflictSetView extends AbstractProtegeResultView implements ConflictSetsChangedListener{



    protected void initialiseOWLView() throws Exception {

        super.initialiseOWLView();



        DebugManager.getInstance().addConflictSetsChangedListener(this);

        updateListModel(DebugManager.getInstance().getConflictSets());

    }

    public void conflictSetsChanged(ConflictSetsChangedEvent e) {
        updateListModel(e.getConflictSets());
    }

    protected void updateListModel(Set<? extends FormulaSet<OWLLogicalAxiom>> confl) {
        WorkspaceTab workspace = (WorkspaceTab) getOWLWorkspace().getWorkspaceTab("at.ainf.protegeview.WorkspaceTab");
         if(confl == null) {
            ((DefaultListModel)list.getModel()).clear();
              return;
        }
        //workspace.addAxiomToResultsList( (DefaultListModel)list.getModel(), "Conflict Set ", confl);
        CostsEstimator<OWLLogicalAxiom> es = null;
        if (workspace.getSearch().getSearchStrategy() instanceof UniformCostSearchStrategy) {
            es = workspace.getSearch().getCostsEstimator();
        }
        list.addAxiomToResultsList(null,"Conflict Set ", confl,null);
        list.setConflictSetLst(true);


    }

    protected void disposeOWLView() {
        DebugManager.getInstance().removeConflictSetsChangedListener(this);
    }

}
