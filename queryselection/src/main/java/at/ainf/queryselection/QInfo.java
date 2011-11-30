package at.ainf.queryselection;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 03.03.11
 * Time: 19:48
 * To change this template use File | Settings | File Templates.
 */
public class QInfo {

    private List<QueryModuleDiagnosis> currentDiags;
    private int iteration = 0;
    private int numOfEliminatedDiags = 0;
    private double percentForNextQuery = 0d;
    private int totalNumOfEliminatedDiags = 0;
    boolean percentIsSet = false;
    private double cumulatedAlpha;
    private double currentScore;

    public double getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(double currentScore) {
        this.currentScore = currentScore;
    }

    public void setCurrentDiags(List<QueryModuleDiagnosis> diags) {
        this.currentDiags = diags;
    }

    public List<QueryModuleDiagnosis> getCurrentDiags() {
        return this.currentDiags;
    }

    public void setIteration(int i) {
        this.iteration = i;
    }

    public int getIteration() {
        return this.iteration;
    }

    public void setNumberOfEliminatedDiags(int numOfElimDiags) {
        this.numOfEliminatedDiags = numOfElimDiags;
    }

    public int getNumOfEliminatedDiags() {
        return this.numOfEliminatedDiags;
    }

    public void setPercentForNextQuery(double p) {
        this.percentForNextQuery = p;
        percentIsSet = true;
    }

    public double getPercentForNextQuery() {
        return this.percentForNextQuery;
    }

    public boolean isSetPercent() {
        return this.percentIsSet;
    }

    public void setTotalNumOfEliminatedDiags(int num){
        this.totalNumOfEliminatedDiags = num;
    }

    public int getTotalNumOfEliminatedDiags(){
        return this.totalNumOfEliminatedDiags;
    }



    /*
    public void setCumulatedAlpha(double cumulatedAlpha){
        this.cumulatedAlpha = cumulatedAlpha;
    }

    public double getCumulatedAlpha(){
        return this.cumulatedAlpha;
    }
    */


}
