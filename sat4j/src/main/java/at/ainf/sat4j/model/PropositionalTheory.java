/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.sat4j.model;

import at.ainf.diagnosis.model.*;
import at.ainf.diagnosis.storage.FormulaSet;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IteratorInt;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

public class PropositionalTheory extends BaseSearchableObject<IVecIntComparable> {

    //private boolean createNew = false;

    public PropositionalTheory(ISolver solver) {
        super();
        setReasoner(new ReasonerSat4j(solver));
    }

    public PropositionalTheory(IKnowledgeBase<IVecIntComparable> knowledgeBase,
                               AbstractReasoner<IVecIntComparable> reasoner) {
        super(knowledgeBase,reasoner);
    }

    @Override
    protected BaseSearchableObject<IVecIntComparable> getNewInstance(IKnowledgeBase<IVecIntComparable> knowledgeBase,
                                                                     AbstractReasoner<IVecIntComparable> reasoner)
            throws SolverException, InconsistentTheoryException {
        return new PropositionalTheory(knowledgeBase, reasoner);
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
        for (Set<IVecIntComparable> pt : getKnowledgeBase().getPositiveTests()) {
            getReasoner().addFormulasToCache(pt);
        }
        for (Set<IVecIntComparable> et : getKnowledgeBase().getEntailedTests()) {
            getReasoner().addFormulasToCache(et);
        }

        if (!getReasoner().isConsistent())
            return false;

        for (Set<IVecIntComparable> test : getKnowledgeBase().getNegativeTests()) {
            getReasoner().addFormulasToCache(test);
            boolean consistent = getReasoner().isConsistent();
            getReasoner().removeFormulasFromCache(test);
            if (consistent)  return false;
        }

        for (Set<IVecIntComparable> test : getKnowledgeBase().getNonentailedTests()) {
            if (test != null && getReasoner().isEntailed(test)) {
                return false;
            }
        }
        //getReasoner().clearFormulasCache();
        //getReasoner().addFormulasToCache(backup);

        return true;
    }



    public IVecIntComparable addClause(int[] vector) {
        VecIntComparable anInt = new VecIntComparable(vector);
        getKnowledgeBase().addFormulas(Collections.<IVecIntComparable>singleton(anInt));
        return anInt;
    }

    public IVecIntComparable removeClause(IVecIntComparable vector) {
        VecIntComparable vector2 = (VecIntComparable) vector;
        getKnowledgeBase().removeFormulas(Collections.<IVecIntComparable>singleton(vector2));
        return vector;
    }

    public boolean addAll(FormulaSet<IVecIntComparable> vectorSet) {
        Iterator<IVecIntComparable> iterator = vectorSet.iterator();
        while(iterator.hasNext()) {
            VecIntComparable vector = (VecIntComparable) iterator.next();
            getKnowledgeBase().addFormulas(Collections.<IVecIntComparable>singleton(vector));
        }
        return true;
    }
}
