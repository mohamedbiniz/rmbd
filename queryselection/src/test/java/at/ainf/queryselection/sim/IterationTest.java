package at.ainf.queryselection.sim;

import at.ainf.queryselection.*;
import at.ainf.queryselection.diaggenerators.*;
import at.ainf.queryselection.probassigners.Mode;
import at.ainf.queryselection.probassigners.RandomDistributionAssign;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 14.04.11
 * Time: 17:45
 * To change this template use File | Settings | File Templates.
 */
public class IterationTest {

    static final int D1_GT_DELTA_D2 = 1;
    static final int D1_GT_DELTA_ALL = 2;

    private int deltaStrategy = D1_GT_DELTA_D2;

    @Test
    public void runIterationTest() {
        int iterations = 2;
        double targDiagSectorU = 0.0d;
        double targDiagSectorO = 0.05d;
        int numberOfAllDiags = 200;
        double sigma = 1.1d;  // sigma >= 1 heißt, dass sigma nicht betrachtet wird - wir warten, bis user die target diagnosis identifiziert
        double delta = 3d;  // 3d guter wert - minscore ab und zu besser als split
        boolean verbose = false;
        boolean userRecognizesTargetDiag = true;
        RiskPreferences rp = new RiskPreferences(0.3d, 0.05d, 0.5d);
        int numOfLeadingDiags = 9;
        double maxPenalty = 5d;
        double staticRiskPercent = 0.3d;
        //DiagGenMode diagGeneratorMode = DiagGenMode.EXTREME;
        RiskPreferences [] rpArray = {  new RiskPreferences(0.25d, 0.0d, 0.5d),
                                        new RiskPreferences(0.3d, 0.1d, 0.5d),
                                        new RiskPreferences(0.35d, 0.2d, 0.5d),
                                        new RiskPreferences(0.4d, 0.3d, 0.5d),
                                        new RiskPreferences(0.2d, 0.0d, 0.4d),
                                        new RiskPreferences(0.25d, 0.1d, 0.4d),
                                        new RiskPreferences(0.3d, 0.2d, 0.4d),
                                        new RiskPreferences(0.15d, 0.0d, 0.3d),
                                        new RiskPreferences(0.2d, 0.1d, 0.3d),
                                        new RiskPreferences(0.1d, 0.0d, 0.2d)  };

        for(DiagGenMode diagGeneratorMode : DiagGenMode.values()){

            for(double i = 0.0d; i<= 0.8d; i += 0.2d){
                targDiagSectorU = i;
                targDiagSectorO = targDiagSectorU + 0.1d;
                for(QuProvider qp : QuProvider.values()){
                    if(qp == QuProvider.DYNAMICRISK || qp == QuProvider.DYNAMICRISK_NOADAPT || qp == QuProvider.DYNAMICRISK_CONDITIONALADAPT ){
                        for(RiskPreferences r : rpArray){
                             iterations(qp, iterations, targDiagSectorU, targDiagSectorO, numberOfAllDiags, sigma, delta, r, verbose,
                                     userRecognizesTargetDiag, numOfLeadingDiags, maxPenalty, staticRiskPercent, diagGeneratorMode);
                        }
                    } else if(qp == QuProvider.STATICRISK){
                        for(double staticRiskPerc = 0.1d; staticRiskPerc<=0.5d; staticRiskPerc += 0.1d){
                            iterations(qp, iterations, targDiagSectorU, targDiagSectorO, numberOfAllDiags, sigma, delta, rp, verbose,
                                    userRecognizesTargetDiag, numOfLeadingDiags, maxPenalty, staticRiskPerc, diagGeneratorMode);
                        }
                    } else if(qp == QuProvider.PENALTY){
                        for(int j = 0; j<=10; j += 2){
                            iterations(qp, iterations, targDiagSectorU, targDiagSectorO, numberOfAllDiags, sigma, delta, rp, verbose,
                            userRecognizesTargetDiag, numOfLeadingDiags, (double)j, staticRiskPercent, diagGeneratorMode);
                        }
                    }else if(qp == QuProvider.MINSCORE || qp == QuProvider.SPLIT){
                         iterations(qp, iterations, targDiagSectorU, targDiagSectorO, numberOfAllDiags, sigma, delta, rp, verbose,
                                userRecognizesTargetDiag, numOfLeadingDiags, maxPenalty, staticRiskPercent, diagGeneratorMode);
                    }else{
                        //iterations(qp, iterations, targDiagSectorU, targDiagSectorO, numberOfAllDiags, sigma, delta, rp, verbose,
                        //        userRecognizesTargetDiag, numOfLeadingDiags, maxPenalty, staticRiskPercent, diagGeneratorMode);
                    }
                }
            }
        }
//        iterations(QuProvider.MINSCORE, iterations, targDiagSectorU, targDiagSectorO, numberOfAllDiags, sigma, rp, verbose, userRecognizesTargetDiag, numOfLeadingDiags, maxPenalty);
//        iterations(QuProvider.DYNAMICRISK, iterations, targDiagSectorU, targDiagSectorO, numberOfAllDiags, sigma, rp, verbose, userRecognizesTargetDiag, numOfLeadingDiags);
//        iterations(QuProvider.DYNAMICRISK_NOADAPT, iterations, targDiagSectorU, targDiagSectorO, numberOfAllDiags, sigma, rp, verbose, userRecognizesTargetDiag, numOfLeadingDiags);
//        iterations(QuProvider.SPLIT, iterations, targDiagSectorU, targDiagSectorO, numberOfAllDiags, sigma, rp, verbose, userRecognizesTargetDiag, numOfLeadingDiags);
//        iterations(QuProvider.DYNAMICRISK_CONDITIONALADAPT, iterations, targDiagSectorU, targDiagSectorO, numberOfAllDiags, sigma, rp, verbose, userRecognizesTargetDiag, numOfLeadingDiags);
    }





