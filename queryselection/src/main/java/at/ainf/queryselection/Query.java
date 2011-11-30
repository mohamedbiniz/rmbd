package at.ainf.queryselection;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 10.02.11
 * Time: 16:41
 * To change this template use File | Settings | File Templates.
 */

import java.util.*;


public class Query implements Observer {

    private LinkedList<QueryModuleDiagnosis> d_X;
    private LinkedList<QueryModuleDiagnosis> d_notX;
    private LinkedList<QueryModuleDiagnosis> d_0;
    private int iteration;

    private LinkedList<QueryModuleDiagnosis> d_X_restDiags;
    private LinkedList<QueryModuleDiagnosis> d_notX_restDiags;
    private LinkedList<QueryModuleDiagnosis> d_0_restDiags;


    private Set queryAxioms;
    //private double score;
    //private double prob_X;
    //private double prob_notX;


    @Override
    public boolean equals(Object obj) {
        if (obj == null)  return false;
        return queryAxioms.equals(((Query)obj).getQueryAxioms());    //To change body of overridden methods use File | Settings | File Templates.
    }

    public Query() {
        d_X = new LinkedList<QueryModuleDiagnosis>();
        d_notX = new LinkedList<QueryModuleDiagnosis>();
        d_0 = new LinkedList<QueryModuleDiagnosis>();
        d_X_restDiags = new LinkedList<QueryModuleDiagnosis>();
        d_notX_restDiags = new LinkedList<QueryModuleDiagnosis>();
        d_0_restDiags = new LinkedList<QueryModuleDiagnosis>();

        queryAxioms = new HashSet();

    }

    public Set getQueryAxioms() {
        return queryAxioms;
    }

    public void setQueryAxioms(Set queryAxioms) {
        this.queryAxioms = queryAxioms;
    }

    public double getAlpha() {
        int numOfAllDiags = this.getNumOfAllDiags();
        int numOfDiagsInD_0 = this.getD_0().size();
        int minNumOfElimDiags = this.getMinNumOfElimDiags();
        double probOfMinNumOfElimDiags = this.getProbabilityOfMinNumOfElimDiagsSet();
        double probOfD_0 = this.getProb_0();
        double alpha = (numOfAllDiags - numOfDiagsInD_0 - 2 * minNumOfElimDiags) /
                (numOfAllDiags * (2 * probOfMinNumOfElimDiags + probOfD_0) - numOfDiagsInD_0 - 2 * minNumOfElimDiags);
        return alpha;
    }

    public double getProbabilityOfMinNumOfElimDiagsSet() {
        double probability;
        if (this.getMinNumOfElimDiags() == this.getMaxNumOfElimDiags()) {
            probability = Math.max(this.getProb_X(), this.getProb_notX());
        } else {
            if (this.getMinNumOfElimDiags() == this.getD_X().size()) {
                probability = this.getProb_X();
            } else {
                probability = this.getProb_notX();
            }
        }
        return probability;
    }

    public LinkedList<QueryModuleDiagnosis> getD_X_restDiags() {
        return this.d_X_restDiags;
    }

    public void setD_X_restDiags(LinkedList<QueryModuleDiagnosis> d_X_restDiags) {
        this.d_X_restDiags = d_X_restDiags;
    }

    public LinkedList<QueryModuleDiagnosis> getD_notX_restDiags() {
        return this.d_notX_restDiags;
    }

    public void setD_notX_restDiags(LinkedList<QueryModuleDiagnosis> d_notX_restDiags) {
        this.d_notX_restDiags = d_notX_restDiags;
    }

    public LinkedList<QueryModuleDiagnosis> getD_0_restDiags() {
        return this.d_0_restDiags;
    }

    public void setD_0_restDiags(LinkedList<QueryModuleDiagnosis> d_0_restDiags) {
        this.d_0_restDiags = d_0_restDiags;
    }

    public int getMinNumOfElimDiags() {
        return Math.min(this.getD_X().size(), this.getD_notX().size());
    }

    public int getMaxNumOfElimDiags() {
        return Math.max(this.getD_X().size(), this.getD_notX().size());
    }

    public double getMinPercentOfElimDiags() {
        return (double)getMinNumOfElimDiags() / (double)getNumOfAllDiags();
    }

