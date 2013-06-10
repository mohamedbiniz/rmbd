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
public class CoreSymbol implements Comparable<CoreSymbol> {
    private final Integer symbol;
    private final int level;
    private Set<CoreSymbol> roots = new HashSet<CoreSymbol>();
    private final int rootsNumber;

    public CoreSymbol(int symbol, int level, int rootsNumber) {
        this.symbol = Math.abs(symbol);
        this.level = level;
        this.rootsNumber = rootsNumber;
    }

    public CoreSymbol(int lit, CoreSymbol parent) {
        this(lit, parent.getLevel() + 1, parent.rootsNumber);
        if (parent.roots.isEmpty())
            this.roots.add(parent);
        else
            this.roots.addAll(parent.getRoots());
    }

    public CoreSymbol(CoreSymbol literal) {
        this(literal.getSymbol(), 0, literal.rootsNumber);
    }

    public CoreSymbol(int literal, int rootsNumber) {
        this(literal, 0, rootsNumber);
    }

    public int getLevel() {
        return this.level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoreSymbol treeNode = (CoreSymbol) o;
        return symbol.equals(treeNode.symbol);

    }

    @Override
    public int hashCode() {
        return this.symbol.hashCode();
    }

    public boolean isConflicting() {
        return this.rootsNumber > 0 && this.roots.size() >= rootsNumber;
    }

    public Integer getSymbol() {
        return symbol;
    }

    @Override
    public int compareTo(CoreSymbol o) {
        if (this == o) return 0;
        if (o == null || getClass() != o.getClass())
            throw new IllegalArgumentException("The symbol " + o + " is not comparable!");
        return this.symbol.compareTo(o.symbol);
    }

    public <T extends CoreSymbol> void updateSymbol(T parent, IVecInt clause) {
        this.roots.addAll(parent.getRoots());
    }

    public Set<CoreSymbol> getRoots() {
        return roots;
    }
}
