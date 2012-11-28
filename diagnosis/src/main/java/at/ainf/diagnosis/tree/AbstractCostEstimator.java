package at.ainf.diagnosis.tree;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: kostya
 * Date: 28.11.12
 * Time: 09:56
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractCostEstimator<T> implements CostsEstimator<T> {

    private final Set<T> faultyFormulas;

    public AbstractCostEstimator(Set<T> faultyFormulas){
        this.faultyFormulas = faultyFormulas;
    }

    protected Set<T> getFaultyFormulas() {
        return faultyFormulas;
    }

    @Override
    public void computeNodePathCosts(Node<T> node){
        Node<T> parent = node.getParent();
        T axiom = node.getArcLabel();
        BigDecimal fProb = getAxiomCosts(axiom);
        BigDecimal t = BigDecimal.ONE.subtract(fProb);
        t = parent.getNodePathCosts().divide(t);
        BigDecimal nodePathCosts = t.multiply(fProb);
        node.setNodePathCosts(nodePathCosts);
        node.setCostsEstimator(this);
    }


    @Override
    public BigDecimal getFormulaSetCosts(Set<T> formulas) {
        BigDecimal probability = BigDecimal.ONE;
        if (formulas != null)
            for (T axiom : formulas) {
                probability = probability.multiply(getAxiomCosts(axiom));
            }
        Collection<T> activeFormulas = new ArrayList<T>(faultyFormulas);
        activeFormulas.removeAll(formulas);
        for (T axiom : activeFormulas) {
            probability = probability.multiply(BigDecimal.ONE.subtract(getAxiomCosts(axiom)));
        }
        return probability;
    }
}