    private void iterations(QuProvider querySelectionStrategy, int totalIterations, double targDiagSectorU, double targDiagSectorO, int numberOfAllDiags, double sigma, double delta,
                            RiskPreferences rp, boolean verbose, boolean userRegocnizesTD, int numOfLeadingDiags, double maxPenalty, double staticRiskPercent, DiagGenMode diagGeneratorMode) {
        if (querySelectionStrategy == QuProvider.MINSCORE) {
            System.out.println("++++++++++++++++++++ MIN SCORE ++++++++++++++++++++++++++++++++");
        } else if (querySelectionStrategy == QuProvider.DYNAMICRISK) {
            System.out.println("++++++++++++++++++++ DYN RISK  ++++++++++++++++++++++++++++++++");
        } else if (querySelectionStrategy == QuProvider.DYNAMICRISK_NOADAPT) {
            System.out.println("++++++++++++++++++++ DYN RISK NO ADAPT ++++++++++++++++++++++++");
        } else if (querySelectionStrategy == QuProvider.SPLIT) {
            System.out.println("++++++++++++++++++++ SPLIT ++++++++++++++++++++++++++++++++++++");
        } else if (querySelectionStrategy == QuProvider.DYNAMICRISK_CONDITIONALADAPT) {
            System.out.println("++++++++++++++++++++ DYN RISK CONDITIONAL ADAPT +++++++++++++++");
        } else if (querySelectionStrategy == QuProvider.PENALTY) {
            System.out.println("++++++++++++++++++++ PENALTY ++++++++++++++++++++++++++++++++++");
        } else if (querySelectionStrategy == QuProvider.STATICRISK) {
            System.out.println("++++++++++++++++++++ STATIC RISK (NO ADAPT) +++++++++++++++++++");
        }

        if (verbose) {
            verboseIterations(querySelectionStrategy, totalIterations, targDiagSectorU, targDiagSectorO, numberOfAllDiags, sigma, delta, rp,
                    userRegocnizesTD, numOfLeadingDiags, maxPenalty, staticRiskPercent, diagGeneratorMode);
        } else {
            nonVerboseIterations(querySelectionStrategy, totalIterations, targDiagSectorU, targDiagSectorO, numberOfAllDiags, sigma, delta, rp,
                    userRegocnizesTD, numOfLeadingDiags, maxPenalty, staticRiskPercent, diagGeneratorMode);
        }

        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");


    }






