/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.sat4j.model;

import at.ainf.diagnosis.model.*;
import org.sat4j.specs.*;

import java.util.*;

public class PropositionalTheory extends BaseSearchableObject<IVecIntComparable> {

    //private boolean createNew = false;

    public PropositionalTheory(ISolver solver) {
        super();
        setReasoner(new ReasonerSat4j(solver));
    }

    private IVecIntComparable negate(IVecIntComparable formula) {
        IVecIntComparable res = new VecIntComparable();
        for (IteratorInt iter = formula.iterator(); iter.hasNext(); ) {
            int val = iter.next() * -1;
            res.push(val);
        }
        return res;
    }

    public ReasonerSat4j getReasoner() {
        return (ReasonerSat4j) super.getReasoner();
    }

    public boolean verifyConsistency() throws SolverException {

        //LinkedHashSet<IVecIntComparable> backup = new LinkedHashSet<IVecIntComparable>();
        //backup.addAll(getReasoner().getFormulasCache());

        getReasoner().addFormulasToCache(getKnowledgeBase().getBackgroundFormulas());
        boolean result = getReasoner().isConsistent();


        //getReasoner().clearFormulasCache();
        //getReasoner().addFormulasToCache(backup);

        return result;
    }

    public IVecIntComparable addClause(int[] vector) {
        VecIntComparable anInt = new VecIntComparable(vector);
        getKnowledgeBase().addFormulas(Collections.<IVecIntComparable>singleton(anInt));
        return anInt;
    }


}
