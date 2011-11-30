package at.ainf.diagnosis.partitioning;

import at.ainf.diagnosis.model.ITheory;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.model.UnsatisfiableFormulasException;
import at.ainf.diagnosis.storage.HittingSet;
import org.apache.log4j.Logger;

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

    private Set<? extends HittingSet<Id>> hittingSets;

    private int partitionsCount = 0;

    private final ScoringFunction<Id> scoring;

    private Postprocessor postprocessor = null;

    private ArrayList<Partition<Id>> partitions = new ArrayList<Partition<Id>>();

    private Partition<Id> bestPartition = null;

    private double threshold = 0.01d;

    public BruteForce(ITheory<Id> theory, ScoringFunction<Id> function) {
        this.theory = theory;
        this.scoring = function;
    }

    public <E extends HittingSet<Id>> Partition<Id> generatePartition(Set<E> hittingSets)
            throws SolverException, UnsatisfiableFormulasException {
        if (this.scoring == null)
            throw new IllegalStateException("Scoring function is not set!");
        // save the original hitting sets
        this.hittingSets = Collections.unmodifiableSet(hittingSets);
        // preprocessing
        Set<E> hs = new LinkedHashSet<E>(hittingSets);
        removeCommonEntailments(hs);
        for (Iterator<E> hsi = hs.iterator(); hsi.hasNext(); )
            if (hsi.next().getEntailments().isEmpty())
                hsi.remove();
        getScoringFunction().normalize(hs);

        // find the best partition
        Set<E> desc = new LinkedHashSet<E>(new TreeSet<E>(hs).descendingSet());
        Partition<Id> partition = findPartition(desc, new LinkedHashSet<E>());
        if (logger.isDebugEnabled())
            logger.debug("Searched through " + getPartitionsCount() + " partitionsCount");
        if (getPostprocessor() != null)
            partition = getPostprocessor().run(getPartitions());
        restoreEntailments(hittingSets);
        return partition;
    }

    protected <E extends HittingSet<Id>> void restoreEntailments(Set<E> hittingSets) {
        for (E hs : hittingSets)
            hs.restoreEntailments();
    }


    protected <E extends HittingSet<Id>> void removeCommonEntailments(Set<E> hittingSets) throws SolverException {
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

    protected boolean verifyPartition(Partition<Id> partition)
            throws SolverException, UnsatisfiableFormulasException {
        Set<Id> ent = getCommonEntailments(partition.dx);
        partition.partition = Collections.unmodifiableSet(ent);
        if (logger.isDebugEnabled())
            logger.debug("Common entailments: " + partition.partition);
        if (ent == null || ent.isEmpty())
            return false;
        // partition the rest of diagnoses
        for (HittingSet<Id> hs : getHittingSets()) {
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

    protected <E extends HittingSet<Id>> Set<Id> getCommonEntailments(Set<E> dx) throws SolverException {
        Set<Id> intersection = null;
        for (HittingSet<Id> hs : dx) {
            if (intersection == null)
                intersection = new LinkedHashSet<Id>(hs.getEntailments());
            else
                intersection.retainAll(hs.getEntailments());
            if (intersection.isEmpty())
                return intersection;
        }
        return intersection;
    }

    protected <E extends HittingSet<Id>> Partition<Id> findPartition(Set<E> hittingSets, Set<E> head)
            throws SolverException, UnsatisfiableFormulasException {

        if (this.bestPartition != null && this.bestPartition.score < this.threshold)
            return this.bestPartition;
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
                    if (getPartitions() != null)
                        getPartitions().add(part);
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

        //if (this.bestPartition == null || (getScoringFunction().getScore(part) <= this.bestPartition.score))
        partHead = findPartition(tail, head);

        head.remove(hs);

        Partition<Id> best = partHead;
        if (getScoringFunction().getScore(part) < getScoringFunction().getScore(partHead)) {
            best = part;
        }
        if (this.bestPartition == null || (best != null && this.bestPartition.score > best.score))
            this.bestPartition = best;
        return best;
    }

    protected void incPartitionsCount() {
        this.partitionsCount++;
    }

    protected <E extends HittingSet<Id>> HittingSet<Id> getOriginalHittingSet(E el) {
        for (HittingSet<Id> elem : getHittingSets()) {
            if (el.compareTo(elem) == 0)
                return elem;
        }
        return null;
    }


    public ITheory<Id> getTheory() {
        return theory;
    }

    public ScoringFunction<Id> getScoringFunction() {
        return this.scoring;
    }

    public Set<? extends HittingSet<Id>> getHittingSets() {
        return hittingSets;
    }

    protected void setHittingSets(Set<? extends HittingSet<Id>> hittingSets) {
        this.hittingSets = hittingSets;
    }

    public int getPartitionsCount() {
        return partitionsCount;
    }

    public Postprocessor getPostprocessor() {
        return postprocessor;
    }

    public List<Partition<Id>> getPartitions() {
        return partitions;
    }

    public void setPostprocessor(Postprocessor proc) {
        if (proc != null) {
            this.postprocessor = proc;
        } else {
            this.partitions = null;
        }
    }


}