    private void nonVerboseIterations(QuProvider querySelectionStrategy, int totalIterations, double targDiagSectorU, double targDiagSectorO, int numberOfAllDiags,
                                      double sigma, double delta, RiskPreferences rp, boolean userRecognizesTD, int numOfLeadingDiags, double maxPenalty, double staticRiskPercent, DiagGenMode diagGenMode) {
        int iterationCount = 1;

        int numOfTrueTargetDiagFound = 0;
        int numOfFalseTargetDiagFound = 0;
        int numTargetDiagInFinalSetOfLeadingDiags = 0;
        int numTargetDiagNotInFinalSetOfLeadingDiags = 0;

        double avgNumOfTooHighRiskT = 0d;
        double avgNumOfTooHighRiskF = 0d;
        double avgNumOfTooHighRisk = 0d;
        double avgNumOfTooHighRiskTILD = 0d;
        double avgNumOfTooHighRiskNTILD = 0d;

        double avgNumOfAdaptsT = 0d;
        double avgNumOfAdaptsF = 0d;
        double avgNumOfAdapts = 0d;
        double avgNumOfAdaptsTILD = 0d;
        double avgNumOfAdaptsNTILD = 0d;

        double avgNumOfIterationsTargetDiagInLeadingDiagsT = 0d;
        double avgNumOfIterationsTargetDiagInLeadingDiagsF = 0d;
        double avgNumOfIterationsTargetDiagInLeadingDiags = 0d;
        double avgNumOfIterationsTargetDiagInLeadingDiagsTILD = 0d;
        double avgNumOfIterationsTargetDiagInLeadingDiagsNTILD = 0d;

        double avgEliminationRateDiagsT = 0d;
        double avgEliminationRateDiagsF = 0d;
        double avgEliminationRateDiags = 0d;
        double avgEliminationRateDiagsTILD = 0d;
        double avgEliminationRateDiagsNTILD = 0d;

        double avgEliminationRatePercentT = 0d;
        double avgEliminationRatePercentF = 0d;
        double avgEliminationRatePercent = 0d;
        double avgEliminationRatePercentTILD = 0d;
        double avgEliminationRatePercentNTILD = 0d;

        double avgNumOfQueriesT = 0d;
        double avgNumOfQueriesF = 0d;
        double avgNumOfQueries = 0d;
        double avgNumOfQueriesTILD = 0d;
        double avgNumOfQueriesNTILD = 0d;

        double avgProbOfMostProbDiagnosisT = 0d;
        double avgProbOfMostProbDiagnosisF = 0d;
        double avgProbOfMostProbDiagnosis = 0d;
        double avgProbOfMostProbDiagnosisTILD = 0d;
        double avgProbOfMostProbDiagnosisNTILD = 0d;

        double avgFinallyRemainingNumOfDiagsT = 0d;
        double avgFinallyRemainingNumOfDiagsF = 0d;
        double avgFinallyRemainingNumOfDiags = 0d;
        double avgFinallyRemainingNumOfDiagsTILD = 0d;
        double avgFinallyRemainingNumOfDiagsNTILD = 0d;

        double avgCumulatedAlphaT = 0d;
        double avgCumulatedAlphaF = 0d;
        double avgCumulatedAlpha = 0d;
        double avgCumulatedAlphaTILD = 0d;
        double avgCumulatedAlphaNTILD = 0d;



        int numOfSigmaTermination = 0;
        int numOfUserFoundTermination = 0;
        int numOfNoFurtherQueryTermination = 0;

        ArrayList<Integer> numOfRequiredQueriesList = new ArrayList<Integer>();
        ArrayList<Double> eliminationRateList = new ArrayList<Double>();


        while (iterationCount <= totalIterations) {

            boolean targetDiagnosisFound = false;
            QueryModuleDiagnosis discoveredTargetDiagnosis = null;
            QInfo qInfo = null;
            List<QueryModuleDiagnosis> currentDiagnoses;
            boolean alternativeQuery = false;
            boolean noDiagnosis = false;
            int totalNumOfEliminatedDiags = 0;
            double avgNumOfEliminatedDiags = 0d;
            double avgPercentOfEliminatedDiags = 0d;
            double sumOfPercentOfEliminatedDiags = 0d;

            boolean noFurtherQuery = false;
            boolean probGreaterSigma = false;
            boolean userFoundTargetDiag = false;
            boolean deltaCondition = false;


            IDiagGenerator userDistribution;
            switch(diagGenMode){
                case EXTREME:
                    userDistribution = new SteepDistribution(SteepDistributionMode.ONE_DIV_PARAM_POWER_X,(iterationCount * 7) % totalIterations, targDiagSectorU, targDiagSectorO, 1.5d);  // guter wert: 1.75d
                    break;
                    // ODER: userDistribution = new RandomDistribution(true, targDiagSectorU, targDiagSectorO, true, (iterationCount * 7) % totalIterations);
                case MODERATE:
                    //userDistribution = new RandomDistribution(false, targDiagSectorU, targDiagSectorO, true, (iterationCount * 7) % totalIterations);
                    userDistribution = new SteepDistribution(SteepDistributionMode.ONE_DIV_PARAM_POWER_X,(iterationCount * 7) % totalIterations, targDiagSectorU, targDiagSectorO, 1.1d);  // guter wert: 1.25d
                    break;
                case UNIFORM:
                    userDistribution = new UniformDistribution(targDiagSectorU, targDiagSectorO);
                    break;
                default:
                    throw new IllegalArgumentException("Illegal Diagnosis Generation Mode selected!");

            }

            //IDiagGenerator userDistribution = new RandomDistribution(true, targDiagSectorU, targDiagSectorO, true, (iterationCount * 7) % totalIterations);

            IProbabilityAssigner probAssigner = null;
            if(targDiagSectorU <= 0.1d){  // GOOD CASE
                probAssigner = new RandomDistributionAssign(Mode.CORRELATED, (iterationCount * 13) % totalIterations);
            }else if(targDiagSectorU <= 0.5d){  // AVG CASE
                probAssigner = new RandomDistributionAssign(Mode.INDEPENDENT, (iterationCount * 13) % totalIterations);
            } else if(targDiagSectorU <= 0.9d){  // BAD CASE
                probAssigner = new RandomDistributionAssign(Mode.INVERTED, (iterationCount * 13) % totalIterations);
            }


            IDiagnosisProvider diagProvider = null;
            IQueryProvider queryProvider = null;

            try {
                diagProvider = new SimDiag(userDistribution, probAssigner, numberOfAllDiags, iterationCount);
                diagProvider.assignActualProbabilities();



                if (querySelectionStrategy == QuProvider.SPLIT) {
                    queryProvider = new SplitInHalfQSS();
                } else if (querySelectionStrategy == QuProvider.MINSCORE) {
                    queryProvider = new MinScoreQSS();
                } else if (querySelectionStrategy == QuProvider.DYNAMICRISK) {
                    queryProvider = new DynamicRiskQSS(rp, RiskUpdateStrategy.STANDARD);           // 0.4, 0.2, 0.5
                } else if (querySelectionStrategy == QuProvider.DYNAMICRISK_NOADAPT) {
                    queryProvider = new DynamicRiskNoAdaptQSS(rp, RiskUpdateStrategy.STANDARD);
                } else if (querySelectionStrategy == QuProvider.DYNAMICRISK_CONDITIONALADAPT) {
                    queryProvider = new DynamicRiskConditionalAdaptQSS(rp, RiskUpdateStrategy.STANDARD);
                } else if (querySelectionStrategy == QuProvider.PENALTY) {
                    queryProvider = new PenaltyQSS(maxPenalty);
                } else if (querySelectionStrategy == QuProvider.STATICRISK) {
                    queryProvider = new StaticRiskQSS(staticRiskPercent);
                } else if (querySelectionStrategy == QuProvider.SCOREADAPT) {
                    queryProvider = new ScoreAdaptQSS(0.1d,0.5d);
                }


                queryProvider.setNumOfLeadingDiagnoses(numOfLeadingDiags);
                queryProvider.setDiagnosisProvider(diagProvider);

            } catch (NoDiagnosisFoundException e) {
                noDiagnosis = true;
            }


            currentDiagnoses = queryProvider.getCurrentDiagnoses();

            while (!noDiagnosis && !targetDiagnosisFound && !noFurtherQuery) {


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


                boolean answer;
                try {
                    answer = queryProvider.getQueryAnswer(q);
                    qInfo = queryProvider.setQueryAnswer(answer);

                } catch (AnswerNotKnownException e) {
                    alternativeQuery = true;
                    System.out.println("---->  User wants alternative query!");
                    continue;
                }


                double temp = (double) qInfo.getNumOfEliminatedDiags() / (double) q.getNumOfAllDiags() * 100d;

                totalNumOfEliminatedDiags += qInfo.getNumOfEliminatedDiags();
                avgNumOfEliminatedDiags = totalNumOfEliminatedDiags / qInfo.getIteration();


                sumOfPercentOfEliminatedDiags += temp;
                avgPercentOfEliminatedDiags = sumOfPercentOfEliminatedDiags / qInfo.getIteration();


                currentDiagnoses = qInfo.getCurrentDiags();


//                QueryModuleDiagnosis maxProbDiag = currentDiagnoses.get(0);
//                for (QueryModuleDiagnosis d : currentDiagnoses) {
//                    if (userRecognizesTD) {
//                        if (d.getProbability() > maxProbDiag.getProbability()) {
//                            maxProbDiag = d;
//                        }
//                    } //else {
//                        if (d.getProbability() > sigma) {
//                            targetDiagnosisFound = true;
//                            discoveredTargetDiagnosis = d;
//                            probGreaterSigma = true;
//                            break;
//                        }
//                    //}
//                }
//                if (userRecognizesTD) {
//                    boolean deltaSatisfied = false;
//                    for(QueryModuleDiagnosis d : currentDiagnoses){
//                        if(!d.getName().equals(maxProbDiag.getName())){
//                            if(maxProbDiag.getProbability() >= delta * d.g)
//                        }
//                    }
//                    if (maxProbDiag.isTarget() && deltaSatisfied) {
//                        targetDiagnosisFound = true;
//                        discoveredTargetDiagnosis = maxProbDiag;
//                        userFoundTargetDiag = true;
//                    }
//                }

                BubbleSort<QueryModuleDiagnosis> bubblesort = new BubbleSort<QueryModuleDiagnosis>(new QueryModuleDiagnosisProbabilityComparator());
                QueryModuleDiagnosis [] sortedDiags = new QueryModuleDiagnosis[currentDiagnoses.size()];
                for(int i=0; i<currentDiagnoses.size(); i++){
                    sortedDiags[i] = currentDiagnoses.get(i);
                }
                bubblesort.sort(sortedDiags);
                if(userRecognizesTD){
                    if(sortedDiags.length > 1){
                        if(deltaStrategy == D1_GT_DELTA_D2){
                            if(sortedDiags[0].isTarget() && sortedDiags[0].getProbability() > delta * sortedDiags[1].getProbability()){
                                targetDiagnosisFound = true;
                                userFoundTargetDiag = true;
                                deltaCondition = true;
                                discoveredTargetDiagnosis = sortedDiags[0];
                            }
                        }else{
                            double sum = 0d;
                            for(int i = 1; i<sortedDiags.length; i++){
                                sum += sortedDiags[i].getProbability();
                            }
                            if(sortedDiags[0].isTarget() && sortedDiags[0].getProbability() > delta * sum){
                                targetDiagnosisFound = true;
                                userFoundTargetDiag = true;
                                deltaCondition = true;
                                discoveredTargetDiagnosis = sortedDiags[0];
                            }
                        }
                    }else{
                        targetDiagnosisFound = true;
                        discoveredTargetDiagnosis = sortedDiags[0];
                    }
                }
                if(sortedDiags[0].getProbability() > sigma){
                        targetDiagnosisFound = true;
                        discoveredTargetDiagnosis = sortedDiags[0];
                        probGreaterSigma = true;
                }
            } ////////////////////////////////////////////////////////////////////////////////////////////////////////// one iteration ends here


            if (noFurtherQuery) {
                //System.out.println("NQ");
                numOfNoFurtherQueryTermination++;
            } else if (targetDiagnosisFound) {
                if(userFoundTargetDiag){
                    //System.out.println("UF");
                    numOfUserFoundTermination++;
                }else if(probGreaterSigma){
                    //System.out.println(">S");
                    numOfSigmaTermination++;
                }
            } else if (noDiagnosis) {
                System.out.println("Diagnosis session cannot be started - no potential diagnoses could be found!");
            }


            //System.out.println("ANS=" + discoveredTargetDiagnosis.isTarget());
            if (discoveredTargetDiagnosis.isTarget()) {
                numOfTrueTargetDiagFound++;

                avgFinallyRemainingNumOfDiagsT += currentDiagnoses.size();
                avgNumOfTooHighRiskT += queryProvider.getTooHighRiskCounter();
                avgNumOfAdaptsT += queryProvider.getAdaptationCounter();
                avgNumOfIterationsTargetDiagInLeadingDiagsT += queryProvider.getTargetDiagWithinLeadingDiagsCounter();
                avgEliminationRateDiagsT += avgNumOfEliminatedDiags;
                avgEliminationRatePercentT += avgPercentOfEliminatedDiags;
                avgNumOfQueriesT += qInfo.getIteration();
                avgProbOfMostProbDiagnosisT += discoveredTargetDiagnosis.getProbability();
                try{
                    IDistributionAdaptationQSS qp = (IDistributionAdaptationQSS)queryProvider;
                    avgCumulatedAlphaT += qp.getCumulatedAlpha();
                }catch(ClassCastException e){
                    //do nothing, because queryprovider is no IDistributionAdaptationQSS and has no cumulated alpha
                }


                numTargetDiagInFinalSetOfLeadingDiags++;

                avgFinallyRemainingNumOfDiagsTILD += currentDiagnoses.size();
                avgNumOfTooHighRiskTILD += queryProvider.getTooHighRiskCounter();
                avgNumOfAdaptsTILD += queryProvider.getAdaptationCounter();
                avgNumOfIterationsTargetDiagInLeadingDiagsTILD += queryProvider.getTargetDiagWithinLeadingDiagsCounter();
                avgEliminationRateDiagsTILD += avgNumOfEliminatedDiags;
                avgEliminationRatePercentTILD += avgPercentOfEliminatedDiags;
                avgNumOfQueriesTILD += qInfo.getIteration();
                avgProbOfMostProbDiagnosisTILD += discoveredTargetDiagnosis.getProbability();
                try{
                    IDistributionAdaptationQSS qp = (IDistributionAdaptationQSS)queryProvider;
                    avgCumulatedAlphaTILD += qp.getCumulatedAlpha();
                }catch(ClassCastException e){
                    //do nothing, because queryprovider is no IDistributionAdaptationQSS and has no cumulated alpha
                }
            } else {
                numOfFalseTargetDiagFound++;
                boolean TILD = false;

                for (QueryModuleDiagnosis d : currentDiagnoses) {
                    if (d.isTarget()) {
                        TILD = true;
                    }
                }

                if (TILD) {
                    numTargetDiagInFinalSetOfLeadingDiags++;

                    avgFinallyRemainingNumOfDiagsTILD += currentDiagnoses.size();
                    avgNumOfTooHighRiskTILD += queryProvider.getTooHighRiskCounter();
                    avgNumOfAdaptsTILD += queryProvider.getAdaptationCounter();
                    avgNumOfIterationsTargetDiagInLeadingDiagsTILD += queryProvider.getTargetDiagWithinLeadingDiagsCounter();
                    avgEliminationRateDiagsTILD += avgNumOfEliminatedDiags;
                    avgEliminationRatePercentTILD += avgPercentOfEliminatedDiags;
                    avgNumOfQueriesTILD += qInfo.getIteration();
                    avgProbOfMostProbDiagnosisTILD += discoveredTargetDiagnosis.getProbability();
                    try{
                        IDistributionAdaptationQSS qp = (IDistributionAdaptationQSS)queryProvider;
                        avgCumulatedAlphaTILD += qp.getCumulatedAlpha();
                    }catch(ClassCastException e){
                        //do nothing, because queryprovider is no IDistributionAdaptationQSS and has no cumulated alpha
                    }
                } else {
                    numTargetDiagNotInFinalSetOfLeadingDiags++;

                    avgFinallyRemainingNumOfDiagsNTILD += currentDiagnoses.size();
                    avgNumOfTooHighRiskNTILD += queryProvider.getTooHighRiskCounter();
                    avgNumOfAdaptsNTILD += queryProvider.getAdaptationCounter();
                    avgNumOfIterationsTargetDiagInLeadingDiagsNTILD += queryProvider.getTargetDiagWithinLeadingDiagsCounter();
                    avgEliminationRateDiagsNTILD += avgNumOfEliminatedDiags;
                    avgEliminationRatePercentNTILD += avgPercentOfEliminatedDiags;
                    avgNumOfQueriesNTILD += qInfo.getIteration();
                    avgProbOfMostProbDiagnosisNTILD += discoveredTargetDiagnosis.getProbability();
                    try{
                        IDistributionAdaptationQSS qp = (IDistributionAdaptationQSS)queryProvider;
                        avgCumulatedAlphaNTILD += qp.getCumulatedAlpha();
                    }catch(ClassCastException e){
                        //do nothing, because queryprovider is no IDistributionAdaptationQSS and has no cumulated alpha
                    }
                }

                avgFinallyRemainingNumOfDiagsF += currentDiagnoses.size();
                avgNumOfTooHighRiskF += queryProvider.getTooHighRiskCounter();
                avgNumOfAdaptsF += queryProvider.getAdaptationCounter();
                avgNumOfIterationsTargetDiagInLeadingDiagsF += queryProvider.getTargetDiagWithinLeadingDiagsCounter();
                avgEliminationRateDiagsF += avgNumOfEliminatedDiags;
                avgEliminationRatePercentF += avgPercentOfEliminatedDiags;
                avgNumOfQueriesF += qInfo.getIteration();
                avgProbOfMostProbDiagnosisF += discoveredTargetDiagnosis.getProbability();
                try{
                    IDistributionAdaptationQSS qp = (IDistributionAdaptationQSS)queryProvider;
                    avgCumulatedAlphaF += qp.getCumulatedAlpha();
                }catch(ClassCastException e){
                    //do nothing, because queryprovider is no IDistributionAdaptationQSS and has no cumulated alpha
                }
            }

            //System.out.println("|D|    = " + currentDiagnoses.size());
            avgFinallyRemainingNumOfDiags += currentDiagnoses.size();

            //System.out.println("HR#    = " + queryProvider.getTooHighRiskCounter());
            avgNumOfTooHighRisk += queryProvider.getTooHighRiskCounter();

            //System.out.println("ADPT#  = " + queryProvider.getAdaptationCounter());
            avgNumOfAdapts += queryProvider.getAdaptationCounter();

            //System.out.println("TiLD#  = " + queryProvider.getTargetDiagWithinLeadingDiagsCounter());
            avgNumOfIterationsTargetDiagInLeadingDiags += queryProvider.getTargetDiagWithinLeadingDiagsCounter();

            //System.out.println("ER(D)  = " + avgNumOfEliminatedDiags);
            avgEliminationRateDiags += avgNumOfEliminatedDiags;

            //System.out.println("ER(%)  = " + avgPercentOfEliminatedDiags);
            avgEliminationRatePercent += avgPercentOfEliminatedDiags;
            eliminationRateList.add(avgPercentOfEliminatedDiags);

            //System.out.println("Q#     = " + qInfo.getIteration());
            avgNumOfQueries += qInfo.getIteration();
            numOfRequiredQueriesList.add(qInfo.getIteration());

            //System.out.println("P(MPD) = " + discoveredTargetDiagnosis.getProbability());
            avgProbOfMostProbDiagnosis += discoveredTargetDiagnosis.getProbability();

            //System.out.println("-----");

            try{
                IDistributionAdaptationQSS qp = (IDistributionAdaptationQSS)queryProvider;
                avgCumulatedAlpha += qp.getCumulatedAlpha();
            }catch(ClassCastException e){
                //do nothing, because queryprovider is no IDistributionAdaptationQSS and has no cumulated alpha
            }

            iterationCount++;
        } ///////////////////////////////////////////////////////////////////////////////////////////////////////////// all iterations end here

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //---------------------------------- print summary -----------------------------------------------------------
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////

        boolean riskAwareStrategy = false;              // strategy which needs RiskPreferences as input
        boolean distributionAdaptationStrategy = false; // strategy which adapts the probability distribution of diagnoses

        if(querySelectionStrategy == QuProvider.DYNAMICRISK || querySelectionStrategy == QuProvider.DYNAMICRISK_NOADAPT ||
                querySelectionStrategy == QuProvider.DYNAMICRISK_CONDITIONALADAPT){
             riskAwareStrategy = true;
        }
        if(querySelectionStrategy == QuProvider.STATICRISK || querySelectionStrategy == QuProvider.DYNAMICRISK ||
                querySelectionStrategy == QuProvider.DYNAMICRISK_CONDITIONALADAPT){
             distributionAdaptationStrategy = true;
        }



        String s;

        System.out.println("--------- OVERALL AVERAGE PERFORMANCE ---------");
        System.out.println("DISTRIBUTION = " + diagGenMode);
        System.out.println("TOTAL ITERATIONS = " + totalIterations);
        System.out.println("ITERATIONS where TRUE target diagnosis found = " + numOfTrueTargetDiagFound);
        System.out.println("ITERATIONS where FALSE target diagnosis found = " + numOfFalseTargetDiagFound);
        System.out.println("ITERATIONS where target diagnosis IS in final set of leading diagnoses  = " + numTargetDiagInFinalSetOfLeadingDiags);
        System.out.println("ITERATIONS where target diagnosis IS NOT in final set of leading diagnoses  = " + numTargetDiagNotInFinalSetOfLeadingDiags);
        System.out.println("Number of all diagnoses = " + numberOfAllDiags);
        System.out.println("Number of leading diagnoses = " + numOfLeadingDiags);
        System.out.println("Sector of target diagnosis = [" + targDiagSectorU + "," + targDiagSectorO + "]");
        if(querySelectionStrategy == QuProvider.DYNAMICRISK || querySelectionStrategy == QuProvider.DYNAMICRISK_NOADAPT ||
                querySelectionStrategy == QuProvider.DYNAMICRISK_CONDITIONALADAPT){
            System.out.println("Risk preferences = " + rp.toString());
        }
        if(querySelectionStrategy == QuProvider.STATICRISK){
            System.out.println("Percent = " + staticRiskPercent);
        }
        if(querySelectionStrategy == QuProvider.PENALTY){
            System.out.println("Max Penalty = " + maxPenalty);
        }
        System.out.println("sigma = " + sigma);
        System.out.println("User recognizes target diagnosis = " + userRecognizesTD);
        System.out.println("- - - - - - - - - - - - - - - - - - - - - - - -");
        System.out.println("--- ALL ITERATIONS ---");

        System.out.println("P(TDF)     = " + ((double)numOfTrueTargetDiagFound / (double) totalIterations));
        System.out.println("P(FINTiLD) = " + ((double)numTargetDiagInFinalSetOfLeadingDiags / (double) totalIterations));
        System.out.println("P(>S)      = " + ((double)numOfSigmaTermination / (double) totalIterations));
        System.out.println("P(NQ)      = " + ((double)numOfNoFurtherQueryTermination / (double) totalIterations));
        System.out.println("P(UF)      = " + ((double)numOfUserFoundTermination / (double) totalIterations));
        // ------------------------------------------------------------------------------------------------------------------
        // ------------------------------------------------------------------------------------------------------------------

        System.out.println("Observed elimination rates: ");
        printList(eliminationRateList);
        System.out.println("Observed numbers of queries: ");
        printList(numOfRequiredQueriesList);

        s= "|D|";
        System.out.printf("%-30s\t",s);
        s= "HR#";
        System.out.printf("%-30s\t",s);
        s= "ADPT#";
        System.out.printf("%-30s\t",s);
        s= "TiLD#";
        System.out.printf("%-30s\t",s);
        s= "ER(D)";
        System.out.printf("%-30s\t",s);
        s= "ER(%)";
        System.out.printf("%-30s\t",s);
        s= "Q#";
        System.out.printf("%-30s\t",s);
        s= "P(MPD)";
        System.out.printf("%-30s\t",s);
        if(distributionAdaptationStrategy){
            s= "cuAlpha";
            System.out.printf("%-30s\t",s);
        }
        System.out.print("\n");


        s = "" + avgFinallyRemainingNumOfDiags / (double) totalIterations;
        System.out.printf("%-30s\t", s);

        s = "" + avgNumOfTooHighRisk / (double) totalIterations;
        System.out.printf("%-30s\t", s);

        s = "" + avgNumOfAdapts / (double) totalIterations;
        System.out.printf("%-30s\t", s);

        s = "" + avgNumOfIterationsTargetDiagInLeadingDiags / (double) totalIterations;
        System.out.printf("%-30s\t", s);

        s = "" + avgEliminationRateDiags / (double) totalIterations;
        System.out.printf("%-30s\t", s);

        s = "" + avgEliminationRatePercent / (double) totalIterations;
        System.out.printf("%-30s\t", s);

        s = "" + avgNumOfQueries / (double) totalIterations;
        System.out.printf("%-30s\t", s);

        s = "" + avgProbOfMostProbDiagnosis / (double) totalIterations;
        System.out.printf("%-30s\t", s);
        if(distributionAdaptationStrategy){
            s = "" + avgCumulatedAlpha / (double) totalIterations;
            System.out.printf("%-30s\t", s);
        }


        System.out.println();
        // ------------------------------------------------------------------------------------------------------------------
        // ------------------------------------------------------------------------------------------------------------------
        System.out.println("--- TARGET DIAG FOUND ---");
        /*
        System.out.println("|D|    = " + avgFinallyRemainingNumOfDiagsT / (double) numOfTrueTargetDiagFound);
        System.out.println("HR#    = " + avgNumOfTooHighRiskT / (double) numOfTrueTargetDiagFound);
        System.out.println("ADPT#  = " + avgNumOfAdaptsT / (double) numOfTrueTargetDiagFound);
        System.out.println("TiLD#  = " + avgNumOfIterationsTargetDiagInLeadingDiagsT / (double) numOfTrueTargetDiagFound);
        System.out.println("ER(D)  = " + avgEliminationRateDiagsT / (double) numOfTrueTargetDiagFound);
        System.out.println("ER(%)  = " + avgEliminationRatePercentT / (double) numOfTrueTargetDiagFound);
        System.out.println("Q#     = " + avgNumOfQueriesT / (double) numOfTrueTargetDiagFound);
        System.out.println("P(MPD) = " + avgProbOfMostProbDiagnosisT / (double) numOfTrueTargetDiagFound);
        */


        s = "" + avgFinallyRemainingNumOfDiagsT / (double) numOfTrueTargetDiagFound;
        System.out.printf("%-30s\t", s);

        s = "" + avgNumOfTooHighRiskT / (double) numOfTrueTargetDiagFound;
        System.out.printf("%-30s\t", s);

        s = "" + avgNumOfAdaptsT / (double) numOfTrueTargetDiagFound;
        System.out.printf("%-30s\t", s);

        s = "" + avgNumOfIterationsTargetDiagInLeadingDiagsT / (double) numOfTrueTargetDiagFound;
        System.out.printf("%-30s\t", s);

        s = "" + avgEliminationRateDiagsT / (double) numOfTrueTargetDiagFound;
        System.out.printf("%-30s\t", s);

        s = "" + avgEliminationRatePercentT / (double) numOfTrueTargetDiagFound;
        System.out.printf("%-30s\t", s);

        s = "" + avgNumOfQueriesT / (double) numOfTrueTargetDiagFound;
        System.out.printf("%-30s\t", s);

        s = "" + avgProbOfMostProbDiagnosisT / (double) numOfTrueTargetDiagFound;
        System.out.printf("%-30s\t", s);

        if(distributionAdaptationStrategy){
            s = "" + avgCumulatedAlphaT / (double) numOfTrueTargetDiagFound;
            System.out.printf("%-30s\t", s);
        }

        System.out.println();
        // ------------------------------------------------------------------------------------------------------------------
        // ------------------------------------------------------------------------------------------------------------------
        System.out.println("--- TARGET DIAG NOT FOUND ---");
        /*
        System.out.println("|D|    = " + avgFinallyRemainingNumOfDiagsF / (double) numOfFalseTargetDiagFound);
        System.out.println("HR#    = " + avgNumOfTooHighRiskF / (double) numOfFalseTargetDiagFound);
        System.out.println("ADPT#  = " + avgNumOfAdaptsF / (double) numOfFalseTargetDiagFound);
        System.out.println("TiLD#  = " + avgNumOfIterationsTargetDiagInLeadingDiagsF / (double) numOfFalseTargetDiagFound);
        System.out.println("ER(D)  = " + avgEliminationRateDiagsF / (double) numOfFalseTargetDiagFound);
        System.out.println("ER(%)  = " + avgEliminationRatePercentF / (double) numOfFalseTargetDiagFound);
        System.out.println("Q#     = " + avgNumOfQueriesF / (double) numOfFalseTargetDiagFound);
        System.out.println("P(MPD) = " + avgProbOfMostProbDiagnosisF / (double) numOfFalseTargetDiagFound);
        */


        s = "" + avgFinallyRemainingNumOfDiagsF / (double) numOfFalseTargetDiagFound;
        System.out.printf("%-30s\t", s);

        s = "" + avgNumOfTooHighRiskF / (double) numOfFalseTargetDiagFound;
        System.out.printf("%-30s\t", s);

        s = "" + avgNumOfAdaptsF / (double) numOfFalseTargetDiagFound;
        System.out.printf("%-30s\t", s);

        s = "" + avgNumOfIterationsTargetDiagInLeadingDiagsF / (double) numOfFalseTargetDiagFound;
        System.out.printf("%-30s\t", s);

        s = "" + avgEliminationRateDiagsF / (double) numOfFalseTargetDiagFound;
        System.out.printf("%-30s\t", s);

        s = "" + avgEliminationRatePercentF / (double) numOfFalseTargetDiagFound;
        System.out.printf("%-30s\t", s);

        s = "" + avgNumOfQueriesF / (double) numOfFalseTargetDiagFound;
        System.out.printf("%-30s\t", s);

        s = "" + avgProbOfMostProbDiagnosisF / (double) numOfFalseTargetDiagFound;
        System.out.printf("%-30s\t", s);

        if(distributionAdaptationStrategy){
            s = "" + avgCumulatedAlphaF / (double) numOfFalseTargetDiagFound;
            System.out.printf("%-30s\t", s);
        }

        System.out.println();
        // ------------------------------------------------------------------------------------------------------------------
        // ------------------------------------------------------------------------------------------------------------------
        if(!userRecognizesTD){

                System.out.println("--- TARGET IN LEADING DIAGS ---");
                /*
                System.out.println("|D|    = " + avgFinallyRemainingNumOfDiagsTILD / (double) numTargetDiagInFinalSetOfLeadingDiags);
                System.out.println("HR#    = " + avgNumOfTooHighRiskTILD / (double) numTargetDiagInFinalSetOfLeadingDiags);
                System.out.println("ADPT#  = " + avgNumOfAdaptsTILD / (double) numTargetDiagInFinalSetOfLeadingDiags);
                System.out.println("TiLD#  = " + avgNumOfIterationsTargetDiagInLeadingDiagsTILD / (double) numTargetDiagInFinalSetOfLeadingDiags);
                System.out.println("ER(D)  = " + avgEliminationRateDiagsTILD / (double) numTargetDiagInFinalSetOfLeadingDiags);
                System.out.println("ER(%)  = " + avgEliminationRatePercentTILD / (double) numTargetDiagInFinalSetOfLeadingDiags);
                System.out.println("Q#     = " + avgNumOfQueriesTILD / (double) numTargetDiagInFinalSetOfLeadingDiags);
                System.out.println("P(MPD) = " + avgProbOfMostProbDiagnosisTILD / (double) numTargetDiagInFinalSetOfLeadingDiags);
                */

                s = "" + avgFinallyRemainingNumOfDiagsTILD / (double) numTargetDiagInFinalSetOfLeadingDiags;
                System.out.printf("%-30s\t", s);

                s = "" + avgNumOfTooHighRiskTILD / (double)  numTargetDiagInFinalSetOfLeadingDiags;
                System.out.printf("%-30s\t", s);

                s = "" + avgNumOfAdaptsTILD / (double)  numTargetDiagInFinalSetOfLeadingDiags;
                System.out.printf("%-30s\t", s);

                s = "" + avgNumOfIterationsTargetDiagInLeadingDiagsTILD / (double)  numTargetDiagInFinalSetOfLeadingDiags;
                System.out.printf("%-30s\t", s);

                s = "" + avgEliminationRateDiagsTILD / (double)  numTargetDiagInFinalSetOfLeadingDiags;
                System.out.printf("%-30s\t", s);

                s = "" + avgEliminationRatePercentTILD / (double)  numTargetDiagInFinalSetOfLeadingDiags;
                System.out.printf("%-30s\t", s);

                s = "" + avgNumOfQueriesTILD / (double)  numTargetDiagInFinalSetOfLeadingDiags;
                System.out.printf("%-30s\t", s);

                s = "" + avgProbOfMostProbDiagnosisTILD / (double)  numTargetDiagInFinalSetOfLeadingDiags;
                System.out.printf("%-30s\t", s);

                if(distributionAdaptationStrategy){
                    s = "" + avgCumulatedAlphaTILD / (double) numTargetDiagInFinalSetOfLeadingDiags;
                    System.out.printf("%-30s\t", s);
                }

                System.out.println();

                // ------------------------------------------------------------------------------------------------------------------
                // ------------------------------------------------------------------------------------------------------------------
                System.out.println("--- TARGET DIAG NOT IN LEADING DIAGS ---");
                /*
                System.out.println("|D|    = " + avgFinallyRemainingNumOfDiagsNTILD / (double) numTargetDiagNotInFinalSetOfLeadingDiags);
                System.out.println("HR#    = " + avgNumOfTooHighRiskNTILD / (double) numTargetDiagNotInFinalSetOfLeadingDiags);
                System.out.println("ADPT#  = " + avgNumOfAdaptsNTILD / (double) numTargetDiagNotInFinalSetOfLeadingDiags);
                System.out.println("TiLD#  = " + avgNumOfIterationsTargetDiagInLeadingDiagsNTILD / (double) numTargetDiagNotInFinalSetOfLeadingDiags);
                System.out.println("ER(D)  = " + avgEliminationRateDiagsNTILD / (double) numTargetDiagNotInFinalSetOfLeadingDiags);
                System.out.println("ER(%)  = " + avgEliminationRatePercentNTILD / (double) numTargetDiagNotInFinalSetOfLeadingDiags);
                System.out.println("Q#     = " + avgNumOfQueriesNTILD / (double) numTargetDiagNotInFinalSetOfLeadingDiags);
                System.out.println("P(MPD) = " + avgProbOfMostProbDiagnosisNTILD / (double) numTargetDiagNotInFinalSetOfLeadingDiags);
                */

                s = "" + avgFinallyRemainingNumOfDiagsNTILD / (double) numTargetDiagNotInFinalSetOfLeadingDiags;
                System.out.printf("%-30s\t", s);

                s = "" + avgNumOfTooHighRiskNTILD / (double) numTargetDiagNotInFinalSetOfLeadingDiags;
                System.out.printf("%-30s\t", s);

                s = "" + avgNumOfAdaptsNTILD / (double) numTargetDiagNotInFinalSetOfLeadingDiags;
                System.out.printf("%-30s\t", s);

                s = "" + avgNumOfIterationsTargetDiagInLeadingDiagsNTILD / (double) numTargetDiagNotInFinalSetOfLeadingDiags;
                System.out.printf("%-30s\t", s);

                s = "" + avgEliminationRateDiagsNTILD / (double) numTargetDiagNotInFinalSetOfLeadingDiags;
                System.out.printf("%-30s\t", s);

                s = "" + avgEliminationRatePercentNTILD / (double) numTargetDiagNotInFinalSetOfLeadingDiags;
                System.out.printf("%-30s\t", s);

                s = "" + avgNumOfQueriesNTILD / (double) numTargetDiagNotInFinalSetOfLeadingDiags;
                System.out.printf("%-30s\t", s);

                s = "" + avgProbOfMostProbDiagnosisNTILD / (double) numTargetDiagNotInFinalSetOfLeadingDiags;
                System.out.printf("%-30s\t", s);

                if(distributionAdaptationStrategy){
                    s = "" + avgCumulatedAlphaNTILD / (double) numTargetDiagNotInFinalSetOfLeadingDiags;
                    System.out.printf("%-30s\t", s);
                }

                System.out.println();
        }

        System.out.println("------------------------------------------------");
        // ------------------------------------------------------------------------------------------------------------------
        // ------------------------------------------------------------------------------------------------------------------

    }



