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



    Set<E> getConflicts();

    Set<E> getDiagnoses();

    Set<E> getConflictSets();

    boolean removeConflictSet(E cs);



    boolean addHittingSet(E hittingSet);

    Set<E> getValidHittingSets();

    Set<E> getHittingSets();

    //int getHittingSetsCount();

    //int getDiagsCount();



    boolean removeHittingSet(E hittingSet);

    void invalidateHittingSet(E hittingSet);

    void resetStorage();

    //Set<E> getConflictSets(Id axiom);

    void normalizeValidHittingSets();

    //void addStorageItemListener(StorageItemListener l);

    //void removeStorageItemListener(StorageItemListener l);




}
