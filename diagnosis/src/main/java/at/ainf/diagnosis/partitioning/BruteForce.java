package at.ainf.diagnosis.partitioning;

import at.ainf.diagnosis.partitioning.scoring.Scoring;
import at.ainf.theory.model.ITheory;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.Partition;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 02.05.11
 * Time: 16:26
 * To change this template use File | Settings | File Templates.
 */
public class BruteForce<Id> implements Partitioning<Id> {

    private static Logger logger = Logger.getLogger(BruteForce.class.getName());

    private ITheory<Id> theory;

    private Set<? extends AxiomSet<Id>> hittingSets;

    private int partitionsCount = 0;

    private Scoring<Id> scoring = null;

    private ArrayList<Partition<Id>> partitions = new ArrayList<Partition<Id>>();

    private Partition<Id> bestPartition = null;

    private double threshold = 0.01d;

    public BruteForce(ITheory<Id> theory, Scoring<Id> function) {
        this.theory = theory;
        this.scoring = function;
        this.scoring.setPartitionSearcher(this);
    }

    protected void reset() {
        partitionsCount = 0;
        hittingSets = null;
        partitions = new ArrayList<Partition<Id>>();
        bestPartition = null;

    }

    protected <E extends AxiomSet<Id>> String toString(Set<E> hittingSets) {
        StringBuilder res = new StringBuilder();
        for (E hittingSet : hittingSets) {
            res.append(hittingSet.toString()).append(" ");
        }
        return res.toString();
    }

    protected int numOfHittingSets;

    public int getNumOfHittingSets() {
        return numOfHittingSets;
    }

    public <E extends AxiomSet<Id>> Partition<Id> generatePartition(Set<E> hittingSets)
            throws SolverException, InconsistentTheoryException {

        numOfHittingSets = hittingSets.size();
        reset();
       Set<E> hs = preprocess(hittingSets);

        // find the best partition
        Set<E> desc = new LinkedHashSet<E>(new TreeSet<E>(hs).descendingSet());
        Partition<Id> partition = findPartition(desc, new LinkedHashSet<E>());
        if (logger.isDebugEnabled())
            logger.debug("Searched through " + getPartitionsCount() + " partitionsCount");
        if (getScoring() != null){
            partition = getScoring().runPostprocessor(getPartitions(), partition);
        }
        restoreEntailments(hittingSets);
        return partition;
    }


    public <E extends AxiomSet<Id>> Partition<Id> nextPartition (Partition<Id> lastPartition) throws SolverException, InconsistentTheoryException {
        getPartitions().remove(lastPartition);
        Partition<Id> partition = getPartitions().get(0);
        if (!partition.isVerified) verifyPartition(partition);
        if (getScoring() != null)
            partition = getScoring().runPostprocessor(getPartitions(), partition);
        lastPartition = partition;
        return partition;
    }

    protected <E extends AxiomSet<Id>> Set<E> preprocess(Set<E> hittingSets) throws SolverException {
        ensureCapacity((int) Math.pow(2, hittingSets.size()));
        if (getScoringFunction() == null)
            throw new IllegalStateException("Scoring function is not set!");
        // save the original hitting sets

        setHittingSets(Collections.unmodifiableSet(hittingSets));
        // preprocessing
        Set<E> hs = new LinkedHashSet<E>(hittingSets);
        removeCommonEntailments(hs);
        for (Iterator<E> hsi = hs.iterator(); hsi.hasNext(); )
            if (hsi.next().getEntailments().isEmpty())
                hsi.remove();
        getScoringFunction().normalize(hs);

        return new TreeSet<E>(hs);
    }

    protected <E extends AxiomSet<Id>> void restoreEntailments(Set<E> hittingSets) {
        for (E hs : hittingSets)
            hs.restoreEntailments();
    }


    protected <E extends AxiomSet<Id>> void removeCommonEntailments(Set<E> hittingSets) throws SolverException {
        Set<Id> ent = getCommonEntailments(hittingSets);
        if (!ent.isEmpty())
            for (E hs : hittingSets) {
                Set<Id> hse = new LinkedHashSet<Id>(hs.getEntailments());
                hse.removeAll(ent);
                hs.setEntailments(hse);
            }
    }

