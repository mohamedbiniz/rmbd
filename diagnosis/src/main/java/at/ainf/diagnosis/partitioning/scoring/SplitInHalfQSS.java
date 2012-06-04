package at.ainf.diagnosis.partitioning.scoring;

import at.ainf.theory.storage.AxiomSet;

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
        double size = hittingSets.size();
        if (size > 1)
            for (AxiomSet<T> hs : hittingSets) {
                hs.setMeasure(1 / size);
            }
    }
}
