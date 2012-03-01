package at.ainf.queryselection;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 12.05.11
 * Time: 12:44
 * To change this template use File | Settings | File Templates.
 */
public class PenaltyQSS extends MinScoreQSS{

    double penalty = 0d;
    double maxPenalty;

    public PenaltyQSS(double maxPenalty){
        this.maxPenalty = maxPenalty;
    }

    @Override
    public int getType() {
        return 0;     //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


//    @Override
//    protected Query selectQuery() {
//        //LinkedList<Query> queriesSortedByMinMaxPenalty = new LinkedList<Query>();
//        Query qWithMinMaxPenalty = null;
//        Query q;
//        ///////////////////////////
//        int count = 1;
//        ///////////////////////////
//        while( (q=super.selectQuery()) != null && exceedsMaxPenalty( q )){
//            this.currentQueries.remove(q);
//            //queriesSortedByMaxPenalty = insertSorted(q,queriesSortedByMinMaxPenalty);
//            qWithMinMaxPenalty = updateQueryWithMinMaxPenalty(q,qWithMinMaxPenalty);
//            System.out.println(count + ". trial!");
//        }
//        /*
//        if(!queriesSortedByMaxPenalty.isEmpty()){
//                tooHighRiskCounter++;
//        }
//        */
//        if(qWithMinMaxPenalty != null){
//            tooHighRiskCounter++;        // Minscore would take too much risk
//        }
//        if(q != null){
//            return q;
//        }else{
//            //return queriesSortedByMaxPenalty.getFirst();
//            return qWithMinMaxPenalty;
//        }
//    }


    protected Query selectQuery() {
        LinkedList<Query> candidateQueries = new LinkedList<Query>();
        Query qWithMinMaxPenalty = null;
        for(Query q : currentQueries){
            if(!exceedsMaxPenalty(q)){
                candidateQueries.add(q);
                qWithMinMaxPenalty = updateQueryWithMinMaxPenalty(q,qWithMinMaxPenalty);
            }
        }
        if( !candidateQueries.isEmpty() ){
            Collections.sort(candidateQueries,new QueryScoreComparator());
            /*//////////////////
            for(Query q : candidateQueries){
                System.out.println("sc = " + q.getPartitionScore());
            }
            *//////////////////
            return candidateQueries.getFirst();
        }else{
            return qWithMinMaxPenalty;
        }

    }

    protected Query[] sortQueriesByScore(List<Query> queries){
        if (queries.size() == 0) {
            return null;
        }
        Query[] qArray = new Query[queries.size()];
        for (int i = 0; i < queries.size(); i++) {
            qArray[i] = this.currentQueries.get(i);
        }

        /////////Bubble Sort//////////////
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
        //////////////////////////////////

        return qArray;
    }

    public QInfo setQueryAnswer(boolean answer){
        QInfo qInfo = super.setQueryAnswer(answer);
        int numOfElimDiags = qInfo.getNumOfEliminatedDiags();

        /*///////////////////////////
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("Max Penalty: " + this.maxPenalty);
        System.out.println("Old Penalty: " + this.penalty);
        *////////////////////////////

        updatePenalty(askedQuery,numOfElimDiags);

        /*///////////////////////////
        System.out.println("New Penalty: " + this.penalty);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        *////////////////////////////

        return qInfo;

    }

    private boolean exceedsMaxPenalty(Query q){
        boolean exceeds = false;
        if(penalty + getMaxPenaltyOfQuery(q) > maxPenalty){
            exceeds = true;
        }
        return exceeds;
    }

    private int getMaxPenaltyOfQuery(Query q){   // to obtain (preference) ordering of queries
        /*
        if( (  (int)Math.floor((double)q.getNumOfLeadingDiags()/(double)2) - q.getMinNumOfElimDiags() ) == 0){
             return -1;
        }else{
            return (int)Math.floor((double)q.getNumOfLeadingDiags()/(double)2) - q.getMinNumOfElimDiags();
        }
        */
        return (int)Math.floor((double)q.getNumOfAllDiags()/(double)2) - q.getMinNumOfElimDiags();
    }

    private Query updateQueryWithMinMaxPenalty(Query q, Query currentQueryWithMinMaxPenalty){
        if(currentQueryWithMinMaxPenalty == null){
            currentQueryWithMinMaxPenalty = q;
        }else{
            if(getMaxPenaltyOfQuery(q) < getMaxPenaltyOfQuery(currentQueryWithMinMaxPenalty)){
                currentQueryWithMinMaxPenalty = q;
            }
        }
        return currentQueryWithMinMaxPenalty;
    }

    private void updatePenalty(Query q, int numOfEliminatedDiags){
        penalty += getPenaltyOfAnsweredQuery(q,numOfEliminatedDiags);
    }

    private double getPenaltyOfAnsweredQuery(Query q, int numOfEliminatedDiags){
        return (int)Math.floor((double)q.getNumOfAllDiags()/(double)2) - numOfEliminatedDiags;
    }

    /*
    private LinkedList<Query> insertSorted(Query q, LinkedList<Query> list){
        if(list.isEmpty()){
            list.add(q);
        }else{
            for(int i=0; i<list.size(); i++){
                if(i<list.size()-1){
                    if(getMaxPenaltyOfQuery(q) <= getMaxPenaltyOfQuery(list.get(i)) ){
                        list.add(i,q);
                    }
                }else{
                    if(getMaxPenaltyOfQuery(q) <= getMaxPenaltyOfQuery(list.get(i)) ){
                        list.add(i,q);
                    }else{
                        list.addLast(q);
                    }
                }

            }
        }
        return list;
    }
    */

}
