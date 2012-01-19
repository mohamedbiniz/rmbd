package at.ainf.queryselection.diaggenerators;

import at.ainf.queryselection.IDiagGenerator;
import at.ainf.queryselection.NoDiagnosisFoundException;
import at.ainf.queryselection.QueryModuleDiagnosis;
import at.ainf.queryselection.sim.Diagnosis;

import java.util.LinkedList;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 14.02.11
 * Time: 15:58
 * To change this template use File | Settings | File Templates.
 */
public class RandomDistribution implements IDiagGenerator {

    private boolean extreme;
    private double targDiagLower;
    private double targDiagUpper;
    private boolean sorted;
    private boolean weightAdjust;
    private Random randomGenerator;

    public RandomDistribution(boolean depend, double targDiagLower /*first diag is D0, not D1*/, double targDiagUpper,
                              boolean sorted, long randomSeed) {
        this.extreme = depend;
        this.targDiagLower = targDiagLower;
        this.targDiagUpper = targDiagUpper;
        this.sorted = sorted;
        this.weightAdjust = weightAdjust;
        this.randomGenerator = new Random(randomSeed);


    }

    public LinkedList<QueryModuleDiagnosis> getDiags(int numOfDiags) throws NoDiagnosisFoundException {
        // sorted heißt, dass die diags nach wahrscheinlichkeit sortiert werden, und dann die tatsächlich
        // die (targDiagUpper - targDiagLower) besten, average oder schlechtesten
        // diagnosen gewählt werden
        LinkedList<QueryModuleDiagnosis> diagnoses = new LinkedList<QueryModuleDiagnosis>();
        int intLowerBound = (int) (targDiagLower * numOfDiags);
        int intUpperBound = (int) (targDiagUpper * numOfDiags);
        if (!extreme) {

            double[] prob = new double[numOfDiags];
            double normalizationFactor = 0;
            for (int i = 0; i < prob.length; i++) {

                prob[i] = randomGenerator.nextDouble();
                normalizationFactor += prob[i];
            }
            for (int i = 0; i < prob.length; i++) {
                QueryModuleDiagnosis d = new Diagnosis("D" + i, prob[i] / normalizationFactor, false);
                diagnoses.add(d);
            }

        } else {

            double[] prob = new double[numOfDiags];
            double restProb = 1.000000;
            for (int i = 0; i < prob.length; i++) {
                //System.out.println("RESTPROB_" + i + ":  " + restProb);
                if (i < prob.length - 1) {
                    prob[i] = randomGenerator.nextDouble() * restProb;
                    restProb -= prob[i];
                } else {
                    prob[i] = restProb;
                }
            }
            for (int i = 0; i < prob.length; i++) {
                QueryModuleDiagnosis d = new Diagnosis("D" + i, prob[i], false);
                diagnoses.add(d);
            }

        }
        if (sorted) {
            boolean unsorted = true;
            QueryModuleDiagnosis temp;
            QueryModuleDiagnosis[] sortedDiags = new QueryModuleDiagnosis[numOfDiags];
            int m = 0;
            for (QueryModuleDiagnosis d : diagnoses) {
                sortedDiags[m] = d;
                m++;
            }
            while (unsorted) {
                unsorted = false;
                for (int i = 0; i < sortedDiags.length - 1; i++)
                    if (sortedDiags[i].getProbability() < sortedDiags[i + 1].getProbability()) {
                        temp = sortedDiags[i];
                        sortedDiags[i] = sortedDiags[i + 1];
                        sortedDiags[i + 1] = temp;
                        unsorted = true;
                    }
            }
            diagnoses.clear();
            for (int k = 0; k < sortedDiags.length; k++) {
                //System.out.println("XX  " + sortedDiags[k].getProbability());
                diagnoses.add(k, sortedDiags[k]);
            }

        }
        int interval = intUpperBound - intLowerBound;
        int r = (int) Math.floor(randomGenerator.nextDouble() * interval);
        r += intLowerBound;
        if (r == numOfDiags) r--;
        diagnoses.get(r).setIsTarget(true);
        /*/////////////////////////
        System.out.println("int lower = " + intLowerBound + "---- int upper = " + intUpperBound);
        System.out.println("r = " + r + "   ---- target diagnosis is " + diagnoses.get(r).getName() + "!!!");
        *//////////////////////////
        /*//////////////////////////
        double sum = 0d;
        for(QueryModuleDiagnosis d : diagnoses){
            System.out.println("p = " + d.getProbability());
            sum += d.getProbability();
        }
        System.out.println("sum = " + sum);
        for(int l = 0; l < diagnoses.size()-1; l++){
            System.out.println("p[i]/p[i+1] = " + diagnoses.get(l).getProbability()/diagnoses.get(l+1).getProbability());
        }
        *///////////////////////////
        if (diagnoses.isEmpty()) {
            throw new NoDiagnosisFoundException();
        }
        return diagnoses;
    }


}

