package at.ainf.protegeview.queryaskingview;

import at.ainf.protegeview.WorkspaceTab;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.Partition;
import at.ainf.protegeview.controlpanel.QueryDebuggerPreference;
import at.ainf.protegeview.debugmanager.DebugManager;
import at.ainf.protegeview.debugmanager.ResetReqEvent;
import at.ainf.protegeview.debugmanager.ResetReqListener;
import at.ainf.protegeview.queryaskingview.buttons.QueryQuestList;
import at.ainf.protegeview.queryaskingview.buttons.QueryQuestListItem;
import at.ainf.protegeview.testcasesentailmentsview.SectionType;
import at.ainf.protegeview.testcasesentailmentsview.TcaeFrameSection;
import at.ainf.protegeview.testcasesentailmentsview.TcaeFrameSectionItem;
import at.ainf.protegeview.views.ResultsListSectionItem;
import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponentMediator;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.03.11
 * Time: 09:03
 * To change this template use File | Settings | File Templates.
 */
public class QueryShowPanel extends JPanel implements ResetReqListener {

    public static final String ID = QueryShowPanel.class.getName();

    private JTextArea instructionArea;

    private JPanel marginPanel;

    private JLabel marginLabel;

    private Icon backgroundImage;

    private boolean notifyDisplaying;

    private String title;


    private QueryQuestList queryList;


    protected DefaultListModel queryListModel;

    private WorkspaceTab workspace;

    //private IQueryProvider queryGenerator;

    private Partition<OWLLogicalAxiom> actQuery;

    private DiagProvider diagProvider;

    private JDialog dialog;

    private QueryAskingToolbar toolbar;

    public QueryShowPanel(WorkspaceTab ws) {


        super();

        this.workspace = ws;
        // this.editorKit = editorKit;
        this.title = "Query";
        // this.dialog = dialog;
        notifyDisplaying = true;

        createUsrInterface();
        workspace.setQueryShowPanel(this);

        DebugManager.getInstance().addResetReqListener(this);

    }


    /* public ModelManager getModelManager() {
        return editorKit.getModelManager();
    }


    public EditorKit getEditorKit() {
        return editorKit;
    }

    public void setBackgroundImage(String name) {
        backgroundImage = Icons.getIcon(name);
    }

    public void setMarginImage(Icon icon) {
        marginLabel.setIcon(icon);
    }*/

    public void processResetReq(ResetReqEvent e) {
        diagProvider = null;
        queryListModel.clear();
        actQuery = null;
    }

