package at.ainf.pluginprotege.debugmanager;

import java.util.EventListener;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 18.08.11
 * Time: 12:09
 * To change this template use File | Settings | File Templates.
 */
public interface TreeNodeChangedListener extends EventListener {

    void treeNodeChanged(TreeNodeChangedEvent e);

}
