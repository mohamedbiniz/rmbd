package at.ainf.pluginprotege.menuactions;

import at.ainf.pluginprotege.WorkspaceTab;
import org.protege.editor.core.ui.workspace.CustomWorkspaceTabsManager;
import org.protege.editor.core.ui.workspace.TabbedWorkspace;
import org.protege.editor.core.ui.workspace.WorkspaceTabPlugin;
import org.protege.editor.owl.model.OWLWorkspace;

import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 13.05.11
 * Time: 10:43
 * To change this template use File | Settings | File Templates.
 */
public class OpenDTabAction extends AbstractAction {

    public void actionPerformed(ActionEvent e) {

        org.protege.editor.core.ui.workspace.WorkspaceTab tab = null;
        TabbedWorkspace tabbedWorkspace = (TabbedWorkspace)getWorkspace();

        if (tabbedWorkspace.containsTab("at.ainf.pluginprotege.WorkspaceTab")) {
            tab = getWS();
        }
        else {
            for (WorkspaceTabPlugin plugin : tabbedWorkspace.getOrderedPlugins())
                if (plugin.getId().equals("at.ainf.pluginprotege.WorkspaceTab")) {
                    tab = tabbedWorkspace.addTabForPlugin(plugin);
                    break;
                }

        }
        tabbedWorkspace.setSelectedTab(tab);
    }

}
