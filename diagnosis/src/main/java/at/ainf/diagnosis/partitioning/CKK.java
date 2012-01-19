package at.ainf.diagnosis.partitioning;

import at.ainf.theory.model.ITheory;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.HittingSet;
import at.ainf.theory.storage.Partition;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 02.05.11
 * Time: 16:26
 * To change this template use File | Settings | File Templates.
 */
public class CKK<Id> extends BruteForce<Id> implements Partitioning<Id> {

    private static Logger logger = Logger.getLogger(CKK.class.getName());

    private class Differencing<E extends HittingSet<Id>> {
        private Set<E> left = new LinkedHashSet<E>();
        private Set<E> right = new LinkedHashSet<E>();
        private Set<E> tail = new LinkedHashSet<E>();
        private double difference;
        private double sumLeft = 0;
        private double sumRight = 0;


        public Differencing(Set<E> desc) {
            tail.addAll(desc);
        }

        public Differencing(Differencing<E> differencing) {
            right.addAll(differencing.right);
            left.addAll(differencing.left);
            tail.addAll(differencing.tail);
            sumLeft = differencing.sumLeft;
            sumRight = differencing.sumRight;
        }

        public Differencing<E> addLess(E element) {
            Differencing<E> res = new Differencing<E>(this);
            res.tail.remove(element);

            if (res.sumLeft <= res.sumRight) {
                res.left.add(element);
                res.sumLeft += element.getMeasure();
            } else {
                res.right.add(element);
                res.sumRight += element.getMeasure();
            }

            res.difference = Math.abs(res.sumLeft - res.sumRight);
            return res;
        }

        public Differencing<E> addMore(E element) {
            Differencing<E> res = new Differencing<E>(this);
            res.tail.remove(element);

            if (res.sumLeft > res.sumRight) {
                res.left.add(element);
                res.sumLeft += element.getMeasure();
            } else {
                res.right.add(element);
                res.sumRight += element.getMeasure();
            }

            res.difference = Math.abs(res.sumLeft - res.sumRight);
            return res;
        }

        public boolean isEmpty() {
            return this.tail.isEmpty();
        }

        public E getNext() {
            Iterator<E> ti = this.tail.iterator();
            return ti.next();
        }
    }

    private double threshold = 0.01d;

    public CKK(ITheory<Id> theory, ScoringFunction<Id> function) {
        super(theory, function);
    }

    public <E extends HittingSet<Id>> Partition<Id> generatePartition(Set<E> hittingSets)
            throws SolverException, InconsistentTheoryException {
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

        // find the best partition
        Set<E> desc = new LinkedHashSet<E>(new TreeSet<E>(hs).descendingSet());

        Differencing<E> dif = new Differencing<E>(desc);

        findPartition(dif);
        Collections.sort(getPartitions(), new Comparator<Partition<Id>>() {
            public int compare(Partition<Id> o1, Partition<Id> o2) {
                int res = ((Double) o1.difference).compareTo(o2.difference);
                if (res == 0) {
                    return -1 * Integer.valueOf(o1.dx.size()).compareTo(o2.dx.size());
                }
                return res;
            }
        });

        Partition<Id> partition = null;
        double bestdiff = Double.MAX_VALUE;
        int count = 0;
        for (Partition<Id> part : getPartitions()) {
            if ((part.difference < bestdiff || (part.difference == bestdiff && compare(partition, part))) && verifyPartition(part)) {
                double score = getScoringFunction().getScore(part);
                double best = getScoringFunction().getScore(partition);
                if ((score < best) || (score == best && diff(part) < diff(partition))) {
                    partition = part;
                    if (partition.difference < bestdiff)
                        bestdiff = partition.difference;
                }
                count++;
            }

            if (partition != null && partition.score < getThreshold())
                break;
        }

        if (logger.isInfoEnabled())
            logger.info("Searched through " + count + "/" + getPartitionsCount() + " partitionsCount");
        if (getPostprocessor() != null)
            partition = getPostprocessor().run(getPartitions());
        restoreEntailments(hittingSets);
        return partition;
    }

    private boolean compare(Partition<Id> partition, Partition<Id> part) {
        if (part == null || partition == null)
            return true;
        return part.dx.size() > partition.dx.size();
    }

    private double diff(Partition<Id> part) {
        if (part == null)
            return Double.MAX_VALUE;
        return Math.abs(part.dx.size() - part.dnx.size()) + part.dz.size() / 2;
    }


    protected boolean verifyPartition(Partition<Id> partition)
            throws SolverException, InconsistentTheoryException {
        Set<Id> ent = partition.partition;
        // partition the rest of diagnoses
        for (HittingSet<Id> hs : getHittingSets()) {
            if (!partition.dx.contains(hs)) {
                if (hs.getEntailments().containsAll(ent)) {
                    partition.dx.add(hs);
                    partition.difference += hs.getMeasure();
                } else if (!getTheory().diagnosisConsistent(hs, ent))
                    partition.dnx.add(hs);
                else if (getTheory().diagnosisEntails(hs, ent)) {
                    partition.dx.add(hs);
                    partition.difference += hs.getMeasure();
                } else
                    partition.dz.add(hs);
            }
        }
        return true;
    }

    protected <E extends HittingSet<Id>> void findPartition(Differencing<E> diff)
            throws SolverException, InconsistentTheoryException {

        if ((diff == null || diff.tail.isEmpty())) {
            if (diff.left.isEmpty())
                return;
            else {
                incPartitionsCount();
                Partition<Id> part = new Partition<Id>();
                for (E el : diff.left)
                    part.dx.add(el);
                Set<Id> ent = getCommonEntailments(part.dx);
                if (ent == null || ent.isEmpty())
                    return;
                part.partition = Collections.unmodifiableSet(ent);
                if (logger.isDebugEnabled())
                    logger.debug("Adding partition with common entailments: " + part.partition);
                part.difference = diff.difference;
                getPartitions().add(part);
                return;
            }
        }

        E hs = diff.getNext();

        Differencing<E> less = diff.addLess(hs);
        findPartition(less);

        Differencing<E> more = diff.addMore(hs);
        findPartition(more);
    }


}
