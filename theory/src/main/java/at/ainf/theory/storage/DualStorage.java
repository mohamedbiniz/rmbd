package at.ainf.theory.storage;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 04.02.12
 * Time: 21:20
 * To change this template use File | Settings | File Templates.
 */
public class DualStorage<Id> extends SimpleStorage<Id> {

    @Override
    public Set<AxiomSet<Id>> getConflicts() {
        return Collections.unmodifiableSet(copy(validHittingSets));
    }

    @Override
    public Set<AxiomSet<Id>> getDiagnoses() {
        Set<AxiomSet<Id>> diags = new LinkedHashSet<AxiomSet<Id>>();

        for (AxiomSet<Id> d : copy(conflicts)) {
            if (d.isValid())
              diags.add(d);
        }
        return Collections.unmodifiableSet(diags);

    }

    @Override
    public void invalidateHittingSet(AxiomSet<Id> hs) {

    }


}
