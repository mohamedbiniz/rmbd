package at.ainf.pluginprotege.menuactions;

import at.ainf.pluginprotege.WorkspaceTab;
import at.ainf.pluginprotege.testcasesentailmentsview.SectionType;
import at.ainf.pluginprotege.testcasesentailmentsview.TcaeFrameSectionItem;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.*;

import java.awt.event.ActionEvent;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 15.03.11
 * Time: 12:55
 * To change this template use File | Settings | File Templates.
 */
public class LoadTestcasesAction extends AbstractAction {

    public void actionPerformed(ActionEvent e) {
        getWS().loadTestcasesAction();
    }

}
