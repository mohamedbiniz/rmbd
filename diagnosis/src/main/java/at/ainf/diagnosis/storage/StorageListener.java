package at.ainf.diagnosis.storage;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 30.05.11
 * Time: 11:54
 * To change this template use File | Settings | File Templates.
 */
public interface StorageListener<T extends HittingSet<Id>, Id>{
    void add(T newObject, boolean addValid);
    boolean remove(T oldObject);
}