    protected void ensureCapacity(int cap) {
        this.partitions.ensureCapacity(cap);
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public boolean verifyPartition(Partition<Id> partition)
            throws SolverException, InconsistentTheoryException {
        Set<Id> ent = getCommonEntailments(partition.dx);
        partition.partition = Collections.unmodifiableSet(ent);
        if (logger.isDebugEnabled())
            logger.debug("Common entailments: " + partition.partition);
        if (ent == null || ent.isEmpty())
            return false;
        // partition the rest of diagnoses
        for (AxiomSet<Id> hs : getHittingSets()) {
            if (!partition.dx.contains(hs)) {
                if (hs.getEntailments().containsAll(ent))
                    partition.dx.add(hs);
                else if (!getTheory().diagnosisConsistent(hs, ent))
                    partition.dnx.add(hs);
                else if (getTheory().diagnosisEntails(hs, ent))
                    partition.dx.add(hs);
                else
                    partition.dz.add(hs);
            }
        }
        return true;
    }

    protected <E extends AxiomSet<Id>> Set<Id> getCommonEntailments(Set<E> dx) throws SolverException {
        Set<Id> intersection = null;
        for (AxiomSet<Id> hs : dx) {
            if (intersection == null)
                intersection = new LinkedHashSet<Id>(hs.getEntailments());
            else
                intersection.retainAll(hs.getEntailments());
            if (intersection.isEmpty())
                return intersection;
        }
        return intersection;
    }

    protected <E extends AxiomSet<Id>> Partition<Id> findPartition(Set<E> hittingSets, Set<E> head)
            throws SolverException, InconsistentTheoryException {

        //if (this.bestPartition != null && this.bestPartition.score < this.threshold)
        //    return this.bestPartition;
        if ((hittingSets == null || hittingSets.isEmpty())) {
            if (head.isEmpty())
                return new Partition<Id>();
            else {
                incPartitionsCount();
                Partition<Id> part = new Partition<Id>();
                for (E el : head)
                    part.dx.add(getOriginalHittingSet(el));
                if (logger.isDebugEnabled())
                    logger.debug("Creating a partition with dx: " + head);
                if (verifyPartition(part)) {
                    if (logger.isDebugEnabled())
                        logger.debug("Created partition: \n dx:" + part.dx + "\n dnx:" + part.dnx + "\n dz:" + part.dz);
                    if (getPartitions() != null && !getPartitions().contains(part)) {
                        getPartitions().add(part);
                    }
                    return part;
                }
                return null;
            }
        }

        Set<E> tail = new LinkedHashSet<E>(hittingSets);
        Iterator<E> ti = tail.iterator();
        E hs = ti.next();
        ti.remove();


        if (logger.isDebugEnabled())
            logger.debug("Partitions: " + partitionsCount + " head: " + head.size() + " hsets:" + hittingSets.size());
        Partition<Id> part = findPartition(tail, head);

        head.add(hs);
        Partition<Id> partHead = null;

        //if (this.bestPartition == null || (getScoringFunction().getPartitionScore(part) <= this.bestPartition.score))
        partHead = findPartition(tail, head);

        head.remove(hs);

        Partition<Id> best = partHead;
        if (getScoringFunction().getScore(part).compareTo(getScoringFunction().getScore(partHead)) < 0) {
            best = part;
        }
        if (this.bestPartition == null || (best != null && this.bestPartition.score.compareTo(best.score) > 0))
            this.bestPartition = best;
        return best;
    }

    protected void incPartitionsCount() {
        this.partitionsCount++;
    }

    protected <E extends AxiomSet<Id>> AxiomSet<Id> getOriginalHittingSet(E el) {
        for (AxiomSet<Id> elem : getHittingSets()) {
            if (el.compareTo(elem) == 0)
                return elem;
        }
        return null;
    }


    public ITheory<Id> getTheory() {
        return theory;
    }

    public Scoring<Id> getScoringFunction() {
        return this.scoring;
    }

    public Set<? extends AxiomSet<Id>> getHittingSets() {
        return hittingSets;
    }

    protected void setHittingSets(Set<? extends AxiomSet<Id>> hittingSets) {
        this.hittingSets = hittingSets;
    }

    public int getPartitionsCount() {
        return partitionsCount;
    }

    public Scoring<Id> getScoring() {
        return scoring;
    }

    public List<Partition<Id>> getPartitions() {
        return partitions;
    }

}
