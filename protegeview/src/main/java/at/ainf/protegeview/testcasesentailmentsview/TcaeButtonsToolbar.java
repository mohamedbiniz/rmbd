package at.ainf.protegeview.testcasesentailmentsview;

import at.ainf.protegeview.WorkspaceTab;
import at.ainf.protegeview.controlpanel.DebugIconsLoader;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 03.08.11
 * Time: 11:32
 * To change this template use File | Settings | File Templates.
 */
public class TcaeButtonsToolbar extends JPanel {


    private JToolBar toolBar;


    public TcaeButtonsToolbar(final WorkspaceTab workspaceTab) {
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
                workspaceTab.loadTestcasesAction();
            }
        };
        toolBar.add(new TcaeButtonsButton("Load","Load Testcases",DebugIconsLoader.LOAD,KeyEvent.VK_L,action));
        action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                workspaceTab.saveTestcasesAction();
            }
        };
        toolBar.add(new TcaeButtonsButton("Save","Save Testcases",DebugIconsLoader.SAVE,KeyEvent.VK_S,action));
        action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                workspaceTab.doConfigOptions();
            }
        };
        toolBar.addSeparator();
        toolBar.add(new TcaeButtonsButton("Options","Options",DebugIconsLoader.OPTIONS,KeyEvent.VK_O,action));
        action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                workspaceTab.doConfigurationWizard();
            }
        };
        toolBar.add(new TcaeButtonsButton("Wizard","Configuration Wizard",DebugIconsLoader.WIZARD,KeyEvent.VK_W,action));
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
        toolBar.add(new TcaeButtonsButton("Calculate","Calculate",DebugIconsLoader.SEARCH,KeyEvent.VK_C,action));



    }


}
