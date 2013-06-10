package at.ainf.owlapi3.reasoner.cores;

import at.ainf.owlapi3.reasoner.HornSatReasoner;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: kostya
 * Date: 28.05.13
 * Time: 10:34
 * To change this template use File | Settings | File Templates.
 */
public class AxiomCore extends AbstractCore<CoreTreeSymbol>{

    // TODO check for a and b -> c
    // TODO remove dependent classes (levels)
    // convert clauses to OWL

    public AxiomCore(HornSatReasoner reasoner, int symbols) {
        super(reasoner, symbols);
    }

    public AxiomCore(HornSatReasoner reasoner) {
        super(reasoner);
    }

    @Override
    protected CoreTreeSymbol createSymbol(int lit, CoreTreeSymbol parent, IVecInt clause) {
        return new CoreTreeSymbol(lit, parent, clause);
    }

    @Override
    protected CoreTreeSymbol createSymbol(Integer literal, int rootsNumber) {
        return new CoreTreeSymbol(literal, rootsNumber);
    }

    @Override
    protected Collection<CoreTreeSymbol> createFringe() {
        return new LinkedList<CoreTreeSymbol>();
    }

    @Override
    protected CoreTreeSymbol getNextSymbol(Collection<CoreTreeSymbol> fringe) {
        return ((LinkedList<CoreTreeSymbol>) fringe).poll();
    }


}
