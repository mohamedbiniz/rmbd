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



    public boolean push(Collection<T> formulas) {
        if (formulas == null)
            return false;
        stackCount.add(formulaStack.size());
        resetResult();
        this.formulaStack.addAll(formulas);
        return true;
    }

    public void pop() {
        if (this.stackCount.isEmpty())
            return;
        resetResult();


        int index = this.stackCount.removeLast();
        //int size = this.formulaStack.size();
        int count = 0;
        for (Iterator<T> iterator = formulaStack.iterator(); iterator.hasNext(); ) {
            iterator.next();
            if (count >= index)
                iterator.remove();
            count++;
        }
    }

    public void clean() {
        formulaStack.clear();
        stackCount.clear();

    }


    public Set<T> getFormulaStack() {
        return Collections.unmodifiableSet(this.formulaStack);
    }

    /*public int getTheoryCount() {
        return this.lastStackCount.size();
    }*/

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
