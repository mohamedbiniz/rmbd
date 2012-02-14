package at.ainf.diagnosis.partitioning.postprocessor;

import at.ainf.theory.storage.Partition;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 13.02.12
 * Time: 19:20
 * To change this template use File | Settings | File Templates.
 */


public class StaticRiskQSS<T> extends MinScoreQSS<T> {

	    protected double c;

        protected int numOfLeadingDiags;

	    public StaticRiskQSS(double c) {
	        super();
	        this.c = c;
	    }



    public void updateNumOfCurrentLeadingDiags(int numOfLeadingDiags) {
        this.numOfLeadingDiags = numOfLeadingDiags;
    }

    protected Partition<T> selectMinScorePartition(List<Partition<T>> partitions) {
	        return super.run(partitions);
	    }

	    protected int getMaxPossibleNumOfDiagsToEliminate() {
	        return (int) Math.floor((double) (numOfLeadingDiags / 2d));
	    }

	    protected void preprocessC(){
	    	double maxPossibleC;
            if ((maxPossibleC = (double)this.getMaxPossibleNumOfDiagsToEliminate() / (double)numOfLeadingDiags) < c) {
	            c = maxPossibleC;
	        }else if (c < 0d)
                c = 0;
	    }

        protected int convertCToNumOfDiags(double c) {
	        int num = (int) Math.ceil((double) numOfLeadingDiags * c);
	        if (num > ((double)numOfLeadingDiags / 2d)) {
	            num--;
	        }
	        return num;
	    }

        protected LinkedList<Partition<T>> getLeastCautiousNonHighRiskPartitions(int numOfDiagsToElim, List<Partition<T>> partitions){
            LinkedList<Partition<T>> leastCautiousNonHighRiskQueries = new LinkedList<Partition<T>>();
            for(Partition<T> p : partitions){
                if(getMinNumOfElimDiags(p) == numOfDiagsToElim){
                     leastCautiousNonHighRiskQueries.add(p);
                }
            }
            return leastCautiousNonHighRiskQueries;
        }

	    public Partition<T> run(List<Partition<T>> partitions) {

	    	preprocessC();
	    	int numOfDiagsToElim = convertCToNumOfDiags(c);
	        Partition<T> minScorePartition;
	        if (getMinNumOfElimDiags(( minScorePartition = selectMinScorePartition(partitions))) >= numOfDiagsToElim) {
	            return minScorePartition;
	        }
	        for (; numOfDiagsToElim <= getMaxPossibleNumOfDiagsToEliminate(); numOfDiagsToElim++) {
	            LinkedList<Partition<T>> leastCautiousNonHighRiskPartitions = getLeastCautiousNonHighRiskPartitions(numOfDiagsToElim, partitions);     // candidateQueries = X_min,k
	            if (leastCautiousNonHighRiskPartitions.isEmpty()) {
	                continue;
	            }
	            return Collections.min(leastCautiousNonHighRiskPartitions, new ScoreComparator());
            }
            return Collections.min(partitions,new ScoreComparator());

	    }


}










