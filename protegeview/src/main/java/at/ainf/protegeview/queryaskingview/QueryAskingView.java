package at.ainf.protegeview.queryaskingview;

import at.ainf.protegeview.WorkspaceTab;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 04.08.11
 * Time: 08:49
 * To change this template use File | Settings | File Templates.
 */
public class QueryAskingView extends AbstractOWLViewComponent {

    protected void initialiseOWLView() throws Exception {

        WorkspaceTab workspace = (WorkspaceTab) getOWLWorkspace().getWorkspaceTab("at.ainf.protegeview.WorkspaceTab");
        setLayout(new BorderLayout());
        QueryShowPanel panel = new QueryShowPanel(workspace);

        QueryAskingToolbar toolbar = new QueryAskingToolbar(workspace,panel);

        panel.setToolbar(toolbar);

        add(toolbar,BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);
    }

    protected void disposeOWLView() {

    }
}
