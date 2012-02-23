package at.ainf.queryselection;


import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 22.02.11
 * Time: 15:10
 * To change this template use File | Settings | File Templates.
 */
public class StaticRiskQSS extends MinScoreQSS implements IDistributionAdaptationQSS{

    protected double percent; // k
    protected double cumulatedAlpha = 1d;
    protected double minCumulatedAlpha = 0.1d;


    public StaticRiskQSS(double p) {
        super();
        this.percent = p;
    }

     public double getCumulatedAlpha() {
        return cumulatedAlpha;
    }

    protected double calculateNewCumulatedAlpha(double alpha) {  // returns admissible (non cumulated) alpha (according to minCumulatedAlpha)
        double newCumulatedAlpha;
        double admissibleAlpha = alpha;
        //System.out.println("calc alpha = " + alpha);
        if ((newCumulatedAlpha = this.cumulatedAlpha * alpha) < minCumulatedAlpha) {
            admissibleAlpha = minCumulatedAlpha / this.cumulatedAlpha;
            newCumulatedAlpha = minCumulatedAlpha;
        }
        this.cumulatedAlpha = newCumulatedAlpha;
        if (admissibleAlpha < 1d) {
            this.adaptationCounter++;
            //System.out.println("<<<<<<<<<<<<<<<<<<<<< ADAPT >>>>>>>>>>>>>>>>>>>> alpha = " + admissibleAlpha);
        }
        return admissibleAlpha;
    }

    protected void adaptProbabilitiesWithAlpha(double alpha) {
        //System.out.println(" + + + + + + + + + + + + adaptProbsWithAlpha");
        for (QueryModuleDiagnosis d : this.currentDiags) {
            //System.out.println("oldprob(" + d.getName() + " = " + d.getProbability());
            double newProb = 1d / (double)this.currentDiags.size() + alpha * (d.getProbability() - 1d / (double)this.currentDiags.size());
            d.setProbability(newProb);
            //System.out.println("newprob(" + d.getName() + " = " + d.getProbability());
        }
    }

    protected void adaptProbabilitiesWithCumulatedAlpha() {
        //System.out.println("Cumulated alpha = " + this.cumulatedAlpha);
        for (QueryModuleDiagnosis d : this.currentDiags) {
            //System.out.println("c_oldprob(" + d.getName() + " = " + d.getProbability());
            double newProb = 1d / (double)this.currentDiags.size() + this.cumulatedAlpha * (d.getProbability() - 1d / (double)this.currentDiags.size());
            d.setProbability(newProb);
            //System.out.println("c_oldprob(" + d.getName() + " = " + d.getProbability());
        }
    }

    public QInfo setQueryAnswer(boolean a) {
        QInfo qInfo = super.setQueryAnswer(a);
        qInfo.setPercentForNextQuery(this.percent);
        this.adaptProbabilitiesWithCumulatedAlpha();
        return qInfo;

    }

    public int getType() {
        return TYPE_STATIC_ADAPT;
    }

    public String getName() {
        return NAME_DISTADAPT;
    }

    protected int percentToNumber(double p) {
        int num = (int) Math.ceil((double) this.getCurrentDiagnoses().size() * p);
        if (num > ((double)this.getCurrentDiagnoses().size() / 2d)) {
            num--;
        }
        return num;
    }

    protected LinkedList<Query> getCandidateQueries(int minNumOfElimDiags) {
        LinkedList<Query> candidateQueries = new LinkedList<Query>();
        for (Query q : this.currentQueries) {
            /*
            if(){

            }
            */
            if (q.getMinNumOfElimDiags() == minNumOfElimDiags && q.getProbabilityOfMinNumOfElimDiagsSet() > 0.5d && q.getMinNumOfElimDiags() < q.getMaxNumOfElimDiags()) {
                candidateQueries.add(q);
            }
        }
        return candidateQueries;

    }

    protected int getMaxPossibleNumOfDiagsToEliminate() {
        return (int) Math.floor((double) (this.currentDiags.size() / 2d));
    }

    private LinkedList<Query> getQueriesWithD_0Empty(LinkedList<Query> queries) {
        LinkedList<Query> resultingQueries = new LinkedList<Query>();
        for (Query q : queries) {
            if (q.getD_0().isEmpty()) {
                resultingQueries.add(q);
            }
        }
        return resultingQueries;
    }

    protected Query[] getQueriesSortedByScore(List<Query> queries) {
        if (queries.size() == 0) {
            //System.out.println("NULL");
            return null;
        }
        Query[] qArray = new Query[queries.size()];
        for (int i = 0; i < queries.size(); i++) {
            qArray[i] = queries.get(i);
        }
        boolean unsorted = true;
        Query temp;

        while (unsorted) {
            unsorted = false;
            for (int i = 0; i < qArray.length - 1; i++)
                if (qArray[i].getScore() > qArray[i + 1].getScore()) {
                    temp = qArray[i];
                    qArray[i] = qArray[i + 1];
                    qArray[i + 1] = temp;
                    unsorted = true;
                }
        }

        return qArray;
    }