    private void verboseIterations(QuProvider querySelectionStrategy, int totalIterations, double targDiagSectorU, double targDiagSectorO, int numberOfAllDiags, double sigma, double delta,
                                   RiskPreferences rp, boolean userRecognizesTD, int numOfLeadingDiags, double maxPenalty, double staticRiskPercent, DiagGenMode diagGenMode) {

        int iterationCount = 1;


        while (iterationCount <= totalIterations) {

            boolean targetDiagnosisFound = false;
            QueryModuleDiagnosis discoveredTargetDiagnosis = null;
            QInfo qInfo = null;
            List<QueryModuleDiagnosis> currentDiagnoses;
            boolean alternativeQuery = false;
            boolean noFurtherQuery = false;
            boolean noDiagnosis = false;
            int totalNumOfEliminatedDiags = 0;
            double avgNumOfEliminatedDiags = 0d;
            double avgPercentOfEliminatedDiags = 0d;
            double sumOfPercentOfEliminatedDiags = 0d;


            IDiagGenerator userDistribution;
            switch(diagGenMode){
                case EXTREME:
                    userDistribution = new SteepDistribution(SteepDistributionMode.ONE_DIV_PARAM_POWER_X,(iterationCount * 7) % totalIterations, targDiagSectorU, targDiagSectorO, 1.75d);
                    break;
                    // ODER: userDistribution = new RandomDistribution(true, targDiagSectorU, targDiagSectorO, true, (iterationCount * 7) % totalIterations);
                case MODERATE:
                    userDistribution = new SteepDistribution(SteepDistributionMode.ONE_DIV_PARAM_POWER_X,(iterationCount * 7) % totalIterations, targDiagSectorU, targDiagSectorO, 1.25d);
                    //userDistribution = new RandomDistribution(false, targDiagSectorU, targDiagSectorO, true, (iterationCount * 7) % totalIterations);
                    break;
                case UNIFORM:
                    userDistribution = new UniformDistribution(targDiagSectorU,targDiagSectorO);
                    break;
                default:
                    throw new IllegalArgumentException("Illegal Diagnosis Generation Mode selected!");

            }

            //IDiagGenerator userDistribution = new RandomDistribution(false, targDiagSectorU, targDiagSectorO, true, (iterationCount * 7) % totalIterations);

            IProbabilityAssigner probAssigner = new RandomDistributionAssign(Mode.INVERTED, (iterationCount * 13) % totalIterations);

            IDiagnosisProvider diagProvider = null;
            IQueryProvider queryProvider = null;
            try {
                diagProvider = new SimDiag(userDistribution, probAssigner, numberOfAllDiags, iterationCount);
                diagProvider.assignActualProbabilities();

                if (querySelectionStrategy == QuProvider.SPLIT) {
                    queryProvider = new SplitInHalfQSS();
                    queryProvider.setNumOfLeadingDiagnoses(numOfLeadingDiags);
                    queryProvider.setDiagnosisProvider(diagProvider);
                } else if (querySelectionStrategy == QuProvider.MINSCORE) {
                    queryProvider = new MinScoreQSS();
                    queryProvider.setNumOfLeadingDiagnoses(numOfLeadingDiags);
                    queryProvider.setDiagnosisProvider(diagProvider);
                } else if (querySelectionStrategy == QuProvider.DYNAMICRISK) {
                    queryProvider = new DynamicRiskQSS(rp, RiskUpdateStrategy.STANDARD);           // 0.4, 0.2, 0.5
                    queryProvider.setNumOfLeadingDiagnoses(numOfLeadingDiags);
                    queryProvider.setDiagnosisProvider(diagProvider);
                } else if (querySelectionStrategy == QuProvider.DYNAMICRISK_NOADAPT) {
                    queryProvider = new DynamicRiskNoAdaptQSS(rp, RiskUpdateStrategy.STANDARD);
                    queryProvider.setNumOfLeadingDiagnoses(numOfLeadingDiags);
                    queryProvider.setDiagnosisProvider(diagProvider);
                } else if (querySelectionStrategy == QuProvider.DYNAMICRISK_CONDITIONALADAPT) {
                    queryProvider = new DynamicRiskConditionalAdaptQSS(rp, RiskUpdateStrategy.STANDARD);
                    queryProvider.setNumOfLeadingDiagnoses(numOfLeadingDiags);
                    queryProvider.setDiagnosisProvider(diagProvider);
                } else if (querySelectionStrategy == QuProvider.PENALTY) {
                    queryProvider = new PenaltyQSS(maxPenalty);
                    queryProvider.setNumOfLeadingDiagnoses(numOfLeadingDiags);
                    queryProvider.setDiagnosisProvider(diagProvider);
                } else if (querySelectionStrategy == QuProvider.STATICRISK) {
                    queryProvider = new StaticRiskQSS(staticRiskPercent);
                    queryProvider.setNumOfLeadingDiagnoses(numOfLeadingDiags);
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


                //////////////////// GUI-OUTPUT /////////////////////
                double s, a, b, c, g = 0d;
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
                //System.out.println("__________________________________________________________________________________a+b+c = " + g);
                /////////////////////////////////////////////////////
                boolean answer = false;
                try {
                    answer = queryProvider.getQueryAnswer(q);
                    qInfo = queryProvider.setQueryAnswer(answer);

                } catch (AnswerNotKnownException e) {
                    alternativeQuery = true;
                    System.out.println("---->  User wants alternative query!");
                    continue;
                }


                //////////////////// GUI-OUTPUT /////////////////////

                System.out.println("User's answer is " + answer);


                /////////////////////////////////////////////////////


                //////////////////// GUI-OUTPUT /////////////////////
                double temp;
                System.out.println("NUMBER OF DIAGNOSES ELIMINATED = " + qInfo.getNumOfEliminatedDiags() + " from " + q.getNumOfAllDiags() + " total remaining diagnoses! " +
                        " -------> " + (temp = (qInfo.getNumOfEliminatedDiags() / q.getNumOfAllDiags() * 100d)) + " percent");
                System.out.println("##############    Risk (= percent) for next query = " + qInfo.getPercentForNextQuery());
                /////////////////////////////////////////////////////


                totalNumOfEliminatedDiags += qInfo.getNumOfEliminatedDiags();
                avgNumOfEliminatedDiags = totalNumOfEliminatedDiags / qInfo.getIteration();
                System.out.println("---------- Average Elimination Rate (in Diagnoses) so far = " + avgNumOfEliminatedDiags + " -----------");

                sumOfPercentOfEliminatedDiags += temp;
                avgPercentOfEliminatedDiags = sumOfPercentOfEliminatedDiags / qInfo.getIteration();
                System.out.println("---------- Average Elimination Rate (in percent of leading diagnoses) so far = " + avgPercentOfEliminatedDiags + " -----------");


                currentDiagnoses = qInfo.getCurrentDiags();


                QueryModuleDiagnosis maxProbDiag = currentDiagnoses.get(0);
                for (QueryModuleDiagnosis d : currentDiagnoses) {
                    if (userRecognizesTD) {
                        if (d.getProbability() > maxProbDiag.getProbability()) {
                            maxProbDiag = d;
                        }
                    } else {
                        if (d.getProbability() > sigma) {
                            targetDiagnosisFound = true;
                            discoveredTargetDiagnosis = d;
                            break;
                        }
                    }
                }
                if (userRecognizesTD) {
                    if (maxProbDiag.isTarget()) {
                        targetDiagnosisFound = true;
                        discoveredTargetDiagnosis = maxProbDiag;
                    }
                }

            }
            if (noFurtherQuery) {
                System.out.println("No more queries available --- most probable diagnosis is " + discoveredTargetDiagnosis.getName());
            } else if (targetDiagnosisFound) {
                System.out.println("Probability of most probable diagnosis exceeds sigma --- Found diagnosis is " + discoveredTargetDiagnosis.getName());

            } else if (noDiagnosis) {
                System.out.println("Diagnosis session cannot be started - no potential diagnoses could be found!");
            }


            System.out.println("Number of remaining diagnoses after debugging session is " + currentDiagnoses.size());
            System.out.println("Algorithm's answer is --- " + discoveredTargetDiagnosis.isTarget());
            System.out.println("Actual target diagnosis is --- " + probAssigner.getTargetDiag().getName());
            System.out.println(queryProvider.getTooHighRiskCounter() + " times MinScore wanted to take too high risk!");
            System.out.println(queryProvider.getAdaptationCounter() + " times an alpha-adaptation was performed!");
            System.out.println(queryProvider.getTargetDiagWithinLeadingDiagsCounter() + " iterations the target diagnosis was within the set of leading diagnoses!");
            System.out.println("__________________________________________________________________________________________________________________________");
            System.out.println("__________________________________________________________________________________________________________________________");
            System.out.println("__________________________________________________________________________________________________________________________");

            iterationCount++;
        }
    }

    private void printList(List<? extends Number> list){
        //if(list.get(0).)
        for(Number n : list){
            System.out.printf("%6.2f \t", n.doubleValue() );
        }
        System.out.print("\n");
    }

}
