package at.ainf.protegeview.gui.queryview;

import at.ainf.protegeview.gui.toolboxview.AbstractGuiButton;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 07.09.12
 * Time: 17:14
 * To change this template use File | Settings | File Templates.
 */
public class CommitButton extends AbstractGuiButton {

    public CommitButton(final QueryView queryView) {
        super("Commit","Commit Answer", "Accept2.png", KeyEvent.VK_C,
                new AbstractAction(){
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        queryView.getEditorKitHook().getActiveOntologyDiagnosisSearcher().doCommitQuery();
                    }
                }
        );

    }
}
