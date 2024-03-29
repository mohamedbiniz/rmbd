package at.ainf.diagnosis.partitioning.scoring;

import at.ainf.diagnosis.storage.Partition;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 13.02.12
 * Time: 17:52
 * To change this template use File | Settings | File Templates.
 */
public class PenaltyQSS<T> extends MinScoreQSS<T> {

    double maxPenalty;
    double penalty;



    public PenaltyQSS(double maxPenalty) {
        this.maxPenalty = maxPenalty;
        penalty = 0d;
    }


    protected boolean canExceedMaxPenalty(Partition<T> partition) {
        return penalty + getMaxPenaltyOfQuery(partition) > maxPenalty;
    }


    protected int getMaxPenaltyOfQuery(Partition<T> partition){
        return (int)Math.floor((double) numOfLeadingDiags/(double)2) - getMinNumOfElimDiags(partition);
    }


    public void updateParameters(boolean answerToLastQuery){

        preprocessBeforeUpdate(answerToLastQuery);

        penalty += (int)Math.floor((double) numOfLeadingDiags/(double)2) - numOfEliminatedLeadingDiags;
    }


    private Partition<T> bestNonCandidate(List<Partition<T>> nonCandidates){

        List<Partition<T>> nonCandidatesFiltered = new LinkedList<Partition<T>>();

        for(Partition<T> partition : nonCandidates){
            if(partition.dx.size() != partition.dnx.size())
                nonCandidatesFiltered.add(partition);
        }

        Partition<T> bestNonCandidate = Collections.max(nonCandidatesFiltered, new MinNumOfElimDiagsComparator());

        for(Partition<T> partition : nonCandidatesFiltered){
            if(getMinNumOfElimDiags(partition) == getMinNumOfElimDiags(bestNonCandidate) && partition.score.compareTo(bestNonCandidate.score) < 0)
                bestNonCandidate = partition;
        }

        return bestNonCandidate;
    }


    private Partition<T> bestCandidate(List<Partition<T>> candidates){
        Partition<T> bestCandidate = Collections.min(candidates,new ScoreComparator());
        return bestCandidate;
    }


    public Partition<T> runPostprocessor(List<Partition<T>> partitions, Partition<T> currentBest) {
        preprocessBeforeRun(getPartitionSearcher().getNumOfHittingSets());

        List<Partition<T>> candidates = new LinkedList<Partition<T>>();
        List<Partition<T>> nonCandidates = new LinkedList<Partition<T>>();


        for (Partition<T> partition : partitions) {
            if (!canExceedMaxPenalty(partition))
                candidates.add(partition);
            else
                nonCandidates.add(partition);
        }
        

        Partition<T> result;

        if (candidates.isEmpty()){
            result = bestNonCandidate(nonCandidates);
        }else{
            result = bestCandidate(candidates);
        }

        lastQuery = result;
        return result;
        
    }

}
