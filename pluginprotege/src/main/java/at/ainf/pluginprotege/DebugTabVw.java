package at.ainf.pluginprotege;

import at.ainf.pluginprotege.controlpanel.DebugIconsLoader;
import org.protege.editor.core.ui.util.Resettable;
import org.protege.editor.core.ui.view.ViewsPane;
import org.protege.editor.core.ui.view.ViewsPaneMemento;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 10.11.11
 * Time: 10:53
 * To change this template use File | Settings | File Templates.
 */
public class DebugTabVw extends AbstractOWLViewComponent implements Resettable {
    public static final String ID = "at.ainf.pluginprotege.DebugTabVw";

    private JTabbedPane cardPanel;

    private java.util.List<ViewsPane> viewsPanes = new ArrayList<ViewsPane>();

    private static final String QUERY_PANEL = "Query";

    private static final String DIAGNOSES_PANEL = "Diagnoses";

    private static final String OPTIONS_PANEL = "Options";

    private static final String TESTCASES_PANEL = "Testcases";

    private static final String PREVIEW_PANEL = "Preview";

    private static final String REVISION_PANEL = "Revision";

    private static final String META_PANEL = "Metamodel";

    protected void initialiseOWLView() throws Exception {
        setLayout(new BorderLayout());
        cardPanel = new JTabbedPane();


        cardPanel.setFont(cardPanel.getFont().deriveFont(20));
        add(cardPanel);
        //cardPanel.setLayout(cardLayout);
        //cardPanel.addTab(BLANK_PANEL,new JPanel());
        createViewPanes(false);
        /*getOWLWorkspace().getOWLSelectionModel().addListener(new OWLSelectionModelListener() {
            public void selectionChanged() throws Exception {
                processSelection();
            } }); */
        getView().setShowViewBar(false);
        //processSelection();
    }


    private void createViewPanes(boolean reset) {
        addPane(QUERY_PANEL,
                "/debug-tab-query-panel.xml",
                "at.ainf.pluginprotege.DebugTabVw.query",
                reset,DebugIconsLoader.QUERY);
        cardPanel.setMnemonicAt(0, KeyEvent.VK_Q);


        addPane(DIAGNOSES_PANEL,
                "/debug-tab-diagnoses-panel.xml",
                "at.ainf.pluginprotege.DebugTabVw.diagnoses",
                reset, DebugIconsLoader.DIAGNOSES);
        cardPanel.setMnemonicAt(1, KeyEvent.VK_D);


        addPane(OPTIONS_PANEL,
                "/debug-tab-options-panel.xml",
                "at.ainf.pluginprotege.DebugTabVw.option",
                reset, DebugIconsLoader.OPTION);
        cardPanel.setMnemonicAt(2, KeyEvent.VK_O);


        addPane(TESTCASES_PANEL,
                "/debug-tab-testcases-panel.xml",
                "at.ainf.pluginprotege.DebugTabVw.testcases",
                reset, DebugIconsLoader.TESTCASES);
        cardPanel.setMnemonicAt(3, KeyEvent.VK_T);


        addPane(PREVIEW_PANEL,
                "/debug-tab-preview-panel.xml",
                "at.ainf.pluginprotege.DebugTabVw.preview",
                reset, DebugIconsLoader.PREVIEW);
        cardPanel.setMnemonicAt(4, KeyEvent.VK_P);


        addPane(REVISION_PANEL,
                "/debug-tab-revision-panel.xml",
                "at.ainf.pluginprotege.DebugTabVw.revision",
                reset, DebugIconsLoader.REVISION);
        cardPanel.setMnemonicAt(5, KeyEvent.VK_R);

        addPane(META_PANEL,
                "/debug-tab-metamodeling-panel.xml",
                "at.ainf.pluginprotege.DebugTabVw.metamodel",
                reset, DebugIconsLoader.METAMODELING);
        cardPanel.setMnemonicAt(5, KeyEvent.VK_M);

    }


    private void addPane(String panelId, String configFile, String viewPaneId, boolean reset, String iconS) {
        URL clsURL = getClass().getResource(configFile);
        ViewsPane pane = new ViewsPane(getOWLWorkspace(), new ViewsPaneMemento(clsURL, viewPaneId, reset));
        cardPanel.addTab(panelId,DebugIconsLoader.getIcon(iconS),pane);
        viewsPanes.add(pane);
    }


    public void reset() {
        for (ViewsPane pane : viewsPanes){
            cardPanel.remove(pane);
            pane.dispose();
        }

        viewsPanes.clear();
        createViewPanes(true);
        validate();

        for (ViewsPane pane : viewsPanes){
            pane.saveViews();
        }
    }


    /*private void processSelection() {
        OWLEntity entity = getOWLWorkspace().getOWLSelectionModel().getSelectedEntity();
        if (entity == null) {
            selectPanel(BLANK_PANEL);
        }
        else {
            entity.accept(new OWLEntityVisitor() {
                public void visit(OWLClass cls) {
                    selectPanel(QUERY_PANEL);
                }


                public void visit(OWLObjectProperty property) {
                    selectPanel(DIAGNOSES_PANEL);
                }


                public void visit(OWLDataProperty property) {
                    selectPanel(OPTIONS_PANEL);
                }


                public void visit(OWLAnnotationProperty property) {
                    selectPanel(TESTCASES_PANEL);
                }


                public void visit(OWLNamedIndividual individual) {
                    selectPanel(PREVIEW_PANEL);
                }


                public void visit(OWLDatatype dataType) {
                    selectPanel(REVISION_PANEL);
                }
            });
        }
    }


    private void selectPanel(String name) {
        cardLayout.show(cardPanel, name);
    }*/


    protected void disposeOWLView() {
        for (ViewsPane pane : viewsPanes){
            pane.saveViews();
            pane.dispose();
        }
    }




}
