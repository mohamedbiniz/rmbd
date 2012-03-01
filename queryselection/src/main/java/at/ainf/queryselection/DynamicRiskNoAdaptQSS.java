package at.ainf.queryselection;

import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 18.04.11
 * Time: 13:04
 * To change this template use File | Settings | File Templates.
 */
public class DynamicRiskNoAdaptQSS extends DynamicRiskQSS {

    public DynamicRiskNoAdaptQSS(RiskPreferences rp, RiskUpdateStrategy rus) {
        super(rp, rus);
    }

    protected Query selectQuery() {
        double perc = this.percent;
        double temp;
        if ((temp = (double)this.getMaxPossibleNumOfDiagsToEliminate() / (double)this.currentDiags.size()) < perc) {
            perc = temp;
            //System.out.println("admissible percent = " + perc);
        }
        if (this.currentQueries.isEmpty()) {
            return null;
        }
        int numOfDiagsToElim = percentToNumber(perc);
        Query q;
        if ((q = selectMinScoreQuery()).getMinNumOfElimDiags() >= numOfDiagsToElim) {
            //System.out.println("DynRiskNoAdapt: MinScore query has score: " + q.getPartitionScore());
            return q;
        }
        this.tooHighRiskCounter++;
        /*///////////// GUI-OUTPUT /////////////////
        System.out.println("***********************************MINSCORE TOO MUCH RISK***********************************");
        System.out.println("The minScore-Query with D_X = " + q.d_XToString(true) + "\t\t P(D_X) = " + (q.getProb_X()));
        System.out.println("and D_notX = " + q.d_notXToString(true) + "\t\t P(D_notX) = " + (q.getProb_notX()));
        System.out.println("and D_0 = " + q.d_0ToString(true) + "\t\t P(D_0) = " + (q.getProb_0()));
        System.out.println("eliminates AT LEAST: " + q.getMinNumOfElimDiags() + " diagnoses --- corresponds to " + q.getMinPercentOfElimDiags()*100f + " percent!");
        System.out.println("BUT SHOULD eliminate AT LEAST: " + numOfDiagsToElim + " diagnoses --- corresponds to " + numOfDiagsToElim/this.currentDiags.size()*100f + " percent!");
        System.out.println("Therefore another query is selected!!");
        System.out.println("*******************************************************************************************");
        *///////////////////////////////////////////
        for (; numOfDiagsToElim <= getMaxPossibleNumOfDiagsToEliminate(); numOfDiagsToElim++) {
            LinkedList<Query> candidateQueries = getCandidateQueries(numOfDiagsToElim);     // candidateQueries = X_min,k
            if (candidateQueries.isEmpty()) {
                //System.out.println("No candidate queries for k = " + numOfDiagsToElim);
                continue;
            } else {
                //System.out.println("Candidate query for k = " + numOfDiagsToElim + " found!");
                Query[] candidateQueriesSortedByScore = getQueriesSortedByScore(candidateQueries);
                return candidateQueriesSortedByScore[0];
            }
        }
        Query[] allQueriesSortedByScore = this.getQueriesSortedByScore(this.currentQueries);
        //System.out.println("No better query found - must return MinScore Query!!");
        return allQueriesSortedByScore[0];
    }

}
