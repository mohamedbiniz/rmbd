package at.ainf.diagnosis.debugger;

import at.ainf.diagnosis.tree.CostsEstimator;
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

    private CostsEstimator<Id> costsEstimator;

    public void init() {
        SimpleStorage<Id> storage = new SimpleStorage<Id>();
        search = new UniformCostSearch<Id>(storage, costsEstimator);
        search.setSearcher(new NewQuickXplain<Id>());
        if(theory != null) getTheory().reset();
        search.setTheory(getTheory());
        conflictSetsListener = new StorageConflictSetsListenerImpl();
        hittingSetsListener = new StorageHittingSetsListenerImpl();
        search.getStorage().addStorageConflictSetsListener(conflictSetsListener);
        search.getStorage().addStorageHittingSetsListener(hittingSetsListener);
    }

    public ProbabilityQueryDebugger(ITheory<Id> idITheory, CostsEstimator<Id> costEst) {
        super(idITheory, false);
        this.costsEstimator = costEst;
        init();

    }
}
