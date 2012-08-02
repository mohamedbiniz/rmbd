package at.ainf.diagnosis.partitioning.scoring;

import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.tree.Rounding;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 10.02.12
 * Time: 11:50
 * To change this template use File | Settings | File Templates.
 */
public class SplitInHalfQSS<T> extends MinScoreQSS<T> {

    public String toString() {
        return "Split";
    }

    public void normalize(Set<? extends AxiomSet<T>> hittingSets) {
        BigDecimal size = new BigDecimal(Integer.toString(hittingSets.size()));
        if (size.compareTo(BigDecimal.ONE)>0)
            for (AxiomSet<T> hs : hittingSets) {
                hs.setMeasure(BigDecimal.ONE.divide(size, Rounding.PRECISION,Rounding.ROUNDING_MODE));
            }
    }
}
