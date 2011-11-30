package at.ainf.queryselection;

import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 06.06.11
 * Time: 17:55
 * To change this template use File | Settings | File Templates.
 */
public class ScoreAdaptQSS extends MinScoreQSS{

    private double currentScore;
    private double maxCurrentScore;

    public ScoreAdaptQSS(double initialScore, double maxScore){
        this.currentScore = initialScore;
        this.maxCurrentScore = maxScore;
    }



    protected Query selectQuery() {
        Query[] sortedQueryArray = this.getQueriesSortedByScore();
        if (sortedQueryArray == null) {
            return null;
        }
        Query q;
        for(int i=0; i<sortedQueryArray.length; i++){
            if((q=sortedQueryArray[i]).getScore() >= currentScore){
                return q;
            }
        }
        Query splitQuery = sortedQueryArray[0];
        for(int i=0; i<sortedQueryArray.length; i++){
            if((q=sortedQueryArray[i]).getMinNumOfElimDiags() > splitQuery.getMinNumOfElimDiags()){
                splitQuery = q;
            }
            if(splitQuery.getMinNumOfElimDiags() >= currentDiags.size() / 2){
                break;
            }
        }
        return splitQuery;
    }

    private void updateCurrentScore(boolean bonus, double bias){
        if(bonus == true){
            currentScore += - 0.1d;//- Math.abs(bias)/2d;
            if(currentScore < 0d){
                currentScore = 0d;
            }
        }else{
            currentScore += 0.1d;//+ Math.abs(bias)/2d;
            if(currentScore > maxCurrentScore){
                currentScore = maxCurrentScore;
            }
        }
    }


    public QInfo setQueryAnswer(boolean a) {
        LinkedList<QueryModuleDiagnosis> diagsToEliminate = this.getDiagsEliminatedByQuery(a);
        boolean bonus;
        double bias = askedQuery.getProb_X() - askedQuery.getProb_notX();
        if(a == true){
            if(bias >= 0d){
                bonus = true;
            }else{
                bonus = false;
            }
        }else{  // a == false
            if(bias < 0d){
                bonus = true;
            }else{
                bonus = false;
            }
        }

        updateCurrentScore(bonus,bias);

        for (QueryModuleDiagnosis d : diagsToEliminate) {
            this.currentDiags.remove(d);
        }

        QInfo qInfo = new QInfo();
        qInfo.setCurrentDiags(this.getCurrentDiagnoses());  // here the probability update takes place
        qInfo.setIteration(this.step);
        qInfo.setNumberOfEliminatedDiags(diagsToEliminate.size());
        qInfo.setCurrentScore(currentScore);

        return qInfo;
    }

}
