package at.ainf.diagnosis.storage;

import java.util.EventObject;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 16.11.11
 * Time: 14:03
 * To change this template use File | Settings | File Templates.
 */
public class StorageItemAddedEvent extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public StorageItemAddedEvent(Object source) {
        super(source);
    }

}
