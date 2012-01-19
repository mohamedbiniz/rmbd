package at.ainf.theory.storage;

import java.util.EventListener;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 16.11.11
 * Time: 14:03
 * To change this template use File | Settings | File Templates.
 */
public interface StorageItemListener extends EventListener {

    void elementAdded (StorageItemAddedEvent e);

}


