package at.ainf.theory.model;

import at.ainf.theory.Searchable;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 13.07.11
 * Time: 18:52
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractSearchableObject<T> implements Searchable<T> {

    private final LinkedHashSet<T> formulaStack = new LinkedHashSet<T>();
    private final LinkedList<Integer> stackCount = new LinkedList<Integer>();
    private Boolean result = null;

    /**
     * Adds a statement to the theory.
     */
    public boolean push(T formula) {
        if (formula == null)
            return false;
        push();
        resetResult();
        this.formulaStack.add(formula);
        return true;
    }

    public boolean push(Collection<T> formulas) {
        if (formulas == null)
            return false;
        push();
        resetResult();
        this.formulaStack.addAll(formulas);
        return true;
    }

    /**
     * This method
     * and {@link #push(Object)} to add new statements.
     */
    public boolean push() {
        return stackCount.add(formulaStack.size());
    }

    /**
     * This method uses to make an
     * empty theory and then adds all statements of the current theory into it
     * using {@link #push(java.util.Collection)}.
     */
    public void pop() {
        pop(1);
    }

    public void pop(int stackCount) {
        if (this.stackCount.isEmpty())
            return;
        resetResult();
        /*
        int btc = 0;
        int size = this.stackCount.size();
        if (size == btc)
            throw new IllegalStateException("Trying to remove the background theory!");
        if (size - stackCount < btc)
            throw new IllegalArgumentException("Illegal stack count value!");
         */

        for (int i = 1; i < stackCount; i++)
            this.stackCount.removeLast();

        int index = this.stackCount.removeLast();
        //int size = this.formulaStack.size();
        int count = 0;
        for (Iterator<T> iterator = formulaStack.iterator(); iterator.hasNext(); ) {
            iterator.next();
            if (count >= index)
                iterator.remove();
            count++;
        }
        //for (int i = index; i < size; i++)
        //    this.formulaStack.removeLast();
    }

    protected void resetResult() {
        this.result = null;
    }

    protected Boolean getResult() {
        return this.result;
    }

    protected void setResult(Boolean result) {
        this.result = result;
    }

    public Set<T> getFormulaStack() {
        return Collections.unmodifiableSet(this.formulaStack);
    }

    public int getTheoryCount() {
        return this.stackCount.size();
    }

    public boolean verifyRequirements() throws SolverException {
        return verifyConsistency();
    }
}
