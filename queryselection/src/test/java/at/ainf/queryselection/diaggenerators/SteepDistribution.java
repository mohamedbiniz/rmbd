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
 * Date: 12.05.11
 * Time: 11:50
 * To change this template use File | Settings | File Templates.
 */
public class SteepDistribution implements IDiagGenerator{

    private SteepDistributionMode mode;
    private Random randomGenerator;
    private double targDiagLower;
    private double targDiagUpper;
    private double param;

    public SteepDistribution(SteepDistributionMode mode, long randomSeed, double targDiagLower, double targDiagUpper, double param){
        this.mode = mode;
        this.randomGenerator = new Random(randomSeed);
        this.targDiagLower = targDiagLower;
        this.targDiagUpper = targDiagUpper;
        this.param = param;
    }

    public LinkedList<QueryModuleDiagnosis> getDiags(int numOfDiags) throws NoDiagnosisFoundException {
        LinkedList<QueryModuleDiagnosis> diagnoses = new LinkedList<QueryModuleDiagnosis>();
        double[] prob = new double[numOfDiags];
        double normalizationFactor = 0d;
        for (int i = 0; i < prob.length; i++) {
            double weight = getWeight(mode, (double)i + 3d );
            prob[i] = randomGenerator.nextDouble() * weight;
            normalizationFactor += prob[i];
        }
        for (int i = 0; i < prob.length; i++) {
            QueryModuleDiagnosis d = new Diagnosis("D" + i, prob[i]/normalizationFactor, false);
            diagnoses.add(d);
        }

        ///////////////////--- sort diagnoses by decreasing probability ---//////////////////

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

        ////////////////////////////////////////////////////////////////////////////////////

        int intLowerBound = (int) (targDiagLower * numOfDiags);
        int intUpperBound = (int) (targDiagUpper * numOfDiags);
        int interval = intUpperBound - intLowerBound;
        int r = (int) Math.floor(randomGenerator.nextDouble() * interval);
        r += intLowerBound;
        if (r == numOfDiags) r--;
        diagnoses.get(r).setIsTarget(true);
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
        return diagnoses;
    }

    private double getWeight(SteepDistributionMode m, double i){
        double weight;
        if(m == SteepDistributionMode.ONE_DIV_X_POWER_PARAM){
            weight = 1d/Math.pow(i,param);
        }else if(m == SteepDistributionMode.ONE_DIV_PARAM_POWER_X){
            weight = 1d/Math.pow(param,i);
        }else{
            throw new IllegalArgumentException("Illegal SteepDistribution Mode selected!!");
        }
        return weight;
    }

}




