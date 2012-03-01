package at.ainf.queryselection.unused;

import at.ainf.queryselection.Query;
import at.ainf.queryselection.QueryModuleDiagnosis;

import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 10.02.11
 * Time: 16:39
 * To change this template use File | Settings | File Templates.
 */
public class DynamicRiskAdaptationAlgorithm extends StaticRiskAdaptationAlgorithm {

    private double kUpperBound = 0.5f; //0.4f;
    private double kLowerBound = 0.0f; //0.1f;
    private int kAdaptStrategy;
    private boolean kUpperBoundFixed = false;

    public DynamicRiskAdaptationAlgorithm(LinkedList<QueryModuleDiagnosis> diags, LinkedList<Query> queries, double p, int strategy) {
        super(AbstractQuerySelectionAlgorithm.TYPE_DYNAMIC_ADAPT, diags, queries, p, AbstractQuerySelectionAlgorithm.NAME_VARDAA);
        this.kAdaptStrategy = strategy;
    }

    public String getName() {
        return AbstractQuerySelectionAlgorithm.NAME_VARDAA;
    }

    public int getType() {
        return AbstractQuerySelectionAlgorithm.TYPE_DYNAMIC_ADAPT;
    }

    public void updateKUpperBound(int numOfDiags) { ////////////////////////////////////////////////// ACHTUNG: edit!!! Epsilon
        double epsilon = 0.1f;
        this.kUpperBound = (Math.floor((double) (numOfDiags / 2) - epsilon)) / numOfDiags;
        //( Math.floor((double)this.getCurrentDiagnoses().size()/2))/  this.getCurrentDiagnoses().size();
    }

    /*
	public void performAlgo(){
		System.out.println(this.getName() + " " + AbstractQuerySelectionAlgorithm.START_MESS);
		System.out.println( AbstractQuerySelectionAlgorithm.PROBAPRIORI_MESS + this.probDistToString());
		System.out.println( AbstractQuerySelectionAlgorithm.PROB_ENTROPY + this.getDistributionEntropy() + "    numOfDiags = " + this.getCurrentDiagnoses().size());
		System.out.println( AbstractQuerySelectionAlgorithm.LINE_SEP);
		while(this.getCurrentDiagnoses().size() > 1){
			this.setStep(this.getStep() + 1);

			System.out.println("++++++ current k = " + this.percent);
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
			if(!kUpperBoundFixed){
				this.updateKUpperBound(this.getCurrentDiagnoses().size() - q.getNumOfEliminatedDiags(q.getAnswer()));
			}
			this.determinePercent(q,this.kAdaptStrategy,this.kLowerBound,this.kUpperBound);
			this.updateProbabilities(q, answer);
			this.updateQueries(q);



			System.out.println( AbstractQuerySelectionAlgorithm.PROBPOST_MESS + this.probDistToString());
			System.out.println( AbstractQuerySelectionAlgorithm.PROB_ENTROPY + this.getDistributionEntropy() + "    numOfDiags = " + this.getCurrentDiagnoses().size());
			System.out.println( AbstractQuerySelectionAlgorithm.LINE_SEP);
		}

		System.out.println(this.getName() + " " + AbstractQuerySelectionAlgorithm.SOLUTIONSTEPS_MESS + this.getStep() + " queries");
		System.out.println(this.getName() + " " + AbstractQuerySelectionAlgorithm.SOLUTIONDIAG_MESS + this.getCurrentDiagnoses().get(0).getName());

	}
    */

