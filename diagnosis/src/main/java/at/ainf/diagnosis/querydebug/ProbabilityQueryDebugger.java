package at.ainf.diagnosis.querydebug;

import at.ainf.theory.model.ITheory;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.theory.storage.SimpleStorage;
import at.ainf.diagnosis.tree.NodeCostsEstimator;
import at.ainf.diagnosis.tree.UniformCostSearch;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 18.11.11
 * Time: 12:59
 * To change this template use File | Settings | File Templates.
 */
public class ProbabilityQueryDebugger<Id> extends SimpleQueryDebugger<Id> {

    private NodeCostsEstimator<Id> nodeCostsEstimator;

    public void init() {
        SimpleStorage<Id> storage = new SimpleStorage<Id>();
        search = new UniformCostSearch<Id>(storage,nodeCostsEstimator);
        search.setSearcher(new NewQuickXplain<Id>());
        search.setTheory(getTheory());

    }

    public void setNodeCostsEstimator(NodeCostsEstimator<Id> est) {
        nodeCostsEstimator = est;
    }

    public ProbabilityQueryDebugger(ITheory<Id> idITheory, NodeCostsEstimator<Id> costEst) {
        super(idITheory, false);
        this.nodeCostsEstimator = costEst;
        init();

    }

}
