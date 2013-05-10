package at.ainf.protegeview.buttonview;

import at.ainf.protegeview.WorkspaceTab;
import at.ainf.protegeview.controlpanel.DebugIconsLoader;
import at.ainf.protegeview.queryaskingview.QueryShowPanel;
import at.ainf.protegeview.testcasesentailmentsview.TcaeButtonsButton;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 21.09.11
 * Time: 12:55
 * To change this template use File | Settings | File Templates.
 */
public class ButtonToolbar extends JPanel {

    private JToolBar toolBar;

    private int UNDEFINED_CHAR = -1;

    public ButtonToolbar(final WorkspaceTab workspaceTab, final QueryShowPanel panel) {
        toolBar = new JToolBar();
        add(toolBar);
        toolBar.setOpaque(false);
        toolBar.setFloatable(false);
        toolBar.setBorderPainted(false);
        toolBar.setBorder(null);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        AbstractAction action;

                action = new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        workspaceTab.doConfigurationWizard();
                    }
                };
                toolBar.add(new TcaeButtonsButton("Config Wizard","Configuration Wizard",DebugIconsLoader.WIZARD,KeyEvent.VK_W,action));
                action = new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        workspaceTab.doConfigOptions();
                    }
                };

                toolBar.add(new TcaeButtonsButton("Options","Options",DebugIconsLoader.OPTIONS,KeyEvent.VK_O,action));
                action = new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        workspaceTab.doResetAct();
                    }
                };
                toolBar.addSeparator();
                toolBar.add(new TcaeButtonsButton("Reset","Reset",DebugIconsLoader.CLEAR,KeyEvent.VK_R,action));
                action = new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        workspaceTab.doCalculateHittingSet();
                    }
                };

                toolBar.addSeparator();
                toolBar.add(new TcaeButtonsButton("Calculate Diagnoses","Calculate",DebugIconsLoader.SEARCH,KeyEvent.VK_D,action));

        toolBar.add(Box.createHorizontalGlue());

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (workspaceTab.getPanel()!=null)
                workspaceTab.getPanel().init();
            }
        };
        toolBar.add(new TcaeButtonsButton("Get Query","Start New Query Session",
                DebugIconsLoader.INIT, KeyEvent.VK_Q,action));

        /*action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                panel.processNextButton();
            }
        };
        toolBar.add(new TcaeButtonsButton("Next","Next", DebugIconsLoader.NEXT, UNDEFINED_CHAR,action));*/

                toolBar.addSeparator();

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (workspaceTab.getPanel()!=null)
                    workspaceTab.getPanel().applyChanged();
            }
        };
        toolBar.add(new TcaeButtonsButton("Confirm Answer","Confirm", DebugIconsLoader.CONFIRM, KeyEvent.VK_C,action));



    }
}
