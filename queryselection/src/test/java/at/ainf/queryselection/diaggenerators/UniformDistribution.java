package at.ainf.queryselection.diaggenerators;

import at.ainf.queryselection.IDiagGenerator;
import at.ainf.queryselection.QueryModuleDiagnosis;
import at.ainf.queryselection.sim.Diagnosis;

import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 04.02.11
 * Time: 17:57
 * To change this template use File | Settings | File Templates.
 */
public class UniformDistribution implements IDiagGenerator {

    private double targDiagLower, targDiagUpper;


    public UniformDistribution(double targDiagLower, double targDiagUpper){
        this.targDiagLower = targDiagLower;
        this.targDiagUpper = targDiagUpper;
    }

    public LinkedList<QueryModuleDiagnosis> getDiags(int numOfDiags) {
        LinkedList<QueryModuleDiagnosis> diagnoses = new LinkedList<QueryModuleDiagnosis>();
        for (int i = 1; i <= numOfDiags; i++) {
            QueryModuleDiagnosis d = new Diagnosis("D" + i, 1d / (double) numOfDiags, false);
            diagnoses.add(d);
        }
        int intLowerBound = (int) (targDiagLower * numOfDiags);
        int intUpperBound = (int) (targDiagUpper * numOfDiags);
        int interval = intUpperBound - intLowerBound;
        int r = (int) Math.floor(Math.random() * interval);
        r += intLowerBound;
        if (r == numOfDiags) r--;
        diagnoses.get(r).setIsTarget(true);
        return diagnoses;
    }

}
