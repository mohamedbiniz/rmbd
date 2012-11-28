package at.ainf.diagnosis.tree;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 25.02.12
 * Time: 00:22
 * To change this template use File | Settings | File Templates.
 */
public class SimpleCostsEstimator<Id> extends AbstractCostEstimator<Id> implements CostsEstimator<Id> {

    public SimpleCostsEstimator() {
        super(new LinkedHashSet<Id>());
    }

    public SimpleCostsEstimator(Set<Id> faultyFormulas) {
        super(faultyFormulas);
    }

    public BigDecimal getFormulaSetCosts(Set<Id> formulas) {
        return new BigDecimal("0.1");
    }

    public BigDecimal getFormulaCosts(Id label) {
        return new BigDecimal("0.1");
    }

}
