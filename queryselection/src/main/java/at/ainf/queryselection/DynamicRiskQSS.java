package at.ainf.queryselection;


import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 22.02.11
 * Time: 15:11
 * To change this template use File | Settings | File Templates.
 */
public class DynamicRiskQSS extends StaticRiskQSS implements IDistributionAdaptationQSS{

    private double kUpperBound = 0.5f; //0.4f;
    private double kLowerBound = 0.0f; //0.1f;
    private RiskUpdateStrategy riskUpdateStrategy;


    //private boolean kUpperBoundFixed = false;


    /*
    public DynamicRiskQSS(LinkedList<Diagnosis> diags, LinkedList<Query> queries, double p, int strategy){
        super(AbstractQuerySelectionStrategy.TYPE_DYNAMIC_ADAPT, p, AbstractQuerySelectionStrategy.NAME_VARDAA);
        this.kAdaptStrategy = strategy;

    }
    */

    public DynamicRiskQSS(RiskPreferences rp, RiskUpdateStrategy riskUpdateStrategy) {
        super(rp.getInitialRisk());
        this.kLowerBound = rp.getLowerBound();
        this.kUpperBound = rp.getUpperBound();
        this.riskUpdateStrategy = riskUpdateStrategy;
    }


    protected Query selectQuery() {
        return super.selectQuery();
    }

    public QInfo setQueryAnswer(boolean a) {
        QInfo qInfo = super.setQueryAnswer(a);

        LinkedList<QueryModuleDiagnosis> diagsToEliminate = this.getDiagsEliminatedByQuery(a);
        int numOfElimDiags = diagsToEliminate.size();
        this.updatePercent(numOfElimDiags);
        qInfo.setPercentForNextQuery(this.percent);

        return qInfo;


    }

    private void updatePercent(int numOfElimDiags) {
        if (this.riskUpdateStrategy == RiskUpdateStrategy.STANDARD) {
            double epsilon = 0.001d;
            double interval = kUpperBound - kLowerBound;
            /*//////////////////////////////////
            System.out.println("upper bound = " + kUpperBound);
            System.out.println("lower bound = " + kLowerBound);
            System.out.println("interval = " + interval);
            System.out.println("Risk before = " + this.percent);
            *///////////////////////////////////
            double increment = 2d * ((Math.floor((double)this.askedQuery.getNumOfAllDiags() / 2d) - (double)numOfElimDiags) / (double)this.askedQuery.getNumOfAllDiags()) * interval;
            /*//////////////////////////////////
            System.out.println("increment = 2 * (" + ( Math.floor(this.askedQuery.getNumOfLeadingDiags()/2)) + " - " +
                     numOfElimDiags  + ") / " + this.askedQuery.getNumOfLeadingDiags() + ") * " + interval + " = " + increment);
            *//////////////////////////////////
            if (this.percent + increment > kUpperBound) {
                this.percent = kUpperBound;
            } else if (this.percent + increment < kLowerBound) {
                this.percent = kLowerBound;
            } else {
                this.percent += increment;
            }
            /*
            double max;
            if(this.percent > (max=this.getMaxPossibleNumOfDiagsToEliminate()/this.askedQuery.getNumOfLeadingDiags())){
                this.percent = max;
            }
            */
        } else if (this.riskUpdateStrategy == RiskUpdateStrategy.ELIM_HISTORY) {
            double lowerWeight, upperWeight;
            if( (lowerWeight = ( this.avgEliminationRate + (double)numOfElimDiags / (double)this.askedQuery.getNumOfAllDiags() )) >= 1d){
                lowerWeight = 1d;
            }
            upperWeight = 1d - lowerWeight;
            this.percent = lowerWeight * kLowerBound + upperWeight * kUpperBound;
        }
    }


    public String getName() {
        return NAME_VARDAA;
    }

    public int getType() {
        return TYPE_DYNAMIC_ADAPT;
    }

