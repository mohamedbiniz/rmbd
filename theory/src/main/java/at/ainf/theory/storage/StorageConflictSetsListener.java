package at.ainf.theory.storage;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.01.12
 * Time: 10:42
 * To change this template use File | Settings | File Templates.
 */
public interface StorageConflictSetsListener {
    void conflictSetAdded (StorageItemAddedEvent e);

}
