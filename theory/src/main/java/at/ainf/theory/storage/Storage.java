package at.ainf.theory.storage;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 20.04.11
 * Time: 15:57
 * To change this template use File | Settings | File Templates.
 */
public interface Storage<E extends AxiomSet<Id>, Id> {

    boolean addConflict(E conflict);

    void setConflicts(Set<E> conflicts);

    Set<E> getConflictSets();

    boolean removeConflictSet(E cs);

    int getConflictsCount();

    boolean addHittingSet(E hittingSet);

    Set<E> getValidHittingSets();

    Set<E> getHittingSets();

    int getHittingSetsCount();

    void setHittingSets(Set<E> hittingSets);

    boolean removeHittingSet(E hittingSet);

    void invalidateHittingSet(E hittingSet);

    void resetStorage();

    Set<E> getConflictSets(Id axiom);

    void normalizeValidHittingSets();

    void addStorageItemListener(StorageItemListener l);

    void removeStorageItemListener(StorageItemListener l);

    public void addStorageConflictSetsListener(StorageConflictSetsListener l);

    public void removeStorageConflictSetsListener(StorageConflictSetsListener l);

    public void addStorageHittingSetsListener(StorageHittingSetsListener l);

    public void removeStorageHittingSetsListener(StorageHittingSetsListener l);

}
