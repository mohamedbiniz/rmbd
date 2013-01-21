package at.ainf.protegeview.debugmanager;

import java.util.EventListener;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 23.08.11
 * Time: 13:50
 * To change this template use File | Settings | File Templates.
 */
public interface ConflictSetSelectedListener extends EventListener {

    void conflictSetSelected (ConflictSetSelectedEvent e);
}
