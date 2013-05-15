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
public class Core {
    private final int signatureSize;
    private final int constraintsCount;
    private final HornSatReasoner reasoner;
    protected Multimap<Integer, Integer> symbols;
    protected boolean isHornComplete = true;
    protected Set<OWLClass> relevantClasses = null;

    public Core(HornSatReasoner reasoner, int symbols, int constraints) {
        this.reasoner = reasoner;
        this.signatureSize = symbols;
        this.constraintsCount = constraints;
        this.symbols = HashMultimap.create(symbols, constraints);
    }

    public Core(HornSatReasoner reasoner) {
        this(reasoner, 16, 2);
    }

    public void addSymbols(IVecInt clause) {
        for (IteratorInt it = clause.iterator(); it.hasNext(); ) {
            int literal = Math.abs(it.next());
            symbols.put(literal, 0);
        }
    }

    public HornSatReasoner getReasoner() {
        return reasoner;
    }

    public boolean isHornComplete() {
        return isHornComplete;
    }

    public Set<Integer> getSymbolsSet() {
        return this.symbols.keySet();
    }

    public Set<OWLClass> getRelevantClasses() {
        if (this.relevantClasses == null) {
            //final Set<Integer> symbolsSet = getSymbolsSet();
            this.relevantClasses = reasoner.convertToOWLClasses(this);
        }
        return this.relevantClasses;
    }

    public Multimap<Integer, Integer> getSymbolsMap() {
        return this.symbols;
    }

    public Core extractCore(Integer literal) {
        return extractCore(literal, 0);
    }

    protected Core extractCore(Integer literal,int level) {
        // remove negation
        int symbol = Math.abs(literal);
        if (getSymbolsSet().contains(symbol)) return this;
        symbols.put(symbol, level);

        // analyze clauses in which symbol is positive, i.e. in the head of a rule
        for (IVecInt clause : this.reasoner.getSymbolsToClauses().get(symbol)) {
            if (!this.reasoner.getSolverClauses().containsKey(clause))
                continue;
            if (!isBCHornClause(clause)) isHornComplete = false;

            if (isExpandable(clause))
                expand(clause, level);
        }
        return this;
    }

    protected boolean isExpandable(IVecInt clause) {
        return true;
    }

    protected void expand(IVecInt clause, int level){
        for (IteratorInt iterator = clause.iterator(); iterator.hasNext(); ) {
            int lit = iterator.next();
            if (lit < 0) extractCore(lit, ++level);
        }
    }

    public Core extractCore(IVecInt constraint) { // Multimap<Integer, Integer> supportingMap
        Multimap<Integer, Integer> supportingSymbols = HashMultimap.create(this.signatureSize, this.constraintsCount);
        for (IteratorInt iterator = constraint.iterator(); iterator.hasNext(); ) {
            int symbol = iterator.next();
            Core localCore;
            //if (!supportingMap.containsKey(symbol)) {
            localCore = new Core(getReasoner(), this.signatureSize, this.constraintsCount).extractCore(symbol);
            if (!localCore.isHornComplete())
                this.isHornComplete = false;
            //supportingMap.putAll(symbol, localCore.getSymbolsSet());
            //} else symbols = supportingMap.get(symbol);

            // TODO check for a and b -> c
            // TODO remove dependent classes (levels)
            // convert clauses to OWL

            if (supportingSymbols.isEmpty())
                supportingSymbols.putAll(localCore.symbols);
            else
                supportingSymbols.keySet().retainAll(localCore.getSymbolsSet());

            if (supportingSymbols.size() == this.signatureSize)
                break;
        }
        this.symbols.putAll(supportingSymbols);
        return this;
    }


    /**
     * Detect if the clause corresponds to a rule of a form a->b
     * @param clause clause to be analyzed
     * @return true if correspondence is identified
     */
    protected boolean isBCHornClause(IVecInt clause) {
        return clause.size() <= 2 && clause.get(0) * clause.get(1) < 0;
    }

    /**
     * Verifies whether an input clause is a Horn clause
     *
     * @param clause input clause to be verified in DIMACS format, with no <code>0</code>  values allowed
     * @return a positive integer if the head has one element, 0 if the head is empty and
     *         <code>null</code> if the clause is not a Horn clause
     */
    private Integer getHornClauseHead(IVecInt clause) {
        int head = 0;
        for (IteratorInt it = clause.iterator(); it.hasNext(); ) {
            int literal = it.next();
            if (literal > 0 && head > 0)
                return null;
            else if (literal > 0)
                head = literal;
        }
        return head;
    }
}
