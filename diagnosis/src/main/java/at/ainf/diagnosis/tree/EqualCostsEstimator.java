package at.ainf.diagnosis.tree;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 20.12.12
 * Time: 17:21
 * To change this template use File | Settings | File Templates.
 */
public class EqualCostsEstimator<Id> extends AbstractCostEstimator<Id> {



    private BigDecimal defaultCosts;

    public EqualCostsEstimator(Set<Id> axioms, BigDecimal costs) {
        super(axioms);
        this.defaultCosts = costs;
    }


    @Override
    public BigDecimal getFormulaCosts(Id label) {
        return defaultCosts;

    }

}
