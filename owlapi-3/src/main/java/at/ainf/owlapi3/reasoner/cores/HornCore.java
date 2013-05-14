package at.ainf.owlapi3.reasoner.cores;

import at.ainf.owlapi3.reasoner.HornSatReasoner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;
import org.semanticweb.owlapi.model.OWLClass;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: kostya
 * Date: 14.05.13
 * Time: 20:15
 * To change this template use File | Settings | File Templates.
 */
public class HornCore extends Core {


    public HornCore(HornSatReasoner reasoner, int symbols, int constraints) {
        super(reasoner, symbols, constraints);
    }

    public HornCore(HornSatReasoner reasoner) {
        super(reasoner);
    }

    protected HornCore extractCore(Integer literal, int level) {
        // remove negation
        int symbol = Math.abs(literal);
        if (getSymbolsSet().contains(symbol)) return this;
        symbols.put(symbol, level);
        //if (core.selectedClasses.contains(symbol))
        //    core.selectedScore++;
        // analyze clauses in which symbol is positive, i.e. in the head of a rule
        for (IVecInt clause : getReasoner().getSymbolsToClauses().get(symbol)) {
            if (!getReasoner().getSolverClauses().containsKey(clause))
                continue;
            final boolean isHornClause = isHornClause(clause);
            if (!isHornClause) isHornComplete = false;
            if (!isHornClause) {
                continue;
            }
            //Set<Integer> neg = containsAllNegativeSymbols(clause, false);

            for (IteratorInt iterator = clause.iterator(); iterator.hasNext(); ) {
                int lit = iterator.next();
                if (lit < 0) extractCore(lit, ++level);
            }
        }
        return this;
    }
}
