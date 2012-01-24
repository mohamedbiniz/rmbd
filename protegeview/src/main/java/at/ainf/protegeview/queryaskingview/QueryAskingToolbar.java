package at.ainf.protegeview.queryaskingview;

import at.ainf.protegeview.WorkspaceTab;
import at.ainf.protegeview.controlpanel.DebugIconsLoader;
import at.ainf.protegeview.queryaskingview.buttons.QueryButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 04.08.11
 * Time: 09:58
 * To change this template use File | Settings | File Templates.
 */
public class QueryAskingToolbar extends JPanel {

    private JToolBar toolBar;

    final QueryButton noButton;

    final QueryButton yesButton;

    private int UNDEFINED_CHAR = -1;

    public void deactButtons() {
        yesButton.setActivate(false);
        noButton.setActivate(false);
    }

    public QueryAskingToolbar(final WorkspaceTab workspaceTab, final QueryShowPanel panel) {
        toolBar = new JToolBar();
        add(toolBar);
        toolBar.setOpaque(false);
        toolBar.setFloatable(false);
        toolBar.setBorderPainted(false);
        toolBar.setBorder(null);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        AbstractAction action;

        /*action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                panel.init();
            }
        };
        toolBar.add(new TcaeButtonsButton("Get Query","Start New Query Session",
                DebugIconsLoader.INIT, UNDEFINED_CHAR,action));

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                panel.processNextButton();
            }
        };
        toolBar.add(new TcaeButtonsButton("Next","Next", DebugIconsLoader.NEXT, UNDEFINED_CHAR,action));

        toolBar.add(Box.createHorizontalGlue());

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                panel.applyChanged();
            }
        };
      toolBar.add(new TcaeButtonsButton("Confirm Answer","Confirm", DebugIconsLoader.CONFIRM, UNDEFINED_CHAR,action));*/

        JLabel label = new JLabel("Should the ontology entail the set of axioms?");
        label.setFont(label.getFont().deriveFont(Font.BOLD,12) );
        toolBar.add(label);

        toolBar.add(Box.createHorizontalGlue());
        // toolBar.addSeparator();

        noButton = new QueryButton("No","No for all axioms", DebugIconsLoader.NOFORALL,
                KeyEvent.VK_N, null,  DebugIconsLoader.NOFORALLACTIVATED);
        //toolBar.add(noButton);

        yesButton = new QueryButton("Yes","Yes for all axioms", DebugIconsLoader.YESFORALL,
                KeyEvent.VK_Y, null,  DebugIconsLoader.YESFORALLACTIVATED);
        //toolBar.add(yesButton);

        noButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                yesButton.setActivate(false);
                noButton.processClickFromUser();
                panel.removeMarkers();
                if (noButton.isActivated())
                  panel.setNonEntailedMarkers();
                else
                  panel.removeMarkers();
                panel.touchModelElements();
                // panel.processNegAnswer();
            }
        });

        yesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                panel.removeMarkers();
                noButton.setActivate(false);
                yesButton.processClickFromUser();
                if (yesButton.isActivated())
                  panel.setEntailedMarkers();
                else
                  panel.removeMarkers();
                panel.touchModelElements();
                // panel.processPosAnswer();
            }
        });

    }
}