    final protected void createUsrInterface() {

        setLayout(new BorderLayout());

        queryList = new QueryQuestList(this);
        queryList.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
                Color.LIGHT_GRAY), "Query " +
                ""), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        queryListModel = new DefaultListModel();
        queryList.setModel(queryListModel);

        add(ComponentFactory.createScrollPane(queryList));
        queryList.setCellRenderer(new OWLCellRenderer(workspace.getOWLEditorKit()) {

            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                Object renderableValue = value;
                if (value instanceof ResultsListSectionItem) {
                    ResultsListSectionItem item = (ResultsListSectionItem) value;
                    renderableValue = item.getOWLObject();
                }
                setHighlightKeywords(true);
                //setWrap(true);
                //setStrikeThrough(true);
                //setOpaque(true);
                setPreferredWidth(list.getWidth());
                return super.getListCellRendererComponent(list, renderableValue, index, isSelected, cellHasFocus);
            }
        });
        (queryList).setMediator(new LinkedObjectComponentMediator(workspace.getOWLEditorKit(), queryList));

    }

    public void setButtonMarkedIfActiveAll() {
        if (isAllEntailedButtonMarked()) {
            toolbar.yesButton.setActivate(true);
        } else if (isAllNonEntailedButtonMarked()) {
            toolbar.noButton.setActivate(true);
        } else {

        }


    }

    public void touchModelElements() {
        for (int i = 0; i < queryListModel.size(); i++) {
            queryListModel.set(i, queryListModel.get(i));
        }
    }

    public boolean isItemMarkedDifferent(QueryQuestListItem item, boolean entailed) {

        if (entailed) {
            for (int i = 0; i < queryListModel.size(); i++) {
                QueryQuestListItem item2 = (QueryQuestListItem) queryListModel.get(i);
                if (item2.isNonEntailedMarked() && item != item2) {
                    return true;
                }
            }
        } else {
            for (int i = 0; i < queryListModel.size(); i++) {
                QueryQuestListItem item2 = (QueryQuestListItem) queryListModel.get(i);
                if (item2.isEntailedMarked() && item != item2) {
                    return true;
                }
            }
        }
        return false;

    }

    public boolean isAllNonEntailedButtonMarked() {
        for (int i = 0; i < queryListModel.size(); i++) {
            QueryQuestListItem item = (QueryQuestListItem) queryListModel.get(i);
            if (!item.isNonEntailedMarked()) {
                return false;
            }

        }
        return true;
    }

    public void unmarkAxioms(boolean entailed) {
        for (int i = 0; i < queryListModel.size(); i++) {
            QueryQuestListItem item = (QueryQuestListItem) queryListModel.get(i);
            if (entailed && item.isEntailedMarked())
                item.setEntailedMarked(false);
            else if (!entailed && item.isNonEntailedMarked())
                item.setNonEntailedMarked(false);
        }
    }

    public boolean isAllEntailedButtonMarked() {
        for (int i = 0; i < queryListModel.size(); i++) {
            QueryQuestListItem item = (QueryQuestListItem) queryListModel.get(i);
            if (!item.isEntailedMarked()) {
                return false;
            }

        }
        return true;
    }


    public void deactToolbarYesNoButton() {
        toolbar.deactButtons();
    }

    public void init() {

        if (workspace.getSearch()==null)
            workspace.doResetAct2();
        if (diagProvider == null)
            diagProvider = new DiagProvider(workspace.getSearch(),
                    QueryDebuggerPreference.getInstance().isQueryMinimizerActive(), QueryDebuggerPreference.getInstance().getNumOfLeadingDiags());

        // actQuery = null;
        getNextQuery();
    }

    protected void processAnswer(SectionType t) {
        TcaeFrameSection sec = workspace.getListSections().get(t);
        List<TcaeFrameSectionItem> items = workspace.getSectionItems().get(sec);
        items.add(new TcaeFrameSectionItem(sec.getOWLEditorKit(), sec, null, null, actQuery.partition));

        workspace.saveTestcasesAction();
        workspace.displaySection(); //workspace.tcaeFramelist.displaySection();
    }

    public void setNonEntailedMarkers() {
        for (int i = 0; i < queryListModel.size(); i++) {
            QueryQuestListItem item = (QueryQuestListItem) queryListModel.get(i);
            item.setNonEntailedMarked(true);

        }
    }

    public void setEntailedMarkers() {
        for (int i = 0; i < queryListModel.size(); i++) {
            QueryQuestListItem item = (QueryQuestListItem) queryListModel.get(i);
            item.setEntailedMarked(true);

        }
    }

    public void removeMarkers() {
        for (int i = 0; i < queryListModel.size(); i++) {
            QueryQuestListItem item = (QueryQuestListItem) queryListModel.get(i);
            item.setNonEntailedMarked(false);
            item.setEntailedMarked(false);

        }
    }

    public Set<OWLLogicalAxiom> getQueryAxiomsEntailed() {
        Set<OWLLogicalAxiom> r = new HashSet<OWLLogicalAxiom>();
        for(int i = 0; i < queryListModel.size(); i++) {
            if (((QueryQuestListItem)queryListModel.get(i)).isEntailedMarked())
                r.add(((QueryQuestListItem)queryListModel.get(i)).getAxiom());
        }
        return r;
    }

    public Set<OWLLogicalAxiom> getQueryAxiomsNonEntailed() {
        Set<OWLLogicalAxiom> r = new HashSet<OWLLogicalAxiom>();
        for(int i = 0; i < queryListModel.size(); i++) {
            if (((QueryQuestListItem)queryListModel.get(i)).isNonEntailedMarked())
                r.add(((QueryQuestListItem)queryListModel.get(i)).getAxiom());
        }
        return r;
    }

    public Set<OWLLogicalAxiom> getQueryAxioms() {
        Set<OWLLogicalAxiom> r = new HashSet<OWLLogicalAxiom>();
        for(int i = 0; i < queryListModel.size(); i++) {
            r.add(((QueryQuestListItem)queryListModel.get(i)).getAxiom());
        }
        return r;
    }

    public void setQueryListModel(Collection<OWLLogicalAxiom> axioms) {
        queryListModel.clear();
        for (OWLLogicalAxiom a : axioms) {
            queryListModel.addElement(new QueryQuestListItem(a, workspace));
        }
    }

    private boolean getNextQuery() {

        if (queryListModel.size() != 0) {
            JOptionPane.showMessageDialog(null, "Please confirm the answer you have made before you ask for a new query ", "Not confirmed", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        Partition<OWLLogicalAxiom> last = actQuery;
        if (workspace.getSearch().getStorage().getValidHittingSets().size() == 1) {
            JOptionPane.showMessageDialog(null, "There is only one diagnosis left", "Only one diagnosis", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        actQuery = diagProvider.getQuery();
        if (actQuery == null || actQuery.partition == null) {
            //dialog.setVisible(false);
            JOptionPane.showMessageDialog(null, "There is no query left", "No possible Query", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        if (actQuery.dx.size() != 0 && actQuery.dnx.size() == 0 && actQuery.dz.size() == 0) {
            JOptionPane.showMessageDialog(null, "There is only positive answer possible", "Only one answer possible", JOptionPane.INFORMATION_MESSAGE);
            //return false;
        }

        if (actQuery.dx.size() == 0 && actQuery.dnx.size() != 0 && actQuery.dz.size() == 0) {
            JOptionPane.showMessageDialog(null, "There is only negative answer possible", "Only one answer possible", JOptionPane.INFORMATION_MESSAGE);
            //return false;
        }

        if (last != null && actQuery.partition.equals(last.partition)) {

            //dialog.setVisible(false);
            JOptionPane.showMessageDialog(null, "There is again the same query so no discrimination between leading diagnoses is possible.", "Same Query", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }


        setQueryListModel(actQuery.partition);
        return true;


    }

    public void processPosAnswer() {

        processAnswer(SectionType.ET);
        try {
            //for (OWLLogicalAxiom a : actQuery.partition)
            //    workspace.getSearch().getTheory().addEntailedTest (a);
            workspace.getSearch().getTheory().addEntailedTest(actQuery.partition);
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // diagProvider.calculateLead();

        Collection<AxiomSet<OWLLogicalAxiom>> diag = new HashSet<AxiomSet<OWLLogicalAxiom>>();

        for (AxiomSet<OWLLogicalAxiom> q : workspace.getSearch().getStorage().getValidHittingSets()) {
            diag.add(q);
        }

        //workspace.addAxiomToResultsList(workspace.getHittingSetListModel(), "Diagnosis", diag);

        //getNextQuery();
    }

    public void processNegAnswer() {

        processAnswer(SectionType.NET);
        try {
            //for (OWLLogicalAxiom a : actQuery.partition)
            //    workspace.getSearch().getTheory().addNonEntailedTest(a);
            workspace.getSearch().getTheory().addNonEntailedTest(actQuery.partition);
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        //diagProvider.calculateLead();

        Collection<AxiomSet<OWLLogicalAxiom>> diag = new HashSet<AxiomSet<OWLLogicalAxiom>>();

        for (AxiomSet<OWLLogicalAxiom> q : workspace.getSearch().getStorage().getValidHittingSets()) {
            diag.add(q);
        }

        //workspace.addAxiomToResultsList(workspace.getHittingSetListModel(), "Diagnosis", diag);

        //getNextQuery();
    }

    /*public void processNextButton() {
        getNextQuery();
    }*/

    /*private static Set<Class> nonTransparentComponents;


    static {
        nonTransparentComponents = new HashSet<Class>();
        nonTransparentComponents.add(JTextComponent.class);
        nonTransparentComponents.add(JList.class);
        nonTransparentComponents.add(JTree.class);
        nonTransparentComponents.add(JTable.class);
        nonTransparentComponents.add(JScrollPane.class);
        nonTransparentComponents.add(JComboBox.class);
    }


    protected void setComponentTransparency(Component component) {
        if (component instanceof JComponent) {
            for (Class c : nonTransparentComponents) {
                if (c.isInstance(component)) {
                    return;
                }
            }
            ((JComponent) component).setOpaque(false);
        }
        if (component instanceof Container) {
            Container container = (Container) component;
            Component[] components = container.getComponents();
            for (int i = 0; i < components.length; i++) {
                setComponentTransparency(components[i]);
            }
        }} */


    public void setInstructions(String instructions) {
        instructionArea.setText(instructions);
    }


    public Dimension getPreferredSize() {
        return new Dimension(800, 550);
    }

    public void applyChanged() {
        if (diagProvider == null) return;
        if (toolbar.yesButton.isActivated()) {
            processPosAnswer();
            // getNextQuery();
            //updateHsLst();

            queryListModel.clear();
            toolbar.deactButtons();
            diagProvider.calculateLead();
            return;
        } else if (toolbar.noButton.isActivated()) {

            processNegAnswer();
            // getNextQuery();
            //updateHsLst();
            queryListModel.clear();
            toolbar.deactButtons();
            diagProvider.calculateLead();
            return;
        }

        Set<OWLLogicalAxiom> negativeTestCases = new HashSet<OWLLogicalAxiom>();
        Set<OWLLogicalAxiom> positiveTestCases = new HashSet<OWLLogicalAxiom>();
        for (Enumeration p = queryListModel.elements(); p.hasMoreElements(); ) {
            QueryQuestListItem item = (QueryQuestListItem) p.nextElement();
            if (item.isEntailedMarked()) {

                OWLLogicalAxiom axiom = item.getAxiom();
                //TcaeFrameSection sec = workspace.getListSections().get(SectionType.ET);
                //List<TcaeFrameSectionItem> items = workspace.getSectionItems().get(sec);

                //items.add(new TcaeFrameSectionItem(sec.getOWLEditorKit(), sec, null, null, axiom));
                //workspace.displaySection();

                positiveTestCases.add(axiom);
                //workspace.getSearch().getTheory().addEntailedTest(axiom);

            }
            if (item.isNonEntailedMarked()) {

                OWLLogicalAxiom axiom = item.getAxiom();
                /*TcaeFrameSection sec = workspace.getListSections().get(SectionType.NET);
                List<TcaeFrameSectionItem> items = workspace.getSectionItems().get(sec);
                items.add(new TcaeFrameSectionItem(sec.getOWLEditorKit(), sec, null, null, axiom));
                workspace.displaySection();*/

                negativeTestCases.add(axiom);
                //workspace.getSearch().getTheory().addNonEntailedTest(axiom);

            }
        }

        if (positiveTestCases.size() > 0) {
            TcaeFrameSection sec = workspace.getListSections().get(SectionType.ET);
            List<TcaeFrameSectionItem> items = workspace.getSectionItems().get(sec);

            items.add(new TcaeFrameSectionItem(sec.getOWLEditorKit(), sec, null, null, positiveTestCases));

            workspace.displaySection();
            try {
                workspace.getSearch().getTheory().addEntailedTest(positiveTestCases);
            } catch (SolverException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InconsistentTheoryException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        else if (negativeTestCases.size() > 0) {
            TcaeFrameSection sec = workspace.getListSections().get(SectionType.NET);
            List<TcaeFrameSectionItem> items = workspace.getSectionItems().get(sec);
            items.add(new TcaeFrameSectionItem(sec.getOWLEditorKit(), sec, null, null, negativeTestCases));
            workspace.displaySection();
            try {
                workspace.getSearch().getTheory().addNonEntailedTest(negativeTestCases);
            } catch (SolverException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InconsistentTheoryException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        else {
            JOptionPane.showMessageDialog(null, "You have to mark at least one axiom ", "No decision made", JOptionPane.WARNING_MESSAGE);
            return;
        }

        workspace.saveTestcasesAction();
        queryListModel.clear();
        diagProvider.calculateLead();
        //  getNextQuery();

    }

    public void setToolbar(QueryAskingToolbar toolbar) {
        this.toolbar = toolbar;
    }


    /*protected void createUsrInterface(JComponent parent) {
        JComponent content = createHittingSetPanel();

        parent.add (content);
    }*/


    private class HolderPanel extends JPanel {

        /**
         *
         */
        private static final long serialVersionUID = 5476717953072456842L;
        private Color color;


        public HolderPanel() {
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(20, 20, 20, 20)));
            setLayout(new BorderLayout(7, 20));
            setOpaque(false);
            color = new Color(255, 255, 255, 230);
        }


        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Rectangle r = g.getClipBounds();
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(color);
            g2.fillRect(r.x, r.y, r.width, r.height);
        }
    }


    /*protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            backgroundImage.paintIcon(this, g, 0, 0);
        }
    }*/

}