    public double getMaxPercentOfElimDiags() {
        return (double)getMaxNumOfElimDiags() / (double)getNumOfAllDiags();
    }


    public int getIteration() {
        return this.iteration;
    }

    public void setIteration(int i) {
        this.iteration = i;
    }

    public void setD_X(LinkedList<QueryModuleDiagnosis> diags) {
        this.d_X = diags;
    }

    public void setD_notX(LinkedList<QueryModuleDiagnosis> diags) {
        this.d_notX = diags;
    }

    public void setD_0(LinkedList<QueryModuleDiagnosis> diags) {
        this.d_0 = diags;
    }

    public LinkedList<QueryModuleDiagnosis> getD_X() {
        return this.d_X;
    }

    public LinkedList<QueryModuleDiagnosis> getD_XProbPositive() {
        LinkedList<QueryModuleDiagnosis> diagsWithPosProb = new LinkedList<QueryModuleDiagnosis>();
        for (QueryModuleDiagnosis d : this.getD_X()) {
            if (d.getProbability() > 0) {
                diagsWithPosProb.add(d);
            }
        }
        return diagsWithPosProb;
    }

    public LinkedList<QueryModuleDiagnosis> getD_notX() {
        return this.d_notX;
    }

    public LinkedList<QueryModuleDiagnosis> getD_notXProbPositive() {
        LinkedList<QueryModuleDiagnosis> diagsWithPosProb = new LinkedList<QueryModuleDiagnosis>();
        for (QueryModuleDiagnosis d : this.getD_notX()) {
            if (d.getProbability() > 0) {
                diagsWithPosProb.add(d);
            }
        }
        return diagsWithPosProb;
    }

    public LinkedList<QueryModuleDiagnosis> getD_0() {
        return this.d_0;
    }

    public LinkedList<QueryModuleDiagnosis> getD_0ProbPositive() {
        LinkedList<QueryModuleDiagnosis> diagsWithPosProb = new LinkedList<QueryModuleDiagnosis>();
        for (QueryModuleDiagnosis d : this.getD_0()) {
            if (d.getProbability() > 0) {
                diagsWithPosProb.add(d);
            }
        }
        return diagsWithPosProb;
    }

    /*
     public void setProb_X(double p){
         this.prob_X = p;
     }

     public void setProb_notX(double p){
         this.prob_notX = p;
     }
     */

    public double getActualProbabilityForPositiveAnswer() {
        return (this.getActualProb_X() + this.getActualProb_0() / 2) / (this.getActualProb_X() + this.getActualProb_notX() + this.getActualProb_0());
    }

    public double getProb_X() {
        double prob = 0;
        for (QueryModuleDiagnosis d : d_X) {
            prob += d.getProbability();
        }
        return prob;
    }

    public double getActualProb_X() {
        double prob = 0;
        for (QueryModuleDiagnosis d : d_X) {
            prob += d.getActualProbability();
        }
        return prob;
    }

    public double getProb_notX() {
        double prob = 0;
        for (QueryModuleDiagnosis d : d_notX) {
            prob += d.getProbability();
        }
        return prob;
    }

    public double getActualProb_notX() {
        double prob = 0;
        for (QueryModuleDiagnosis d : d_notX) {
            prob += d.getActualProbability();
        }
        return prob;
    }

    public double getProb_0() {
        double prob = 0;
        for (QueryModuleDiagnosis d : d_0) {
            prob += d.getProbability();
        }
        return prob;
    }

    public double getActualProb_0() {
        double prob = 0;
        for (QueryModuleDiagnosis d : d_0) {
            prob += d.getActualProbability();
        }
        return prob;
    }

    public double getScore() {

        double score =  this.getProb_X()    *   log( this.getProb_X(), 2d) +
                        this.getProb_notX() *   log( this.getProb_notX(), 2d) +
                        this.getProb_0()        + 1d;
        return score;

    }

    private double log(double value, double base) {
        if (value == 0)
            return 0;
        return Math.log(value) / Math.log(base);
    }

    /*
     public double getScore(){
         if(this.getProb_X() > 0f){
             double score = (double)this.getProb_X() * (Math.log((double)this.getProb_X()) / Math.log((double)2)) +
                 this.getProb_notX() * (Math.log((double)this.getProb_notX()) / Math.log((double)2)) +
                 this.getProb_0() + 1;
             return score;
         }
         return 1d;

     }*/

