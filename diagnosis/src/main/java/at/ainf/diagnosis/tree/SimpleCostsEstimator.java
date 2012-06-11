package at.ainf.diagnosis.tree;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 25.02.12
 * Time: 00:22
 * To change this template use File | Settings | File Templates.
 */
public class SimpleCostsEstimator<Id> implements CostsEstimator<Id> {
    public double getAxiomSetCosts(Set<Id> labelSet) {
        return 0.1;
    }

    public double getAxiomCosts(Id label) {
        return 0.1;
    }
}
