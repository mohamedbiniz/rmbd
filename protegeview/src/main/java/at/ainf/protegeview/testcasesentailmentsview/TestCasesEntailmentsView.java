package at.ainf.protegeview.testcasesentailmentsview;

import at.ainf.protegeview.WorkspaceTab;
import at.ainf.protegeview.controlpanel.OptionsDialog;
import at.ainf.protegeview.debugmanager.DebugManager;
import org.apache.log4j.Logger;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.core.ui.error.ErrorLogPanel;
import org.protege.editor.core.ui.workspace.Workspace;
import org.protege.editor.core.ui.workspace.WorkspaceFrame;
import org.protege.editor.core.ui.workspace.WorkspaceManager;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.SaveConfirmationPanel;
import org.protege.editor.owl.ui.view.cls.AbstractOWLClassViewComponent;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.02.11
 * Time: 10:52
 * To change this template use File | Settings | File Templates.
 */
public class TestCasesEntailmentsView extends AbstractOWLClassViewComponent {

    /**
     *
     */
    private static final long serialVersionUID = -7899828024396593253L;
    private TcaeFramelist frameList;

    private static Logger logger = Logger.getLogger(TestCasesEntailmentsView.class.getName());

    public void initialiseClassView() throws Exception {
        frameList = new TcaeFramelist(getOWLEditorKit(), new TcaeFrame(getOWLEditorKit()));
        setLayout(new BorderLayout());
        //add(new TcaeButtonsToolbar((WorkspaceTab) getOWLWorkspace().getWorkspaceTab("at.ainf.protegeview.WorkspaceTab")), BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(frameList);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(sp,BorderLayout.CENTER);

        ((WorkspaceTab) getOWLWorkspace().getWorkspaceTab("at.ainf.protegeview.WorkspaceTab")).createOWLTheory();
        OptionsDialog.getDialog().getProbabTableModel().loadFromOntology();
        OptionsDialog.getDialog().getProbabTableModel().loadFromCalcMap();

        OptionsDialog.getDialog().getProbabTableModel().updateCalcMapWithMap();
        ((WorkspaceTab) getOWLWorkspace().getWorkspaceTab("at.ainf.protegeview.WorkspaceTab")).createOWLTheory();

        //OptionsDialog.getDialog().getProbabTableModel().saveToTheOnt();

        /*EditorKitManager manager = ProtegeManager.getInstance().getEditorKitManager();
        final Workspace workspace = getWorkspace();
        final WorkspaceFrame frame = manager.getWorkspaceManager().getFrame(workspace);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {

                ((WorkspaceTab) getOWLWorkspace().getWorkspaceTab("at.ainf.protegeview.WorkspaceTab")).saveTestcasesAction();

            }
        });*/

        ((WorkspaceTab) getOWLWorkspace().getWorkspaceTab("at.ainf.protegeview.WorkspaceTab")).loadTestcasesAction();
        WorkspaceManager manager = ProtegeManager.getInstance().getEditorKitManager().getWorkspaceManager();
        final WorkspaceFrame frame = manager.getFrame(getWorkspace());

        for (WindowListener l : frame.getWindowListeners()) {
            if (l.toString().startsWith("org.protege.editor.core.ui.workspace.WorkspaceManager")) {
                frame.removeWindowListener(l);

                frame.addWindowListener(new WindowAdapter() {

                    public void handleSave (Set<OWLOntology> dirtyOntologies) throws Exception {

                        OWLEditorKit editorKit = (OWLEditorKit) getWorkspace().getEditorKit();
                    try {
                        //Set<OWLOntology> dirtyOntologies = editorKit.getModelManager().getDirtyOntologies();
                        for (OWLOntology ont : new HashSet<OWLOntology>(dirtyOntologies)) {
                            editorKit.getModelManager().save(ont);
                        }
                        //editorKit.getModelManager().save();
                        editorKit.getWorkspace().save();
                        /*for (URI uri : newPhysicalURIs) {
                            addRecent(uri);
                        }
                        newPhysicalURIs.clear(); */
                        SaveConfirmationPanel.showDialog(editorKit, dirtyOntologies);
                    }
                    catch (OWLOntologyStorerNotFoundException e) {
                        OWLOntology ont = editorKit.getModelManager().getActiveOntology();
                        OWLOntologyFormat format = editorKit.getModelManager().getOWLOntologyManager().getOntologyFormat(ont);
                        String message = "Could not save ontology in the specified format (" + format + ").\n" + "Please select 'Save As' and choose another format.";
                        logger.warn(message);
                        ErrorLogPanel.showErrorDialog(new OWLOntologyStorageException(message, e));
                    }
                }

                    public boolean doClose(Workspace workspace) {
                        boolean close = true;
                        if (workspace.getEditorKit().getModelManager().isDirty()) {
                            Set<OWLOntology> dirtyOntologies = getOWLModelManager().getDirtyOntologies();
                            for (Iterator<OWLOntology> i = dirtyOntologies.iterator(); i.hasNext(); ) {
                                if (i.next().getOntologyID().isAnonymous()) {
                                    i.remove();
                                }
                            }

                            if (dirtyOntologies.size() == 0 ) {
                                 ProtegeManager.getInstance().disposeOfEditorKit(workspace.getEditorKit());
                                    return true;
                            }

                            // Ask user if they want to save?
                            int ret = JOptionPane.showConfirmDialog(workspace,
                                                                    "Save modified ontologies?",
                                                                    "Unsaved ontologies",
                                                                    JOptionPane.YES_NO_CANCEL_OPTION,
                                                                    JOptionPane.WARNING_MESSAGE);
                            if (ret == JOptionPane.YES_OPTION) {
                                try {
                                       handleSave (dirtyOntologies);
                                       //workspace.getEditorKit().handleSave();
                                }
                                catch (Exception e) {
                                    close = false;
                                }
                            }
                            else if (ret == JOptionPane.NO_OPTION){
                            }
                            else{
                                close = false;
                            }
                        }

                        if (close) {
                            DebugManager.getInstance().reset();
                            ProtegeManager.getInstance().disposeOfEditorKit(workspace.getEditorKit());
                        }

                        return close;
                    }

                    public void windowClosing(WindowEvent e) {
                        if (doClose(getWorkspace())){
                            // Remove the listener
                            frame.removeWindowListener(this);
                            frame.dispose();
                        }
                    }
                });
            }
        }


        //WorkspaceTab workspace = (WorkspaceTab) getOWLWorkspace().getWorkspaceTab("at.ainf.protegeview.WorkspaceTab");
        //workspace.posTestCaseListModel

        // frameList.addToPopupMenu(new ConvertSelectionToEquivalentClassAction());
        // frameList.addToPopupMenu(new CreateNewEquivalentClassAction<OWLClass>());
        // frameList.addToPopupMenu(new CreateClosureAxiomAction());

        /*if (workspace.posTestCaseListModel != null) {
            frameList.setModel (workspace.posTestCaseListModel);
        }
        else {
            DefaultListModel model = new DefaultListModel();
            frameList.setModel(model);
            workspace.posTestCaseListModel = model;
            model.addElement(new TcaeFrameSection(workspace, TcaeFrameSection.TestCaseType.POSITIVE_TEST_CASE_LIST_SECTION,getOWLEditorKit(),null));
        } */
    }