    protected void/*double*/ determinePercent(Query q, int strategy, double kLowerBound, double kUpperBound) {

        switch (strategy) {
            case 10: {
                double increment = 1.0f;
                if (q.getNumOfEliminatedDiags(q.getAnswer()) / q.getNumOfAllDiags() < 0.5f) {
                    if (this.percent <= kUpperBound - increment) {
                        this.percent += increment;
                    } else {
                        this.percent = kUpperBound;
                    }
                } else {
                    if (this.percent >= kLowerBound + increment) {
                        this.percent -= increment;
                    } else {
                        this.percent = kLowerBound;
                    }
                }
                break;
            }
            case 11: {
                double proportion = 0f;
                double increment = 1.0f;
                if ((proportion = (q.getNumOfEliminatedDiags(q.getAnswer()) / Math.floor((double) q.getNumOfAllDiags() / (double) 2))) < 1.0f) {
                    increment = (kUpperBound - kLowerBound) * (1 - proportion);
                    this.percent += increment;
                } else {
                    increment = (kUpperBound - kLowerBound) * (1 - (1 / proportion));
                    this.percent -= increment;
                }
                break;
            }
            case 1: {
                //System.out.println("n = " + this.getCurrentDiagnoses().size());
                //System.out.println("upperbound: " + kUpperBound);
                double interval = kUpperBound - kLowerBound;
                double increment = 2 * ((Math.floor(this.getCurrentDiagnoses().size() / 2) - q.getNumOfEliminatedDiags(q.getAnswer())) / this.getCurrentDiagnoses().size()) * interval;
                //System.out.println("increment = " + increment);
                if (this.percent + increment > kUpperBound) {
                    //System.out.println("IF");
                    this.percent = kUpperBound;
                } else if (this.percent + increment < kLowerBound) {
                    //System.out.println("ELSE IF");
                    this.percent = kLowerBound;
                } else {
                    //System.out.println("ELSE");
                    this.percent += increment;
                    //System.out.println("ELSE: percent = " + this.percent);
                }
                break;
            }
            case 2: {
                //System.out.println("upperbound: " + kUpperBound);
                double increment = ((Math.floor(this.getCurrentDiagnoses().size() / 2) - q.getNumOfEliminatedDiags(q.getAnswer())) / this.getCurrentDiagnoses().size());
                //System.out.println("increment = " + increment);
                if (this.percent + increment > kUpperBound) {
                    this.percent = kUpperBound;
                } else if (this.percent + increment < kLowerBound) {
                    this.percent = kLowerBound;
                } else {
                    this.percent += increment;
                }
                break;
            }
            case 3: {
                //System.out.println("upperbound: " + kUpperBound);
                double interval = (kUpperBound - kLowerBound) / 5f;
                if (kUpperBound > this.percent && kLowerBound < this.percent) {
                    interval = Math.min(this.percent - kLowerBound, kUpperBound - this.percent);
                    //System.out.println("IF");
                }
                double increment = 2 * ((Math.floor(this.getCurrentDiagnoses().size() / 2) - q.getNumOfEliminatedDiags(q.getAnswer())) / this.getCurrentDiagnoses().size()) * interval;
                //System.out.println("increment = " + increment);
                if (this.percent + increment > kUpperBound) {
                    this.percent = kUpperBound;
                } else if (this.percent + increment < kLowerBound) {
                    this.percent = kLowerBound;
                } else {
                    this.percent += increment;
                }
                break;
            }
            case 4: {
                if ((int) Math.floor(this.getCurrentDiagnoses().size() / 2) - (int) q.getNumOfEliminatedDiags(q.getAnswer()) > 0) {
                    this.percent = kUpperBound;
                } else if ((int) Math.floor(this.getCurrentDiagnoses().size() / 2) - (int) q.getNumOfEliminatedDiags(q.getAnswer()) < 0) {
                    this.percent = kLowerBound;
                } else {

                }
                break;
            }
        }


        //System.out.println("determinePercent: " + this.percent);
        //return this.percent;
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
			if(!kUpperBoundFixed){
				this.updateKUpperBound(this.getCurrentDiagnoses().size() - q.getNumOfEliminatedDiags(q.getAnswer()));
			}
			this.determinePercent(q,this.kAdaptStrategy,this.kLowerBound,this.kUpperBound);
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

