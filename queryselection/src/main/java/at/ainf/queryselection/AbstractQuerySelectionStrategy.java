package at.ainf.queryselection;

import at.ainf.theory.model.SolverException;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 22.02.11
 * Time: 15:07
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractQuerySelectionStrategy extends Observable implements IQueryProvider {

    public static final String START_MESS = "is starting diagnosis discrimination procedure";
    public static final String QUERYRESULT_MESS = "As result of this query the remaining set of diagnoses is: ";
    public static final String POSRESULT_MESS = "D_X + D_0 = ";
    public static final String NEGRESULT_MESS = "D_notX + D_0 = ";
    public static final String PROBAPRIORI_MESS = "Probability distribution of diagnoses before query: ";
    public static final String PROBPOST_MESS = "Probability distribution of diagnoses after query: ";
    public static final String PROBPOSTADAPT_MESS = "Probability distribution of diagnoses after adaptation: ";
    public static final String SOLUTIONSTEPS_MESS = "found a solution after asking ";
    public static final String SOLUTIONDIAG_MESS = "The solution is diagnosis ";
    public static final String PROB_ENTROPY = "The probability distribution entropy = ";
    public static final String LINE_SEP = "------------------------------------------------------------------------------------------------------------------------------";

    public static final int TYPE_MINSCORE = 1;
    public static final String NAME_MINSCORE = "Minimal Score Algorithm";
    public static final int TYPE_STATIC_ADAPT = 2;
    public static final String NAME_DISTADAPT = "Static Risk Adaptation Algorithm";
    public static final int TYPE_SPLIT = 3;
    public static final String NAME_SPLIT = "Split-In-Half Algorithm";
    public static final int TYPE_DYNAMIC_ADAPT = 4;
    public static final String NAME_VARDAA = "Dynamic Risk Adaptation Algorithm";


    protected List<QueryModuleDiagnosis> currentDiags;
    protected List<Query> currentQueries;
    protected int step;
    protected Query askedQuery;
    protected IDiagnosisProvider diagnosisProvider;
    protected int numOfLeadingDiagnoses;

    protected int adaptationCounter = 0;   // counts how often an alpha-adaptation takes place
    protected int tooHighRiskCounter = 0; // counts how often MinScore would take more risk than specified by risk parameter k
    protected int targetDiagWithinLeadingDiagsCounter = 0; // counts, in how many iterations the target diagnosis is within the set of leading diagnoses
    protected double avgEliminationRate = 0d;
    protected int totalNumOfElimDiags = 0;
    protected int numOfTotalLeadingDiagsSoFar = 0;
    //protected double alpha = 1f;


    /*
    public AbstractQuerySelectionStrategy(int type, String name){

        this.currentDiags = new LinkedList<Diagnosis>();
        this.currentQueries = new LinkedList<Query>();
        this.step = 0;
        this.askedQuery = null;



        for(Query q : this.currentQueries){
            this.addObserver(q);
        }

    }
    */

    public AbstractQuerySelectionStrategy() {
        this.currentDiags = new LinkedList<QueryModuleDiagnosis>();
        this.currentQueries = new LinkedList<Query>();
        this.step = 0;
        this.askedQuery = null;


        for (Query q : this.currentQueries) {
            this.addObserver(q);
        }
    }

    public abstract int getType();

    public abstract String getName();

    public String getStrategyName() {
        return this.getName();
    }

    public int getTooHighRiskCounter() {
        return this.tooHighRiskCounter;
    }

    public int getAdaptationCounter() {
        return this.adaptationCounter;
    }

    public int getTargetDiagWithinLeadingDiagsCounter() {
        return this.targetDiagWithinLeadingDiagsCounter;
    }

    public void setNumOfLeadingDiagnoses(int n) {
        this.numOfLeadingDiagnoses = n;
    }

    public void setDiagnosisProvider(IDiagnosisProvider diagnosisProvider) {
        this.diagnosisProvider = diagnosisProvider;
    }

    protected LinkedList<QueryModuleDiagnosis> getDiagsEliminatedByQuery(boolean queryAnswer) {
        LinkedList<QueryModuleDiagnosis> diagsToEliminate;
        if (queryAnswer == true) {
            diagsToEliminate = this.askedQuery.getD_notX();
            for (QueryModuleDiagnosis d : this.askedQuery.getD_notX_restDiags()) {
                if (!d.isTarget()) {
                    d.setInconsistent();
                }
            }

        } else {
            diagsToEliminate = this.askedQuery.getD_X();
            for (QueryModuleDiagnosis d : this.askedQuery.getD_X_restDiags()) {
                if (!d.isTarget()) {
                    d.setInconsistent();
                }
            }
        }
        // for all diagnoses in D_0 set Bayes penalty (= multiply with 1/2)
        for (QueryModuleDiagnosis d : this.askedQuery.getD_0()) {
            d.incTimesInD_0();
        }
        for (QueryModuleDiagnosis d : this.askedQuery.getD_0_restDiags()) {
            d.incTimesInD_0();
        }
        return diagsToEliminate;
    }

    public QInfo setQueryAnswer(boolean a) {
        LinkedList<QueryModuleDiagnosis> diagsToEliminate = this.getDiagsEliminatedByQuery(a);

        updateAvgEliminationRate(diagsToEliminate.size(), currentDiags.size()); // darf nicht weiter nach unten positioniert werden, wegen 2. Argument

        for (QueryModuleDiagnosis d : diagsToEliminate) {
            this.currentDiags.remove(d);
        }

        QInfo qInfo = new QInfo();
        qInfo.setCurrentDiags(this.getCurrentDiagnoses());  // here the probability update takes place
        qInfo.setIteration(this.step);
        qInfo.setNumberOfEliminatedDiags(diagsToEliminate.size());



        return qInfo;
    }

    protected void updateAvgEliminationRate(int numOfElimDiags, int numOfDiagsInCurrentLDWindow){
        this.totalNumOfElimDiags += numOfElimDiags;
        this.numOfTotalLeadingDiagsSoFar += numOfDiagsInCurrentLDWindow;

        this.avgEliminationRate = (double)this.totalNumOfElimDiags / (double)numOfTotalLeadingDiagsSoFar;
    }


    public List<QueryModuleDiagnosis> getCurrentDiagnoses()  {

        /*if(this.currentDiags.isEmpty()){
            if((this.currentDiags = this.diagnosisProvider.getDiagnoses(this.numOfLeadingDiagnoses)).isEmpty()){
                throw new NoDiagnosisFoundException();
            }
        }else
        */
        if (this.currentDiags.size() == this.numOfLeadingDiagnoses) {
            return this.currentDiags;
        } else {
            currentDiags = new LinkedList<QueryModuleDiagnosis>(this.diagnosisProvider.getDiagnoses(this.numOfLeadingDiagnoses ));
            /*List<QueryModuleDiagnosis> newDiags = this.diagnosisProvider.getDiagnoses(this.numOfLeadingDiagnoses - this.currentDiags.size());
            if (!newDiags.isEmpty()) {
                for (QueryModuleDiagnosis d : newDiags) {
                    this.currentDiags.add(d);
                }
            }/ */
        }
        this.applyBayesToProbabilities();
        this.normalizeProbabilities();
        //this.applyAlphaAdaptToProbabilities();
        return this.currentDiags;
    }



    protected void applyBayesToProbabilities() {
        for (QueryModuleDiagnosis d : this.currentDiags) {
            double userAssignedProb = d.getUserAssignedProbability();
            //System.out.println("p(" + d.getName() +") = " + d.getUserAssignedProbability() + " before Bayes!");
            d.setProbability(userAssignedProb * Math.pow((double) 1 / (double) 2, (double) d.getTimesInD_0()));
            //System.out.println("Bayes: " + userAssignedProb + " * " + Math.pow((double)1/2,(double)d.getTimesInD_0()) + " ...");
            //System.out.println("p(" + d.getName() +") = " + d.getProbability() + " after Bayes!");
        }
    }


    protected void normalizeProbabilities() {
        double sumOfProbabilities = 0;
        for (QueryModuleDiagnosis d : this.currentDiags) {
            sumOfProbabilities += d.getProbability();
        }

        /*////////////////////
        double sum = 0f;
        */////////////////////
        for (QueryModuleDiagnosis d : this.currentDiags) {
            /*///////////////////
            System.out.print("normalizeProbs() ---- before : " + d.getName() + " --- " + d.getProbability());
            */////////////////////

            d.setProbability(d.getProbability() / sumOfProbabilities);

            /*////////////////////
            System.out.println("--- after : " + d.getProbability());
            sum += d.getProbability();
            */////////////////////
        }
        /*///////////////////
        System.out.println("prob-sum = " + sum);
        */////////////////////

        /*
        this.setChanged();
        this.notifyObservers(this.currentDiags);
        */
    }


    protected abstract Query selectQuery();

    public Query getQuery(boolean alternativeQuery) throws NoFurtherQueryException, SolverException, SingleDiagnosisLeftException {




        if (!alternativeQuery) {

            this.step++;
            if (!this.getCurrentDiagnoses().isEmpty()) {
                if(this.currentDiags.size() == 1){
                    throw new SingleDiagnosisLeftException("There is only one diagnosis left");
                }
                /*//////////////
                int count = 0;
                for(Diagnosis d : this.currentDiags){
                    System.out.println("Diag_" + ++count + " Prob = " + d.getProbability());
                }
                *///////////////
                this.currentQueries = diagnosisProvider.getAllQueries(this.currentDiags);



            }
            /*//////////////
            int c = 0;
            for(Query q : this.currentQueries){
                 System.out.println("Query_" + ++c + ": D_X = " + q.d_XToString(true) + "  P(D_X) = " + q.getProb_X() +
                         "---   D_notX = " + q.d_notXToString(true) + "  P(D_notX) = " + q.getProb_notX() +
                         "---   D_0 = " + q.d_0ToString(true) + "  P(D_0) = " + q.getProb_0());
            }
            *///////////////


        } else {
            if (!this.currentQueries.remove(this.askedQuery)) throw new IllegalStateException();
        }

        //if(this.currentQueries.isEmpty()) return null;

        Query q = this.selectQuery();
        if (q != null) {
            q.setIteration(this.step);
            this.askedQuery = q;
        } else {
            throw new NoFurtherQueryException();
        }
        return q;
    }

    public QueryModuleDiagnosis getMostProbableDiagnosis() {
        QueryModuleDiagnosis mostProbableDiagnosis = this.currentDiags.get(0);
        for (QueryModuleDiagnosis d : this.currentDiags) {
            if (d.getProbability() > mostProbableDiagnosis.getProbability()) {
                mostProbableDiagnosis = d;
            }
        }
        return mostProbableDiagnosis;
    }


    public boolean getQueryAnswer(Query query) throws AnswerNotKnownException {
        boolean answer = false;
        boolean targetDiagInCurrentDiags = false;
        for (QueryModuleDiagnosis d : this.currentDiags) {
            if (d.isTarget()) {
                targetDiagInCurrentDiags = true;
                this.targetDiagWithinLeadingDiagsCounter++;
            }
        }
        if (targetDiagInCurrentDiags) {
            boolean targetDiagInD_0 = false;
            for (QueryModuleDiagnosis d : query.getD_0()) {
                if (d.isTarget()) {
                    targetDiagInD_0 = true;
                }
            }
            if (!targetDiagInD_0) {
                answer = query.getAnswer();
                return answer;
            } else {
                //targetDiagInCurrentDiags = false;
                //throw new AnswerNotKnownException();
                answer = query.getAnswer();
                return answer;
            }
        }
        if (!targetDiagInCurrentDiags) {
            Random rndGen = new Random((long) query.getQueryHash());
            double r = rndGen.nextDouble();
            if (r < query.getActualProbabilityForPositiveAnswer()) {
                answer = true;
            }
            /*////////////////////////////////////
            double s = 0.75d;
            if(query.getD_X().size() == 1 && query.getProb_X() > s){
                System.out.println("cond1 met");
                answer = false;
            }
            if(query.getD_notX().size() == 1 && query.getProb_notX() > s){
                System.out.println("cond2 met");
                answer = true;
            }
            */////////////////////////////////////
        }
        return answer;
    }


    protected Query[] getQueriesSortedByScore() {
        if (this.currentQueries.size() == 0) {
            return null;
        }
        Query[] qArray = new Query[this.currentQueries.size()];
        for (int i = 0; i < this.currentQueries.size(); i++) {
            qArray[i] = this.currentQueries.get(i);
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


    public List<Query> getCurrentQueries() {
        return this.currentQueries;
    }


    public String probDistToString() {
        int numOfDiags;
        String s = "";
        for (int i = 0; i < (numOfDiags = this.currentDiags.size()); i++) {
            if (i < numOfDiags - 1) {
                s += "P(" + this.currentDiags.get(i).getName() + ") = " + this.currentDiags.get(i).getProbability() + ", ";
            } else {
                s += "P(" + this.currentDiags.get(i).getName() + ") = " + this.currentDiags.get(i).getProbability();
            }
        }
        return s;
    }


    public int getStep() {
        return this.step;
    }

    protected void setCurrentDiagnoses(LinkedList<QueryModuleDiagnosis> diags) {
        this.currentDiags = diags;
    }

    protected void setCurrentQueries(LinkedList<Query> queries) {
        this.currentQueries = queries;
    }

    protected void setStep(int s) {
        this.step = s;
    }

    protected void updateProbabilities(Query q, int answer) {

        LinkedList<QueryModuleDiagnosis> tempDiags = new LinkedList<QueryModuleDiagnosis>();
        LinkedList<QueryModuleDiagnosis> updatedDiags = new LinkedList<QueryModuleDiagnosis>();
        double prob_X = q.getProb_X();
        double prob_notX = q.getProb_notX();
        double prob_0 = q.getProb_0();
        LinkedList<QueryModuleDiagnosis> d_X = q.getD_X();
        LinkedList<QueryModuleDiagnosis> d_notX = q.getD_notX();
        LinkedList<QueryModuleDiagnosis> d_0 = q.getD_0();

        if (answer == 1) {
            for (QueryModuleDiagnosis d : d_X) {
                double p = d.getProbability();
                p = p / (prob_X + (prob_0 / 2));
                d.setProbability(p);
                tempDiags.add(d);
            }
        } else {
            for (QueryModuleDiagnosis d : d_notX) {
                double p = d.getProbability();
                p = p / (prob_notX + (prob_0 / 2));
                d.setProbability(p);
                tempDiags.add(d);
            }
        }
        for (QueryModuleDiagnosis d : d_0) {
            double p = d.getProbability();
            if (answer == 1) {
                p = (1 / 2) * p / (prob_X + (prob_0 / 2));
            } else {
                p = (1 / 2) * p / (prob_notX + (prob_0 / 2));
            }
            d.setProbability(p);
            tempDiags.add(d);
        }

        for (QueryModuleDiagnosis d : tempDiags) {
            if (!(d.getProbability() == 0f)) {
                updatedDiags.add(d);
            }
        }

        this.currentDiags = updatedDiags;
        this.setChanged();
        this.notifyObservers(this.currentDiags);
    }


    protected boolean updateQueries(Query queryToRemove) {
        this.deleteObserver(queryToRemove);
        return this.currentQueries.remove(queryToRemove);
    }

    protected Query[] getQueriesSortedByEliminationRate() {
        if (this.currentQueries.size() == 0) {
            return null;
        }
        Query[] qArray = new Query[this.currentQueries.size()];
        for (int i = 0; i < this.currentQueries.size(); i++) {
            qArray[i] = this.currentQueries.get(i);
        }
        boolean unsorted = true;
        Query temp;

        while (unsorted) {
            unsorted = false;
            for (int i = 0; i < qArray.length - 1; i++)
                if (qArray[i].getMinNumOfElimDiags() < qArray[i + 1].getMinNumOfElimDiags()) {
                    temp = qArray[i];
                    qArray[i] = qArray[i + 1];
                    qArray[i + 1] = temp;
                    unsorted = true;
                }
        }

        return qArray;
    }




    /*
        protected double getDistributionEntropy(){
            double entropy = 0d;
            for(Diagnosis d : this.currentDiags){
                if(d.getProbability() != 1.0f){
                    entropy = entropy - (double)d.getProbability() * (Math.log((double)d.getProbability() / Math.log((double)2)));
                }
            }
            return entropy;
        }

         public QInfo setQueryAnswer(boolean answer){
            // TODO: diagnoses eliminieren, pos bzw. neg test cases setzen, neue diags holen, sodass wieder n diags, probability updaten etc.
            // (und in postqueryinfo speichern)

            return null;
        }

        public void init(AbstractTheory abstractTheory){
            if(abstractTheory instanceof OWLTheory){
                OWLTheory theory = (OWLTheory) abstractTheory;
                AbstractOWLDebugger debugger = new SimpleDebugger();
                debugger.setTheory(theory);
                //this.debugger = debugger;
            }
        }


    */


}



