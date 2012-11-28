package at.ainf.diagnosis.tree;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 25.02.12
 * Time: 00:22
 * To change this template use File | Settings | File Templates.
 */
public class SimpleCostsEstimator<Id> implements CostsEstimator<Id> {

    public BigDecimal getAxiomSetCosts(Set<Id> labelSet) {
        return new BigDecimal("0.1");
    }

    public BigDecimal getAxiomCosts(Id label) {
        return new BigDecimal("0.1");
    }

    @Override
    public BigDecimal getFormulasCosts(Collection<Id> activeFormulars) {
        return BigDecimal.ONE;
    }

}
