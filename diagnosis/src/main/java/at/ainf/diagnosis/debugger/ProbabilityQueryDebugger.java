package at.ainf.diagnosis.debugger;

import at.ainf.diagnosis.tree.NodeCostsEstimator;
import at.ainf.theory.model.ITheory;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.theory.storage.SimpleStorage;
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
        if(theory != null) getTheory().reset();
        search.setTheory(getTheory());
    }

    public ProbabilityQueryDebugger(ITheory<Id> idITheory, NodeCostsEstimator<Id> costEst) {
        super(idITheory, false);
        this.nodeCostsEstimator = costEst;
        init();

    }
}
