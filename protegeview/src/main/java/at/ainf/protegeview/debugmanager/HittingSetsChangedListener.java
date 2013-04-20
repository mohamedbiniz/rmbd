package at.ainf.protegeview.debugmanager;

import java.util.EventListener;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 09.08.11
 * Time: 11:19
 * To change this template use File | Settings | File Templates.
 */
public interface HittingSetsChangedListener extends EventListener {

    void hittingSetsChanged (HittingSetsChangedEvent e);
}
