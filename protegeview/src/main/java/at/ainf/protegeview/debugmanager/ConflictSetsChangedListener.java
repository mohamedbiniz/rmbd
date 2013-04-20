package at.ainf.protegeview.debugmanager;

import java.util.EventListener;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 10.08.11
 * Time: 18:40
 * To change this template use File | Settings | File Templates.
 */
public interface ConflictSetsChangedListener extends EventListener {

    void conflictSetsChanged (ConflictSetsChangedEvent e);

}