    /*
     public double getMinEliminationPercentage(){
         double numOfAllDiags =  this.getD_notX().size() + this.getD_X().size() + this.getD_0().size();
         double numOfPosResultDiags =  this.getD_X().size() + this.getD_0().size();
         double numOfNegResultDiags =  this.getD_notX().size() + this.getD_0().size();
         return Math.min(numOfPosResultDiags/numOfAllDiags, numOfNegResultDiags/numOfAllDiags);
     }
     */

    public double getMinEliminationPercentage() {
        double numOfAllDiags = this.getNumOfDiagsWithPositiveProbability(this.getD_notX()) +
                this.getNumOfDiagsWithPositiveProbability(this.getD_X()) +
                this.getNumOfDiagsWithPositiveProbability(this.getD_0());
        double numOfPosResultDiags = this.getNumOfDiagsWithPositiveProbability(this.getD_X()) +
                this.getNumOfDiagsWithPositiveProbability(this.getD_0());
        double numOfNegResultDiags = this.getNumOfDiagsWithPositiveProbability(this.getD_notX()) +
                this.getNumOfDiagsWithPositiveProbability(this.getD_0());
        return Math.min(numOfPosResultDiags / numOfAllDiags, numOfNegResultDiags / numOfAllDiags);
    }


    private int getNumOfDiagsWithPositiveProbability(LinkedList<QueryModuleDiagnosis> list) {
        int num = 0;
        for (QueryModuleDiagnosis d : list) {
            if (d.getProbability() > 0) {
                num++;
            }
        }
        return num;
    }

    /*
     public int getMinEliminationNumber(){
         return Math.min(this.getNumOfDiagsWithPositiveProbability(this.getD_X()),
                 this.getNumOfDiagsWithPositiveProbability(this.getD_notX()));
     }
     */

    /*
	public int getMaxEliminationNumber(){
		return Math.max(this.getNumOfDiagsWithPositiveProbability(this.getD_X()) ,
				this.getNumOfDiagsWithPositiveProbability(this.getD_notX()) );
	}
	*/

    public String d_XToString(boolean asSet) {
        String s;
        if (asSet) {
            s = "{";
        } else {
            s = "";
        }
        int numOfDiags = 0;
        for (int i = 0; i < (numOfDiags = d_X.size()); i++) {
            if (i < (numOfDiags - 1)) {
                s += d_X.get(i).getName() + ",";
            } else {
                s += d_X.get(i).getName();
            }
        }
        if (asSet) {
            s += "}";
        }
        return s;


    }

    public String d_notXToString(boolean asSet) {
        String s;
        if (asSet) {
            s = "{";
        } else {
            s = "";
        }
        int numOfDiags = 0;
        for (int i = 0; i < (numOfDiags = d_notX.size()); i++) {
            if (i < (numOfDiags - 1)) {
                s += d_notX.get(i).getName() + ",";
            } else {
                s += d_notX.get(i).getName();
            }
        }
        if (asSet) {
            s += "}";
        }
        return s;
    }

    public String d_0ToString(boolean asSet) {
        String s;
        if (asSet) {
            s = "{";
        } else {
            s = "";
        }
        int numOfDiags = 0;
        for (int i = 0; i < (numOfDiags = d_0.size()); i++) {
            if (i < (numOfDiags - 1)) {
                s += d_0.get(i).getName() + ",";
            } else {
                s += d_0.get(i).getName();
            }
        }
        if (asSet) {
            s += "}";
        }
        return s;


    }

    public String d_0ToStringProbPositive(boolean asSet) {
        String s;
        if (asSet) {
            s = "{";
        } else {
            s = "";
        }
        int numOfDiags = this.getNumOfDiagsWithPositiveProbability(this.getD_0());
        int step = 1;
        for (QueryModuleDiagnosis d : this.getD_0()) {
            if (d.getProbability() > 0) {
                if (step < numOfDiags) {
                    s += d.getName() + ",";
                } else {
                    s += d.getName();
                }
                step++;
            }
        }
        if (asSet) {
            s += "}";
        }
        return s;
    }

