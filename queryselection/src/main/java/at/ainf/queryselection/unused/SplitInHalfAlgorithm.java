package at.ainf.queryselection.unused;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 10.02.11
 * Time: 16:36
 * To change this template use File | Settings | File Templates.
 */

import at.ainf.queryselection.Query;
import at.ainf.queryselection.QueryModuleDiagnosis;

import java.util.LinkedList;


public abstract class SplitInHalfAlgorithm extends AbstractQuerySelectionAlgorithm {

    public SplitInHalfAlgorithm(LinkedList<QueryModuleDiagnosis> diags, LinkedList<Query> queries) {
        super(AbstractQuerySelectionAlgorithm.TYPE_SPLIT, diags, queries, AbstractQuerySelectionAlgorithm.NAME_SPLIT);
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

			System.out.println("Query " + this.getStep() + ": Minimal number of diagnoses eliminated: " + q.getMinEliminationNumber());
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

    @Override
    public int getType() {
        return AbstractQuerySelectionAlgorithm.TYPE_SPLIT;
    }

    public String getName() {
        return AbstractQuerySelectionAlgorithm.NAME_SPLIT;
    }

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

			//System.out.println("Query " + this.getStep() + ": Minimal number of diagnoses eliminated: " + q.getMinEliminationNumber());
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

//		System.out.println("SPLIT_HALF = " + this.getStep() + " queries");
		System.out.println(this.getStep());		//
		//System.out.println(this.getName() + " " + AbstractQuerySelectionAlgorithm.SOLUTIONDIAG_MESS + this.getCurrentDiagnoses().get(0).getName());


	}
    */

}

