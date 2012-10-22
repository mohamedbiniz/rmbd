package at.ainf.sat4j.model;

import at.ainf.diagnosis.model.AbstractReasoner;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 19.10.12
 * Time: 10:04
 * To change this template use File | Settings | File Templates.
 */
public class ReasonerSat4j extends AbstractReasoner<IVecIntComparable> {

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

    @Override
    public boolean isConsistent() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
