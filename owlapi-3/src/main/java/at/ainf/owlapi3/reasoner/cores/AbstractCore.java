package at.ainf.owlapi3.reasoner.cores;

import at.ainf.owlapi3.reasoner.OWLSatReasoner;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;
import org.semanticweb.owlapi.model.OWLClass;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: kostya
 * Date: 14.05.13
 * Time: 20:15
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractCore<T extends CoreSymbol> {
    private final int signatureSize;
    // private final int constraintsCount;
    private final OWLSatReasoner reasoner;
    protected Set<T> symbols;
    protected boolean isHornComplete = true;
    protected Set<OWLClass> relevantClasses = null;
    private Map<Integer, T> symbolsCache;

    public AbstractCore(OWLSatReasoner reasoner, int symbols) { //, int constraints) {
        this.reasoner = reasoner;
        this.signatureSize = symbols;
//        this.constraintsCount = constraints;
        this.symbols = new HashSet<T>(symbols);
    }

    public AbstractCore(OWLSatReasoner reasoner) {
        this(reasoner, 16);
    }

    public OWLSatReasoner getReasoner() {
        return reasoner;
    }

    public boolean isHornComplete() {
        return isHornComplete;
    }

    public Set<T> getSymbols() {
        return this.symbols;
    }

    public Set<OWLClass> getRelevantClasses() {
        if (this.relevantClasses == null) {
            //final Set<Integer> symbolsSet = getSymbolsSet();
            this.relevantClasses = reasoner.convertToOWLClasses(getSymbols());
        }
        return this.relevantClasses;
    }

    public void extractCore(T symbol) {
        Collection<T> fringe = createFringe();
        fringe.add(createSymbol(symbol.getSymbol(), 0));
        extractCore(fringe);
    }

    public void extractCore(Integer literal) {
        Collection<T> fringe = createFringe();
        fringe.add(createSymbol(literal, 0));
        extractCore(fringe);
    }


    protected void extractCore(Collection<T> fringe) {
        this.symbolsCache = new HashMap<Integer, T>(this.signatureSize);
        while (!fringe.isEmpty()) {
            // remove negation
            T symbol = getNextSymbol(fringe);
            // skip known conflicting symbols
            if (symbol.isConflicting()) continue; //getSymbols().contains(symbol)

            // analyze clauses in which symbol is positive, i.e. in the head of a rule
            for (IVecInt clause : getReasoner().getSymbolsToClauses().get(symbol.getSymbol())) {
                if (!getReasoner().getSolverClauses().containsKey(clause))
                    continue;
                if (!isBCHornClause(clause)) isHornComplete = false;
                fringe = expand(clause, symbol, fringe);
            }
        }
    }



    protected Collection<T> expand(IVecInt clause, T parent, Collection<T> fringe) {
        if (!isBCHornClause(clause)) this.isHornComplete = false;
        for (IteratorInt iterator = clause.iterator(); iterator.hasNext(); ) {
            int lit = iterator.next();
            if (lit < 0) {
                T symbol = getSymbolsCache().get(Math.abs(lit));
                if (symbol == null){
                    symbol = createSymbol(lit, parent, clause);
                    getSymbolsCache().put(symbol.getSymbol(), symbol);
                    fringe.add(symbol);
                } else
                    symbol.updateSymbol(parent, clause);

                    // TODO conflict is only if the number of parents is equal to the number of elements in a constraint.
                    /*
                    else if (!symbol.isConflicting() && !symbol.getRoots().contains(parent)){
                    fringe.add(symbol);
                    */
                if (symbol.isConflicting())
                    symbols.add(symbol);
            }
        }
        return fringe;
    }

    protected abstract T createSymbol(int lit, T parent, IVecInt clause);
    protected abstract T createSymbol(Integer literal, int rootsNumber);
    protected abstract Collection<T> createFringe();
    protected abstract T getNextSymbol(Collection<T> fringe);

    public void extractCore(IVecInt constraint) {
        Collection<T> fringe = createFringe();
        for (IteratorInt iterator = constraint.iterator(); iterator.hasNext(); ) {
            fringe.add(createSymbol(iterator.next(), constraint.size()));
        }
       extractCore(fringe);
    }

    public void extractCore(Set<IVecInt> constraints) {
        for (IVecInt constraint : constraints) {
            extractCore(constraint);
            if (getSymbols().size() == this.signatureSize)
                break;
        }
    }

    /*
    public Core extractCore(IVecInt constraint) {
        Multimap<Integer, Integer> supportingSymbols = HashMultimap.create(this.signatureSize, this.constraintsCount);
        for (IteratorInt iterator = constraint.iterator(); iterator.hasNext(); ) {
            int symbol = iterator.next();
            Core localCore;
            localCore = new Core(getReasoner(), this.signatureSize, this.constraintsCount).extractCore(symbol);
            if (!localCore.isHornComplete())
                this.isHornComplete = false;

            if (supportingSymbols.isEmpty())
                supportingSymbols.putAll(localCore.symbols);
            else
                supportingSymbols.keySet().retainAll(localCore.getSymbols());

            if (supportingSymbols.size() == this.signatureSize)
                break;
        }
        this.symbols.putAll(supportingSymbols);
        return this;
    }
    */


    /**
     * Detect if the clause corresponds to a rule of a form a->b
     *
     * @param clause clause to be analyzed
     * @return true if correspondence is identified
     */
    protected boolean isBCHornClause(IVecInt clause) {
        return clause.size() <= 2 && getHornClauseHead(clause) != null;
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


    public Map<Integer, T> getSymbolsCache() {
        return symbolsCache;
    }
}
