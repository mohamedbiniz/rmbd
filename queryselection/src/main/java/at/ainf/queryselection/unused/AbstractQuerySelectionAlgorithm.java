package at.ainf.queryselection.unused;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 10.02.11
 * Time: 16:34
 * To change this template use File | Settings | File Templates.
 */

import at.ainf.queryselection.QInfo;
import at.ainf.queryselection.Query;
import at.ainf.queryselection.QueryModuleDiagnosis;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.Map;
import java.util.Observable;


public abstract class AbstractQuerySelectionAlgorithm<T> extends Observable /*implements IQueryProvider<T>*/ {

    protected static final String START_MESS = "is starting diagnosis discrimination procedure";
    protected static final String QUERYRESULT_MESS = "As result of this query the remaining set of diagnoses is: ";
    protected static final String POSRESULT_MESS = "D_X + D_0 = ";
    protected static final String NEGRESULT_MESS = "D_notX + D_0 = ";
    protected static final String PROBAPRIORI_MESS = "Probability distribution of diagnoses before query: ";
    protected static final String PROBPOST_MESS = "Probability distribution of diagnoses after query: ";
    protected static final String PROBPOSTADAPT_MESS = "Probability distribution of diagnoses after adaptation: ";
    protected static final String SOLUTIONSTEPS_MESS = "found a solution after asking ";
    protected static final String SOLUTIONDIAG_MESS = "The solution is diagnosis ";
    protected static final String PROB_ENTROPY = "The probability distribution entropy = ";
    protected static final String LINE_SEP = "------------------------------------------------------------------------------------------------------------------------------";

    private LinkedList<QueryModuleDiagnosis> currentDiags;
    private LinkedList<Query> currentQueries;
    private int step;
    private int type;
    private String name;
    private static Logger logger;

    private int maxNumOfMostProbDiags;
    private int maxDiagnosisLength;
    //private AbstractOWLDebugger debugger;
    private Map<LogicalConstruct, Float> faultProbabilities;

    public static final int TYPE_MINSCORE = 1;
    public static final String NAME_MINSCORE = "Minimal Score Algorithm";
    public static final int TYPE_STATIC_ADAPT = 2;
    public static final String NAME_DISTADAPT = "Static Risk Adaptation Algorithm";
    public static final int TYPE_SPLIT = 3;
    public static final String NAME_SPLIT = "Split-In-Half Algorithm";
    public static final int TYPE_DYNAMIC_ADAPT = 4;
    public static final String NAME_VARDAA = "Dynamic Risk Adaptation Algorithm";

    public AbstractQuerySelectionAlgorithm(int type, LinkedList<QueryModuleDiagnosis> diags, LinkedList<Query> queries, String name) {
        this.type = type;
        this.currentDiags = diags;
        this.currentQueries = queries;
        this.step = 0;
        this.name = name;
        this.logger = Logger.getLogger(this.name);

        for (Query q : this.currentQueries) {
            this.addObserver(q);
        }
    }

    /*
    private LinkedList<Query> getAllQueries(LinkedList<Diagnosis> diags){

		LinkedList<Query> queries = new LinkedList<Query>();

		for(int i = 0; i<(int)Math.pow((double)2, (double)diags.size()); i++){
			Query q = new Query();
			LinkedList<Diagnosis> d_X = new LinkedList<Diagnosis>();
			LinkedList<Diagnosis> d_notX = new LinkedList<Diagnosis>();
			LinkedList<Diagnosis> d_0 = new LinkedList<Diagnosis>();
			char [] binarySetSelect = Integer.toBinaryString(i).toCharArray();;

			for(int j=binarySetSelect.length-1; j >= 0; j-- ){
				if(binarySetSelect[j] == '1'){
					d_X.add(diags.get(j));
				}
			}
			q.setD_X(d_X);
			for(Diagnosis d : diags){
				if(!d_X.contains(d)){
					d_notX.add(d);
				}
			}
			q.setD_notX(d_notX);
			q.setD_0(d_0);
			queries.add(q);
		}

		return queries;
	}
    */


    public abstract PostQueryInfo getPostQueryInfo();

    /*
       public Collection getCurrentDiagnoses() {
           Collection diags = null;
           try {
               this.debugger.getStrategy().setMaxHittingSets(this.maxNumOfMostProbDiags - this.currentDiags.size());
               diags = this.debugger.getHittingSets();  //TODO: nehme nur NEUE Hitting Sets
               //TODO: implementierung von TreeSearch ändern, sodass WEITERgesucht und nicht neu gesucht wird nach diags
           } catch (OWLException e) {
               e.printStackTrace();
           }
           if (diags != null) {
               return diags;
           } else {
               return null;
           }
       }
    //
    public void init(AbstractTheory abstractTheory) {
        if (abstractTheory instanceof OWLTheory) {
            OWLTheory theory = (OWLTheory) abstractTheory;
            AbstractOWLDebugger debugger = new SimpleDebugger();
            debugger.setTheory(theory);
            this.debugger = debugger;
        }
    }*/

    public QInfo setQueryAnswer(boolean answer) {
        // TODO: diagnoses eliminieren, pos bzw. neg test cases setzen, neue diags holen, sodass wieder n diags, probability updaten etc.
        // (und in postqueryinfo speichern)

        return null;
    }

    public void setFaultProbabilities(Map<LogicalConstruct, Float> probs) {
        this.faultProbabilities = probs;
    }

    protected abstract Query selectQuery();

    public abstract Query getQuery();

    public abstract int getType();

    public abstract String getName();


    public void setMaxNumOfMostProbDiags(int n) {
        this.maxNumOfMostProbDiags = n;
    }

    public void setMaxDiagnosisLength(int len) {
        this.maxDiagnosisLength = len;
    }


    protected float getDistributionEntropy() {
        double entropy = 0d;
        for (QueryModuleDiagnosis d : this.currentDiags) {
            if (d.getProbability() != 1.0f) {
                entropy = entropy - (double) d.getProbability() * (Math.log((double) d.getProbability() / Math.log((double) 2)));
            }
        }
        return (float) entropy;
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

    /*
     public LinkedList<Diagnosis> getCurrentDiagnoses(){
         return this.currentDiags;
     }
     */

    public LinkedList<Query> getCurrentQueries() {
        return this.currentQueries;
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

    protected void adaptProbabilities(double alpha) {
        LinkedList<QueryModuleDiagnosis> adaptedDiags = new LinkedList<QueryModuleDiagnosis>();
        int currentNumOfDiags = this.currentDiags.size();
        float uniformDistProbability = 1f / currentNumOfDiags;
        double prob;
        for (QueryModuleDiagnosis d : this.currentDiags) {
            prob = d.getProbability();
            prob = alpha * prob + (1 - alpha) * uniformDistProbability;
            d.setProbability(prob);
            adaptedDiags.add(d);
        }
        this.currentDiags = adaptedDiags;
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

}

