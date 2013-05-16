package at.ainf.diagnosis.tree;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 04.12.12
 * Time: 09:32
 * To change this template use File | Settings | File Templates.
 */
public class ConfidenceCostsEstimator<Id> extends AbstractCostEstimator<Id> {

    private Map<Id,BigDecimal> mappingConfidences;

    private BigDecimal defaultConfidence;

    public ConfidenceCostsEstimator(Set<Id> axioms, BigDecimal defaultConfidence, Map<Id, BigDecimal> mappingConfidences) {
        super(axioms);
        this.defaultConfidence = defaultConfidence;
        this.mappingConfidences = mappingConfidences;
    }


    @Override
    public BigDecimal getFormulaCosts(Id label) {

        BigDecimal cost = mappingConfidences.get(label);
        if (cost != null)
            return BigDecimal.ONE.subtract(cost);
        else
            return BigDecimal.ONE.subtract(defaultConfidence);
    }

}
