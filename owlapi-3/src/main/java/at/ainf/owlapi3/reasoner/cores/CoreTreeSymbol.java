package at.ainf.owlapi3.reasoner.cores;

import org.sat4j.specs.IVecInt;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: kostya
 * Date: 03.06.13
 * Time: 18:23
 * To change this template use File | Settings | File Templates.
 */
public class CoreTreeSymbol extends CoreSymbol{

    private Set<CoreTreeSymbol> parents = new HashSet<CoreTreeSymbol>();
    private Set<IVecInt> arcLabels = new HashSet<IVecInt>();

    public CoreTreeSymbol(int symbol, int level) {
        super(symbol, level);
    }

    public CoreTreeSymbol(int lit, CoreTreeSymbol parent) {
        super(lit, parent);
    }

    public CoreTreeSymbol(CoreTreeSymbol literal) {
        this(literal.getSymbol(), 0);
    }

    public CoreTreeSymbol(int literal) {
        this(literal, 0);
    }

    public CoreTreeSymbol(int lit, CoreTreeSymbol parent, IVecInt clause) {
        this(lit, parent);
        this.parents.add(parent);
        this.arcLabels.add(clause);
    }

    @Override
    public <T extends CoreSymbol> void updateSymbol(T parent, IVecInt clause) {
        super.updateSymbol(parent, clause);
        this.parents.add((CoreTreeSymbol) parent);
        this.arcLabels.add(clause);
    }
}