    /**
     * This method is called to request that the view is updated with
     * the specified class.
     * @param selectedClass The class that the view should be updated with.  Note
     *                      that this may be <code>null</code>, which indicates that the view should
     *                      be cleared
     * @return The actual class that the view is displaying after it has been updated
     *         (may be <code>null</code>)
     */
    protected OWLClass updateView(OWLClass selectedClass) {
        frameList.setRootObject(null);
        return null;
    }


    public void disposeView() {
        frameList.dispose();
    }


     /*

         Logger log = Logger.getLogger(HittingSetView.class);

    private TcaeFramelist negTestCaseList;

    protected void initialiseOWLView() throws Exception {

        /*WorkspaceTab workspace = (WorkspaceTab) getOWLWorkspace().getWorkspaceTab("at.ainf.protegeview.WorkspaceTab");

        setLayout(new BorderLayout(10, 10));

        JComponent hittingSetPanel = createHittingSetPanel();


        add(hittingSetPanel, BorderLayout.CENTER);

        updateGUI();

        if (workspace.negTestCaseListModel != null) {
            negTestCaseList.setModel (workspace.negTestCaseListModel);
        }
        else {
            DefaultListModel model = new DefaultListModel();
            negTestCaseList.setModel(model);
            workspace.negTestCaseListModel = model;
            //model.addElement(new TcaeFrameSection(workspace, TestCaseType.NEGATIVE_TEST_CASE_LIST_SECTION,getOWLEditorKit(),null));
        }


    }

    private JComponent createHittingSetPanel() {
        /*JComponent result = new JScrollPane();
        /*result.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
                Color.LIGHT_GRAY), "Negative Test Cases"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        negTestCaseList = new TcaeFramelist(getOWLEditorKit(), new OWLClassDescriptionFrame(getOWLEditorKit()));
        //negTestCaseList.setShowSubClasses(true);
        result.add(negTestCaseList);
        negTestCaseList.addToPopupMenu(new CreateNewEquivalentClassAction<OWLClass>());
        negTestCaseList.addToPopupMenu(new CreateClosureAxiomAction());

        return result;               return null;
    }




}

      */

}