    private Query[] getQueriesSortedByAlpha(LinkedList<Query> queries) {
        if (queries.size() == 0) {
            return null;
        }
        Query[] qArray = new Query[queries.size()];
        for (int i = 0; i < queries.size(); i++) {
            qArray[i] = queries.get(i);
        }
        boolean unsorted = true;
        Query temp;

        while (unsorted) {
            unsorted = false;
            for (int i = 0; i < qArray.length - 1; i++)
                if (qArray[i].getAlpha() < qArray[i + 1].getAlpha()) {
                    temp = qArray[i];
                    qArray[i] = qArray[i + 1];
                    qArray[i + 1] = temp;
                    unsorted = true;
                }
        }

        return qArray;
    }

    protected Query selectMinScoreQuery() {
        return super.selectQuery();
    }

//        protected Query selectQueryOld(){
//            double alpha;
//            double perc = this.percent;
//            double temp;
//            if((temp=this.getMaxPossibleNumOfDiagsToEliminate()/this.currentDiags.size()) < perc){
//                perc = temp;
//                //System.out.println("admissible percent = " + perc);
//            }
//            if(this.currentQueries.isEmpty()){
//                return null;
//            }
//            int numOfDiagsToElim = percentToNumber(perc);
//            Query q;
//            if((q=selectMinScoreQuery()).getMinNumOfElimDiags() >= numOfDiagsToElim){
//                return q;
//            }
//            this.tooHighRiskCounter++;
//            /*///////////// GUI-OUTPUT /////////////////
//            System.out.println("***********************************MINSCORE TOO MUCH RISK***********************************");
//            System.out.println("The minScore-Query with D_X = " + q.d_XToString(true) + "\t\t P(D_X) = " + (q.getProb_X()));
//            System.out.println("and D_notX = " + q.d_notXToString(true) + "\t\t P(D_notX) = " + (q.getProb_notX()));
//            System.out.println("and D_0 = " + q.d_0ToString(true) + "\t\t P(D_0) = " + (q.getProb_0()));
//            System.out.println("eliminates AT LEAST: " + q.getMinNumOfElimDiags() + " diagnoses --- corresponds to " + q.getMinPercentOfElimDiags()*100f + " percent!");
//            System.out.println("BUT SHOULD eliminate AT LEAST: " + numOfDiagsToElim + " diagnoses --- corresponds to " + numOfDiagsToElim/this.currentDiags.size()*100f + " percent!");
//            System.out.println("Therefore another query is selected!!");
//            System.out.println("*******************************************************************************************");
//            *///////////////////////////////////////////
//            for( ;numOfDiagsToElim <= getMaxPossibleNumOfDiagsToEliminate();numOfDiagsToElim++){
//                LinkedList<Query> candidateQueries = getCandidateQueries(numOfDiagsToElim);     // candidateQueries = X_min,k
//                if(candidateQueries.isEmpty()){
//                    //System.out.println("No candidate queries for k = " + numOfDiagsToElim);
//                    continue;
//                }
//                LinkedList<Query> queriesWithD_0Empty;
//                if(!(queriesWithD_0Empty = getQueriesWithD_0Empty(candidateQueries)).isEmpty()){
//                    Query [] queriesSortedByScore = getQueriesSortedByScore(queriesWithD_0Empty);
//                    alpha = queriesSortedByScore[0].getAlpha();
//                    /*///////////// GUI-OUTPUT /////////////////
//                    System.out.println("++++++++++++++++++++++++++++PERFECT ALPHA AND QUERY FOUND++++++++++++++++++++++++++++");
//                    System.out.println("Calculated value of alpha = " + alpha);
//                    System.out.println("Diagnoses probability distribution before alpha adaptation:" + probDistToString());
//                    *///////////////////////////////////////////
//                    for(Diagnosis d : this.currentDiags){
//                        d.setProbability(alpha * d.getProbability() + (1-alpha) * 1/this.currentDiags.size());
//                    }
//                    /*///////////// GUI-OUTPUT /////////////////
//                    System.out.println("Diagnoses probability distribution after alpha adaptation:" + probDistToString());
//                    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
//                    *///////////////////////////////////////////
//                    this.adaptationCounter++;
//                    return queriesSortedByScore[0];
//                }else{
//                    //System.out.println("++++++++++++++++++++++++++++NO PERFECT QUERY FOUND --> SEARCH+++++++++++++++++++++++++");
//                    Query [] queriesSortedByAlpha = getQueriesSortedByAlpha(candidateQueries);
//                    ///////////////////
//                    boolean searchIsPromising = false;
//                    LinkedList<Query> queriesTemp = new LinkedList<Query>();
//                    int index = 0;
//                    for(Query qy : queriesSortedByAlpha){
//                        if(qy.getProb_0() - qy.getD_0().size()/qy.getNumOfLeadingDiags() > 0){
//                            searchIsPromising = true;
//                            queriesTemp.add(index,qy);
//                            index++;
//                        }
//                        //System.out.println("Should be > 0 --- is = " + (qy.getProb_0() - qy.getD_0().size()/qy.getNumOfLeadingDiags() ));
//                    }
//                    Query[] qTempArr = new Query[0];
//                    qTempArr = queriesTemp.toArray(qTempArr);
//                    queriesSortedByAlpha = qTempArr;
//
//                    //System.out.println("LENGTH = " + queriesSortedByAlpha.length);
//                    ///////////////////
//                    if(searchIsPromising){
//                        LinkedList<DiagnosisMemento> mementos = new LinkedList<DiagnosisMemento>();
//                        for(int i=0; i<queriesSortedByAlpha.length; i++){
//                            alpha = queriesSortedByAlpha[i].getAlpha();
//                            //System.out.println(i+1 + ". trial with alpha = " + alpha);
//                            Query currentQueryWithMaxAlpha = queriesSortedByAlpha[i];
//                            for(Diagnosis d : this.currentDiags){
//                                mementos.add(d.saveToMemento());
//                                d.setProbability(alpha * d.getProbability() + (1-alpha) * 1/this.currentDiags.size());
//                            }
//                            this.setChanged();
//                            this.notifyObservers(this.currentDiags);
//                            Query [] qsSortedByScore = this.getQueriesSortedByScore(this.currentQueries);
//                            /*////////////////////
//                            //for(Query qu : qsSortedByScore){
//                            System.out.println("The minScore-Query with D_X = " + qsSortedByScore[0].d_XToString(true) + "\t\t P(D_X) = " + (qsSortedByScore[0].getProb_X()) + "................");
//                            System.out.println("The minScore-Query with D_notX = " + qsSortedByScore[0].d_notXToString(true) + "\t\t P(D_notX) = " + (qsSortedByScore[0].getProb_notX()) + "................");
//                            System.out.println("The minScore-Query with D_0 = " + qsSortedByScore[0].d_0ToString(true) + "\t\t P(D_0) = " + (qsSortedByScore[0].getProb_0()) + "................");
//                            //}
//                            *///////////////////////
//                            if(qsSortedByScore[0].equals(currentQueryWithMaxAlpha)){
//                                this.adaptationCounter++;
//                                return currentQueryWithMaxAlpha;
//                            }else{
//                                for(Diagnosis d : this.currentDiags){
//                                    for(DiagnosisMemento m : mementos){
//                                        if(d.getName().equals(m.getSavedName())){
//                                            d.setProbability(m.getSavedProbability());
//                                        }
//                                    }
//                                }
//                                this.setChanged();
//                                this.notifyObservers(this.currentDiags);
//                                mementos.clear();
//                            }
//                        }
//                    }
//                }
//
//            }
//            Query [] allQueriesSortedByScore = this.getQueriesSortedByScore(this.currentQueries);
//            //System.out.println("No better query found - must return MinScore Query!!");
//            return allQueriesSortedByScore[0];
//        }

