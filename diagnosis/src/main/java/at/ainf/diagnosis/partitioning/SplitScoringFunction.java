package at.ainf.diagnosis.partitioning;

import at.ainf.theory.storage.AxiomSet;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 11.05.11
 * Time: 09:29
 * To change this template use File | Settings | File Templates.
 */
public class SplitScoringFunction<Id> extends EntropyScoringFunction<Id> implements ScoringFunction<Id> {

    public String toString() {
        return "Split";
    }

    public void normalize(Set<? extends AxiomSet<Id>> hittingSets) {
        double size = hittingSets.size();
        if (size > 1)
            for (AxiomSet<Id> hs : hittingSets) {
               hs.setMeasure(1 / size);
            }
    }
}
