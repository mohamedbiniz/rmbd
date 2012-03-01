package at.ainf.queryselection.unused;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 10.02.11
 * Time: 16:38
 * To change this template use File | Settings | File Templates.
 */

import at.ainf.queryselection.Query;
import at.ainf.queryselection.QueryModuleDiagnosis;

import java.util.LinkedList;


public class StaticRiskAdaptationAlgorithm extends MinScoreAlgorithm {

    protected double percent;

    protected double currentAlpha;

    public StaticRiskAdaptationAlgorithm(int type, LinkedList<QueryModuleDiagnosis> diags, LinkedList<Query> queries, double p, String name) {
        super(type, diags, queries, name);
        this.percent = p;
    }

    public StaticRiskAdaptationAlgorithm(LinkedList<QueryModuleDiagnosis> diags, LinkedList<Query> queries, double p) {
        super(TYPE_STATIC_ADAPT, diags, queries, NAME_DISTADAPT);
        this.percent = p;
    }

    public int getType() {
        return TYPE_STATIC_ADAPT;
    }

    public String getName() {
        return NAME_DISTADAPT;
    }

    /*
	public void performAlgo(){
		System.out.println(this.getName() + " " + AbstractQuerySelectionAlgorithm.START_MESS);
		System.out.println( AbstractQuerySelectionAlgorithm.PROBAPRIORI_MESS + this.probDistToString());
		System.out.println( AbstractQuerySelectionAlgorithm.LINE_SEP);
		while(this.getCurrentDiagnoses().size() > 1){
			this.setStep(this.getStep() + 1);

			Query q = this.selectQuery();
			int answer;
			double priorAdaptEliminationPercentage;

			System.out.println("Number of current diagnoses: " + this.getCurrentDiagnoses().size());

			if(( priorAdaptEliminationPercentage=q.getMinEliminationPercentage() ) < percent){
				this.performAdaptation();
				q = this.selectQuery();

				System.out.println("Selected minimal Score Query eliminates only " + priorAdaptEliminationPercentage*100 + " percent, but any query must eliminate at least " + this.percent*100 + " percent of remaining diagnoses!");
				if(this.currentAlpha == 1.0){
					System.out.println("No suitable query found!   Probability distribution adaptation performed with parameter alpha = " + this.currentAlpha);
					System.out.println( AbstractQuerySelectionAlgorithm.PROBPOSTADAPT_MESS + this.probDistToString());
					System.out.println("The new minimal score query (Query" + this.getStep() + ") is the old minimal score query. It will eliminate at least " + q.getMinEliminationPercentage()*100 + " percent of the remaining diagnoses");
				}else{
					System.out.println("Therefore:   Probability distribution adaptation performed with parameter alpha = " + this.currentAlpha);
					System.out.println( AbstractQuerySelectionAlgorithm.PROBPOSTADAPT_MESS + this.probDistToString());
					System.out.println("The new minimal score query (Query" + this.getStep() + ") for the adapted probability distribution will eliminate at least " + q.getMinEliminationPercentage()*100 + " percent of the remaining diagnoses");
				}

			}


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

    protected Query selectQuery() {
        return super.selectQuery();
    }

    protected void performAdaptation() {
        double alpha = this.determineAlpha();
        this.adaptProbabilities(alpha);
    }

    protected double determineAlpha() {
        LinkedList<Query> queries = this.getCurrentQueries();
        LinkedList<Query> candidateQueries = new LinkedList<Query>();
        boolean candidateQueryFound = false;
        boolean specialCase = false;
        double alpha;
        int numForWhichQueriesExist;
        int minNumToEliminate = this.percentToNumber();
        //System.out.println("+++++++++++++++++++++++++++++++++++++    minNumToElim = " + minNumToEliminate);
        int numOfCurrentDiags = this.getCurrentDiagnoses().size();
        //System.out.println("+++++++++++++++++++++++++++++++++++++    currDiags = " + numOfCurrentDiags);
        int numToEliminate;

        if ((numToEliminate = (numOfCurrentDiags - minNumToEliminate)) < minNumToEliminate) {
            specialCase = true;
            //System.out.println("specialCase!!!");
        }

        while (!candidateQueryFound) {
            if (!specialCase) {
                if (minNumToEliminate <= Math.floor(numOfCurrentDiags / 2)) {
                    for (Query q : queries) {
                        if ((numForWhichQueriesExist = q.getMinNumOfElimDiags()) == minNumToEliminate) {
                            candidateQueryFound = true;
                            //System.out.println("candidFound!");
                            //System.out.println("q-min elim num = " + numForWhichQueriesExist);
                            break;
                        }
                    }
                    if (!candidateQueryFound) {
                        minNumToEliminate++;
                        //System.out.println("minNumElim = " + minNumToEliminate);
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            } else {
                for (Query q : queries) {
                    if ((numForWhichQueriesExist = q.getMinNumOfElimDiags()) == numToEliminate) {
                        candidateQueryFound = true;
                        minNumToEliminate = numToEliminate;
                        break;
                    }
                }
            }
        }  // ab hier beinhaltet minNumToEliminate die minimale Anzahl an Diagnosen (> k prozent ), die mindestans eliminiert werden können (d.h. für die es mindestens eine query gibt)
        for (Query q : queries) {
            if ((q.getMinNumOfElimDiags() == minNumToEliminate)) {
                if (((q.getD_XProbPositive().size() == minNumToEliminate) && (q.getProb_X() >= 0.5)) || ((q.getD_notXProbPositive().size() == minNumToEliminate) && (q.getProb_notX() >= 0.5))) {
                    candidateQueries.add(q);
                    //System.out.println("candidAdd! p(dx) = " + q.getProb_X() + " p(dnx) = " + q.getProb_notX() + " num_dx = " + q.getD_XProbPositive().size() + " num_dnx = " + q.getD_notXProbPositive().size());
                }
            }
        }
        if (!candidateQueries.isEmpty()) {
            if (numOfCurrentDiags != 3) {
                Query minScoreQuery = candidateQueries.get(0);
                for (Query c : candidateQueries) {
                    if (c.getScore() < minScoreQuery.getScore()) {
                        minScoreQuery = c;
                        //System.out.println("minScoreQu altered!");
                        //System.out.println("new minScoreQu: p(dx) = " + c.getProb_X());

                    }
                }
                double p;
                if ((minScoreQuery.getD_XProbPositive().size() + minScoreQuery.getD_0ProbPositive().size()) == minNumToEliminate) {
                    p = minScoreQuery.getProb_X() + minScoreQuery.getProb_0();
                    //System.out.println("p (dx+d0) = " + p);
                } else {
                    p = minScoreQuery.getProb_notX() + minScoreQuery.getProb_0();
                    //System.out.println("p (dnx+d0) = " + p);
                }
                double queryEliminatesPercent = (float) minNumToEliminate / (float) numOfCurrentDiags;
                //System.out.println("QuEliminatesPercent = " + queryEliminatesPercent);
                alpha = (0.5f - queryEliminatesPercent) / ((float) p - queryEliminatesPercent);
                //System.out.println("alpha = " + alpha);
            } else {
                alpha = 1.0f;
            }
            this.currentAlpha = alpha;
            return alpha;
        } else {
            alpha = 1.0f;     // no suitable query found, set alpha = 1 and continue without adaptation taking the original minScore query
            this.currentAlpha = alpha;
            return alpha;
        }

    }

    protected int percentToNumber() {
        int minNumOfEliminatedDiags = (int) Math.ceil((double) this.getCurrentDiagnoses().size() * percent);
        return minNumOfEliminatedDiags;
    }

    /*
	public void performAlgoBW(){
		//System.out.println(this.getName() + " " + AbstractQuerySelectionAlgorithm.START_MESS);
		//System.out.println( AbstractQuerySelectionAlgorithm.PROBAPRIORI_MESS + this.probDistToString());
		//System.out.println( AbstractQuerySelectionAlgorithm.LINE_SEP);
		while(this.getCurrentDiagnoses().size() > 1){
			this.setStep(this.getStep() + 1);

			Query q = this.selectQuery();
			int answer;
			double priorAdaptEliminationPercentage;

			//System.out.println("Number of current diagnoses: " + this.getCurrentDiagnoses().size());

			if(( priorAdaptEliminationPercentage=q.getMinEliminationPercentage() ) < percent){
				this.performAdaptation();
				q = this.selectQuery();

				//System.out.println("Selected minimal Score Query eliminates only " + priorAdaptEliminationPercentage + ", but any query must eliminate at least " + this.percent + " of remaining diagnoses!");
				//System.out.println("Therefore:   Probability distribution adaptation performed with parameter alpha = " + this.currentAlpha);
				//System.out.println( AbstractQuerySelectionAlgorithm.PROBPOSTADAPT_MESS + this.probDistToString());
				//System.out.println("The new minimal score query (Query" + this.getStep() + ") for the adapted probability distribution will eliminate at least " + q.getMinEliminationPercentage() + " percent of the remaining diagnoses");
			}


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

//		System.out.println("DIST_ADAPT = " + this.getStep() + " queries");
		System.out.println(this.getStep());				//
		//System.out.println(this.getName() + " " + AbstractQuerySelectionAlgorithm.SOLUTIONDIAG_MESS + this.getCurrentDiagnoses().get(0).getName());

	}
    */

}

