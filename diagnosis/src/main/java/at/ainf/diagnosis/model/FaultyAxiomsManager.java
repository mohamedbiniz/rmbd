package at.ainf.diagnosis.model;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 10.10.12
 * Time: 16:05
 * To change this template use File | Settings | File Templates.
 */
public class FaultyAxiomsManager<T> {

    private final LinkedHashSet<T> formulaStack = new LinkedHashSet<T>();
    private final LinkedList<Integer> stackCount = new LinkedList<Integer>();
    private Boolean result = null;

    /**
     * Adds a statement to the theory.
     */
    public boolean push(T formula) {
        if (formula == null)
            return false;
        stackCount.add(formulaStack.size());
        resetResult();
        this.formulaStack.add(formula);
        return true;
    }

    public boolean push(Collection<T> formulas) {
        if (formulas == null)
            return false;
        stackCount.add(formulaStack.size());
        resetResult();
        this.formulaStack.addAll(formulas);
        return true;
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

    public Set<T> getFormulaStack() {
        return Collections.unmodifiableSet(this.formulaStack);
    }

    public int getTheoryCount() {
        return this.stackCount.size();
    }

    /**
     * This method uses to make an
     * empty theory and then adds all statements of the current theory into it
     * using {@link #push(java.util.Collection)}.
     */
    public void pop() {
        pop(1);
    }

    public void resetResult() {
        this.result = null;
    }

    public Boolean getResult() {
        return this.result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }


}
