package at.ainf.theory.storage;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 28.04.11
 * Time: 16:06
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractStorage<T extends AxiomSet<Id>, Id> implements Storage<T, Id> {
    private static Logger logger = Logger.getLogger(AbstractStorage.class.getName());
    protected Set<T> hittingSets = new LinkedHashSet<T>();
    protected Set<T> validHittingSets = new LinkedHashSet<T>();
    protected Set<T> conflicts = new LinkedHashSet<T>();

    private StorageListener<T, Id> hittingSetListener = new StorageListener<T, Id>() {
        public boolean remove(T oldObject) {
            boolean remValid = validHittingSets.remove(oldObject);
            if (!hittingSets.remove(oldObject)) {
                // perhaps treeset order is not correct
                hittingSets = copy(hittingSets);
                if (hittingSets.remove(oldObject))
                    logger.error("treeset ordering is not correct - updates of probabilities? ");
                else
                    throw new IllegalStateException("Existing hitting set was not removed!");
            }

            return remValid;
        }

        public void add(T newObject, boolean addValid) {
            if (addValid)
                validHittingSets.add(newObject);
            hittingSets.add(newObject);
        }
    };

    public void resetStorage() {
        for (AxiomSet<Id> hs : this.getHittingSets())
            hs.setListener(null);
        hittingSets.clear();
        conflicts.clear();
        validHittingSets.clear();
    }

    public boolean addConflict(T conflict) {
        if (logger.isInfoEnabled())
            logger.info("Adding a conflict: " + conflict);
        boolean r = conflicts.add(conflict);
        if (r) conflictAdded();
        return r;
    }

    protected void conflictAdded() {
        notifyStorageItemAdded();
        notifyConflictSetAdded();
    }

    public void setConflictSets(Set<T> conflicts) {
        this.conflicts.clear();
        for (T conf : conflicts)
            addConflict(conf);
    }

    public Set<T> getConflictSets() {
        return Collections.unmodifiableSet(copy(conflicts));
    }

    public boolean removeConflictSet(T cs) {
        return this.conflicts.remove(cs);
    }

    public int getConflictsCount() {
        return this.conflicts.size();
    }

    public Set<T> getConflicts() {
        return getConflictSets();
    }

    public Set<T> getDiagnoses() {
        return Collections.unmodifiableSet(getValidHittingSets());
    }

    protected void validHittingSetAdded() {
        notifyStorageItemAdded();
        notifyHittingSetAdded();


    }

    public void setHittingSets(Set<T> hittingSets) {
        this.hittingSets.clear();
        for (T hs : hittingSets)
            addHittingSet(hs);
    }

    public boolean addHittingSet(final T hittingSet) {
        hittingSet.setListener(this.hittingSetListener);
        if (logger.isInfoEnabled()) {
            logger.info("Adding a hitting set: " + hittingSet);
        }

        Set<T> del = new HashSet<T>();
        for (T set : hittingSets) {
            if (set.containsAll(hittingSet))
                del.add(set);
        }
        if (!del.isEmpty())
            for (T ids : del) {
                removeHittingSet(ids);
            }

        boolean val = hittingSets.add(hittingSet);

        if (hittingSet.isValid()) {
            validHittingSets.add(hittingSet);
            validHittingSetAdded();
        }

        return val;
    }

    public boolean removeHittingSet(final T diagnosis) {
        boolean val = hittingSets.remove(diagnosis);
        if (diagnosis.isValid())
            validHittingSets.remove(diagnosis);

        return val;
    }

    public void invalidateHittingSet(T hs) {
        hs.setValid(false);
        this.validHittingSets.remove(hs);
    }

    public Set<T> getValidHittingSets() {
        return copy(validHittingSets);
        /*Set<T> hs = new TreeSet<T>();
        for (T hset : hittingSets)
            if (hset.isValid())
                hs.add(hset);
        return hs;*/
    }

    private Set<T> copy(Set<T> set) {
        Set<T> hs = new LinkedHashSet<T>();
        for (T hset : set)
            hs.add(hset);
        return hs;
    }

    public Set<T> getHittingSets() {
        return Collections.unmodifiableSet(hittingSets);
    }

    public int getHittingSetsCount() {
        return validHittingSets.size();
    }

    public int getDiagsCount() {
        return getDiagnoses().size();
    }

    public Set<T> getConflictSets(Id axiom) {
        Set<T> conflicts = new LinkedHashSet<T>();

        for (T conflict : getConflictSets()) {
            for (Id ax : conflict) {
                if (ax.equals(axiom)) {
                    conflicts.add(conflict);
                    break;
                }
            }
        }
        return conflicts;
    }

    public void normalizeValidHittingSets() {
        Set<T> hittingSets = getDiagnoses();
        double sum = 0;

        for (T hittingSet : hittingSets) {
            sum += hittingSet.getMeasure();
        }

        if (sum == 0 && hittingSets.size() != 0)
            throw new IllegalStateException("Sum of probabilities of all diagnoses is 0!");

        for (T hittingSet : hittingSets) {
            hittingSet.setMeasure(hittingSet.getMeasure() / sum);
        }
    }

    private List<StorageItemListener> listeners = new LinkedList<StorageItemListener>();

    public void addStorageItemListener(StorageItemListener l) {
        listeners.add(l);
    }

    public void removeStorageItemListener(StorageItemListener l) {
        listeners.remove(l);
    }

    private void notifyStorageItemAdded() {
       StorageItemAddedEvent event =new StorageItemAddedEvent(this);

       for (StorageItemListener listener : listeners)
           listener.elementAdded(event);

   }

    private List<StorageConflictSetsListener> conflictSetsListeners = new LinkedList<StorageConflictSetsListener>();

    public void addStorageConflictSetsListener(StorageConflictSetsListener l) {
        conflictSetsListeners.add(l);
    }

    public void removeStorageConflictSetsListener(StorageConflictSetsListener l) {
        conflictSetsListeners.remove(l);
    }

    private void notifyConflictSetAdded() {
       StorageItemAddedEvent event =new StorageItemAddedEvent(this);

       for (StorageConflictSetsListener listener : conflictSetsListeners)
           listener.conflictSetAdded(event);

   }

    private List<StorageHittingSetsListener> hittingSetsListeners = new LinkedList<StorageHittingSetsListener>();

    public void addStorageHittingSetsListener(StorageHittingSetsListener l) {
        hittingSetsListeners.add(l);
    }

    public void removeStorageHittingSetsListener(StorageHittingSetsListener l) {
        hittingSetsListeners.remove(l);
    }

    private void notifyHittingSetAdded() {
       StorageItemAddedEvent event =new StorageItemAddedEvent(this);

       for (StorageHittingSetsListener listener : hittingSetsListeners)
           listener.hittingSetAdded(event);

   }
}