    public void updateKUpperBound(int numOfDiags) { ////////////////////////////////////////////////// ACHTUNG: edit!!! Epsilon
        double epsilon = 0.1f;
        this.kUpperBound = (Math.floor((double) (numOfDiags / 2) - epsilon)) / (double)numOfDiags;
        //( Math.floor((double)this.getCurrentDiagnoses().size()/2))/  this.getCurrentDiagnoses().size();
    }


//    protected void/*double*/ determinePercent(Query q, int strategy, double kLowerBound, double kUpperBound) {
//
//        switch (strategy) {
//            case 10: {
//                double increment = 1.0f;
//                if (q.getNumOfEliminatedDiags(q.getAnswer()) / q.getNumOfLeadingDiags() < 0.5f) {
//                    if (this.percent <= kUpperBound - increment) {
//                        this.percent += increment;
//                    } else {
//                        this.percent = kUpperBound;
//                    }
//                } else {
//                    if (this.percent >= kLowerBound + increment) {
//                        this.percent -= increment;
//                    } else {
//                        this.percent = kLowerBound;
//                    }
//                }
//                break;
//            }
//            case 11: {
//                double proportion = 0f;
//                double increment = 1.0f;
//                if ((proportion = (q.getNumOfEliminatedDiags(q.getAnswer()) / Math.floor((double) q.getNumOfLeadingDiags() / (double) 2))) < 1.0f) {
//                    increment = (kUpperBound - kLowerBound) * (1 - proportion);
//                    this.percent += increment;
//                } else {
//                    increment = (kUpperBound - kLowerBound) * (1 - (1 / proportion));
//                    this.percent -= increment;
//                }
//                break;
//            }
//            case 1: {
//                //System.out.println("n = " + this.getCurrentDiagnoses().size());
//                //System.out.println("upperbound: " + kUpperBound);
//                double interval = kUpperBound - kLowerBound;
//                double increment = 2 * ((Math.floor(this.getCurrentDiagnoses().size() / 2) - q.getNumOfEliminatedDiags(q.getAnswer())) / this.getCurrentDiagnoses().size()) * interval;
//                //System.out.println("increment = " + increment);
//                if (this.percent + increment > kUpperBound) {
//                    //System.out.println("IF");
//                    this.percent = kUpperBound;
//                } else if (this.percent + increment < kLowerBound) {
//                    //System.out.println("ELSE IF");
//                    this.percent = kLowerBound;
//                } else {
//                    //System.out.println("ELSE");
//                    this.percent += increment;
//                    //System.out.println("ELSE: percent = " + this.percent);
//                }
//                break;
//            }
//            case 2: {
//                //System.out.println("upperbound: " + kUpperBound);
//                double increment = ((Math.floor(this.getCurrentDiagnoses().size() / 2) - q.getNumOfEliminatedDiags(q.getAnswer())) / this.getCurrentDiagnoses().size());
//                //System.out.println("increment = " + increment);
//                if (this.percent + increment > kUpperBound) {
//                    this.percent = kUpperBound;
//                } else if (this.percent + increment < kLowerBound) {
//                    this.percent = kLowerBound;
//                } else {
//                    this.percent += increment;
//                }
//                break;
//            }
//            case 3: {
//                //System.out.println("upperbound: " + kUpperBound);
//                double interval = (kUpperBound - kLowerBound) / 5f;
//                if (kUpperBound > this.percent && kLowerBound < this.percent) {
//                    interval = Math.min(this.percent - kLowerBound, kUpperBound - this.percent);
//                    //System.out.println("IF");
//                }
//                double increment = 2 * ((Math.floor(this.getCurrentDiagnoses().size() / 2) - q.getNumOfEliminatedDiags(q.getAnswer())) / this.getCurrentDiagnoses().size()) * interval;
//                //System.out.println("increment = " + increment);
//                if (this.percent + increment > kUpperBound) {
//                    this.percent = kUpperBound;
//                } else if (this.percent + increment < kLowerBound) {
//                    this.percent = kLowerBound;
//                } else {
//                    this.percent += increment;
//                }
//                break;
//            }
//            case 4: {
//                if ((int) Math.floor(this.getCurrentDiagnoses().size() / 2) - (int) q.getNumOfEliminatedDiags(q.getAnswer()) > 0) {
//                    this.percent = kUpperBound;
//                } else if ((int) Math.floor(this.getCurrentDiagnoses().size() / 2) - (int) q.getNumOfEliminatedDiags(q.getAnswer()) < 0) {
//                    this.percent = kLowerBound;
//                } else {
//
//                }
//                break;
//            }
//        }
//
//
//        //System.out.println("determinePercent: " + this.percent);
//        //return this.percent;
//    }



}
