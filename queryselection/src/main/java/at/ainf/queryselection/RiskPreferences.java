package at.ainf.queryselection;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 19.04.11
 * Time: 15:30
 * To change this template use File | Settings | File Templates.
 */
public class RiskPreferences {

    private double lowerBound;
    private double upperBound;
    private double initialRisk;

    public RiskPreferences() {

    }

    public RiskPreferences(double initRisk, double lowerB, double upperB) {
        this.initialRisk = initRisk;
        this.lowerBound = lowerB;
        this.upperBound = upperB;
    }

    public double getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(double lowerBound) {
        this.lowerBound = lowerBound;
    }

    public double getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(double upperBound) {
        this.upperBound = upperBound;
    }

    public double getInitialRisk() {
        return initialRisk;
    }

    public void setInitialRisk(double initialRisk) {
        this.initialRisk = initialRisk;
    }

    public String toString(){
        String string = "initial risk = " + initialRisk + ";   lower bound = " + lowerBound + ";   upper bound = " + upperBound;
        return string;
    }
}
