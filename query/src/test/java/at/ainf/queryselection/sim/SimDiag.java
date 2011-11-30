package at.ainf.queryselection.sim;

//import at.ainf.queryselection.diag;


import at.ainf.queryselection.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 22.02.11
 * Time: 14:57
 * To change this template use File | Settings | File Templates.
 */
public class SimDiag implements IDiagnosisProvider {

    private IDiagGenerator diagGenerator;
    private IProbabilityAssigner probAssigner;
    private LinkedList<QueryModuleDiagnosis> allDiags;
    private int nextDiagToReturn = 0;
    private Random randomGenerator;
    private long randomSeed;


    public SimDiag(IDiagGenerator dg, IProbabilityAssigner pa, int numOfTotalDiags, long randomSeed) throws NoDiagnosisFoundException {
        this.diagGenerator = dg;
        this.probAssigner = pa;
        this.init(numOfTotalDiags);
        this.randomSeed = randomSeed;
        this.randomGenerator = new Random(randomSeed);

    }

    public LinkedList<QueryModuleDiagnosis> getDiagnoses(int numOfDiags) {
        /*///////////////////////
        int count = 0;
        for(Diagnosis d : allDiags){
           if(d.isInconsistent()) count++;
        }
        System.out.println(" - - - - - - - - - - - - - NUM OF INCONSISTENT DIAGS = " + count);
        *////////////////////////
        LinkedList<QueryModuleDiagnosis> diags = new LinkedList<QueryModuleDiagnosis>();
        int diagsReturned = 0;
        int iterations = 0;
        if (this.moreDiagnosesAvailable()) {
            for (int i = nextDiagToReturn; i < allDiags.size() && diagsReturned < numOfDiags; i++) {
                if (!allDiags.get(i).isInconsistent()) {
                    diags.add(this.allDiags.get(i));
                    diagsReturned++;
                }
                iterations++;
            }
            nextDiagToReturn += iterations;
        }
        return diags;
    }

    public List<QueryModuleDiagnosis> getAllDiags() {
        return allDiags;
    }


    private boolean moreDiagnosesAvailable() {

        for (int i = nextDiagToReturn; i < allDiags.size(); i++) {
            if (!allDiags.get(i).isInconsistent()) {
                return true;
            }
        }

        return false;
    }

    public void init(int numOfTotalDiags) throws NoDiagnosisFoundException {
        allDiags = this.diagGenerator.getDiags(numOfTotalDiags);

    }

    public void assignActualProbabilities() {
        allDiags = this.probAssigner.assignProbabilities(allDiags);
    }

    public List<Query> getAllQueries(List diags) {

        LinkedList<Query> queries = new LinkedList<Query>();

        for (int i = 0; i < (int) Math.pow((double) 2, (double) diags.size()); i++) {

            LinkedList<QueryModuleDiagnosis> d_Temp = new LinkedList<QueryModuleDiagnosis>();
            LinkedList<QueryModuleDiagnosis> d_X = new LinkedList<QueryModuleDiagnosis>();


            if (randomGenerator.nextDouble() > 0.4d) {
                continue;
            }
            char[] binarySetSelect = Integer.toBinaryString(i).toCharArray();

            for (int j = binarySetSelect.length - 1; j >= 0; j--) {
                if (binarySetSelect[j] == '1') {
                    d_X.add((QueryModuleDiagnosis) diags.get(j));
                }
            }

            for (Iterator it = diags.iterator(); it.hasNext();) {
                QueryModuleDiagnosis d = (QueryModuleDiagnosis) it.next();
                if (!d_X.contains(d)) {
                    d_Temp.add(d);
                }
            }


            for (int m = 0; m < (int) Math.pow((double) 2, (double) d_Temp.size()); m++) {
                char[] binarySetSelect1 = Integer.toBinaryString(m).toCharArray();

                LinkedList<QueryModuleDiagnosis> d_notX = new LinkedList<QueryModuleDiagnosis>();
                LinkedList<QueryModuleDiagnosis> d_0 = new LinkedList<QueryModuleDiagnosis>();

                if (randomGenerator.nextDouble() > 0.1d) {
                    continue;
                }

                for (int j = binarySetSelect1.length - 1; j >= 0; j--) {
                    if (binarySetSelect1[j] == '1') {
                        d_notX.add(d_Temp.get(j));
                    }
                }
                for (QueryModuleDiagnosis d : d_Temp) {
                    if (!d_notX.contains(d)) {
                        d_0.add(d);
                    }
                }

                Query q = new Query();
                q.setD_X(d_X);
                q.setD_notX(d_notX);
                q.setD_0(d_0);

                LinkedList<QueryModuleDiagnosis> d_X_rest = new LinkedList<QueryModuleDiagnosis>();
                LinkedList<QueryModuleDiagnosis> d_notX_rest = new LinkedList<QueryModuleDiagnosis>();
                LinkedList<QueryModuleDiagnosis> d_0_rest = new LinkedList<QueryModuleDiagnosis>();

                // calculate randomly how many NON LEADING diagnoses are eliminated after a query
                double uD_0 = 0.95f;
                double oD_0 = 0.95f;
                double rangeD_0 = oD_0 - uD_0;
                double r = randomGenerator.nextDouble();
                double pD_0 = r * rangeD_0 + uD_0;
                double pD_X = randomGenerator.nextDouble();
                Random rndGen = new Random((long) q.getQueryHash());
                for (int z = nextDiagToReturn; z < this.allDiags.size(); z++) {

                    if (rndGen.nextDouble() < pD_0) {
                        d_0_rest.add(allDiags.get(z));
                    } else if (rndGen.nextDouble() < pD_X) {
                        d_X_rest.add(allDiags.get(z));
                    } else {
                        d_notX_rest.add(allDiags.get(z));
                    }
                    QueryModuleDiagnosis targetDiag;
                    if ((targetDiag = allDiags.get(z)).isTarget()) {
                        targetDiag.setConsistent();
                    }

                }
                q.setD_0_restDiags(d_0_rest);
                q.setD_notX_restDiags(d_notX_rest);
                q.setD_X_restDiags(d_X_rest);
                /*/////////////////
                for(Diagnosis d : d_X_rest){
                    System.out.println(d.getName());
                }
                *//////////////////
                queries.add(q);
            }
        }

        return queries;
    }


}
