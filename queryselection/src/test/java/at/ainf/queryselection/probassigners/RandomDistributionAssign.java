package at.ainf.queryselection.probassigners;

import at.ainf.queryselection.IProbabilityAssigner;
import at.ainf.queryselection.QueryModuleDiagnosis;

import java.util.LinkedList;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 03.03.11
 * Time: 11:31
 * To change this template use File | Settings | File Templates.
 */
public class RandomDistributionAssign implements IProbabilityAssigner {

    private Mode mode;
    private QueryModuleDiagnosis targetDiag;
    private Random randomGenerator;

    public RandomDistributionAssign(Mode m, long randomSeed) {
        this.mode = m;
        this.randomGenerator = new Random(randomSeed);
    }

    public QueryModuleDiagnosis getTargetDiag() {
        return this.targetDiag;
    }

    public LinkedList<QueryModuleDiagnosis> assignProbabilities(LinkedList<QueryModuleDiagnosis> diags) {
        return this.assignProbs(diags, this.mode);
    }

    private void assignProbsModeInverted(LinkedList<QueryModuleDiagnosis> diags) {
        int numOfDiags = diags.size();
        double[] prob = new double[numOfDiags];
        double normalizationFactor = 0;
        for (int i = 0; i < prob.length; i++) {
            prob[i] = randomGenerator.nextDouble() * 1d / diags.get(i).getProbability();
            normalizationFactor += prob[i];
        }
        for (int i = 0; i < prob.length; i++) {
            diags.get(i).setActualProbability(prob[i] / normalizationFactor);
        }
    }

    private void assignProbsModeCorrelated(LinkedList<QueryModuleDiagnosis> diags) {
        int numOfDiags = diags.size();
        double[] prob = new double[numOfDiags];
        double normalizationFactor = 0;
        for (int i = 0; i < prob.length; i++) {
            prob[i] = randomGenerator.nextDouble() * diags.get(i).getProbability();
            normalizationFactor += prob[i];
        }
        for (int i = 0; i < prob.length; i++) {
            diags.get(i).setActualProbability(prob[i] / normalizationFactor);
        }
    }

    private void assignProbsModeDependent(LinkedList<QueryModuleDiagnosis> diags) {
        int numOfDiags = diags.size();
        double[] prob = new double[numOfDiags];
        double restProb = 1.000000d;
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
            diags.get(i).setActualProbability(prob[i]);
        }
    }

    private void assignProbsModeIndependent(LinkedList<QueryModuleDiagnosis> diags) {
        int numOfDiags = diags.size();
        double[] prob = new double[numOfDiags];
        double normalizationFactor = 0;
        for (int i = 0; i < prob.length; i++) {
            prob[i] = randomGenerator.nextDouble();
            normalizationFactor += prob[i];
        }
        for (int i = 0; i < prob.length; i++) {
            diags.get(i).setActualProbability(prob[i] / normalizationFactor);

        }
    }

    private LinkedList<QueryModuleDiagnosis> assignProbs(LinkedList<QueryModuleDiagnosis> diags, Mode mode) {

        /*/////////////////////////
        for(Diagnosis d : diags){
            if(d.isTarget()){
                System.out.println("---  TARGET " + d.getName() + "  prob = " + d.getProbability());
            }
            System.out.println("---  " + d.getName() + "  prob = " + d.getProbability());
        }
        *///////////////////////////


        if (mode == Mode.INVERTED) {
            this.assignProbsModeInverted(diags);
        } else if (mode == Mode.DEPENDENT) {
            this.assignProbsModeDependent(diags);
        } else if (mode == Mode.INDEPENDENT) {
            this.assignProbsModeIndependent(diags);
        } else if (mode == Mode.CORRELATED) {
            this.assignProbsModeCorrelated(diags);
        }
        QueryModuleDiagnosis mostProbableDiag = diags.getFirst();
        targetDiag = null;
        for (QueryModuleDiagnosis d : diags) {
            if (d.getActualProbability() > mostProbableDiag.getActualProbability()) {
                mostProbableDiag = d;
            }
            if (d.isTarget()) {
                targetDiag = d;
            }
        }
        double temp = mostProbableDiag.getActualProbability();
        mostProbableDiag.setActualProbability(targetDiag.getActualProbability());
        targetDiag.setActualProbability(temp);
        /*///////////////////////
            for(Diagnosis d : diags){
                if(d.isTarget()){
                    System.out.println("TARGET " + d.getName() + "  actualprob = " + d.getActualProbability());
                }
                System.out.println(d.getName() + "  actualprob = " + d.getActualProbability());
            }
        ///
            for(Diagnosis d : diags){
                if(d.isTarget()){
                    System.out.println("TARGET " + d.getName() + "  prob = " + d.getProbability());
                    this.targetDiag = d;
                }
                System.out.println(d.getName() + "  prob.. = " + d.getProbability());
            }
        *///////////////////////////
        return diags;
    }


}
