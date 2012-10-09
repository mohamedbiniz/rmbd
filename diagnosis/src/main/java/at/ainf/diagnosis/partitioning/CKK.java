package at.ainf.diagnosis.partitioning;

import at.ainf.diagnosis.Searchable;
import at.ainf.diagnosis.partitioning.scoring.Scoring;
import at.ainf.diagnosis.model.ITheory;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.storage.Partition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 02.05.11
 * Time: 16:26
 * To change this template use File | Settings | File Templates.
 */
public class CKK<Id> extends BruteForce<Id> implements Partitioning<Id> {

    private static Logger logger = LoggerFactory.getLogger(CKK.class.getName());

    private class Differencing<E extends AxiomSet<Id>> {
        private Set<E> left = new LinkedHashSet<E>();
        private Set<E> right = new LinkedHashSet<E>();
        private Set<E> tail = new LinkedHashSet<E>();
        private BigDecimal difference;
        private BigDecimal sumLeft = new BigDecimal(0);
        private BigDecimal sumRight = new BigDecimal(0);


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

            if (res.sumLeft.compareTo(res.sumRight) < 1) {
                res.left.add(element);
                res.sumLeft = res.sumLeft.add(element.getMeasure());
            } else {
                res.right.add(element);
                res.sumRight = res.sumRight.add(element.getMeasure());
            }

            res.difference = res.sumLeft.subtract(res.sumRight).abs();
            return res;
        }

        public Differencing<E> addMore(E element) {
            Differencing<E> res = new Differencing<E>(this);
            res.tail.remove(element);

            if (res.sumLeft.compareTo(res.sumRight) == 1) {
                res.left.add(element);
                res.sumLeft = res.sumLeft.add(element.getMeasure());
            } else {
                res.right.add(element);
                res.sumRight = res.sumRight.add(element.getMeasure());
            }

            res.difference = res.sumLeft.subtract(res.sumRight).abs();
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

    public CKK(Searchable<Id> theory, Scoring<Id> function) {
        super(theory, function);
    }

    private int count = 0;
    BigDecimal bestdiff = new BigDecimal(Double.MAX_VALUE);

    protected void reset() {
        super.reset();
        count = 0;
        bestdiff = new BigDecimal(Double.MAX_VALUE);
    }

    public <E extends AxiomSet<Id>> Partition<Id> generatePartition(Set<E> hittingSets)
            throws SolverException, InconsistentTheoryException {

        numOfHittingSets = hittingSets.size();
        reset();
        Set<E> hs = preprocess(hittingSets);

        // find the best partition
        Set<E> desc = new LinkedHashSet<E>(new TreeSet<E>(hs).descendingSet());

        Differencing<E> dif = new Differencing<E>(desc);

        findPartition(dif);
        Collections.sort(getPartitions(), new Comparator<Partition<Id>>() {
            public int compare(Partition<Id> o1, Partition<Id> o2) {
                int res = o1.difference.compareTo(o2.difference);
                if (res == 0) {
                    return -1 * Integer.valueOf(o1.dx.size()).compareTo(o2.dx.size());
                }
                return res;
            }
        });

        Partition<Id> partition = null;

        sort(getPartitions());

        partition = nextPartition(partition);

        if (logger.isInfoEnabled())
            logger.info("Searched through " + count + "/" + getPartitionsCount() + " partitionsCount");
        if (partition == null || getPartitions().isEmpty())
            logger.error("No partition found! " + getPartitions().size() + " " + toString(hs));
        partition = getScoring().runPostprocessor(getPartitions(), partition);

        restoreEntailments(hittingSets);
        return partition;
    }

    private void sort(List<Partition<Id>> partitions) {
        Collections.sort(partitions, new Comparator<Partition<Id>>() {
            public int compare(Partition<Id> o1, Partition<Id> o2) {
                return o1.difference.compareTo(o2.difference);
            }
        });
    }

    public <E extends AxiomSet<Id>> Partition<Id> nextPartition(Partition<Id> partition) throws SolverException, InconsistentTheoryException {
        if (partition != null) {
            getPartitions().remove(partition);
            partition = null;
            sort(getPartitions());
        }
        if (getPartitions().isEmpty())
            return null;

        bestdiff = new BigDecimal(Double.MAX_VALUE);
        count = 0;
        List<Partition<Id>> empty = new LinkedList<Partition<Id>>();
        for (Partition<Id> part : getPartitions()) {
            if (
                    (part.difference.compareTo(bestdiff) < 0 ||
                            (partition != null && partition.dnx.size() == 0) ||
                            (part.difference.equals(bestdiff) && compare(partition, part))
                    ) && verifyPartition(part)) {
                if (part.dnx.isEmpty())
                    empty.add(part);
                else {
                    BigDecimal score = getScoringFunction().getScore(part);
                    BigDecimal best = getScoringFunction().getScore(partition);
                    if ((score.compareTo(best) < 0) || (score.compareTo(best) == 0 && diff(part) < diff(partition))) {
                        partition = part;
                        updateDifference(partition);
                        if (partition.difference.compareTo(bestdiff) < 0)
                            bestdiff = partition.difference;
                    }
                }
                count++;
            }
            if (partition != null && partition.score.compareTo(BigDecimal.valueOf(getThreshold())) < 0)
                break;
        }
        getPartitions().removeAll(empty);
        return partition;
    }

    private void updateDifference(Partition<Id> partition) {
        BigDecimal left = sumProbabilities(partition.dx);
        BigDecimal right = sumProbabilities(partition.dnx);
        BigDecimal none = sumProbabilities(partition.dz);
        partition.difference = left.subtract(right).add(none.divide(new BigDecimal(2))).abs();
    }

    private BigDecimal sumProbabilities(Set<AxiomSet<Id>> partition) {
        BigDecimal sum = new BigDecimal(0);
        for (AxiomSet<Id> ids : partition) {
            sum = sum.add(ids.getMeasure());
        }
        return sum;
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


    public boolean verifyPartition(Partition<Id> partition)
            throws SolverException, InconsistentTheoryException {
        if (partition.isVerified)
            return true;
        Set<Id> ent = partition.partition;
        // partition the rest of diagnoses
        for (AxiomSet<Id> hs : getHittingSets()) {
            if (!partition.dx.contains(hs)) {
                if (hs.getEntailments().containsAll(ent)) {
                    partition.dx.add(hs);
                    partition.difference = partition.difference.add(hs.getMeasure());
                } else if (!getTheory().diagnosisConsistent(hs, ent))
                    partition.dnx.add(hs);
                else if (getTheory().diagnosisEntails(hs, ent)) {
                    partition.dx.add(hs);
                    partition.difference = partition.difference.add(hs.getMeasure());
                } else {
                    partition.dz.add(hs);
                    //partition.difference = partition.difference.add(new BigDecimal(hs.getConflictMeasure()/2d));
                }
            }
        }
        partition.isVerified = true;
        return true;
    }

    protected <E extends AxiomSet<Id>> void findPartition(Differencing<E> diff)
            throws SolverException, InconsistentTheoryException {
        // diff == null
        if (diff.tail.isEmpty()) {
            if (diff.left.isEmpty())
                return;
            else {
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
                incPartitionsCount();
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
