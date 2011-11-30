package at.ainf.pluginprotege.menuactions;

import at.ainf.pluginprotege.WorkspaceTab;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.model.OWLWorkspace;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 03.05.11
 * Time: 10:10
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractAction extends ProtegeAction {

    protected WorkspaceTab getWS() {
        return ((WorkspaceTab)((OWLWorkspace)getWorkspace()).getWorkspaceTab("at.ainf.pluginprotege.WorkspaceTab"));
    }

    public void initialise() throws Exception {

    }

    public void dispose() {

    }

}
