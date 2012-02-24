package at.ainf.queryselection.diaggenerators;

import at.ainf.queryselection.IDiagGenerator;
import at.ainf.queryselection.QueryModuleDiagnosis;
import at.ainf.queryselection.sim.Diagnosis;

import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 10.02.11
 * Time: 17:50
 * To change this template use File | Settings | File Templates.
 */
public class BestWorstMixedDistribution implements IDiagGenerator {

    private float percentOfBestDistribution;
    private float percentOfWorstCase;

    public BestWorstMixedDistribution(float percentOfWorstCase, float percentOfBestDistribution) {

        this.percentOfBestDistribution = percentOfBestDistribution;
        this.percentOfWorstCase = percentOfWorstCase;
    }

    public LinkedList<QueryModuleDiagnosis> getDiags(int numOfDiags) {
        float[] bestDist = new float[numOfDiags];
        float[] worstDist = new float[numOfDiags];
        for (int k = 1; k <= numOfDiags; k++) {
            bestDist[k - 1] = (float) Math.pow((double) 2, (double) k);
            worstDist[k - 1] = (float) Math.pow((double) 2, (double) numOfDiags - k + 1);
        }
        LinkedList<QueryModuleDiagnosis> diagnoses = new LinkedList<QueryModuleDiagnosis>();
        float normalizationFactor = 0f;
        for (float f : bestDist) {
            normalizationFactor += f;
        }
        for (int i = 0; i < bestDist.length; i++) {
            QueryModuleDiagnosis d = new Diagnosis("D" + i, percentOfBestDistribution * (bestDist[i] / normalizationFactor) + (1f - percentOfBestDistribution) * worstDist[i] / (normalizationFactor), false);
            diagnoses.add(d);
        }
        diagnoses.get((int) 1 + (int) percentOfWorstCase * ((int) Math.floor((double) numOfDiags / 2) - 1)).setIsTarget(true);
        return diagnoses;
    }
}
