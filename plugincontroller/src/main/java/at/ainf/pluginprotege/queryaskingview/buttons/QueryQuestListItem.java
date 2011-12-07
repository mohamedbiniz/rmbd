package at.ainf.pluginprotege.queryaskingview.buttons;

import at.ainf.diagnosis.tree.UniformCostSearch;
import at.ainf.pluginprotege.WorkspaceTab;
import at.ainf.pluginprotege.views.ResultsListSectionItem;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 25.05.11
 * Time: 13:13
 * To change this template use File | Settings | File Templates.
 */
public class QueryQuestListItem extends ResultsListSectionItem {

    private WorkspaceTab workspace;

    private boolean nonEntailedMarked = false;

    private boolean entailedMarked = false;

    private boolean unknowMarked = false;

    public QueryQuestListItem(OWLLogicalAxiom axiom, WorkspaceTab workspaceTab) {
        super(axiom, axiom, ((UniformCostSearch<OWLLogicalAxiom>) workspaceTab.getSearch()).getNodeCostsEstimator());
        workspace=workspaceTab;


    }

    public boolean isUnknowMarked() {
        return unknowMarked;
    }

    public void setUnknowMarked(boolean unknowMarked) {
        this.unknowMarked = unknowMarked;
    }

    public boolean isNonEntailedMarked() {
        return nonEntailedMarked;
    }

    public void setNonEntailedMarked(boolean nonEntailedMarked) {
        this.nonEntailedMarked = nonEntailedMarked;
    }

    public boolean isEntailedMarked() {
        return entailedMarked;
    }

    public void setEntailedMarked(boolean entailedMarked) {
        this.entailedMarked = entailedMarked;
    }

    public void handleEntailed() {
        entailedMarked = !entailedMarked;
        nonEntailedMarked = false;
    }

    public void handleNotEntailed() {
        entailedMarked = false;
        nonEntailedMarked = !nonEntailedMarked;
    }

    public void handleUnknownEntailed() {
        unknowMarked = !unknowMarked;
    }


}
