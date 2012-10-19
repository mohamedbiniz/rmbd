package at.ainf.sat4j.model;

import at.ainf.diagnosis.model.ReasonerKB;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 10.10.12
 * Time: 16:25
 * To change this template use File | Settings | File Templates.
 */
public class Sat4jReasonerKB extends ReasonerKB<IVecIntComparable> {

    private int numOfLiterals = 0;

    public int getNumOfLiterals() {
        return numOfLiterals;
    }

    public void setNumOfLiterals(int numOfLiterals) {
        this.numOfLiterals = numOfLiterals;
    }

    @Override
    public boolean addReasonedFormulars(Collection<IVecIntComparable> formulas) {
        boolean res = super.addReasonedFormulars(formulas);
        if (res)
            for (IVecIntComparable formula : formulas) {
                this.numOfLiterals += (formula).size();
            }
        return res;
    }

}