    protected Query selectQuery() {     // ATTENTION: condition p(dmin x) > 0.5 is very strong -- rarely fulfilled
        double alpha;
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
            }
            LinkedList<Query> queriesWithD_0Empty;
            if (!(queriesWithD_0Empty = getQueriesWithD_0Empty(candidateQueries)).isEmpty()) {
                Query[] queriesSortedByAlpha = getQueriesSortedByAlpha(queriesWithD_0Empty);
                alpha = queriesSortedByAlpha[0].getAlpha();
                /*///////////// GUI-OUTPUT /////////////////
                System.out.println("++++++++++++++++++++++++++++PERFECT ALPHA AND QUERY FOUND++++++++++++++++++++++++++++");
                System.out.println("Calculated value of alpha = " + alpha);
                System.out.println("Diagnoses probability distribution before alpha adaptation:" + probDistToString());
                *///////////////////////////////////////////
                double admissibleAlpha = this.calculateNewCumulatedAlpha(alpha);
                this.adaptProbabilitiesWithAlpha(admissibleAlpha);
                /*///////////// GUI-OUTPUT /////////////////
                System.out.println("Diagnoses probability distribution after alpha adaptation:" + probDistToString());
                System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                *///////////////////////////////////////////
                //this.adaptationCounter++;
                return queriesSortedByAlpha[0];
            } else {
                //System.out.println("++++++++++++++++++++++++++++NO PERFECT QUERY FOUND +++++++++++++++++++++++++");
                Query[] queriesSortedByAlpha = getQueriesSortedByAlpha(candidateQueries);
                alpha = queriesSortedByAlpha[0].getAlpha();
                double admissibleAlpha = this.calculateNewCumulatedAlpha(alpha);
                this.adaptProbabilitiesWithAlpha(admissibleAlpha);
                //this.adaptationCounter++;
                return queriesSortedByAlpha[0];


            }


        }
        Query[] allQueriesSortedByScore = this.getQueriesSortedByScore(this.currentQueries);
        System.out.println("No better query found - must return MinScore Query!!");
        return allQueriesSortedByScore[0];
    }


}

    
