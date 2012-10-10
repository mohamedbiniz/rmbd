package at.ainf.protegeview.testcasesentailmentsview;

import at.ainf.diagnosis.Searchable;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.protegeview.WorkspaceTab;
import at.ainf.protegeview.testcasesentailmentsview.axiomeditor.OWLAxiomEditor;
import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.editor.core.ui.list.MListButton;
import org.protege.editor.core.ui.list.MListItem;
import org.protege.editor.core.ui.util.InputVerificationStatusChangedListener;
import org.protege.editor.core.ui.util.VerifiedInputEditor;
import org.protege.editor.core.ui.util.VerifyingOptionPane;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.protege.editor.owl.ui.frame.OWLFrameObject;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRow;
import org.protege.editor.owl.ui.framelist.ExplainButton;
import org.protege.editor.owl.ui.framelist.OWLFrameList;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.02.11
 * Time: 11:41
 * To change this template use File | Settings | File Templates.
 */
public class TcaeFramelist extends OWLFrameList<OWLClass> {

    //private OWLEditorKit edit;

    DefaultListModel listModel;
    private WorkspaceTab workspace = null;
    private HashMap<SectionType, TcaeFrameSection> listSections;
    private HashMap<TcaeFrameSection, List<TcaeFrameSectionItem>> sectionItems;


    public TcaeFramelist(OWLEditorKit editorKit, OWLFrame<OWLClass> owlAxiomOWLFrame) {
        super(editorKit, owlAxiomOWLFrame);


        setCellRenderer(new MyOWLFrameListRenderer(editorKit));
        workspace = (WorkspaceTab) editorKit.getOWLWorkspace().getWorkspaceTab("at.ainf.protegeview.WorkspaceTab");
        listSections = workspace.getListSections();
        sectionItems = workspace.getSectionItems();
        listModel = workspace.getTestcasesModel();
        setModel(listModel);

        if (workspace.isFirst()) {

            for (OWLFrameSection section : getFrame().getFrameSections()) {
                listSections.put(((TcaeFrameSection) section).getSectionType(), (TcaeFrameSection) section);
                sectionItems.put((TcaeFrameSection) section, new LinkedList<TcaeFrameSectionItem>());
            }

        }
        //workspace.tcaeFramelist = this;

    }

    /*public void refreshComponent() {
        displaySection();
    }

    /*public void addAxiomToSection(Set<OWLLogicalAxiom> axiom, TcaeFrameSection sec) {
        workspace.testPluginView.testcasesChange = true;
        sectionItems.get(sec).add(new TcaeFrameSectionItem(sec.getOWLEditorKit(), sec, null, getRootObject(), axiom));
    }

    public void displaySection() {
        List<OWLFrameObject> list = new ArrayList<OWLFrameObject>();

        listModel.clear();
        for (SectionType sec : SectionType.values()) {
            TcaeFrameSection section = listSections.get(sec);
            listModel.addElement(section);
            for (TcaeFrameSectionItem r : sectionItems.get(section)) {
                listModel.addElement(r);
            }
        }

    }*/

    public void handleDelete() {
        for (int number : getSelectedIndices()) {
            TcaeFrameSectionItem item = (TcaeFrameSectionItem) getModel().getElementAt(number);
            removeItem(item);
            workspace.setTestcasesChange(true);

        }
        workspace.saveTestcasesAction();
        workspace.displaySection();
    }

    public void removeItem(TcaeFrameSectionItem item) {
        sectionItems.get((TcaeFrameSection)item.getFrameSection()).remove(item);
    }

