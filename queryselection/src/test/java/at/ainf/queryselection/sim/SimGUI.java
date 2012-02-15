package at.ainf.queryselection.sim;

/*import Diagnosis;
import Query;
import at.ainf.queryselection.diaggenerators2.UniformDistribution;
import at.ainf.queryselection.probassigners.*;*/

import at.ainf.queryselection.*;
import at.ainf.queryselection.diaggenerators.RandomDistribution;
import at.ainf.queryselection.diaggenerators.SteepDistribution;
import at.ainf.queryselection.diaggenerators.SteepDistributionMode;
import at.ainf.queryselection.probassigners.Mode;
import at.ainf.queryselection.probassigners.RandomDistributionAssign;
import org.junit.Test;

import java.util.List;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 22.02.11
 * Time: 14:58
 * To change this template use File | Settings | File Templates.
 */
public class SimGUI {


    @Test
    public void testMain() {

        boolean targetDiagnosisFound = false;
        double sigma = 1.1d;
        QueryModuleDiagnosis discoveredTargetDiagnosis = null;
        QInfo qInfo = null;
        List<QueryModuleDiagnosis> currentDiagnoses;
        boolean alternativeQuery = false;
        boolean noFurtherQuery = false;
        boolean noDiagnosis = false;
        int totalNumOfEliminatedDiags = 0;
        double avgNumOfEliminatedDiags = 0f;
        double avgPercentOfEliminatedDiags = 0f;
        double sumOfPercentOfEliminatedDiags = 0d;
        Random rndGen = new Random(2);
        boolean userRecognizesTD = true;
        boolean userFoundTD = false;
        RiskUpdateStrategy riskUpdateStrategy = RiskUpdateStrategy.STANDARD;

        QuProvider querySelectionStrategy = QuProvider.SCOREADAPT;

        IDiagGenerator userDistribution = new SteepDistribution(SteepDistributionMode.ONE_DIV_PARAM_POWER_X, 5, 0.8d, 1.0d, 1.75d);  // new RandomDistribution(true, 0.0d, 0.2d, true, rndGen.nextLong());  // false...no extreme distribution


        IProbabilityAssigner probAssigner = new RandomDistributionAssign(Mode.INVERTED, rndGen.nextLong());

        RiskPreferences rp = new RiskPreferences(0.25d, 0.0d, 0.5d);

        IDiagnosisProvider diagProvider = null;
        IQueryProvider queryProvider = null;
        try {
            diagProvider = new SimDiag(userDistribution, probAssigner, 200, rndGen.nextLong());
            diagProvider.assignActualProbabilities();

            if (querySelectionStrategy == QuProvider.SPLIT) {
                queryProvider = new SplitInHalfQSS();
                queryProvider.setNumOfLeadingDiagnoses(9);
                queryProvider.setDiagnosisProvider(diagProvider);
            } else if (querySelectionStrategy == QuProvider.MINSCORE) {
                queryProvider = new MinScoreQSS();
                queryProvider.setNumOfLeadingDiagnoses(9);
                queryProvider.setDiagnosisProvider(diagProvider);
            } else if (querySelectionStrategy == QuProvider.DYNAMICRISK) {
                queryProvider = new DynamicRiskQSS(rp, riskUpdateStrategy);           // 0.4, 0.2, 0.5
                queryProvider.setNumOfLeadingDiagnoses(9);
                queryProvider.setDiagnosisProvider(diagProvider);
            } else if (querySelectionStrategy == QuProvider.DYNAMICRISK_NOADAPT) {
                queryProvider = new DynamicRiskNoAdaptQSS(rp, riskUpdateStrategy);           // 0.4, 0.2, 0.5
                queryProvider.setNumOfLeadingDiagnoses(9);
                queryProvider.setDiagnosisProvider(diagProvider);
            } else if (querySelectionStrategy == QuProvider.DYNAMICRISK_CONDITIONALADAPT) {
                queryProvider = new DynamicRiskConditionalAdaptQSS(rp, riskUpdateStrategy);           // 0.4, 0.2, 0.5
                queryProvider.setNumOfLeadingDiagnoses(9);
                queryProvider.setDiagnosisProvider(diagProvider);
            } else if (querySelectionStrategy == QuProvider.PENALTY) {
                queryProvider = new PenaltyQSS(5d);           // 0.4, 0.2, 0.5
                queryProvider.setNumOfLeadingDiagnoses(9);
                queryProvider.setDiagnosisProvider(diagProvider);
            } else if (querySelectionStrategy == QuProvider.STATICRISK) {
                queryProvider = new StaticRiskQSS(0.3d);           // 0.4, 0.2, 0.5
                queryProvider.setNumOfLeadingDiagnoses(9);
                queryProvider.setDiagnosisProvider(diagProvider);
            } else if (querySelectionStrategy == QuProvider.SCOREADAPT) {
                queryProvider = new ScoreAdaptQSS(0.1d, 0.9d);           // 0.4, 0.2, 0.5
                queryProvider.setNumOfLeadingDiagnoses(9);
                queryProvider.setDiagnosisProvider(diagProvider);
            }


            //////////////////// GUI-OUTPUT /////////////////////
            System.out.println(queryProvider.getStrategyName() + " " + AbstractQuerySelectionStrategy.START_MESS);
            /////////////////////////////////////////////////////
        } catch (NoDiagnosisFoundException e) {
            noDiagnosis = true;
        }


        currentDiagnoses = queryProvider.getCurrentDiagnoses();

        while (!noDiagnosis && !targetDiagnosisFound && !noFurtherQuery) {

            //////////////////// GUI-OUTPUT /////////////////////
            System.out.println();
            System.out.println();
            System.out.println(AbstractQuerySelectionStrategy.PROBAPRIORI_MESS + queryProvider.probDistToString());
            /////////////////////////////////////////////////////

            Query q = null;
            try {
                q = queryProvider.getQuery(alternativeQuery);
            } catch (NoFurtherQueryException e) {
                noFurtherQuery = true;
                discoveredTargetDiagnosis = queryProvider.getMostProbableDiagnosis();
                continue;
            } catch (Exception e) {
                e.printStackTrace();
            }
            alternativeQuery = false;


            //DEBUG
            /*
            LinkedList<Query> qus = queryProvider.getCurrentQueries();
            LinkedList<Double> sc = new LinkedList<Double>();
            for(Query query : qus){
                System.out.println(query.getScore());
                sc.add(query.getScore());
            }
            *///DEBUG
            //////////////////// GUI-OUTPUT /////////////////////
            double s, a, b, c, g = 0;
            System.out.println("Query " + q.getIteration() + ": has score = " + (s = q.getScore()));
            if (s < 0) {
                System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
            }
            System.out.println("Query " + q.getIteration() + ": D_X = " + q.d_XToString(true) + "\t\t P(D_X) = " + (a = q.getProb_X()));
            System.out.println("Query " + q.getIteration() + ": D_notX = " + q.d_notXToString(true) + "\t\t P(D_notX) = " + (b = q.getProb_notX()));
            System.out.println("Query " + q.getIteration() + ": D_0 = " + q.d_0ToString(true) + "\t\t P(D_0) = " + (c = q.getProb_0()));
            System.out.println("Query " + q.getIteration() + " eliminates AT LEAST: " + q.getMinNumOfElimDiags() + " diagnoses --- corresponds to " + q.getMinPercentOfElimDiags() * 100d + " percent!");
            System.out.println("Query " + q.getIteration() + " eliminates AT MOST: " + q.getMaxNumOfElimDiags() + " diagnoses --- corresponds to " + q.getMaxPercentOfElimDiags() * 100d + " percent!");
            g = a + b + c;
            System.out.println("__________________________________________________________________________________a+b+c = " + g);

            System.out.println("(User) Probability for positive answer: " + ( q.getProb_X() + q.getProb_0() / (double)2));
            System.out.println("(Actual) Probability for positive answer: " + (q.getActualProb_X() + q.getActualProb_0() / 2d) / (q.getActualProb_0() / 2d + q.getActualProb_notX() + q.getActualProb_X()));

            /////////////////////////////////////////////////////
            boolean answer = false;
            try {
                answer = queryProvider.getQueryAnswer(q);     // simulated user answer
                qInfo = queryProvider.setQueryAnswer(answer);

            } catch (AnswerNotKnownException e) {
                alternativeQuery = true;
                System.out.println("---->  User wants alternative query!");
                continue;
            }


            //////////////////// GUI-OUTPUT /////////////////////
            System.out.println("User's answer is " + answer);
            System.out.println(AbstractQuerySelectionStrategy.PROBAPRIORI_MESS + queryProvider.probDistToString());
            /////////////////////////////////////////////////////


            //////////////////// GUI-OUTPUT /////////////////////
            double temp;
            System.out.println("NUMBER OF DIAGNOSES ELIMINATED = " + qInfo.getNumOfEliminatedDiags() + " from " + q.getNumOfAllDiags() + " total remaining diagnoses! " +
                    " -------> " + (temp = ((double)qInfo.getNumOfEliminatedDiags() / (double)q.getNumOfAllDiags() * 100d)) + " percent");
            System.out.println("##############    Risk (= percent) for next query = " + qInfo.getPercentForNextQuery());

            if(querySelectionStrategy == QuProvider.SCOREADAPT){
                System.out.println("- - - - - - - - - current score = " + qInfo.getCurrentScore());
            }
            /////////////////////////////////////////////////////


            totalNumOfEliminatedDiags += qInfo.getNumOfEliminatedDiags();
            avgNumOfEliminatedDiags = (double)totalNumOfEliminatedDiags / (double)qInfo.getIteration();
            System.out.println("---------- Average Elimination Rate (in Diagnoses) so far = " + avgNumOfEliminatedDiags + " -----------");

            sumOfPercentOfEliminatedDiags += temp;
            avgPercentOfEliminatedDiags = sumOfPercentOfEliminatedDiags / (double)qInfo.getIteration();
            System.out.println("---------- Average Elimination Rate (in percent of leading diagnoses) so far = " + avgPercentOfEliminatedDiags + " -----------");


            //LinkedList<Diagnosis> currentDiagnoses = queryProvider.getCurrentDiagnoses();
            currentDiagnoses = qInfo.getCurrentDiags();


            /*
            if(currentDiagnoses.size() == 1 && currentDiagnoses.getFirst().isTarget()){
                targetDiagnosisFound = true;
                discoveredTargetDiagnosis = currentDiagnoses.getFirst();
            }
            */

            /*
            for (QueryModuleDiagnosis d : currentDiagnoses) {
                if (d.getProbability() > sigma) {
                    targetDiagnosisFound = true;
                    discoveredTargetDiagnosis = d;
                }
            }
            */

            QueryModuleDiagnosis maxProbDiag = currentDiagnoses.get(0);
                for (QueryModuleDiagnosis d : currentDiagnoses) {
                    if (userRecognizesTD) {
                        if (d.getProbability() > maxProbDiag.getProbability()) {
                            maxProbDiag = d;
                        }
                    } //else {
                        if (d.getProbability() > sigma) {
                            targetDiagnosisFound = true;
                            discoveredTargetDiagnosis = d;
                            break;
                        }
                    //}
                }
                if (userRecognizesTD) {
                    if (maxProbDiag.isTarget()) {
                        targetDiagnosisFound = true;
                        userFoundTD = true;
                        discoveredTargetDiagnosis = maxProbDiag;
                    }
                }

        }


        if (noFurtherQuery) {
            System.out.println("No more queries available --- most probable diagnosis is " + discoveredTargetDiagnosis.getName());
        } else if (targetDiagnosisFound) {
            if(!userFoundTD){
                System.out.println("Probability of most probable diagnosis exceeds sigma --- Found diagnosis is " + discoveredTargetDiagnosis.getName());
            }else{
                System.out.println("UserFound diagnosis is " + discoveredTargetDiagnosis.getName());
            }


        } else if (noDiagnosis) {
            System.out.println("Diagnosis session cannot be started - no potential diagnoses could be found!");
        }


        System.out.println("Number of remaining diagnoses after debugging session is " + currentDiagnoses.size());
        System.out.println("Algorithm's answer is --- " + discoveredTargetDiagnosis.isTarget());
        System.out.println("Actual target diagnosis is --- " + probAssigner.getTargetDiag().getName());
        System.out.println(queryProvider.getTooHighRiskCounter() + " times MinScore wanted to take too high risk!");
        System.out.println(queryProvider.getAdaptationCounter() + " times an alpha-adaptation was performed!");
        System.out.println(queryProvider.getTargetDiagWithinLeadingDiagsCounter() + " iterations the target diagnosis was within the set of leading diagnoses!");


    }




}


