package at.ainf.diagnosis.tree;


import java.math.BigDecimal;
import java.util.Collection;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 13.06.11
 * Time: 17:27
 * To change this template use File | Settings | File Templates.
 */
public interface CostsEstimator<Id> {

    BigDecimal getAxiomSetCosts(Set<Id> labelSet);

    BigDecimal getAxiomCosts(Id label);

    BigDecimal getFormulasCosts(Collection<Id> activeFormulars);
}
