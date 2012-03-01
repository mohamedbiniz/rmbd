package at.ainf.queryselection.unused;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 10.02.11
 * Time: 16:35
 * To change this template use File | Settings | File Templates.
 */

import at.ainf.queryselection.Query;
import at.ainf.queryselection.QueryModuleDiagnosis;

import java.util.Collection;
import java.util.LinkedList;


public class MinScoreAlgorithm<T> extends AbstractQuerySelectionAlgorithm {

    public MinScoreAlgorithm(int type, LinkedList<QueryModuleDiagnosis> diags, LinkedList<Query> queries, String name) {
        super(type, diags, queries, name);
    }

    @Override
    public PostQueryInfo getPostQueryInfo() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    public Collection<Collection<T>> getCurrentDiagnoses() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public MinScoreAlgorithm(LinkedList<QueryModuleDiagnosis> diags, LinkedList<Query> queries) {
        super(TYPE_MINSCORE, diags, queries, NAME_MINSCORE);
    }

    public int getType() {
        return TYPE_MINSCORE;
    }

    public String getName() {
        return NAME_MINSCORE;
    }

    @Override
    protected Query selectQuery() {
        boolean isMinUnique = true;
        double minScore = 0d;
        LinkedList<Query> minScoreQueries = new LinkedList<Query>();
        Query[] sortedQueryArray = this.getQueriesSortedByScore();
        if (sortedQueryArray == null) {
            return null;
        }
        if (sortedQueryArray.length > 1 && (minScore = sortedQueryArray[0].getScore()) == sortedQueryArray[1].getScore()) {
            //isMinUnique = false;
        }
        if (!isMinUnique) {
            for (int i = 0; i < sortedQueryArray.length; i++) {
                if (sortedQueryArray[i].getScore() == minScore) {
                    minScoreQueries.add(sortedQueryArray[i]);
                } else {
                    break;
                }
            }
            int randomQueryNum = (int) Math.floor(Math.random() * minScoreQueries.size());
            return minScoreQueries.get(randomQueryNum);
        } else {
            return sortedQueryArray[0];
        }

    }


    public Query getQuery() {
        return this.selectQuery();
    }

    /*
     @Override
     public void performAlgo() {
         System.out.println(this.getName() + " " + AbstractQuerySelectionAlgorithm.START_MESS);
         System.out.println( AbstractQuerySelectionAlgorithm.PROBAPRIORI_MESS + this.probDistToString());
         System.out.println( AbstractQuerySelectionAlgorithm.LINE_SEP);
         while(this.getCurrentDiagnoses().size() > 1){
             this.setStep(this.getStep() + 1);

             Query q = this.selectQuery();
             int answer;

             System.out.println("Number of current diagnoses: " + this.getCurrentDiagnoses().size());

             System.out.println("Query " + this.getStep() + ": has score = " + q.getPartitionScore());
             System.out.println("Query " + this.getStep() + ": D_X = " + q.d_XToString(true) + "\t\t P(D_X) = " + q.getProb_X());
             System.out.println("Query " + this.getStep() + ": D_notX = " + q.d_notXToString(true) + "\t\t P(D_notX) = " + q.getProb_notX());
             System.out.println("Query " + this.getStep() + ": D_0 = " + q.d_0ToString(true) + "\t\t P(D_0) = " + q.getProb_0());
             System.out.println("Query " + this.getStep() + ": Answer = " + (answer = q.getAnswer()));

             if(answer == 1){
                 System.out.println( AbstractQuerySelectionAlgorithm.QUERYRESULT_MESS + AbstractQuerySelectionAlgorithm.POSRESULT_MESS + "{" + q.d_XToStringProbPositive(false) + q.d_0ToStringProbPositive(false) + "}");
             }else{
                 System.out.println( AbstractQuerySelectionAlgorithm.QUERYRESULT_MESS + AbstractQuerySelectionAlgorithm.NEGRESULT_MESS + "{" + q.d_notXToStringProbPositive(false) + q.d_0ToStringProbPositive(false) + "}");
             }
             this.updateProbabilities(q, answer);
             this.updateQueries(q);
             System.out.println( AbstractQuerySelectionAlgorithm.PROBPOST_MESS + this.probDistToString());
             System.out.println( AbstractQuerySelectionAlgorithm.LINE_SEP);
         }

         System.out.println(this.getName() + " " + AbstractQuerySelectionAlgorithm.SOLUTIONSTEPS_MESS + this.getStep() + " queries");
         System.out.println(this.getName() + " " + AbstractQuerySelectionAlgorithm.SOLUTIONDIAG_MESS + this.getCurrentDiagnoses().get(0).getName());


     }
     */


    /*
     public void performAlgoBW() {
         //System.out.println(this.getName() + " " + AbstractQuerySelectionAlgorithm.START_MESS);
         //System.out.println( AbstractQuerySelectionAlgorithm.PROBAPRIORI_MESS + this.probDistToString());
         //System.out.println( AbstractQuerySelectionAlgorithm.LINE_SEP);
         while(this.getCurrentDiagnoses().size() > 1){
             this.setStep(this.getStep() + 1);

             Query q = this.selectQuery();
             int answer;

             //System.out.println("Number of current diagnoses: " + this.getCurrentDiagnoses().size());

             //System.out.println("Query " + this.getStep() + ": has score = " + q.getPartitionScore());
             //System.out.println("Query " + this.getStep() + ": D_X = " + q.d_XToString(true) + "\t\t P(D_X) = " + q.getProb_X());
             //System.out.println("Query " + this.getStep() + ": D_notX = " + q.d_notXToString(true) + "\t\t P(D_notX) = " + q.getProb_notX());
             //System.out.println("Query " + this.getStep() + ": D_0 = " + q.d_0ToString(true) + "\t\t P(D_0) = " + q.getProb_0());
             //System.out.println("Query " + this.getStep() + ": Answer = " + (answer = q.getAnswer()));

             answer = q.getAnswer();

             if(answer == 1){
                 //System.out.println( AbstractQuerySelectionAlgorithm.QUERYRESULT_MESS + AbstractQuerySelectionAlgorithm.POSRESULT_MESS + "{" + q.d_XToStringProbPositive(false) + q.d_0ToStringProbPositive(false) + "}");
             }else{
                 //System.out.println( AbstractQuerySelectionAlgorithm.QUERYRESULT_MESS + AbstractQuerySelectionAlgorithm.NEGRESULT_MESS + "{" + q.d_notXToStringProbPositive(false) + q.d_0ToStringProbPositive(false) + "}");
             }
             this.updateProbabilities(q, answer);
             this.updateQueries(q);
             //System.out.println( AbstractQuerySelectionAlgorithm.PROBPOST_MESS + this.probDistToString());
             //System.out.println( AbstractQuerySelectionAlgorithm.LINE_SEP);
         }

 //		System.out.println("MIN__SCORE = " + this.getStep() + " queries");
         System.out.println(this.getStep());		//
         //System.out.println(this.getName() + " " + AbstractQuerySelectionAlgorithm.SOLUTIONDIAG_MESS + this.getCurrentDiagnoses().get(0).getName());


     }
     */


}