/*
if(!sorted){
			if(!depend){
				LinkedList<Diagnosis> diagnoses = new LinkedList<Diagnosis>();
				double [] prob = new double[numOfDiags];
				double normalizationFactor = 0f;
				for(int i=0; i<prob.length; i++){
					prob[i] = Math.random();
					normalizationFactor += prob[i];
				}
				for(int i = 0; i<prob.length; i++){
					Diagnosis d = new Diagnosis("D"+i,prob[i]/normalizationFactor,false);
					diagnoses.add(d);
				}
				int interval = targDiagUpper - targDiagLower;
				int r = (int) Math.floor(Math.random() * interval);
				r += targDiagLower;
				diagnoses.get(r).setIsTarget(true);
				return diagnoses;
			}else{
				LinkedList<Diagnosis> diagnoses = new LinkedList<Diagnosis>();
				double [] prob = new double[numOfDiags];
				double restProb = 1.000000f;
				for(int i=0; i<prob.length; i++){
					//System.out.println("RESTPROB_" + i + ":  " + restProb);
					if(i < prob.length - 1){
						prob[i] = Math.random() * restProb;
						restProb -= prob[i];
					}else{
						prob[i] = restProb;
					}
				}
				for(int i = 0; i<prob.length; i++){
					Diagnosis d = new Diagnosis("D"+i,prob[i],false);
					diagnoses.add(d);
				}
				int interval = targDiagUpper - targDiagLower;
				int r = (int) Math.floor(Math.random() * interval);
				r += targDiagLower;
				diagnoses.get(r).setIsTarget(true);
				return diagnoses;
			}
		}else if(!weightAdjust){
			LinkedList<Diagnosis> diagnoses = new LinkedList<Diagnosis>();
			double [] prob = new double[numOfDiags];
			double restProb = 1.000000f;
			for(int i=0; i<prob.length; i++){
				//System.out.println("RESTPROB_" + i + ":  " + restProb);
				if(i < prob.length - 1){
					prob[i] = Math.random() * restProb;
					restProb -= prob[i];
				}else{
					prob[i] = restProb;
				}
			}
			for(int i = 0; i<prob.length; i++){
				Diagnosis d = new Diagnosis("D"+i,prob[i],false);
				diagnoses.add(d);
			}
			boolean unsorted = true;
			Diagnosis temp;
			Diagnosis [] sortedDiags = new Diagnosis[numOfDiags];
			int m = 0;
			for(Diagnosis d : diagnoses){
				sortedDiags[m] = d;
				m++;
			}
			  while (unsorted){
			     unsorted = false;
			     for (int i=0; i < sortedDiags.length-1; i++)
			        if (sortedDiags[i].getProbability() > sortedDiags[i+1].getProbability()) {
			           temp       = sortedDiags[i];
			           sortedDiags[i]  = sortedDiags[i+1];
			           sortedDiags[i+1] = temp;
			           unsorted = true;
			        }
			 }
			  diagnoses.clear();
			for(int k=0; k<sortedDiags.length; k++){
				//System.out.println("XX  " + sortedDiags[k].getProbability());
				diagnoses.add(k, sortedDiags[k]);
			}
			int interval = targDiagUpper - targDiagLower;
			int r = (int) Math.floor(Math.random() * interval);
			r += targDiagLower;
			diagnoses.get(r).setIsTarget(true);
			return diagnoses;
		}else{
			return null;
		}
	}
*/