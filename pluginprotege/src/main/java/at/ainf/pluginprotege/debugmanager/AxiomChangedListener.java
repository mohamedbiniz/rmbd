package at.ainf.pluginprotege.debugmanager;

import java.util.EventListener;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 08.08.11
 * Time: 16:09
 * To change this template use File | Settings | File Templates.
 */
public interface AxiomChangedListener extends EventListener {

    void axiomChanged (AxiomChangedEvent e);
}
