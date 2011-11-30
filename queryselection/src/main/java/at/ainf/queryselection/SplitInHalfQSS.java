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
public class SplitInHalfQSS extends AbstractQuerySelectionStrategy {

    /*
    public SplitInHalfQSS(LinkedList<Diagnosis> diags, LinkedList<Query> queries){
            super( AbstractQuerySelectionStrategy.TYPE_SPLIT,AbstractQuerySelectionStrategy.NAME_SPLIT);
    }
    */

    public SplitInHalfQSS() {
        super();
    }

    @Override
    protected Query selectQuery() {
        boolean isMaxUnique = true;
        int maxEliminationNum = 0;
        LinkedList<Query> maxEliminationQueries = new LinkedList<Query>();
        Query[] sortedQueryArray = this.getQueriesSortedByEliminationRate();
        if (sortedQueryArray == null) {
            return null;
        }
        if (sortedQueryArray.length > 1 && (maxEliminationNum = sortedQueryArray[0].getMinNumOfElimDiags()) == sortedQueryArray[1].getMinNumOfElimDiags()) {
            //isMaxUnique = false;
        }
        if (!isMaxUnique) {
            for (int i = 0; i < sortedQueryArray.length; i++) {
                if (sortedQueryArray[i].getMinNumOfElimDiags() == maxEliminationNum) {
                    maxEliminationQueries.add(sortedQueryArray[i]);
                } else {
                    break;
                }
            }
            int randomQueryNum = (int) Math.floor(Math.random() * maxEliminationQueries.size());
            return maxEliminationQueries.get(randomQueryNum);
        } else {
            return sortedQueryArray[0];
        }

    }


    public List<QueryModuleDiagnosis> getCurrentDiagnoses() {

        /*if(this.currentDiags.isEmpty()){
            if((this.currentDiags = this.diagnosisProvider.getDiagnoses(this.numOfLeadingDiagnoses)).isEmpty()){
                throw new NoDiagnosisFoundException();
            }
        }else
        */
        if (this.currentDiags.size() == this.numOfLeadingDiagnoses) {
            return this.currentDiags;
        } else {
            List<QueryModuleDiagnosis> newDiags = this.diagnosisProvider.getDiagnoses(this.numOfLeadingDiagnoses - this.currentDiags.size());
            if (!newDiags.isEmpty()) {
                for (QueryModuleDiagnosis d : newDiags) {
                    this.currentDiags.add(d);
                }
            }
        }
        this.normalizeProbabilities();
        return this.currentDiags;
    }


    /*
    public void performAlgo() {
        System.out.println(this.getName() + " " + AbstractQuerySelectionStrategy.START_MESS);
        System.out.println( AbstractQuerySelectionStrategy.PROBAPRIORI_MESS + this.probDistToString());
        System.out.println( AbstractQuerySelectionStrategy.LINE_SEP);
        while(this.getCurrentDiagnoses().size() > 1){
            this.setStep(this.getStep() + 1);

            Query q = this.selectQuery();
            int answer;

            System.out.println("Number of current diagnoses: " + this.getCurrentDiagnoses().size());

            System.out.println("Query " + this.getStep() + ": Minimal number of diagnoses eliminated: " + q.getMinEliminationNumber());
            System.out.println("Query " + this.getStep() + ": D_X = " + q.d_XToString(true) + "\t\t P(D_X) = " + q.getProb_X());
            System.out.println("Query " + this.getStep() + ": D_notX = " + q.d_notXToString(true) + "\t\t P(D_notX) = " + q.getProb_notX());
            System.out.println("Query " + this.getStep() + ": D_0 = " + q.d_0ToString(true) + "\t\t P(D_0) = " + q.getProb_0());
            System.out.println("Query " + this.getStep() + ": Answer = " + (answer = q.getAnswer()));

            if(answer == 1){
                System.out.println( AbstractQuerySelectionStrategy.QUERYRESULT_MESS + AbstractQuerySelectionStrategy.POSRESULT_MESS + "{" + q.d_XToStringProbPositive(false) + q.d_0ToStringProbPositive(false) + "}");
            }else{
                System.out.println( AbstractQuerySelectionStrategy.QUERYRESULT_MESS + AbstractQuerySelectionStrategy.NEGRESULT_MESS + "{" + q.d_notXToStringProbPositive(false) + q.d_0ToStringProbPositive(false) + "}");
            }
            this.updateProbabilities(q, answer);
            this.updateQueries(q);
            System.out.println( AbstractQuerySelectionStrategy.PROBPOST_MESS + this.probDistToString());
            System.out.println( AbstractQuerySelectionStrategy.LINE_SEP);
        }

        System.out.println(this.getName() + " " + AbstractQuerySelectionStrategy.SOLUTIONSTEPS_MESS + this.getStep() + " queries");
        System.out.println(this.getName() + " " + AbstractQuerySelectionStrategy.SOLUTIONDIAG_MESS + this.getCurrentDiagnoses().get(0).getName());


    }
    */

    @Override
    public int getType() {
        return TYPE_SPLIT;
    }

    public String getName() {
        return NAME_SPLIT;
    }

    /*
    public void performAlgoBW() {
        //System.out.println(this.getName() + " " + AbstractQuerySelectionStrategy.START_MESS);
        //System.out.println( AbstractQuerySelectionStrategy.PROBAPRIORI_MESS + this.probDistToString());
        //System.out.println( AbstractQuerySelectionStrategy.LINE_SEP);
        while(this.getCurrentDiagnoses().size() > 1){
            this.setStep(this.getStep() + 1);

            Query q = this.selectQuery();
            int answer;

            //System.out.println("Number of current diagnoses: " + this.getCurrentDiagnoses().size());

            //System.out.println("Query " + this.getStep() + ": Minimal number of diagnoses eliminated: " + q.getMinEliminationNumber());
            //System.out.println("Query " + this.getStep() + ": D_X = " + q.d_XToString(true) + "\t\t P(D_X) = " + q.getProb_X());
            //System.out.println("Query " + this.getStep() + ": D_notX = " + q.d_notXToString(true) + "\t\t P(D_notX) = " + q.getProb_notX());
            //System.out.println("Query " + this.getStep() + ": D_0 = " + q.d_0ToString(true) + "\t\t P(D_0) = " + q.getProb_0());
            //System.out.println("Query " + this.getStep() + ": Answer = " + (answer = q.getAnswer()));

            answer = q.getAnswer();

            if(answer == 1){
                //System.out.println( AbstractQuerySelectionStrategy.QUERYRESULT_MESS + AbstractQuerySelectionStrategy.POSRESULT_MESS + "{" + q.d_XToStringProbPositive(false) + q.d_0ToStringProbPositive(false) + "}");
            }else{
                //System.out.println( AbstractQuerySelectionStrategy.QUERYRESULT_MESS + AbstractQuerySelectionStrategy.NEGRESULT_MESS + "{" + q.d_notXToStringProbPositive(false) + q.d_0ToStringProbPositive(false) + "}");
            }
            this.updateProbabilities(q, answer);
            this.updateQueries(q);
            //System.out.println( AbstractQuerySelectionStrategy.PROBPOST_MESS + this.probDistToString());
            //System.out.println( AbstractQuerySelectionStrategy.LINE_SEP);
        }

//		System.out.println("SPLIT_HALF = " + this.getStep() + " queries");
        System.out.println(this.getStep());		//
        //System.out.println(this.getName() + " " + AbstractQuerySelectionStrategy.SOLUTIONDIAG_MESS + this.getCurrentDiagnoses().get(0).getName());


    }
    */

}