    public String d_notXToStringProbPositive(boolean asSet) {
        String s;
        if (asSet) {
            s = "{";
        } else {
            s = "";
        }
        int numOfDiags = this.getNumOfDiagsWithPositiveProbability(this.getD_notX());
        int step = 1;
        for (QueryModuleDiagnosis d : this.getD_notX()) {
            if (d.getProbability() > 0) {
                if (step < numOfDiags) {
                    s += d.getName() + ",";
                } else {
                    s += d.getName();
                }
                step++;
            }
        }
        if (asSet) {
            s += "}";
        }
        return s;
    }

    public String d_XToStringProbPositive(boolean asSet) {
        String s;
        if (asSet) {
            s = "{";
        } else {
            s = "";
        }
        int numOfDiags = this.getNumOfDiagsWithPositiveProbability(this.getD_X());
        int step = 1;
        for (QueryModuleDiagnosis d : this.getD_X()) {
            if (d.getProbability() > 0) {
                if (step < numOfDiags) {
                    s += d.getName() + ",";
                } else {
                    s += d.getName();
                }
                step++;
            }
        }
        if (asSet) {
            s += "}";
        }
        return s;


    }

    public int getQueryHash() {
        String queryAsString = "";
        for (QueryModuleDiagnosis d : this.getD_X()) {
            queryAsString += d.getName();
        }
        for (QueryModuleDiagnosis d : this.getD_notX()) {
            queryAsString += d.getName();
        }
        for (QueryModuleDiagnosis d : this.getD_0()) {
            queryAsString += d.getName();
        }
        return queryAsString.hashCode();
    }


    public boolean getAnswer() {
        for (QueryModuleDiagnosis d : this.d_X) {
            if (d.isTarget()) {
                return true;
            }
        }
        return false;
    }


    @SuppressWarnings("unchecked")

    public void update(Observable obs, Object diags) {
        LinkedList<Object> objArr = new LinkedList<Object>();
        LinkedList<QueryModuleDiagnosis> changedDiagnoses = new LinkedList<QueryModuleDiagnosis>();
        QueryModuleDiagnosis temp = null;
        if (diags instanceof LinkedList<?>) {
            objArr = (LinkedList<Object>) diags;
            for (int i = 0; i < objArr.size(); i++) {
                if (objArr.get(i) instanceof QueryModuleDiagnosis) {
                    temp = (QueryModuleDiagnosis) objArr.get(i);
                    changedDiagnoses.add(temp);
                }
            }
        }

        LinkedList<QueryModuleDiagnosis> new_d_X = new LinkedList<QueryModuleDiagnosis>();
        LinkedList<QueryModuleDiagnosis> new_d_notX = new LinkedList<QueryModuleDiagnosis>();
        LinkedList<QueryModuleDiagnosis> new_d_0 = new LinkedList<QueryModuleDiagnosis>();

        for (QueryModuleDiagnosis d : this.d_X) {
            boolean found = false;
            for (QueryModuleDiagnosis c : changedDiagnoses) {
                if (d.getName().equals(c.getName())) {
                    new_d_X.add(c);
                    found = true;
                }
            }
            if (!found) {
                d.setProbability(0);
                new_d_X.add(d);
            }
        }
        for (QueryModuleDiagnosis d : this.d_notX) {
            boolean found = false;
            for (QueryModuleDiagnosis c : changedDiagnoses) {
                if (d.getName().equals(c.getName())) {
                    new_d_notX.add(c);
                    found = true;
                }
            }
            if (!found) {
                d.setProbability(0);
                new_d_notX.add(d);
            }
        }
        for (QueryModuleDiagnosis d : this.d_0) {
            boolean found = false;
            for (QueryModuleDiagnosis c : changedDiagnoses) {
                if (d.getName().equals(c.getName())) {
                    new_d_0.add(c);
                    found = true;
                }
            }
            if (!found) {
                d.setProbability(0);
                new_d_0.add(d);
            }
        }

        this.setD_X(new_d_X);
        this.setD_notX(new_d_notX);
        this.setD_0(new_d_0);


    }


    public int getNumOfEliminatedDiags(boolean answer) {
        if (answer == true) {
            return this.getD_notXProbPositive().size() + this.getD_0ProbPositive().size();
        } else {
            return this.getD_XProbPositive().size() + this.getD_0ProbPositive().size();
        }
    }

    public int getNumOfAllDiags() {
        return this.getD_0().size() + this.getD_notX().size() + this.getD_X().size();
    }


}
