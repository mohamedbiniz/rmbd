/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.sat4j.model;

import at.ainf.diagnosis.model.*;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.*;

import java.util.*;

public class PropositionalTheory extends AbstractSearchableObject<IVecIntComparable> {

    private boolean createNew = false;

    public PropositionalTheory(ISolver solver) {
        super(solver);
        setReasoner(new ReasonerSat4j(solver));
    }

    @Override
    protected IVecIntComparable negate(IVecIntComparable formula) {
        IVecIntComparable res = new VecIntComparable();
        for (IteratorInt iter = formula.iterator(); iter.hasNext(); ) {
            int val = iter.next() * -1;
            res.push(val);
        }
        return res;
    }

    public boolean verifyConsistency() throws SolverException {

        LinkedHashSet<IVecIntComparable> backup = new LinkedHashSet<IVecIntComparable>();
        backup.addAll(getReasoner().getReasonendFormulars());

        getReasoner().addReasonedFormulars(getKnowledgeBase().getBackgroundFormulas());
        getReasoner().sync();
        boolean result = getReasoner().isConsistent();


        getReasoner().cleanReasonedFormulars();
        getReasoner().addReasonedFormulars(backup);

        return result;
    }

    public IVecIntComparable addClause(int[] vector) {
        VecIntComparable anInt = new VecIntComparable(vector);
        getKnowledgeBase().addFormular(Collections.<IVecIntComparable>singleton(anInt));
        return anInt;
    }


}