    protected boolean addCheckedPositiveTest(Searchable<OWLLogicalAxiom> theory, Set<OWLLogicalAxiom> axioms) {
        theory.getKnowledgeBase().addPositiveTest(axioms);
        try {
            if (!theory.areTestsConsistent()) {
                theory.getKnowledgeBase().removePositiveTest(axioms);
                JOptionPane.showMessageDialog(null, "There was a solver exception", "SolverException", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (SolverException e) {
            JOptionPane.showMessageDialog(null, "There was a solver exception", "SolverException", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    protected boolean addCheckedNegativeTest(Searchable<OWLLogicalAxiom> theory, Set<OWLLogicalAxiom> axioms) {
        theory.getKnowledgeBase().addNegativeTest(axioms);
        try {
            if (!theory.areTestsConsistent()) {
                theory.getKnowledgeBase().removeNegativeTest(axioms);
                JOptionPane.showMessageDialog(null, "There was a solver exception", "SolverException", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (SolverException e) {
            JOptionPane.showMessageDialog(null, "There was a solver exception", "SolverException", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    protected boolean addCheckedEntailedTest(Searchable<OWLLogicalAxiom> theory, Set<OWLLogicalAxiom> axioms) {
        theory.getKnowledgeBase().addEntailedTest(axioms);
        try {
            if (!theory.areTestsConsistent()) {
                theory.getKnowledgeBase().removeEntailedTest(axioms);
                JOptionPane.showMessageDialog(null, "There was a solver exception", "SolverException", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (SolverException e) {
            JOptionPane.showMessageDialog(null, "There was a solver exception", "SolverException", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    protected boolean addCheckedNonEntailedTest(Searchable<OWLLogicalAxiom> theory, Set<OWLLogicalAxiom> axioms) {
        theory.getKnowledgeBase().addNonEntailedTest(axioms);
        try {
            if (!theory.areTestsConsistent()) {
                theory.getKnowledgeBase().removeNonEntailedTest(axioms);
                JOptionPane.showMessageDialog(null, "There was a solver exception", "SolverException", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (SolverException e) {
            JOptionPane.showMessageDialog(null, "There was a solver exception", "SolverException", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    protected boolean addAxiomToTcae(Set<OWLLogicalAxiom> axiom, SectionType type) {

        if (workspace.getOwlTheory() == null) {
            workspace.createOWLTheory();

        }
        Searchable<OWLLogicalAxiom> theory = workspace.getOwlTheory();

            switch (type) {
                case PT:
                    return addCheckedPositiveTest(theory,axiom);
                case NT:
                    return addCheckedNegativeTest(theory,axiom);
                case ET:
                    return addCheckedEntailedTest(theory,axiom);
                case NET:
                    return addCheckedNonEntailedTest(theory,axiom);
            }

        return true;

    }

    protected OWLAxiomEditor getSectionEditor(OWLFrameObject frameObject,SectionType type) {
        return new OWLAxiomEditor(((TcaeFrameSection) frameObject).getOWLEditorKit(), null,type);
    }

    protected OWLAxiomEditor getSectionItemEditor(OWLFrameObject frameObject, SectionType type) {
        return new OWLAxiomEditor(((TcaeFrameSectionItem) frameObject).getOWLEditorKit(),
                ((TcaeFrameSectionItem) frameObject).getAxioms(),type);
    }

    protected boolean isFromUserConfirmed(Object retVal) {
        return retVal != null && retVal.equals(JOptionPane.OK_OPTION);
    }

    protected void handleEdit() {

        final OWLFrameObject frameObject = (OWLFrameObject) getSelectedValue();

        // If we don't have any editing component then just return
        //OWLClassAxiomEditor axEd = new OWLClassAxiomEditor(edit);
        //axEd.setVisible(true);
        final boolean isRowEditor = frameObject instanceof OWLFrameSectionRow;
        //System.out.println("isRowEditor:  " + isRowEditor);

        //final OWLObjectEditor axiomeditor = frameObject.getEditor();

        final OWLAxiomEditor editor;


        final SectionType type;
        if (frameObject instanceof TcaeFrameSection) {
            editor = getSectionEditor(frameObject,((TcaeFrameSection) frameObject).getSectionType());
            type = ((TcaeFrameSection) frameObject).getSectionType();
        } else if (frameObject instanceof TcaeFrameSectionItem) {
            editor = getSectionItemEditor(frameObject,((TcaeFrameSectionItem) frameObject).getSectionType());
            type = ((TcaeFrameSectionItem) frameObject).getSectionType();
        } else {
            // type = null;
            // editor = null;
            return;
        }

        editor.addPanel();
        editor.selectPreferredEditor();

        /*if (axiomeditor == null) {
            return;
        }
        if (axiomeditor instanceof JWindow) {
            ((JWindow) axiomeditor).setVisible(true);
            return;
        }
        if (axiomeditor instanceof Wizard) {
            int ret = ((Wizard) axiomeditor).showModalDialog();
            if (ret == Wizard.FINISH_RETURN_CODE) {
                axiomeditor.getHandler().handleEditingFinished(axiomeditor.getEditedObjects());
            }
            return;
        }*/
        // Create the editing component dialog - we use an option pane
        // so that the buttons and keyboard actions are what are expected
        // by the user.
        final JComponent editorComponent = editor.getEditorComponent();
        final VerifyingOptionPane optionPane = new VerifyingOptionPane(editorComponent) {

            public void selectInitialValue() {
                // This is overriden so that the option pane dialog default
                // button
                // doesn't get the focus.
            }
        };
        final InputVerificationStatusChangedListener verificationListener = new InputVerificationStatusChangedListener() {
            public void verifiedStatusChanged(boolean verified) {
                /*optionPane.setOKEnabled(verified && frameObject.checkEditorResults(axiomeditor)); */
                optionPane.setOKEnabled(verified);
            }
        };
        // if the axiomeditor is verifying, will need to prevent the OK button from
        // being available
        if (editor instanceof VerifiedInputEditor) {
            ((VerifiedInputEditor) editor)
                    .addStatusChangedListener(verificationListener);
        }

        Preferences prefs = PreferencesManager.getInstance().getApplicationPreferences(ProtegeApplication.ID);
        final Component parent = prefs.getBoolean("DIALOGS_ALWAYS_CENTRED", false) ? SwingUtilities.getAncestorOfClass(Frame.class, getParent()) : getParent();
        final JDialog dlg = optionPane.createDialog(parent, null);
        // The axiomeditor shouldn't be modal (or should it?)
        dlg.setModal(false);
        dlg.setResizable(true);
        dlg.pack();
        dlg.setLocationRelativeTo(parent);
        switch (type) {
            case PT:
                dlg.setTitle("Please enter positive test case: ");
                break;
            case NT:
                dlg.setTitle("Please enter negative test case: ");
                break;
            case ET:
                dlg.setTitle("Please enter entailed axiom:  ");
                break;
            case NET:
                dlg.setTitle("Please enter not entailed axiom:   ");
                break;
        }

        dlg.addComponentListener(new ComponentAdapter() {

            public void componentHidden(ComponentEvent e) {
                Object retVal = optionPane.getValue();
                editorComponent.setPreferredSize(editorComponent.getSize());
                if (isFromUserConfirmed(retVal)) {
                    Set<OWLLogicalAxiom> axiom = editor.getEditedObject();


                    if (isRowEditor) {
                        Set<OWLLogicalAxiom> axioms = new TreeSet<OWLLogicalAxiom>(((TcaeFrameSectionItem) frameObject).getAxioms());
                        switch (type) {
                            case PT:
                                workspace.getSearch().getSearchable().getKnowledgeBase().removePositiveTest(axioms);
                                break;
                            case NT:
                                workspace.getSearch().getSearchable().getKnowledgeBase().removeNegativeTest(axioms);
                                break;
                            case ET:
                                workspace.getSearch().getSearchable().getKnowledgeBase().removeEntailedTest(axioms);
                                break;
                            case NET:
                                workspace.getSearch().getSearchable().getKnowledgeBase().removeNonEntailedTest(axioms);
                                break;
                        }
                        if (!addAxiomToTcae(axiom, type)) return;
                        ((TcaeFrameSectionItem) frameObject).setAxiom(axiom);
                    } else {
                        if (!addAxiomToTcae(axiom, type)) return;
                        workspace.addAxiomToSection(axiom, ((TcaeFrameSection) frameObject));
                    }

                    workspace.saveTestcasesAction();
                    //System.out.println("the actual axiom is: "+ axiom);
                    workspace.displaySection();

                    //axiomeditor.getHandler().handleEditingFinished(axiomeditor.getEditedObjects());
                }
                setSelectedValue(frameObject, true);
                if (editor instanceof VerifiedInputEditor) {
                    ((VerifiedInputEditor) editor)
                            .removeStatusChangedListener(verificationListener);
                }
                // axiomeditor.fullReset();
                if (isRowEditor) {
                    editor.dispose();
                }
            }
        });


        //if (rootObject instanceof OWLObject) {
        //probabDialog.setTitle("Please enter axiom: ");
        /*}
        else if (rootObject != null) {
            probabDialog.setTitle(rootObject.toString());
        }*/

        dlg.setVisible(true);

    }



    public void setRootObject(OWLClass rootObject) {


        workspace.displaySection();

    }

    protected List<MListButton> getButtons(Object value) {
        List<MListButton> buttons = new ArrayList<MListButton>(super.getButtons(value));

        for (Iterator<MListButton> button = buttons.iterator(); button.hasNext();) {
            MListButton mListButton = button.next();
            if (mListButton instanceof ExplainButton) {
                button.remove();
            }
        }

        return buttons;
    }


    protected Color getItemBackgroundColor(MListItem item) {
        return new Color(240, 245, 240);
    }


}
